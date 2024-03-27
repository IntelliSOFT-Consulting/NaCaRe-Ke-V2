package com.capture.app.data

class Mappings {
    fun getTreatmentMapping(): Map<String, Map<String, Any>> {
        return mapOf(
            "Chemotherapy 1st line" to mapOf(
                "treatment" to "VouPJWIla3t",
                "date" to "JkSdwVJ0Cvd",
                "value" to "1st line"
            ),
            "Chemotherapy 2nd line" to mapOf(
                "treatment" to "VouPJWIla3t",
                "date" to "JkSdwVJ0Cvd",
                "value" to "2nd line"
            ),
            "Chemotherapy 3rd line" to mapOf(
                "treatment" to "VouPJWIla3t",
                "date" to "JkSdwVJ0Cvd",
                "value" to "3rd line"
            ),
            "Surgery" to mapOf(
                "treatment" to "Cj3inBBEqoN",
                "date" to "Bv1QzBVyXo3",
                "value" to true
            ),

            "Targeted therapy" to mapOf(
                "treatment" to "GWzmO1k8WIX",
                "date" to "oNPuF57JWui",
                "value" to true
            ),
            "Immunotherapy" to mapOf(
                "treatment" to "uG0nhxpDCEp",
                "date" to "fljqvOpheUV",
                "value" to true
            ),
            "Hormonal Therapy" to mapOf(
                "treatment" to "RYMNbfQI6Xj",
                "date" to "SW7Nxz6M64z",
                "value" to true
            ),
            "External beam radiation" to mapOf(
                "treatment" to "pe5Qlr09BBd",
                "date" to "LTALxLrNHNB",
                "value" to true
            ),
            "Brachytherapy" to mapOf(
                "treatment" to "uQmp4kURCCQ",
                "date" to "yUFcwIifeA9",
                "value" to true
            ),
            "Systemic radiotherapy" to mapOf(
                "treatment" to "jYoWCxPFInU",
                "date" to "ZOdPQ6iLMV4",
                "value" to true
            ),
            "Bone marrow transplant" to mapOf(
                "treatment" to "ZoWjQn9uDfS",
                "date" to "FqimnFgeqq1",
                "value" to true
            )
        )
    }

    fun systemicTherapies(): List<String> {
        return listOf(
            "Chemotherapy 1st line",
            "Chemotherapy 2nd line",
            "Chemotherapy 3rd line",
//            "Targeted therapy",
//            "Immunotherapy",
//            "Hormonal Therapy"
        )
    }
}