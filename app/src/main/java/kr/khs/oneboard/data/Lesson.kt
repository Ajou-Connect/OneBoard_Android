package kr.khs.oneboard.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kr.khs.oneboard.data.request.LessonUpdateRequestDto

@Parcelize
data class Lesson(
    val id: Int,
    val lectureId: Int,
    val title: String,
    val date: String,
    val note: String? = null,
    val type: Int,
    val room: String? = null,
    val meetingId: String? = null,
    val videoUrl: String? = null
) : Parcelable {
    fun toLessonUpdateRequestDto() = LessonUpdateRequestDto(
        title = title,
        date = date,
        type = type,
        room = room,
        meetingId = meetingId,
        videoUrl = videoUrl
    )
}
