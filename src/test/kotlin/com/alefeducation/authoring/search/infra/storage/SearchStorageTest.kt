package com.alefeducation.authoring.search.infra.storage

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders
import com.alefeducation.authoring.search.builders.PoolBuilder
import com.alefeducation.authoring.search.builders.QuestionBuilder
import com.alefeducation.authoring.search.builders.SearchInputBuilder
import com.alefeducation.authoring.search.builders.WorkspaceBuilder
import com.alefeducation.authoring.search.domain.KindType
import com.alefeducation.authoring.search.infra.BaseTestIT
import com.alefeducation.authoring.search.infra.toMap
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class SearchStorageTest(
    @Autowired
    private val searchStorage: SearchStorage
) : BaseTestIT() {

    companion object {
        const val AAT_INDEX_NAME = "aat"
    }

    @AfterEach
    override fun afterEach() {
        reactiveOperations.delete(
            NativeQueryBuilder().withQuery(Query(QueryBuilders.matchAll().build())).build(),
            Any::class.java, IndexCoordinates.of("aat")
        ).block()
    }

    @Test
    fun `should return all the matching workspaces, pools and questions`() = runTest {
        // GIVEN
        val givenQuestionCount = 5
        val givenPoolCount = 5
        val givenWorkspaceCount = 5

        // create 5 question containing string test in the code
        (1..givenQuestionCount).map {
            QuestionBuilder(id = UUID.randomUUID().toString(), code = "test$it").build().toMap()
        }.let { reactiveOperations.saveAll(it, IndexCoordinates.of(AAT_INDEX_NAME)).blockLast() }

        // create 5 workspaces containing string test in the name
        (1..givenWorkspaceCount).map {
            WorkspaceBuilder(id = UUID.randomUUID().toString(), name = "test$it").build().toMap()
        }.let { reactiveOperations.saveAll(it, IndexCoordinates.of(AAT_INDEX_NAME)).blockLast() }

        // create 5 pools containing string test in the name
        (1..givenPoolCount).map {
            PoolBuilder(id = UUID.randomUUID().toString(), name = "test$it").build().toMap()
        }.let { reactiveOperations.saveAll(it, IndexCoordinates.of(AAT_INDEX_NAME)).blockLast() }

        // refresh index to make sure all the documents are searchable
        reactiveOperations.indexOps(IndexCoordinates.of(AAT_INDEX_NAME)).refresh().block()

        // WHEN
        searchStorage.searchFor(SearchInputBuilder().term("test*").build()).run {
            // THEN
            this.total shouldBe givenQuestionCount + givenPoolCount + givenWorkspaceCount
            this.pools!!.run {
                this.total shouldBe givenPoolCount
                this.hits.size shouldBe givenPoolCount
            }
            this.questions!!.run {
                this.total shouldBe givenQuestionCount
                this.hits.size shouldBe givenQuestionCount
            }
            this.workspaces!!.run {
                this.total shouldBe givenWorkspaceCount
                this.hits.size shouldBe givenWorkspaceCount
            }
        }
    }

    @Test
    fun `should return only questions when kind type is questions`() = runTest {
        // GIVEN
        val givenQuestionCount = 5
        val givenPoolCount = 5
        val givenWorkspaceCount = 5

        // create 5 question containing string test in the code
        (1..givenQuestionCount).map {
            QuestionBuilder(id = UUID.randomUUID().toString(), code = "test$it").build().toMap()
        }.run {
            reactiveOperations.saveAll(this, IndexCoordinates.of(AAT_INDEX_NAME)).blockLast()
        }
        // create 5 workspaces containing string test in the name
        (1..givenWorkspaceCount).map {
            WorkspaceBuilder(id = UUID.randomUUID().toString(), name = "test$it").build().toMap()
        }.run {
            reactiveOperations.saveAll(this, IndexCoordinates.of(AAT_INDEX_NAME)).blockLast()
        }
        // create 5 pools containing string test in the name
        (1..givenPoolCount).map {
            PoolBuilder(id = UUID.randomUUID().toString(), name = "test$it").build().toMap()
        }.run {
            reactiveOperations.saveAll(this, IndexCoordinates.of(AAT_INDEX_NAME)).blockLast()
        }

        // refresh index to make sure all the documents are searchable
        reactiveOperations.indexOps(IndexCoordinates.of(AAT_INDEX_NAME)).refresh().block()

        // WHEN
        searchStorage.searchFor(SearchInputBuilder().term("test*").kinds(listOf(KindType.QUESTIONS)).build()).run {
            // THEN
            this.total shouldBe givenQuestionCount
            this.pools shouldBe null
            this.workspaces shouldBe null
            this.questions!!.run {
                this.total shouldBe givenQuestionCount
                this.hits.size shouldBe givenQuestionCount
            }
        }
    }

    @Test
    fun `should restrict inner hits data count based on limit passed in the request`() = runTest {
        // GIVEN
        val givenQuestionCount = 10
        val givenPoolCount = 15
        val givenWorkspaceCount = 20
        val givenLimit = 6

        // create 5 question containing string test in the code
        (1..givenQuestionCount).map {
            QuestionBuilder(id = UUID.randomUUID().toString(), code = "test$it").build().toMap()
        }.run {
            reactiveOperations.saveAll(this, IndexCoordinates.of(AAT_INDEX_NAME)).blockLast()
        }
        // create 5 workspaces containing string test in the name
        (1..givenWorkspaceCount).map {
            WorkspaceBuilder(id = UUID.randomUUID().toString(), name = "test$it").build().toMap()
        }.run {
            reactiveOperations.saveAll(this, IndexCoordinates.of(AAT_INDEX_NAME)).blockLast()
        }
        // create 5 pools containing string test in the name
        (1..givenPoolCount).map {
            PoolBuilder(id = UUID.randomUUID().toString(), name = "test$it").build().toMap()
        }.run {
            reactiveOperations.saveAll(this, IndexCoordinates.of(AAT_INDEX_NAME)).blockLast()
        }

        // refresh index to make sure all the documents are searchable
        reactiveOperations.indexOps(IndexCoordinates.of(AAT_INDEX_NAME)).refresh().block()

        // WHEN
        searchStorage.searchFor(SearchInputBuilder().term("test*").limit(givenLimit).build()).run {
            // THEN
            this.total shouldBe givenQuestionCount + givenPoolCount + givenWorkspaceCount
            this.pools!!.run {
                this.total shouldBe givenPoolCount
                this.hits.size shouldBe givenLimit
            }
            this.workspaces!!.run {
                this.total shouldBe givenWorkspaceCount
                this.hits.size shouldBe givenLimit
            }
            this.questions!!.run {
                this.total shouldBe givenQuestionCount
                this.hits.size shouldBe givenLimit
            }
        }
    }

    @Test
    fun `should successfully paginate for the questions when inner hits are greater than the limit `() = runTest {
        // GIVEN
        val givenQuestionCount = 8
        val givenLimit = 5

        // create 5 question containing string test in the code
        (1..givenQuestionCount).map {
            QuestionBuilder(id = UUID.randomUUID().toString(), code = "test$it").build().toMap()
        }.run {
            reactiveOperations.saveAll(this, IndexCoordinates.of(AAT_INDEX_NAME)).blockLast()
        }

        // refresh index to make sure all the documents are searchable
        reactiveOperations.indexOps(IndexCoordinates.of(AAT_INDEX_NAME)).refresh().block()

        // WHEN
        searchStorage.searchFor(SearchInputBuilder().term("test*").limit(5).offset(0).build()).run {
            // THEN
            this.total shouldBe givenQuestionCount
            this.questions!!.run {
                this.total shouldBe givenQuestionCount
                this.hits.size shouldBe givenLimit
            }
        }

        // we have fetched 5 question already, lets fetch next 5

        // WHEN
        searchStorage.searchFor(SearchInputBuilder().term("test*").limit(5).offset(5).build()).run {
            // THEN
            this.total shouldBe givenQuestionCount
            this.questions!!.run {
                this.total shouldBe givenQuestionCount
                this.hits.size shouldBe 3 // it should be 3 as we have already fetched 5 questions
            }
        }
    }
}
