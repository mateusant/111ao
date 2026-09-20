package com.example.gis

import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Float = 3.0f
)

data class BoundingBox(
    val minLat: Double,
    val minLng: Double,
    val maxLat: Double,
    val maxLng: Double
)

data class AdministrativeBoundary(
    val name: String,
    val type: String, // "PROVINCIA", "MUNICIPIO", "BAIRRO"
    val centerLat: Double,
    val centerLng: Double,
    val polygonPoints: List<Pair<Double, Double>> // pairs of lat, lng
)

object GisEngine {

    // Center of Luanda / Reference Operations
    const val DEFAULT_CENTER_LAT = -8.8383
    const val DEFAULT_CENTER_LNG = 13.2344

    /**
     * Converts WGS84 Lat/Lng to UTM (Zone 33S for Angola / Luanda region)
     */
    fun toUtm(lat: Double, lng: Double): String {
        // Approximate calculation for Zone 33S (standard military UTM grid)
        val zone = ((lng + 180) / 6).toInt() + 1
        val hemisphere = if (lat >= 0) "N" else "S"
        
        val latRad = Math.toRadians(lat)
        val lngRad = Math.toRadians(lng)
        val centralMeridian = Math.toRadians(((zone - 1) * 6 - 180 + 3).toDouble())
        
        val a = 6378137.0 // WGS84 major axis
        val k0 = 0.9996
        val xVal = 500000 + (a * (lngRad - centralMeridian) * cos(latRad) * k0)
        val yVal = if (lat >= 0) {
            a * latRad * k0
        } else {
            10000000 + (a * latRad * k0)
        }
        
        return String.format(Locale.US, "%d%s %.0f m E, %.0f m N", zone, hemisphere, xVal, yVal)
    }

    /**
     * Converts decimal degrees to Degrees, Minutes, Seconds (DMS)
     */
    fun toDms(coordinate: Double, isLatitude: Boolean): String {
        val direction = if (isLatitude) {
            if (coordinate >= 0) "N" else "S"
        } else {
            if (coordinate >= 0) "E" else "W"
        }
        val absVal = abs(coordinate)
        val degrees = absVal.toInt()
        val minutesFull = (absVal - degrees) * 60
        val minutes = minutesFull.toInt()
        val seconds = (minutesFull - minutes) * 60
        return String.format(Locale.US, "%d°%02d'%04.1f\"%s", degrees, minutes, seconds, direction)
    }

    fun toDmsFormatted(lat: Double, lng: Double): String {
        return "${toDms(lat, true)}  ${toDms(lng, false)}"
    }

    /**
     * Haversine Distance in meters between two coordinates
     */
    fun haversineDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Area calculation for a polygon of lat/lng points (in square meters)
     */
    fun calculatePolygonAreaM2(points: List<Pair<Double, Double>>): Double {
        if (points.size < 3) return 0.0
        var total = 0.0
        val earthRadius = 6371000.0
        
        for (i in points.indices) {
            val p1 = points[i]
            val p2 = points[(i + 1) % points.size]
            
            val lat1Rad = Math.toRadians(p1.first)
            val lat2Rad = Math.toRadians(p2.first)
            val lonDiffRad = Math.toRadians(p2.second - p1.second)
            
            total += lonDiffRad * (2 + sin(lat1Rad) + sin(lat2Rad))
        }
        val area = abs(total * earthRadius * earthRadius / 2.0)
        return area
    }

    /**
     * Total length of a polyline in meters
     */
    fun calculatePolylineLengthMeters(points: List<Pair<Double, Double>>): Double {
        if (points.size < 2) return 0.0
        var total = 0.0
        for (i in 0 until points.size - 1) {
            total += haversineDistanceMeters(
                points[i].first, points[i].second,
                points[i + 1].first, points[i + 1].second
            )
        }
        return total
    }

    /**
     * Checks if a coordinate (lat, lng) falls inside a polygon of vertices
     */
    fun isPointInPolygon(lat: Double, lng: Double, polygon: List<Pair<Double, Double>>): Boolean {
        if (polygon.size < 3) return false
        var inside = false
        var j = polygon.size - 1
        for (i in polygon.indices) {
            val xi = polygon[i].second
            val yi = polygon[i].first
            val xj = polygon[j].second
            val yj = polygon[j].first
            val intersect = ((yi > lat) != (yj > lat)) &&
                    (lng < (xj - xi) * (lat - yi) / (yj - yi) + xi)
            if (intersect) inside = !inside
            j = i
        }
        return inside
    }

