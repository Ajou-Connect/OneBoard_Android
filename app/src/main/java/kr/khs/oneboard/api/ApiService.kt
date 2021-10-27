package kr.khs.oneboard.api

import kr.khs.oneboard.data.Assignment
import kr.khs.oneboard.data.Lecture
import kr.khs.oneboard.data.Notice
import kr.khs.oneboard.data.api.Response
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("healthCheck")
    suspend fun healthCheck(): Response<Any>

    @POST("auth/login")
    suspend fun login(): Response<Any>

    @GET("auth/logout")
    suspend fun logout(): Response<Any>

    @POST("auth/check")
    suspend fun loginCheck(): Response<Any>

    @GET("user")
    suspend fun getUserInfo(): Response<Any>

    @GET("user/lectures")
    suspend fun getUserLectures(): Response<List<Lecture>>

    @GET("lecture")
    suspend fun getUserLecture(): Response<Lecture>

    @GET("lecture/notice")
    suspend fun getNoticeList(lectureId: Int): Response<List<Notice>>

    // todo 등록, 수정, 삭제
    @POST("lecture/notice")
    suspend fun postNotice()

//    @GET("lecture/attendance")
//    suspend fun getAttendance()
//
//    @POST("lecture/attendance")
//    suspend fun postAttendance()

    @GET("lecture/assignment")
    suspend fun getAssignmentList(lectureId: Int): Response<List<Assignment>>

    // todo 등록, 수정, 삭제
    @POST("lecture/assignment")
    suspend fun postAssignment()

    // todo lecture/assignment/result부터
}