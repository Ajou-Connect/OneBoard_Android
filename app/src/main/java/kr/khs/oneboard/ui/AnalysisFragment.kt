package kr.khs.oneboard.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kr.khs.oneboard.R
import kr.khs.oneboard.core.BaseFragment
import kr.khs.oneboard.databinding.FragmentAnalysisBinding
import kr.khs.oneboard.utils.DialogUtil
import kr.khs.oneboard.utils.createOnBoardingDialog
import kr.khs.oneboard.utils.getOnBoardingSpf
import kr.khs.oneboard.viewmodels.AnalysisViewModel
import kr.khs.oneboard.views.LineView
import kr.khs.oneboard.views.PieHelper

@AndroidEntryPoint
class AnalysisFragment : BaseFragment<FragmentAnalysisBinding, AnalysisViewModel>() {
    override val viewModel: AnalysisViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.lessonList.observe(viewLifecycleOwner) { lessonList ->
            initSpinner(lessonList.map { it.title })
        }

        viewModel.understandingList.observe(viewLifecycleOwner) { understandingList ->
            if (understandingList.isEmpty()) {
                binding.analysisHorizontalView.visibility = View.INVISIBLE
                binding.analysisNoUnderstanding.visibility = View.VISIBLE
                return@observe
            }

            binding.analysisHorizontalView.visibility = View.VISIBLE
            binding.analysisNoUnderstanding.visibility = View.GONE

            val bottomTextList = ArrayList<String>()
            val dataList = ArrayList<ArrayList<Int>>()
            val oList = ArrayList<Int>()
            val xList = ArrayList<Int>()

            understandingList.forEach {
                bottomTextList.add(it.createdDt)
                oList.add(it.yes)
                xList.add(it.no)
            }
            dataList.add(oList)
            dataList.add(xList)

            initPlot(
                bottomTextList,
                dataList
            )
        }

        viewModel.quizList.observe(viewLifecycleOwner) { quizList ->
            if (quizList.isEmpty()) {
                binding.analysisQuizSpinner.visibility = View.GONE
                binding.analysisPie.visibility = View.INVISIBLE
                binding.analysisNoQuiz.visibility = View.VISIBLE
                return@observe
            }

            binding.analysisQuizSpinner.visibility = View.VISIBLE
            binding.analysisPie.visibility = View.VISIBLE
            binding.analysisNoQuiz.visibility = View.GONE

            initQuizSpinner(
                quizList.map { it.question }
            )
        }
    }

    override fun getFragmentViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAnalysisBinding = FragmentAnalysisBinding.inflate(layoutInflater)

    override fun init() {
        binding.viewTitle.root.text = "분석 정보 확인"

        viewModel.getLessonList(parentViewModel.getLecture().id)
    }

    private fun initSpinner(list: List<String>) {
        with(binding.analysisSpinner) {
            adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, list)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.getAnalysisInfo(
                        parentViewModel.getLecture().id,
                        viewModel.lessonList.value!![position].lessonId
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
            setSelection(0, false)
        }
    }

    private fun initQuizSpinner(list: List<String>) {
        with(binding.analysisQuizSpinner) {
            adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, list)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val correct = viewModel.quizList.value!![position].correctNum
                    val incorrect = viewModel.quizList.value!![position].incorrectNum

                    initPie(
                        (correct / (correct + incorrect.toFloat())) * 100,
                        (incorrect / (correct + incorrect.toFloat())) * 100
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}

            }
            setSelection(0, false)
        }
    }

    private fun initPie(oRatio: Float, xRatio: Float) {
        val pieHelperList = ArrayList<PieHelper>()
        pieHelperList.add(PieHelper(oRatio, "맞은 사람", Color.BLUE))
        pieHelperList.add(PieHelper(xRatio, "틀린 사람", Color.RED))

        with(binding.analysisPie) {
            setDate(pieHelperList)
            setOnPieClickListener {
                if (it <= 0)
                    return@setOnPieClickListener

                DialogUtil.createDialog(
                    requireContext(),
                    "${pieHelperList[it].title} : ${pieHelperList[it].percentStr}",
                    positiveText = "확인",
                    positiveAction = { }
                )
            }
        }
    }

    private fun initPlot(bottomTextList: ArrayList<String>, dataList: ArrayList<ArrayList<Int>>) {
//        val bottomTextList = ArrayList<String>()
//        val dataList = ArrayList<ArrayList<Int>>()
//        val sample1 = ArrayList<Int>()
//        val sample2 = ArrayList<Int>()
//        val random = Math.random() * 9 + 1
//
//        for (i in 0 until 10) {
//            bottomTextList.add(i.toString())
//            sample1.add((Math.random() * random).toInt())
//            sample2.add((Math.random() * random).toInt())
//        }
//
//        dataList.add(sample1)
//        dataList.add(sample2)

        with(binding.analysisPlot) {
            setDrawDotLine(true)
            setShowPopup(LineView.SHOW_POPUPS_MAXMIN_ONLY)
            setBottomTextList(bottomTextList)
            setColorArray(intArrayOf(Color.RED, Color.BLUE, Color.GRAY, Color.CYAN))
            setDataList(dataList)
        }
    }

    override fun initOnBoarding() {
        if (getOnBoardingSpf(this.javaClass.simpleName).not()) {
            createOnBoardingDialog()
        }
    }
}