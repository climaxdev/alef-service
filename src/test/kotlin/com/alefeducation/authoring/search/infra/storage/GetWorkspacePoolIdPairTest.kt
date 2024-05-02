package com.alefeducation.authoring.search.infra.storage

import com.alefeducation.authoring.search.builders.PoolBuilder
import com.alefeducation.authoring.search.builders.WorkspaceBuilder
import com.alefeducation.authoring.search.infra.BaseTestIT
import com.alefeducation.authoring.search.infra.toMap
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates

@OptIn(ExperimentalCoroutinesApi::class)
class GetWorkspacePoolIdPairTest(
    @Autowired
    private val getWorkspacePoolIdPair: GetWorkspacePoolIdPair
) : BaseTestIT() {

    companion object {
        const val AAT_INDEX_NAME = "aat"
    }

    @Test
    fun `should successfully fetch the associated workspaceId for the given pools`() = runTest {

        // GIVEN
        val org = "shared"
        // create 5 pool containing string test in the name
        (1..5).map {
            PoolBuilder(id = "pool_id_$it", name = "test$it").build().toMap()
        }.let { reactiveOperations.saveAll(it, IndexCoordinates.of(AAT_INDEX_NAME)).blockLast() }

        // create 3 workspaces first workspace will have poolid - pool_id_1, pool_id_2
        WorkspaceBuilder(id = "test-workspace-id-1", name = "test1", pools = listOf("pool_id_1", "pool_id_2")).build().toMap()
            .let { reactiveOperations.save(it, IndexCoordinates.of(AAT_INDEX_NAME)).block() }
        // second workspace will have poolid - pool_id_3, pool_id_4
        WorkspaceBuilder(id = "test-workspace-id-2", name = "test2", pools = listOf("pool_id_3", "pool_id_4")).build().toMap()
            .let { reactiveOperations.save(it, IndexCoordinates.of(AAT_INDEX_NAME)).block() }
        // third workspace will have poolid - pool_id_5
        WorkspaceBuilder(id = "test-workspace-id-3", name = "test3", pools = listOf("pool_id_5")).build().toMap()
            .let { reactiveOperations.save(it, IndexCoordinates.of(AAT_INDEX_NAME)).block() }

        // refresh index to make sure all the documents are searchable

        reactiveOperations.indexOps(IndexCoordinates.of(AAT_INDEX_NAME)).refresh().block()

        // WHEN
        getWorkspacePoolIdPair(org, listOf("pool_id_1", "pool_id_2", "pool_id_3", "pool_id_4", "pool_id_5")).toList().forEach {
            // THEN
            when (it.poolId) {
                "pool_id_1", "pool_id_2" -> it.workspaceId shouldBe "test-workspace-id-1"
                "pool_id_3", "pool_id_4" -> it.workspaceId shouldBe "test-workspace-id-2"
                "pool_id_5" -> it.workspaceId shouldBe "test-workspace-id-3"
                else -> throw IllegalStateException("Unexpected pool id ${it.poolId}")
            }
        }
    }
}
