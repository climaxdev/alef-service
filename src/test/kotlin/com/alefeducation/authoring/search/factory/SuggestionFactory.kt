package com.alefeducation.authoring.search.factory

import com.alefeducation.authoring.search.domain.FieldPriority
import com.alefeducation.authoring.search.domain.FieldPriority.QUESTION_BODY
import com.alefeducation.authoring.search.domain.FieldPriority.QUESTION_CODE
import com.alefeducation.authoring.search.domain.FieldPriority.WORKSPACE
import com.alefeducation.authoring.search.domain.Suggestion

class SuggestionFactory {
    companion object {
        fun createSuggestions(): List<Suggestion> {
            val suggestions = mutableListOf<Suggestion>()
            create(suggestions, { i -> "Math-12$i" }, WORKSPACE)
            create(suggestions, { "math distance formula" }, QUESTION_BODY)
            create(suggestions, { i -> "M4-1$i" }, QUESTION_CODE)
            return suggestions
        }

        private fun create(suggestions: MutableList<Suggestion>, text: (Int) -> String, fieldPriority: FieldPriority) {
            for (i in 1..5) {
                suggestions.add(Suggestion(text(i), i.toDouble(), fieldPriority))
            }
        }
    }
}
