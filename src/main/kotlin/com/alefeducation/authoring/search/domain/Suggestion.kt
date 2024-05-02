package com.alefeducation.authoring.search.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class Suggestion(
    @JsonProperty
    val text: String,
    val score: Double,
    val priority: FieldPriority
)

enum class SuggestionKindType(val value: String) {
    KEYWORD("keyword"),
}

enum class FieldPriority(val value: Int) {
    WORKSPACE(4),
    POOL_CODE(3),
    QUESTION_CODE(2),
    QUESTION_BODY(1),
}
