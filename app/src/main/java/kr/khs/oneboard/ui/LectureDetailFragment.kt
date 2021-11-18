package kr.khs.oneboard.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kr.khs.oneboard.core.BaseFragment
import kr.khs.oneboard.databinding.FragmentLectureDetailBinding
import kr.khs.oneboard.viewmodels.LectureDetailViewModel

@AndroidEntryPoint
class LectureDetailFragment : BaseFragment<FragmentLectureDetailBinding, LectureDetailViewModel>() {
    override val viewModel: LectureDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.lectureInfo.observe(viewLifecycleOwner) {
            // sample
            binding.temp.text = """
            ${it.title} - ${it.semester}
            ${it.professor}
            공지사항, 과제 등의 추가적인 정보는
            추후 추가 예정입니다.
        """.trimIndent()
        }
    }

    override fun getFragmentViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLectureDetailBinding =
        FragmentLectureDetailBinding.inflate(inflater, container, false)

    override fun init() {
        getSafeArgs()
        inflateMenu(true)

    }

    private fun getSafeArgs() {
        viewModel.setLectureInfo(parentViewModel.getLecture().id)
    }

}