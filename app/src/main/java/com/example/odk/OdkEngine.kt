package com.example.odk

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class OdkFieldType {
    TEXT,
    INTEGER,
    DECIMAL,
    SELECT_ONE,
    SELECT_MULTIPLE,
    GEOPOINT,
    GEOTRACE,
    GEOSHAPE,
    IMAGE,
    DATE,
    TIME,
    NOTE,
    CALCULATE
}

data class OdkChoice(
    val name: String,
    val label: String
)

data class OdkField(
    val name: String,
    val label: String,
    val type: OdkFieldType,
    val isRequired: Boolean = false,
    val hint: String? = null,
    val choices: List<OdkChoice> = emptyList(),
    val defaultValue: String? = null,
    val relevantCondition: String? = null, // e.g. "categoria == 'Roubo'"
    val calculationExpression: String? = null
)

data class OdkFormDefinition(
    val id: String,
    val title: String,
    val version: String,
    val category: String,
    val description: String,
    val fields: List<OdkField>
)

object OdkEngine {

    val CLEAN_DEFAULT_USER = com.example.data.model.UserEntity(
        badgeNumber = "PARTICULAR",
        name = "",
        role = "AGENTE_CAMPO",
        unit = "",
        email = "",
        pin = "",
        phone = "",
        patent = ""
    )

    val OFFICIAL_USERS = listOf(CLEAN_DEFAULT_USER)

    val CATEGORIES = listOf(
        "Furto",
        "Roubo",
        "Agressão",
        "Homicídio",
        "Violência",
        "Acidente",
        "Vandalismo",
        "Tráfico",
        "Desaparecimento",
        "Incêndio",
        "Outros"
    )

    val MUNICIPIOS = listOf(
        "Luanda",
        "Maianga",
        "Kilamba Kiaxi",
        "Belas",
        "Cazenga",
        "Viana",
        "Talatona",
        "Cacuaco"
    )

    val BAIRROS = mapOf(
        "Maianga" to listOf("Maianga Central", "Alvalade", "Cassenda", "Prenda", "Rocha Pinto"),
        "Luanda" to listOf("Ingombota", "Maculusso", "Kinaxixi", "Ilha do Cabo", "Mutamba", "Rangel", "Sambizanga"),
        "Kilamba Kiaxi" to listOf("Palanca", "Golfe", "Havemos de Voltar", "Nova Vida", "Calemba 2"),
        "Belas" to listOf("Kilamba", "Benfica", "Ramiros", "Morro dos Veados", "Camama"),
        "Cazenga" to listOf("Hoji Ya Henda", "Calaumenda", "Tala Hadi", "Terra Vermelha"),
        "Viana" to listOf("Viana Sede", "Zango 1", "Zango 2", "Zango 3", "Estalagem", "Capalanga"),
        "Talatona" to listOf("Talatona Sede", "Lar do Patriota", "Cidade Universitária", "Futungo de Belas"),
        "Cacuaco" to listOf("Cacuaco Sede", "Kikolo", "Fundão", "Vidrul")
    )

    /**
     * Automatic generation of incident identifier: OC-YYYY-NNNNN
     */
    fun generateIncidentNumber(): String {
        val year = SimpleDateFormat("yyyy", Locale.US).format(Date())
        val randomNum = (10000..99999).random()
        return "OC-$year-$randomNum"
    }

    /**
     * Automatic generation of cartographic survey code: LEV-YYYY-NNNN
     */
    fun generateSurveyCode(): String {
        val year = SimpleDateFormat("yyyy", Locale.US).format(Date())
        val randomNum = (1000..9999).random()
        return "LEV-$year-$randomNum"
    }

