package kr.khs.oneboard.repository

import kr.khs.oneboard.core.UseCase
import kr.khs.oneboard.data.Assignment
import kr.khs.oneboard.data.Notice
import kr.khs.oneboard.data.request.NoticeUpdateRequestDto

interface LectureRepository {
    suspend fun getNoticeList(lectureId: Int): UseCase<List<Notice>>

    suspend fun postNotice(lectureId: Int, notice: NoticeUpdateRequestDto): UseCase<Boolean>

    suspend fun putNotice(lectureId: Int, noticeId: Int, notice: NoticeUpdateRequestDto): UseCase<Boolean>

    suspend fun deleteNotice(lectureId: Int, noticeId: Int): UseCase<Boolean>

    suspend fun getAssignmentList(lectureId: Int): UseCase<List<Assignment>>

    suspend fun postAssignment(assignment: Assignment): UseCase<Boolean>
}