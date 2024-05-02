package com.alefeducation.authoring.search.builders

data class Pool(
    val id: String,
    val name: String,
    val questionCodePrefix: String,
    val status: String,
    val questions: List<Question>,
    val provider: String,
    val createdAt: String,
    val updatedAt: String,
    val createdBy: String,
    val updatedBy: String,
    val organisations: List<String>,
    val `class`: String
) {
    data class Question(val id: String)
}

data class PoolBuilder(
    var id: String = "5b5adf35017d140001907d1b",
    var name: String = "test pool name 1",
    var questionCodePrefix: String = "EA6_MLO_038",
    var status: String = "PUBLISHED",
    val questions: List<Pool.Question> = listOf(
        Pool.Question("33101"), Pool.Question("33105"), Pool.Question("33108"), Pool.Question("33111"),
        Pool.Question("33112"), Pool.Question("33117"), Pool.Question("33123"), Pool.Question("33129"),
        Pool.Question("33133"), Pool.Question("33137"), Pool.Question("33142"), Pool.Question("33147"),
        Pool.Question("33151"), Pool.Question("33154"), Pool.Question("33159"), Pool.Question("33164"),
        Pool.Question("33166"), Pool.Question("33169"), Pool.Question("33174"), Pool.Question("33176"),
        Pool.Question("33180"), Pool.Question("33184"), Pool.Question("33188"), Pool.Question("33202"),
        Pool.Question("33206"), Pool.Question("33209"), Pool.Question("33219"), Pool.Question("33530"),
        Pool.Question("33531"), Pool.Question("33532")
    ),
    var provider: String = "SARAS",
    var createdAt: String = "2018-07-27T09:00:37Z",
    var updatedAt: String = "2019-01-17T07:16:00Z",
    var createdBy: String = "SYSTEM_ADMIN",
    var updatedBy: String = "SYSTEM_ADMIN",
    var organisations: List<String> = listOf("shared"),
    var `class`: String = "com.alefeducation.assessmentlibrary"
) {
    fun build() =
        Pool(
            id,
            name,
            questionCodePrefix,
            status,
            questions,
            provider,
            createdAt,
            updatedAt,
            createdBy,
            updatedBy,
            organisations,
            `class`
        )
}
