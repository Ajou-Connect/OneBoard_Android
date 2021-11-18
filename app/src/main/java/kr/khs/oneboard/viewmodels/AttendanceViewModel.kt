package kr.khs.oneboard.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kr.khs.oneboard.core.BaseViewModel
import kr.khs.oneboard.data.AttendanceLesson
import kr.khs.oneboard.data.AttendanceStudent
import kr.khs.oneboard.repository.LectureRepository
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(private val lectureRepository: LectureRepository) :
    BaseViewModel() {

    private val _attendanceList = MutableLiveData<List<AttendanceStudent>>()
    val attendanceList: LiveData<List<AttendanceStudent>>
        get() = _attendanceList

    fun getAttendanceList(lectureId: Int) {
        viewModelScope.launch {
            showProgress()
            _attendanceList.value = lectureRepository.getAttendanceList(lectureId).data!!
            hideProgress()
        }
    }

    fun updateAttendance(item: AttendanceLesson) {
        val temp = _attendanceList.value!!
        out@ for (student in temp) {
            for (lesson in student.lessonList) {
                if (lesson.id == item.id) {
                    lesson.check = item.check
                    break@out
                }
            }
        }
    }

    fun saveAttendanceList() {
        viewModelScope.launch {
            // todo 서버 작업
        }
    }
}