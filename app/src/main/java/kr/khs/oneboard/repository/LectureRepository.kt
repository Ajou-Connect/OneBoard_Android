package kr.khs.oneboard.repository

import kr.khs.oneboard.core.UseCase
import kr.khs.oneboard.data.Assignment
import kr.khs.oneboard.data.AttendanceStudent
import kr.khs.oneboard.data.Lecture
import kr.khs.oneboard.data.Notice
import kr.khs.oneboard.data.request.AssignmentUpdateRequestDto
import kr.khs.oneboard.data.request.AttendanceUpdateRequestDto
import kr.khs.oneboard.data.request.NoticeUpdateRequestDto

interface LectureRepository {
    suspend fun getDetailLecture(lectureId: Int): UseCase<Lecture>

    suspend fun getNoticeList(lectureId: Int): UseCase<List<Notice>>

    suspend fun postNotice(lectureId: Int, notice: NoticeUpdateRequestDto): UseCase<Boolean>

    suspend fun putNotice(
        lectureId: Int,
        noticeId: Int,
        notice: NoticeUpdateRequestDto
    ): UseCase<Boolean>

    suspend fun deleteNotice(lectureId: Int, noticeId: Int): UseCase<Boolean>

    suspend fun getAssignmentList(lectureId: Int): UseCase<List<Assignment>>

    suspend fun postAssignment(
        lectureId: Int,
        assignment: AssignmentUpdateRequestDto
    ): UseCase<Boolean>

    suspend fun putAssignment(
        lectureId: Int,
        assignmentId: Int,
        assignment: AssignmentUpdateRequestDto
    ): UseCase<Boolean>

    suspend fun deleteAssignment(lectureId: Int, assignmentId: Int): UseCase<Boolean>

    suspend fun getAttendanceList(lectureId: Int): UseCase<List<AttendanceStudent>>

    suspend fun postAttendanceList(
        lectureId: Int,
        dto: AttendanceUpdateRequestDto
    ): UseCase<Boolean>
}