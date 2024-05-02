package com.alefeducation.authoring.search.builders

data class Workspace(
    val id: String,
    val name: String,
    val curriculumId: String,
    val gradeId: String,
    val subjectId: String,
    val defaultLanguage: String,
    val users: List<String>,
    val pools: List<String>,
    val unassignedQuestions: List<String>,
    val archivedQuestions: List<String>,
    val createdAt: String,
    val updatedAt: String,
    val `class`: String,
    val organisations: List<String>,
)

data class WorkspaceBuilder(
    val id: String = "A0lqc6JxxPZN23v3uoEkgQ==",
    val name: String = "test workspace name 1",
    val curriculumId: String = "392027",
    val gradeId: String = "104322",
    val subjectId: String = "453323",
    val defaultLanguage: String = "EN_US",
    val users: List<String> = listOf(
        "44", "45", "731568", "819767", "517764", "273", "835211", "32203", "355", "707291", "57586",
        "359", "361149", "390367", "503707", "519893", "283", "164", "962693", "165", "877911", "297151"
    ),
    val pools: List<String> = listOf(
        "5b5adf35017d140001907d1b", "5b6546932be9da000159de86"
    ),
    val unassignedQuestions: List<String> = listOf(
        "MA11_MLO_001_Q35", "MA11_MLO_010_Q_36", "MA11_MLO_010_Q_42"
    ),
    val archivedQuestions: List<String> = listOf(),
    val createdAt: String = "2020-06-11T05:38:23Z",
    val updatedAt: String = "2020-06-11T05:38:23Z",
    val `class`: String = "com.alefeducation.assessmentworkspace.model.Workspace",
    val organisations: List<String> = listOf("shared")
) {
    fun build() = Workspace(
        id, name, curriculumId, gradeId, subjectId, defaultLanguage, users, pools,
        unassignedQuestions, archivedQuestions, createdAt, updatedAt, `class`, organisations
    )
}
