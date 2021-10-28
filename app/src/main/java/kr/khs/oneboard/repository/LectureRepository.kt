package kr.khs.oneboard.repository

import kr.khs.oneboard.data.Assignment
import kr.khs.oneboard.data.Notice

interface LectureRepository {
    suspend fun getNoticeList(lectureId: Int): List<Notice>

    suspend fun postNotice(): Boolean

    suspend fun getAssignmentList(lectureId: Int): List<Assignment>

    suspend fun postAssignment(): Boolean
}