package com.example.gis

data class PoliceUnit(
    val code: String,
    val name: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val municipio: String,
    val bairro: String
)

object CartographicData {

    /**
     * Official Administrative Boundaries for WebGIS representation
     */
    val ADMINISTRATIVE_BOUNDARIES = listOf(
        AdministrativeBoundary(
            name = "Maianga",
            type = "MUNICIPIO",
            centerLat = -8.8400,
            centerLng = 13.2300,
            polygonPoints = listOf(
                Pair(-8.8250, 13.2150),
                Pair(-8.8220, 13.2450),
                Pair(-8.8550, 13.2500),
                Pair(-8.8600, 13.2180)
            )
        ),
        AdministrativeBoundary(
            name = "Cazenga",
            type = "MUNICIPIO",
            centerLat = -8.8050,
            centerLng = 13.2950,
            polygonPoints = listOf(
                Pair(-8.7850, 13.2750),
                Pair(-8.7900, 13.3200),
                Pair(-8.8250, 13.3150),
                Pair(-8.8200, 13.2700)
            )
        ),
        AdministrativeBoundary(
            name = "Kilamba Kiaxi",
            type = "MUNICIPIO",
            centerLat = -8.8700,
            centerLng = 13.2500,
            polygonPoints = listOf(
                Pair(-8.8550, 13.2350),
                Pair(-8.8500, 13.2800),
                Pair(-8.8950, 13.2750),
                Pair(-8.8900, 13.2300)
            )
        ),
        AdministrativeBoundary(
            name = "Belas",
            type = "MUNICIPIO",
            centerLat = -8.9800,
            centerLng = 13.2400,
            polygonPoints = listOf(
                Pair(-8.9300, 13.2100),
                Pair(-8.9350, 13.2900),
                Pair(-9.0300, 13.2800),
                Pair(-9.0200, 13.2000)
            )
        )
    )

    /**
     * Official Police Units & Critical Infrastructure POIs
     */
    val OFFICIAL_POIS = listOf(
        com.example.data.model.LocationPoiEntity(
            code = "POI-01",
            name = "Comando Provincial de Luanda",
            type = "Esquadra Policial",
            latitude = -8.8150,
            longitude = 13.2380,
            municipio = "Luanda",
            bairro = "Ingombota",
            riskLevel = "Seguro"
        ),
        com.example.data.model.LocationPoiEntity(
            code = "POI-02",
            name = "1ª Divisão Policial - Maianga",
            type = "Esquadra Policial",
            latitude = -8.8410,
            longitude = 13.2260,
            municipio = "Maianga",
            bairro = "Alvalade",
            riskLevel = "Seguro"
        ),
        com.example.data.model.LocationPoiEntity(
            code = "POI-03",
            name = "Esquadra do Cazenga (Hoji Ya Henda)",
            type = "Esquadra Policial",
            latitude = -8.8020,
            longitude = 13.2950,
            municipio = "Cazenga",
            bairro = "Hoji Ya Henda",
            riskLevel = "Alerta"
        ),
        com.example.data.model.LocationPoiEntity(
            code = "POI-04",
            name = "Hospital Central Américo Boavida",
            type = "Hospital",
            latitude = -8.8250,
            longitude = 13.2620,
            municipio = "Rangel",
            bairro = "Terra Nova",
            riskLevel = "Normal"
        ),
        com.example.data.model.LocationPoiEntity(
            code = "POI-05",
            name = "Posto Policial do Kilamba",
            type = "Esquadra Policial",
            latitude = -8.9950,
            longitude = 13.2550,
            municipio = "Belas",
            bairro = "Kilamba",
            riskLevel = "Seguro"
        )
    )

    /**
     * Ocorrências de demonstração removidas para produção diária e permanente.
     */
    val OFFICIAL_SAMPLE_INCIDENTS: List<com.example.data.model.IncidentEntity> = emptyList()

    /**
     * Levantamentos de demonstração removidos para produção diária e permanente.
     */
    val OFFICIAL_SAMPLE_SURVEYS: List<com.example.data.model.CartographicSurveyEntity> = emptyList()

    /**
     * Identificadores de demonstração legados para limpeza automática da base de dados.
     */
    val LEGACY_DEMO_INCIDENT_NUMBERS = listOf(
        "OC-2026-00101", "OC-2026-00102", "OC-2026-00103",
        "OC-2026-00104", "OC-2026-00105", "OC-2026-00106",
        "RO-2025-001", "HO-2025-002", "TR-2025-003",
        "FU-2025-004", "AM-2025-005", "VA-2025-006",
        "DR-2025-007", "VI-2025-008"
    )

    val LEGACY_DEMO_SURVEY_CODES = listOf(
        "LEV-2026-0012", "LEV-2026-0015", "LEV-2026-0034",
        "SURV-2025-001", "SURV-2025-002"
    )

    /**
     * Identificadores de agentes de demonstração legados para remoção automática.
     */
    val DEMO_USER_BADGES = listOf(
        "POL-442", "POL-553", "POL-319", "POL-104", "POL-001", "POL-882"
    )
}
