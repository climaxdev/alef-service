package com.alefeducation.authoring.search.domain

class Constants {
    companion object {
        const val KIND = "_kind"
        const val SEPARATOR = "-"
        const val SUGGESTER_CONTEXT = "_suggester_context"
        const val MAX_SUGGESTION_LIMIT = 5
    }
    class Search {
        companion object {
            const val DEFAULT_LIMIT = 5
            const val ORGANISATIONS_FIELD = "organisations"
            const val COLLAPSED_BY_NAME = "by_kind"
            const val COLLAPSED_BY_FIELD = "_kind.keyword"
        }
    }
}
