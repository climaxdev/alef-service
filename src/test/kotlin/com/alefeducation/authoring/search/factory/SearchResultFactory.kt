package com.alefeducation.authoring.search.factory

import com.alefeducation.authoring.search.domain.PoolsHit
import com.alefeducation.authoring.search.domain.PoolsResult
import com.alefeducation.authoring.search.domain.QuestionsHit
import com.alefeducation.authoring.search.domain.QuestionsResult
import com.alefeducation.authoring.search.domain.SearchResult
import com.alefeducation.authoring.search.domain.WorkspaceHit
import com.alefeducation.authoring.search.domain.WorkspacesResult

object SearchResultFactory {

    fun createSearchResult(withPoolCount: Long, withQuestionCount: Long, withWorkspaceCount: Long) = SearchResult.Builder()
        .workspaces(WorkspacesResultFactory.createWorkspacesResult(withWorkspaceCount))
        .questions(QuestionsResultFactory.createQuestionsResult(withQuestionCount))
        .pools(PoolsResultFactory.createPoolsResult(withPoolCount))
        .total(withPoolCount + withQuestionCount + withWorkspaceCount)
        .build()
}

object PoolsResultFactory {
    fun createPoolsResult(withPoolCount: Long): PoolsResult {
        (1..withPoolCount).map { PoolsHit("pool_name_$it", "pool_id_$it") }.let {
            return PoolsResult(it, withPoolCount)
        }
    }
}

object QuestionsResultFactory {
    fun createQuestionsResult(withQuestionCount: Long): QuestionsResult {
        (1..withQuestionCount).map { QuestionsHit("question_code_$it", "question_id_$it") }.let {
            return QuestionsResult(it, withQuestionCount)
        }
    }
}

object WorkspacesResultFactory {
    fun createWorkspacesResult(withWorkspaceCount: Long): WorkspacesResult {
        (1..withWorkspaceCount).map { WorkspaceHit("workspace_name_$it", "workspace_id_$it") }.let {
            return WorkspacesResult(it, withWorkspaceCount)
        }
    }
}
