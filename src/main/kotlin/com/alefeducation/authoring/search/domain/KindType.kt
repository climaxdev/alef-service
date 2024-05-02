package com.alefeducation.authoring.search.domain

enum class KindType(val value: String) {
    WORKSPACES("workspaces"),
    QUESTIONS("questions"),
    POOLS("pools");

    companion object {
        fun getKindTypeFor(value: String): KindType? = values().find { it.value == value }
    }
}
