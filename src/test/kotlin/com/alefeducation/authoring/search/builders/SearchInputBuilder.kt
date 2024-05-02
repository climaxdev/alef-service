package com.alefeducation.authoring.search.builders

import com.alefeducation.authoring.search.domain.Constants
import com.alefeducation.authoring.search.domain.KindType
import com.alefeducation.authoring.search.infra.http.inputs.SearchInput

data class SearchInputBuilder(
    var org: String = "shared",
    var kinds: List<KindType> = KindType.values().toList(),
    var offset: Int = 0,
    var limit: Int = Constants.Search.DEFAULT_LIMIT,
    var term: String = "test"
) {
    fun org(org: String) = apply { this.org = org }
    fun kinds(kinds: List<KindType>) = apply { this.kinds = kinds }
    fun offset(offset: Int) = apply { this.offset = offset }
    fun limit(limit: Int) = apply { this.limit = limit }
    fun term(term: String) = apply { this.term = term }

    fun build() = SearchInput(org, kinds, offset, limit, term)
}
