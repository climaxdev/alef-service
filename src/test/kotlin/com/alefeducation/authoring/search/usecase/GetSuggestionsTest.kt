package com.alefeducation.authoring.search.usecase

import com.alefeducation.authoring.search.domain.KindType.QUESTIONS
import com.alefeducation.authoring.search.domain.KindType.WORKSPACES
import com.alefeducation.authoring.search.domain.SuggestionKindType
import com.alefeducation.authoring.search.factory.SuggestionFactory
import com.alefeducation.authoring.search.infra.http.inputs.GetSuggestionInput
import com.alefeducation.authoring.search.infra.storage.SuggestionStorage
import io.kotest.matchers.collections.shouldContainAnyOf
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class GetSuggestionsTest {

    private val suggestionStorage: SuggestionStorage = mockk()
    private val getSuggestions = GetSuggestions(suggestionStorage)

    @Test
    fun `should filter suggestions according to priority`() = runTest {
        val suggestions = SuggestionFactory.createSuggestions()
        val expectedSuggestions = suggestions.filter { it.score == 5.0 }
        val suggestionInput =
            GetSuggestionInput(
                term = "ma",
                limit = 5,
                kind = listOf(WORKSPACES, QUESTIONS),
                "shared",
                SuggestionKindType.KEYWORD
            )
        coEvery { suggestionStorage.suggestions(suggestionInput) } answers { expectedSuggestions.asFlow() }

        val actualSuggestions = getSuggestions(suggestionInput)

        actualSuggestions.toList() shouldContainAnyOf expectedSuggestions
    }
}
