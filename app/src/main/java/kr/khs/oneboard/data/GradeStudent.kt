package kr.khs.oneboard.data

import com.squareup.moshi.Json

data class GradeStudent(
    val lectureId: Int,
    val lectureTitle: String,
    val userId: Int,
    val userName: String,
    val studentNumber: String,
    val totalScore: Float,
    val submitScore: Float,
    val attendScore: Float,
    val result: String,
    val submitList: List<GradeSubmit>? = null,
) {
    data class GradeSubmit(
        val submitId: Int,
        val assignmentId: Int,
        val assignmentTitle: String,
        val score: Float
    )
}

data class GradeRatio(
    @Json(name = "aratio")
    val aRatio: Int,
    @Json(name = "bratio")
    val bRatio: Int
)