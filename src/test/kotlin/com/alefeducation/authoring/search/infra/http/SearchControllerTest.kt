package com.alefeducation.authoring.search.infra.http

import com.alefeducation.authoring.search.AbstractWebIntegrationTest
import com.alefeducation.authoring.search.domain.KindType
import com.alefeducation.authoring.search.domain.SearchResult
import com.alefeducation.authoring.search.infra.http.inputs.SearchInput
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.web.reactive.function.BodyInserters

@ExperimentalCoroutinesApi
class SearchControllerTest : AbstractWebIntegrationTest() {

    @BeforeAll
    fun `data init`() {
        val mapper = ObjectMapper()
        // insert 9 entries 3 for each kind, and each entry have string test in the feedback or name
        val searchData = javaClass.getResource("/search-data.json")!!.readText()
        val items = mapper.readValue<List<Any>>(searchData)

        val indexCoordinates = IndexCoordinates.of("aat")
        for (item in items) {
            reactiveOperations.save(item, indexCoordinates).block()
        }

        // refresh index to make sure all the documents are searchable
        reactiveOperations.indexOps(IndexCoordinates.of("aat")).refresh().block()
    }

    @Test
    fun `should return success result when search with the valid term`() = runTest {
        val requestBody = SearchInput(
            org = "shared",
            term = "test",
            limit = 5,
            kinds = listOf(KindType.QUESTIONS, KindType.POOLS, KindType.WORKSPACES),
        )

        val expectedTotalHits = 9L
        val expectedPoolHits = 3L
        val expectedWorkspaceHits = 3L
        val expectedQuestionHits = 3L

        val result = searchResult(requestBody)

        result.total shouldBe expectedTotalHits
        result.pools?.total shouldBe expectedPoolHits
        result.workspaces?.total shouldBe expectedWorkspaceHits
        result.questions?.total shouldBe expectedQuestionHits
    }

    @Test
    fun `should return pools, questions, workspaces count equal to the limit passed in request`() = runTest {
        val givenLimit = 2
        val requestBody = SearchInput(
            org = "shared",
            term = "test",
            limit = givenLimit,
            kinds = listOf(KindType.QUESTIONS, KindType.POOLS, KindType.WORKSPACES),
        )

        val expectedTotalHits = 9L
        val expectedPoolHits = 3L
        val expectedWorkspaceHits = 3L
        val expectedQuestionHits = 3L

        val result = searchResult(requestBody)

        result.total shouldBe expectedTotalHits
        result.pools?.run {
            total shouldBe expectedPoolHits
            hits.size shouldBe givenLimit
        }
        result.workspaces?.run {
            total shouldBe expectedWorkspaceHits
            hits.size shouldBe givenLimit
        }
        result.questions?.run {
            total shouldBe expectedQuestionHits
            hits.size shouldBe givenLimit
        }
    }

    @Test
    fun `should return pools by given workspace id`() = runTest {
        val givenLimit = 2
        val requestBody = SearchInput(
            org = "shared",
            term = "test",
            limit = givenLimit,
            kinds = listOf(KindType.POOLS, KindType.WORKSPACES),
        )
        // all pools belong to the same workspace
        val expectedWorkspaceId = "A0lqc6JxxPZN23v3uoEkgQ=="

        val result = searchResult(requestBody)

        result.pools?.run {
            hits.all { it.workspaceId == expectedWorkspaceId }
        }
    }

    private fun searchResult(requestBody: SearchInput): SearchResult {
        val result = webClient.post()
            .uri { uriBuilder ->
                uriBuilder.path("/v1/search").build()
            }
            .body(BodyInserters.fromValue(requestBody))
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(SearchResult::class.java)
            .returnResult().responseBody!!
        return result
    }
}
