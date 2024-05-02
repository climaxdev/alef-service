package com.alefeducation.authoring.search.infra.http

import com.alefeducation.authoring.search.AbstractWebIntegrationTest
import com.alefeducation.authoring.search.domain.KindType.POOLS
import com.alefeducation.authoring.search.domain.KindType.QUESTIONS
import com.alefeducation.authoring.search.domain.KindType.WORKSPACES
import com.alefeducation.authoring.search.domain.SuggestionKindType.KEYWORD
import com.alefeducation.authoring.search.domain.projections.SuggestionProjection
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates

@ExperimentalCoroutinesApi
class SuggestionControllerTest : AbstractWebIntegrationTest() {

    @BeforeAll
    fun `data init`() {

        val mapper = ObjectMapper()
        val suggestionsJsonData = javaClass.getResource("/suggestions-data.json")!!.readText()
        val items = mapper.readValue<List<Any>>(suggestionsJsonData)

        val indexCoordinates = IndexCoordinates.of("aat")
        for (item in items) {
            reactiveOperations.save(item, indexCoordinates).block()
        }

        // refresh index to make sure all the documents are searchable
        reactiveOperations.indexOps(IndexCoordinates.of("aat")).refresh().block()
    }

    @Test
    fun `should return suggestions for give input {question code}`() = runTest {
        val expectedSuggestions = listOf(
            SuggestionProjection("keyword", "2021_Math_G08_MML_0070_Q_15")
        )

        val actualSuggestions = requestSuggestionsApi("2021_", "5", listOf(QUESTIONS))

        actualSuggestions shouldBe expectedSuggestions
    }

    @Test
    fun `should return suggestions for give input {pool code}`() = runTest {
        val expectedSuggestions = listOf(
            SuggestionProjection("keyword", "MA10_MLO_033"),
            SuggestionProjection("keyword", "MA10_MLO_034"),
        )
        requestSuggestionsApi("MA", "5", listOf(POOLS))
        val actualSuggestions = requestSuggestionsApi("MA", "5", listOf(POOLS))

        actualSuggestions shouldBe expectedSuggestions
    }

    @Test
    fun `should return suggestions for give input {question body}`() = runTest {
        val expectedSuggestions = listOf(
            SuggestionProjection("keyword", "What is the length of line segment AB?Use the dist")
        )

        val actualSuggestions = requestSuggestionsApi("What is the length", "5", listOf(QUESTIONS))

        actualSuggestions shouldBe expectedSuggestions
    }

    @Test
    fun `should return suggestions for give input {workspace}`() = runTest {
        val expectedSuggestions = listOf(
            SuggestionProjection("keyword", "Arabic - Below Level"),
            SuggestionProjection("keyword", "Arabic - Grade 10"),
            SuggestionProjection("keyword", "Arabic - Grade 11"),
            SuggestionProjection("keyword", "Arabic - Grade 12"),
            SuggestionProjection("keyword", "Arabic Diagnostic Tests")
        )

        val actualSuggestions = requestSuggestionsApi("Arabic", "5", listOf(WORKSPACES))

        actualSuggestions shouldBe expectedSuggestions
    }

    @Test
    fun `should return pools, questions, workspaces count equal to the limit passed in request`() = runTest {
        val expectedSuggestions = listOf(
            SuggestionProjection("keyword", "Math Advance - Grade 10"),
            SuggestionProjection("keyword", "MA_Math_G08_MML_0070_Q_15"),
            SuggestionProjection("keyword", "MA10_MLO_033"),
            SuggestionProjection("keyword", "MA10_MLO_034"),
        )

        val actualSuggestions = requestSuggestionsApi("Ma", "10", listOf(POOLS, QUESTIONS, WORKSPACES))

        actualSuggestions shouldBe expectedSuggestions
    }

    private fun requestSuggestionsApi(
        term: String,
        limit: String,
        kinds: List<Any>
    ): MutableList<SuggestionProjection> =
        webClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/v1/suggestions")
                    .queryParam("organisation", "shared")
                    .queryParam("term", term)
                    .queryParam("limit", limit)
                    .queryParam("kinds", kinds)
                    .queryParam("suggestionKind", KEYWORD).build()
            }
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList(SuggestionProjection::class.java)
            .returnResult().responseBody!!
}