    /**
     * 5 ODK Official Built-in Forms
     */
    val BUILT_IN_FORMS: List<OdkFormDefinition> = listOf(
        // FORM 1: Ocorrência Criminal Padrão
        OdkFormDefinition(
            id = "ODK-CRIM-01",
            title = "Registo de Ocorrência Criminal Padrão",
            version = "2026.1",
            category = "Criminal",
            description = "Formulário oficial para levantamento e registo em campo de ocorrências criminais, geolocalização e evidências.",
            fields = listOf(
                OdkField(
                    name = "numero_ocorrencia",
                    label = "Número da Ocorrência",
                    type = OdkFieldType.CALCULATE,
                    hint = "Gerado automaticamente pelo sistema"
                ),
                OdkField(
                    name = "data_ocorrencia",
                    label = "Data da Ocorrência",
                    type = OdkFieldType.DATE,
                    isRequired = true
                ),
                OdkField(
                    name = "hora_ocorrencia",
                    label = "Hora da Ocorrência",
                    type = OdkFieldType.TIME,
                    isRequired = true
                ),
                OdkField(
                    name = "categoria_criminal",
                    label = "Categoria Criminal",
                    type = OdkFieldType.SELECT_ONE,
                    isRequired = true,
                    choices = CATEGORIES.map { OdkChoice(it, it) }
                ),
                OdkField(
                    name = "tipo_ocorrencia",
                    label = "Tipologia Específica",
                    type = OdkFieldType.TEXT,
                    isRequired = true,
                    hint = "Ex: Roubo com arma de fogo na via pública"
                ),
                OdkField(
                    name = "descricao_factos",
                    label = "Descrição Detalhada dos Factos",
                    type = OdkFieldType.TEXT,
                    isRequired = true,
                    hint = "Relato cronológico e circunstâncias apuradas"
                ),
                OdkField(
                    name = "municipio",
                    label = "Município",
                    type = OdkFieldType.SELECT_ONE,
                    isRequired = true,
                    choices = MUNICIPIOS.map { OdkChoice(it, it) }
                ),
                OdkField(
                    name = "bairro",
                    label = "Bairro / Localidade",
                    type = OdkFieldType.TEXT,
                    isRequired = true,
                    hint = "Ex: Maianga Central"
                ),
                OdkField(
                    name = "local_ocorrencia",
                    label = "Ponto de Referência / Endereço",
                    type = OdkFieldType.TEXT,
                    isRequired = true,
                    hint = "Ex: Rua Comandante Gika, junto ao Banco BFA"
                ),
                OdkField(
                    name = "posicao_geopoint",
                    label = "Coordenadas GPS (geopoint)",
                    type = OdkFieldType.GEOPOINT,
                    isRequired = true,
                    hint = "Captura de Latitude, Longitude, Altitude e Precisão"
                ),
                OdkField(
                    name = "agente_responsavel",
                    label = "Nome / Código do Agente",
                    type = OdkFieldType.TEXT,
                    isRequired = true
                ),
                OdkField(
                    name = "unidade_departamento",
                    label = "Unidade / Divisão Policial",
                    type = OdkFieldType.TEXT,
                    isRequired = true
                ),
                OdkField(
                    name = "prioridade",
                    label = "Nível de Prioridade",
                    type = OdkFieldType.SELECT_ONE,
                    isRequired = true,
                    choices = listOf(
                        OdkChoice("Baixa", "Baixa"),
                        OdkChoice("Média", "Média"),
                        OdkChoice("Alta", "Alta"),
                        OdkChoice("Crítica", "Crítica")
                    ),
                    defaultValue = "Média"
                ),
                OdkField(
                    name = "fotografia_local",
                    label = "Fotografia Georreferenciada do Local",
                    type = OdkFieldType.IMAGE,
                    hint = "Anexo de imagem com registo de integridade"
                ),
                OdkField(
                    name = "observacoes",
                    label = "Observações Adicionais / Diligências",
                    type = OdkFieldType.TEXT
                )
            )
        ),

        // FORM 2: Levantamento Cartográfico e Infraestrutura Crítica
        OdkFormDefinition(
            id = "ODK-GEO-02",
            title = "Levantamento Cartográfico e Infraestrutura Crítica",
            version = "2026.1",
            category = "Cartografia",
            description = "Coleta geoespacial avançada de pontos de interesse, rotas de fuga, perímetros de risco e edificações estratégicas.",
            fields = listOf(
                OdkField("codigo_levantamento", "Código do Levantamento", OdkFieldType.CALCULATE),
                OdkField("titulo_local", "Designação do Local / Infraestrutura", OdkFieldType.TEXT, isRequired = true),
                OdkField(
                    name = "tipo_geometria",
                    label = "Tipo de Geometria Espacial",
                    type = OdkFieldType.SELECT_ONE,
                    isRequired = true,
                    choices = listOf(
                        OdkChoice("PONTO", "Ponto Geográfico (geopoint)"),
                        OdkChoice("LINHA", "Rota / Via / Linha de Fuga (geotrace)"),
                        OdkChoice("POLIGONO", "Perímetro / Área de Risco (geoshape)"),
                        OdkChoice("CROQUI", "Croqui Técnico Esboçado")
                    )
                ),
                OdkField("municipio", "Município", OdkFieldType.SELECT_ONE, choices = MUNICIPIOS.map { OdkChoice(it, it) }),
                OdkField("bairro", "Bairro", OdkFieldType.TEXT, isRequired = true),
                OdkField("sistema_coordenadas", "Sistema de Coordenadas", OdkFieldType.SELECT_ONE, choices = listOf(
                    OdkChoice("WGS 84", "WGS 84 (Geográficas Decimais)"),
                    OdkChoice("UTM", "UTM (Universal Transverse Mercator - Zona 33S)"),
                    OdkChoice("GEOGRAFICAS_DMS", "Geográficas DMS (Graus, Minutos, Segundos)")
                ), defaultValue = "WGS 84"),
                OdkField("coordenadas_gps", "Levantamento Geoespacial", OdkFieldType.GEOPOINT, isRequired = true),
                OdkField("observacoes_cartograficas", "Observações Cartográficas", OdkFieldType.TEXT)
            )
        ),

        // FORM 3: Auto de Apreensão e Cadeia de Custódia
        OdkFormDefinition(
            id = "ODK-CUST-03",
            title = "Auto de Apreensão e Cadeia de Custódia",
            version = "2026.1",
            category = "Forense",
            description = "Registo formal de objetos, armas, entorpecentes ou documentos apreendidos com hash SHA-256 de integridade.",
            fields = listOf(
                OdkField("numero_ocorrencia_ref", "Ocorrência Vinculada", OdkFieldType.TEXT, isRequired = true),
                OdkField("tipo_objeto", "Tipo de Material Apreendido", OdkFieldType.SELECT_ONE, isRequired = true, choices = listOf(
                    OdkChoice("Arma de Fogo", "Arma de Fogo"),
                    OdkChoice("Arma Branca", "Arma Branca"),
                    OdkChoice("Substância Entorpecente", "Substância Entorpecente"),
                    OdkChoice("Veículo", "Veículo / Meio de Transporte"),
                    OdkChoice("Equipamento Eletrónico", "Equipamento Eletrónico"),
                    OdkChoice("Valores Monetários", "Valores Monetários"),
                    OdkChoice("Documentos", "Documentos / Outros")
                )),
                OdkField("descricao_detalhada", "Número de Série / Características", OdkFieldType.TEXT, isRequired = true),
                OdkField("posicao_coleta", "Local de Coleta (geopoint)", OdkFieldType.GEOPOINT, isRequired = true),
                OdkField("agente_custodia", "Agente Responsável pela Custódia", OdkFieldType.TEXT, isRequired = true),
                OdkField("lacre_seguranca", "Número do Lacre Forense", OdkFieldType.TEXT, isRequired = true)
            )
        ),

        // FORM 4: Vistoria de Segurança Comunitária e Patrulhamento
        OdkFormDefinition(
            id = "ODK-PATR-04",
            title = "Vistoria de Segurança Comunitária e Patrulhamento",
            version = "2026.1",
            category = "Prevenção",
            description = "Diagnóstico situacional de iluminação pública, pontos cegos, aglomerações suspeitas e condições de segurança.",
            fields = listOf(
                OdkField("setor_patrulha", "Setor / Quadrante Policial", OdkFieldType.TEXT, isRequired = true),
                OdkField("iluminacao_publica", "Estado da Iluminação Pública", OdkFieldType.SELECT_ONE, choices = listOf(
                    OdkChoice("Boa", "Boa / Funcional"),
                    OdkChoice("Deficiente", "Deficiente / Parcial"),
                    OdkChoice("Inexistente", "Inexistente / Escuridão Total")
                )),
                OdkField("vulnerabilidade_area", "Índice de Vulnerabilidade Local", OdkFieldType.SELECT_ONE, choices = listOf(
                    OdkChoice("Baixo", "Baixo Risco"),
                    OdkChoice("Médio", "Médio Risco"),
                    OdkChoice("Crítico", "Crítico / Área Conflituosa")
                )),
                OdkField("ponto_vistoriado", "Localização (geopoint)", OdkFieldType.GEOPOINT, isRequired = true),
                OdkField("recomendacoes", "Recomendações Operacionais", OdkFieldType.TEXT)
            )
        ),

        // FORM 5: Notificação de Incidente Rápido / Despacho de Emergência
        OdkFormDefinition(
            id = "ODK-EMERG-05",
            title = "Notificação de Incidente Rápido / Despacho SOS",
            version = "2026.1",
            category = "Emergência",
            description = "Envio ultrarrápido de alerta em campo com prioridade máxima e coordenadas instantâneas.",
            fields = listOf(
                OdkField("chamada_tipo", "Natureza do Alerta SOS", OdkFieldType.SELECT_ONE, isRequired = true, choices = listOf(
                    OdkChoice("Apoio Policial Imediato", "Apoio Policial Imediato"),
                    OdkChoice("Disparos de Arma de Fogo", "Disparos de Arma de Fogo"),
                    OdkChoice("Acidente com Vítimas", "Acidente com Vítimas"),
                    OdkChoice("Perseguição em Andamento", "Perseguição em Andamento")
                )),
                OdkField("prioridade_urgente", "Prioridade", OdkFieldType.TEXT, defaultValue = "Crítica"),
                OdkField("localizacao_gps_rapida", "Posição Imediata (geopoint)", OdkFieldType.GEOPOINT, isRequired = true),
                OdkField("mensagem_resumida", "Mensagem Resumida / Situação", OdkFieldType.TEXT, isRequired = true)
            )
        )
    )

    /**
     * Converts a completed form payload into standard OpenRosa ODK XML format
     */
    fun buildOpenRosaXml(
        formId: String,
        version: String,
        instanceId: String = UUID.randomUUID().toString(),
        data: Map<String, String>
    ): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<data id=\"$formId\" version=\"$version\">\n")
        sb.append("  <meta>\n")
        sb.append("    <instanceID>uuid:$instanceId</instanceID>\n")
        sb.append("    <submissionDate>${SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())}</submissionDate>\n")
        sb.append("  </meta>\n")
        data.forEach { (key, value) ->
            val escaped = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            sb.append("  <$key>$escaped</$key>\n")
        }
        sb.append("</data>")
        return sb.toString()
    }
}