    /**
     * Approximate shortest distance in meters from a point (pLat, pLng) to a segment (a-b)
     */
    fun distanceToSegmentMeters(
        pLat: Double, pLng: Double,
        aLat: Double, aLng: Double,
        bLat: Double, bLng: Double
    ): Double {
        val l2 = (bLat - aLat) * (bLat - aLat) + (bLng - aLng) * (bLng - aLng)
        if (l2 == 0.0) return haversineDistanceMeters(pLat, pLng, aLat, aLng)
        val t = (((pLat - aLat) * (bLat - aLat) + (pLng - aLng) * (bLng - aLng)) / l2).coerceIn(0.0, 1.0)
        val projLat = aLat + t * (bLat - aLat)
        val projLng = aLng + t * (bLng - aLng)
        return haversineDistanceMeters(pLat, pLng, projLat, projLng)
    }

    /**
     * Shortest distance in meters from a point to any segment of a polyline
     */
    fun distanceToPolylineMeters(lat: Double, lng: Double, polyline: List<Pair<Double, Double>>): Double {
        if (polyline.isEmpty()) return Double.MAX_VALUE
        if (polyline.size == 1) return haversineDistanceMeters(lat, lng, polyline[0].first, polyline[0].second)
        var minDist = Double.MAX_VALUE
        for (i in 0 until polyline.size - 1) {
            val d = distanceToSegmentMeters(
                lat, lng,
                polyline[i].first, polyline[i].second,
                polyline[i + 1].first, polyline[i + 1].second
            )
            if (d < minDist) minDist = d
        }
        return minDist
    }

    /**
     * Well-Known Text (WKT) Point format compatible with PostGIS
     */
    fun toWktPoint(lat: Double, lng: Double): String {
        return String.format(Locale.US, "POINT(%.6f %.6f)", lng, lat)
    }

    /**
     * Well-Known Text (WKT) Polygon format compatible with PostGIS
     */
    fun toWktPolygon(points: List<Pair<Double, Double>>): String {
        if (points.isEmpty()) return "POLYGON EMPTY"
        val closed = if (points.first() != points.last()) points + points.first() else points
        val coords = closed.joinToString(", ") { String.format(Locale.US, "%.6f %.6f", it.second, it.first) }
        return "POLYGON(($coords))"
    }

