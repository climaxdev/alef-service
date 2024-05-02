package com.alefeducation.authoring.datasync

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component

@Component
class ChangeLogEventFactory {
    private val objectMapper = jacksonObjectMapper()

    companion object {
        private const val poolPersistedEvent =
            """ {"id":"q01","name":"Default pool title","questionCodePrefix":"Default pool title","status":"DRAFT","questions":[{"id":"question01"}],"provider":"SARAS","createdBy":"author","updatedBy":"author","createdAt":"2023-06-16T17:34:16","updatedAt":"2023-06-16T17:34:16","organisations":["shared"], "_kind": "POOLS"} """

        private const val workspacePersistedEvent =
            """ {"id":"458be08f-b607-41e3-ba42-59bc8a99f365","name":"Workspace Test","curriculumId":"curriculum TEST","gradeId":"4","subjectId":"math_subject_id","defaultLanguage":"EN_GB","users":["TEST_USER_ID"],"pools":["TEST_POOL_ID"],"unassignedQuestions":[],"archivedQuestions":[],"createdAt":"2023-06-20T16:46:38.130734","updatedAt":"2023-06-20T16:46:38.130779", "organisations": ["shared"], "_kind": "WORKSPACES"} """

        private const val questionPersistedEvent =
            """{"type":"MULTIPLE_CHOICE","id":"753e68b9-f729-4765-b379-e7bc9a076d10","code":"TEST_Q11","version":0,"language":"EN_GB","variant":"EN_GB","body":{"hints":[],"generalFeedback":"General Feedback","correctAnswerFeedback":"Correct Answer Feedback","wrongAnswerFeedback":"Wrong Answer Feedback","prompt":"Question prompt","choices":{"minChoice":0,"maxChoice":1,"layoutColumns":1,"shuffle":true,"listType":"NONE","choiceItems":[{"feedback":"Answer 1 feedback","choiceId":0,"weight":100.0,"answer":"Answer 1"},{"feedback":"Answer 1 feedback","choiceId":1,"weight":100.0,"answer":"Answer 1"},{"feedback":"Answer 1 feedback","choiceId":2,"weight":100.0,"answer":"Answer 1"}]},"passage":null},"validation":{"validResponse":{"type":"MULTIPLE_CHOICE","choiceIds":[2]},"scoringType":"EXACT_MATCH"},"stage":"IN_PROGRESS","maxScore":1,"metadata":{"keywords":["keyword1","keyword2"],"resourceType":"TEQ1","summativeAssessment":false,"difficultyLevel":"EASY","cognitiveDimensions":["ANALYZING","APPLYING","CREATING"],"knowledgeDimensions":["CONCEPTUAL","FACTUAL"],"lexileLevel":"any","copyrights":["ALEF","PUBLIC_DOMAIN"],"conditionsOfUse":["cOu1","cOu2"],"formatType":"QUESTION","author":"authorName","authoredDate":"2018-06-18T14:50:52","curriculumOutcomes":[{"type":"strand","id":"strand_id","name":"name","description":"description","curriculum":"curriculum","grade":"grade","subject":"subject"}],"skillId":null,"cefrLevel":"A1","proficiency":"READING","subSkill":"READING_COMPREHENSION"},"createdBy":"email","createdAt":"2023-06-21T22:45:07.182981","updatedBy":"email","updatedAt":"2023-06-21T22:45:07.182999","tags":null,"toolkits":null,"organisations":["shared"],"status":"DRAFT", "_kind": "QUESTIONS"} """
    }

    fun createPoolPersistedEvent(): Map<*, *> = objectMapper.readValue(poolPersistedEvent, Map::class.java)
    fun createWorkspacePersistedEvent(): Map<*, *> = objectMapper.readValue(workspacePersistedEvent, Map::class.java)

    fun createQuestionPersistedEvent(): Map<*, *> = objectMapper.readValue(questionPersistedEvent, Map::class.java)
}
