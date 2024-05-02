package com.alefeducation.authoring.search.builders

data class Question(
    val id: String,
    val metadata: Metadata,
    val code: String,
    val updatedBy: String,
    val language: String,
    val type: String,
    val body: Body,
    val maxScore: Int,
    val version: Int,
    val createdAt: String,
    val stage: String,
    val organisations: List<String>,
    val createdBy: String,
    val variant: String,
    val validation: Validation,
    val updatedAt: String,
    val `class`: String,
) {
    data class Metadata(
        val cognitiveDimensions: List<String>,
        val summativeAssessment: Boolean,
        val keywords: List<String>,
        val copyrights: List<String>,
        val difficultyLevel: String,
        val author: String,
        val conditionsOfUse: List<String>,
        val authoredDate: String,
        val curriculumOutcomes: List<String>,
        val knowledgeDimensions: String,
        val lexileLevel: String,
        val formatType: String,
        val resourceType: String
    )

    data class Body(
        val hints: List<Any>,
        val generalFeedback: String,
        val correctAnswerFeedback: String,
        val wrongAnswerFeedback: String,
        val choices: Choices,
        val prompt: String
    ) {
        data class Choices(
            val maxChoice: Int,
            val listType: String,
            val choiceItems: List<ChoiceItem>,
            val layoutColumns: Int,
            val shuffle: Boolean,
            val minChoice: Int
        )

        data class ChoiceItem(
            val choiceId: Int,
            val weight: Double,
            val feedback: String,
            val answer: String
        )
    }

    data class Validation(
        val validResponse: ValidResponse,
        val scoringType: String,
    ) {
        data class ValidResponse(
            val choiceIds: List<Int>,
        )
    }
}

data class QuestionBuilder(
    var id: String = "0040031b-721e-e9f0-52dd-c5cf6aed6786",
    var metadata: Question.Metadata = Question.Metadata(
        listOf("REMEMBERING"), false, listOf("Q14457"), listOf("META_METRICS"),
        "EASY", "vd01_math@test.com", emptyList(), "2022-03-21T00:00:00.000",
        emptyList(), "FACTUAL", "8", "QUESTION", "TEQ2"
    ),
    var code: String = "US9_Metametrics_strands_prototype_622",
    var updatedBy: String = "shashank@alef.com",
    var language: String = "EN_US",
    var type: String = "MULTIPLE_CHOICE",
    var body: Question.Body = Question.Body(
        emptyList(), "test", "", "", Question.Body.Choices(0, "", emptyList(), 0, false, 0), ""
    ),
    var maxScore: Int = 1,
    var version: Int = 0,
    var createdAt: String = "2022-09-19T07:53:18Z",
    var stage: String = "APPROVED",
    var organisations: List<String> = listOf("shared"),
    var createdBy: String = "shashank@alef.com",
    var variant: String = "EN_US",
    var validation: Question.Validation = Question.Validation(
        Question.Validation.ValidResponse(listOf(496)), "EXACT_MATCH"
    ),
    var updatedAt: String = "2022-09-19T07:53:18Z",
    var `class`: String = "com.alefeducation.assessmentquestion"
) {
    fun build(): Question {
        return Question(
            id, metadata, code, updatedBy, language, type, body, maxScore, version,
            createdAt, stage, organisations, createdBy, variant, validation, updatedAt, `class`
        )
    }
}
