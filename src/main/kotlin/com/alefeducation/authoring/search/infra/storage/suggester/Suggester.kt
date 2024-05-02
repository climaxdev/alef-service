package com.alefeducation.authoring.search.infra.storage.suggester

import co.elastic.clients.elasticsearch.core.search.CompletionContext
import co.elastic.clients.elasticsearch.core.search.Context
import co.elastic.clients.elasticsearch.core.search.FieldSuggester
import co.elastic.clients.elasticsearch.core.search.SuggestFuzziness
import com.alefeducation.authoring.search.domain.Constants
import com.alefeducation.authoring.search.domain.Constants.Companion.SEPARATOR
import com.alefeducation.authoring.search.infra.http.inputs.GetSuggestionInput
import com.alefeducation.authoring.search.infra.storage.StorageSuggester

internal interface SuggesterFactory<out T : StorageSuggester> {
    operator fun invoke(suggestionInput: GetSuggestionInput): T
}

internal abstract class Suggester(protected val suggestionInput: GetSuggestionInput) {
    abstract val fieldSuggesterName: String
    abstract val fieldRawName: String
    abstract val applicableKinds: List<String>
    abstract val name: String
    abstract fun build(): FieldSuggester
}

internal abstract class CompletionSuggester(suggestionInput: GetSuggestionInput) : StorageSuggester(suggestionInput) {
    override fun build(): FieldSuggester {
        return FieldSuggester.Builder().prefix(suggestionInput.term).completion(
            completionBuilder()
                .build()
        ).build()
    }

    protected open fun completionBuilder(): co.elastic.clients.elasticsearch.core.search.CompletionSuggester.Builder {
        return co.elastic.clients.elasticsearch.core.search.CompletionSuggester.Builder()
            .field(fieldSuggesterName)
            .contexts(getContexts())
            .size(suggestionInput.limit)
            .skipDuplicates(true)
    }

    private fun getContexts(): Map<String, List<CompletionContext>> =
        mapOf<String, List<CompletionContext>>(
            Constants.SUGGESTER_CONTEXT to applicableKinds.map { applicableKind ->
                val context = suggestionInput.organisation + SEPARATOR + applicableKind
                CompletionContext.Builder().context(Context.Builder().category(context).build()).build()
            }
        )
}

internal class QuestionBodySuggester(suggestionInput: GetSuggestionInput) :
    CompletionSuggester(suggestionInput) {
    override val fieldSuggesterName: String = "body.prompt_plain.completion"
    override val fieldRawName: String = "body.prompt_plain"
    override val applicableKinds = listOf("questions")
    override val name: String = "question-body-suggest"

    companion object : SuggesterFactory<QuestionBodySuggester> {
        override fun invoke(suggestionInput: GetSuggestionInput): QuestionBodySuggester =
            QuestionBodySuggester(suggestionInput)
    }

    override fun completionBuilder(): co.elastic.clients.elasticsearch.core.search.CompletionSuggester.Builder {
        return super.completionBuilder().fuzzy(SuggestFuzziness.Builder().fuzziness("AUTO").build())
    }
}

internal class WorkspaceSuggester(suggestionInput: GetSuggestionInput) :
    CompletionSuggester(suggestionInput) {
    override val fieldSuggesterName: String = "name.completion"
    override val fieldRawName: String = "name"
    override val applicableKinds = listOf("workspaces")
    override val name: String = "workspace-name-suggest"

    companion object : SuggesterFactory<WorkspaceSuggester> {
        override fun invoke(suggestionInput: GetSuggestionInput): WorkspaceSuggester =
            WorkspaceSuggester(suggestionInput)
    }

    override fun completionBuilder(): co.elastic.clients.elasticsearch.core.search.CompletionSuggester.Builder {
        return super.completionBuilder().fuzzy(SuggestFuzziness.Builder().fuzziness("AUTO").build())
    }
}

internal class QuestionCodeSuggester(suggestionInput: GetSuggestionInput) :
    CompletionSuggester(suggestionInput) {
    override val fieldSuggesterName: String = "code.completion"
    override val fieldRawName: String = "code"
    override val applicableKinds = listOf("questions")
    override val name: String = "question-code-suggest"

    companion object : SuggesterFactory<QuestionCodeSuggester> {
        override fun invoke(suggestionInput: GetSuggestionInput): QuestionCodeSuggester =
            QuestionCodeSuggester(suggestionInput)
    }
}

internal class PoolsCodeSuggester(suggestionInput: GetSuggestionInput) :
    CompletionSuggester(suggestionInput) {
    override val fieldSuggesterName: String = "name.completion"
    override val fieldRawName: String = "name"
    override val applicableKinds = listOf("pools")
    override val name: String = "pool-code-suggest"

    companion object : SuggesterFactory<PoolsCodeSuggester> {
        override fun invoke(suggestionInput: GetSuggestionInput): PoolsCodeSuggester =
            PoolsCodeSuggester(suggestionInput)
    }
}