    /**
     * Generates PostGIS DDL and spatial query schema
     */
    fun getPostGisDdl(): String {
        return """
        -- =======================================================
        -- OCORRÊNCIA REMOTA - POSTGRESQL + POSTGIS SPATIAL SCHEMA
        -- =======================================================
        CREATE EXTENSION IF NOT EXISTS postgis;
        CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

        -- Tabela de Ocorrências com Geometria Espacial (SRID 4326)
        CREATE TABLE IF NOT EXISTS ocorrencias (
            id BIGSERIAL PRIMARY KEY,
            numero_ocorrencia VARCHAR(50) UNIQUE NOT NULL,
            data_ocorrencia DATE NOT NULL,
            hora_ocorrencia TIME NOT NULL,
            tipo_ocorrencia VARCHAR(100) NOT NULL,
            categoria_criminal VARCHAR(50) NOT NULL,
            descricao TEXT NOT NULL,
            local_nome VARCHAR(200) NOT NULL,
            bairro VARCHAR(100) NOT NULL,
            municipio VARCHAR(100) NOT NULL,
            provincia VARCHAR(100) DEFAULT 'Luanda',
            geom GEOMETRY(Point, 4326) NOT NULL,
            precisao_gps REAL DEFAULT 5.0,
            altitude REAL DEFAULT 0.0,
            agente_codigo VARCHAR(50) NOT NULL,
            agente_nome VARCHAR(150) NOT NULL,
            unidade VARCHAR(150) NOT NULL,
            estado VARCHAR(50) DEFAULT 'Registada',
            prioridade VARCHAR(30) DEFAULT 'Média',
            sync_status VARCHAR(30) DEFAULT 'SYNCHRONIZED',
            odk_form_id VARCHAR(50) DEFAULT 'ODK-CRIM-01',
            created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
        );

        -- Índice Espacial GiST para Alta Performance Geoespacial
        CREATE INDEX IF NOT EXISTS idx_ocorrencias_geom ON ocorrencias USING GIST (geom);
        CREATE INDEX IF NOT EXISTS idx_ocorrencias_data ON ocorrencias(data_ocorrencia);
        CREATE INDEX IF NOT EXISTS idx_ocorrencias_categoria ON ocorrencias(categoria_criminal);
        CREATE INDEX IF NOT EXISTS idx_ocorrencias_municipio ON ocorrencias(municipio);

        -- Tabela de Levantamentos Cartográficos (Linhas e Polígonos)
        CREATE TABLE IF NOT EXISTS levantamentos_cartograficos (
            id BIGSERIAL PRIMARY KEY,
            codigo_levantamento VARCHAR(50) UNIQUE NOT NULL,
            titulo VARCHAR(150) NOT NULL,
            tipo_geometria VARCHAR(30) NOT NULL,
            geom GEOMETRY(Geometry, 4326) NOT NULL,
            comprimento_metros DOUBLE PRECISION,
            area_m2 DOUBLE PRECISION,
            agente_codigo VARCHAR(50) NOT NULL,
            municipio VARCHAR(100),
            bairro VARCHAR(100),
            observacoes TEXT,
            timestamp TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
        );
        CREATE INDEX IF NOT EXISTS idx_levantamentos_geom ON levantamentos_cartograficos USING GIST (geom);

        -- Tabela de Evidências e Cadeia de Custódia
        CREATE TABLE IF NOT EXISTS evidencias (
            id BIGSERIAL PRIMARY KEY,
            numero_ocorrencia VARCHAR(50) REFERENCES ocorrencias(numero_ocorrencia) ON DELETE CASCADE,
            tipo_evidencia VARCHAR(50) NOT NULL,
            titulo VARCHAR(150) NOT NULL,
            descricao TEXT,
            hash_sha256 VARCHAR(64) NOT NULL,
            geom GEOMETRY(Point, 4326),
            registrado_por VARCHAR(50) NOT NULL,
            timestamp TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
        );

        -- Tabela de Auditoria de Operações
        CREATE TABLE IF NOT EXISTS auditoria_ocorrencias (
            id BIGSERIAL PRIMARY KEY,
            data_hora TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
            usuario_cracha VARCHAR(50) NOT NULL,
            usuario_nome VARCHAR(150) NOT NULL,
            acao VARCHAR(50) NOT NULL,
            tabela VARCHAR(50) NOT NULL,
            registro_id VARCHAR(50) NOT NULL,
            detalhes TEXT
        );
        """.trimIndent()
    }

    /**
     * Generates a standard GeoJSON FeatureCollection string from coordinates and properties
     */
    fun generateGeoJson(
        features: List<Pair<Pair<Double, Double>, Map<String, String>>>
    ): String {
        val sb = StringBuilder()
        sb.append("{\n  \"type\": \"FeatureCollection\",\n  \"features\": [\n")
        
        features.forEachIndexed { index, (coords, props) ->
            val (lat, lng) = coords
            sb.append("    {\n")
            sb.append("      \"type\": \"Feature\",\n")
            sb.append("      \"geometry\": {\n")
            sb.append("        \"type\": \"Point\",\n")
            sb.append(String.format(Locale.US, "        \"coordinates\": [%.6f, %.6f]\n", lng, lat))
            sb.append("      },\n")
            sb.append("      \"properties\": {\n")
            val propEntries = props.entries.toList()
            propEntries.forEachIndexed { pIdx, entry ->
                val comma = if (pIdx < propEntries.size - 1) "," else ""
                val escaped = entry.value.replace("\"", "\\\"")
                sb.append("        \"${entry.key}\": \"$escaped\"$comma\n")
            }
            sb.append("      }\n")
            val featureComma = if (index < features.size - 1) "," else ""
            sb.append("    }$featureComma\n")
        }
        sb.append("  ]\n}")
        return sb.toString()
    }
}
