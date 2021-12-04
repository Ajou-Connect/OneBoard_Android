package kr.khs.oneboard.repository

import kr.khs.oneboard.core.UseCase

interface SessionRepository {
    suspend fun leaveLesson(
        lectureId: Int,
        lessonId: Int
    ): UseCase<Boolean>

    suspend fun postAttendanceProfessor(
        lectureId: Int,
        lessonId: Int
    ): UseCase<Boolean>

    suspend fun postAttendanceStudent(
        lectureId: Int,
        lessonId: Int
    ): UseCase<Boolean>

    suspend fun postUnderStanding(
        lectureId: Int,
        lessonId: Int,
        liveId: Int,
        select: String
    ): UseCase<Boolean>

    suspend fun getUnderStanding(
        lectureId: Int,
        lessonId: Int,
        liveId: Int,
        understandingId: Int
    ): UseCase<Boolean>

    suspend fun getQuiz(
        lectureId: Int,
        lessonId: Int,
        liveId: Int
    ): UseCase<Boolean>

    suspend fun postQuiz(
        lectureId: Int,
        lessonId: Int,
        liveId: Int,
        quizId: Int,
        answer: Int
    ): UseCase<Boolean>
}