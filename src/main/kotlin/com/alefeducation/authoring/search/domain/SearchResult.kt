package com.alefeducation.authoring.search.domain

data class WorkspaceHit(val name: String, val id: String)

data class QuestionsHit(
    val code: String,
    val id: String
)

data class PoolsHit(
    val name: String,
    val id: String,
    val workspaceId: String? = null
)

data class WorkspacesResult(
    val hits: List<WorkspaceHit>,
    val total: Long
)

data class QuestionsResult(
    val hits: List<QuestionsHit>,
    val total: Long
)

data class PoolsResult(
    val hits: List<PoolsHit>,
    val total: Long
) {
    fun enrichWithWorkspaceId(workspaceIdByPoolId: Map<String, String>): PoolsResult {
        this.hits.map { it.copy(workspaceId = workspaceIdByPoolId[it.id]) }.toList().let {
            return this.copy(hits = it)
        }
    }
}

data class SearchResult(
    val workspaces: WorkspacesResult?,
    val questions: QuestionsResult?,
    val pools: PoolsResult?,
    val total: Long
) {
    fun enrichPoolsWithWorkspaceId(workspaceIdByPoolId: Map<String, String>?): SearchResult {
        if (workspaceIdByPoolId == null) return this
        return this.copy(pools = this.pools?.enrichWithWorkspaceId(workspaceIdByPoolId))
    }

    internal class Builder(
        private var workspaces: WorkspacesResult? = null,
        private var questions: QuestionsResult? = null,
        private var pools: PoolsResult? = null,
        private var total: Long? = 0
    ) {
        fun workspaces(workspaces: WorkspacesResult) = apply { this.workspaces = workspaces }
        fun questions(questions: QuestionsResult) = apply { this.questions = questions }
        fun pools(pools: PoolsResult) = apply { this.pools = pools }
        fun total(total: Long) = apply { this.total = total }
        fun build() = SearchResult(
            workspaces = workspaces,
            questions = questions,
            pools = pools,
            total = total!!
        )
    }
}

data class WorkspacePoolIdPair(val workspaceId: String, val poolId: String)
