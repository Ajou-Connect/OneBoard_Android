package kr.khs.oneboard.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kr.khs.oneboard.api.ApiService
import kr.khs.oneboard.core.UseCase
import kr.khs.oneboard.utils.SUCCESS
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Named

class SessionRepositoryImpl @Inject constructor(
    @Named("withJWT") private val apiService: ApiService,
) : SessionRepository {
    override suspend fun leaveLesson(lectureId: Int, lessonId: Int): UseCase<Boolean> {
        var returnValue: UseCase<Boolean>
        delay(1000)
        returnValue = UseCase.success(true)

//        try {
//            withContext(Dispatchers.IO) {
//                val response = apiService.leaveLesson(lectureId, lessonId)
//
//                returnValue = UseCase.success(response.result == SUCCESS)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            returnValue = UseCase.error("error")
//        }

        return returnValue
    }

    override suspend fun postAttendanceProfessor(lectureId: Int, lessonId: Int): UseCase<Boolean> {
        var returnValue: UseCase<Boolean>

        try {
            withContext(Dispatchers.IO) {
                val response = apiService.postAttendanceStudent(lectureId, lessonId)

                returnValue = UseCase.success(response.result == SUCCESS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            returnValue = UseCase.error("error")
        }

        return returnValue
    }

    override suspend fun postAttendanceStudent(lectureId: Int, lessonId: Int): UseCase<Boolean> {
        var returnValue: UseCase<Boolean>

        try {
            withContext(Dispatchers.IO) {
                val response = apiService.postAttendanceProfessor(lectureId, lessonId)

                returnValue = UseCase.success(response.result == SUCCESS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            returnValue = UseCase.error("error")
        }

        return returnValue
    }

    override suspend fun postUnderStanding(
        lectureId: Int,
        lessonId: Int,
        liveId: Int,
        select: String
    ): UseCase<Boolean> {
        var returnValue: UseCase<Boolean>

        try {
            withContext(Dispatchers.IO) {
                val body = JSONObject().apply {
                    put("select", select)
                }
                val response = apiService.postUnderStanding(lectureId, lessonId, liveId, body)

                returnValue = UseCase.success(response.result == SUCCESS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            returnValue = UseCase.error("error")
        }

        return returnValue
    }

    override suspend fun getUnderStanding(
        lectureId: Int,
        lessonId: Int,
        liveId: Int,
        understandingId: Int
    ): UseCase<Boolean> {
        var returnValue: UseCase<Boolean>

        try {
            withContext(Dispatchers.IO) {
                val response =
                    apiService.getUnderStanding(lectureId, lessonId, liveId, understandingId)

                returnValue = UseCase.success(response.result == SUCCESS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            returnValue = UseCase.error("error")
        }

        return returnValue
    }

    override suspend fun getQuiz(lectureId: Int, lessonId: Int, liveId: Int): UseCase<Boolean> {
        var returnValue: UseCase<Boolean>

        try {
            withContext(Dispatchers.IO) {
                val response = apiService.getQuiz(lectureId, lessonId, liveId)

                returnValue = UseCase.success(response.result == SUCCESS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            returnValue = UseCase.error("error")
        }

        return returnValue
    }

    override suspend fun postQuiz(
        lectureId: Int,
        lessonId: Int,
        liveId: Int,
        quizId: Int,
        answer: Int
    ): UseCase<Boolean> {
        var returnValue: UseCase<Boolean>

        try {
            withContext(Dispatchers.IO) {
                val body = JSONObject().apply {
                    put("answer", answer)
                }
                val response = apiService.postQuiz(lectureId, lessonId, liveId, quizId, body)

                returnValue = UseCase.success(response.result == SUCCESS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            returnValue = UseCase.error("error")
        }

        return returnValue
    }
}