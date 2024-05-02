package com.alefeducation.authoring.search.infra.http.inputs

import com.alefeducation.authoring.search.domain.Constants
import com.alefeducation.authoring.search.domain.KindType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class SearchInput(
    @NotBlank
    val org: String,
    @NotEmpty
    val kinds: List<KindType>,
    val offset: Int = 0,
    val limit: Int = Constants.Search.DEFAULT_LIMIT,
    @NotBlank
    val term: String
)
