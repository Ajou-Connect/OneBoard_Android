package kr.khs.oneboard.ui

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.activity.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kr.khs.oneboard.R
import kr.khs.oneboard.core.rawdata.RawDataRenderer
import kr.khs.oneboard.core.zoom.AudioRawDataUtil
import kr.khs.oneboard.core.zoom.BaseSessionActivity
import kr.khs.oneboard.core.zoom.NotificationService
import kr.khs.oneboard.data.request.QuizRequestDto
import kr.khs.oneboard.databinding.DialogCreateQuizBinding
import kr.khs.oneboard.databinding.DialogQuizBinding
import kr.khs.oneboard.databinding.DialogUnderstandingBinding
import kr.khs.oneboard.utils.*
import kr.khs.oneboard.viewmodels.SessionViewModel
import org.json.JSONObject
import timber.log.Timber
import us.zoom.sdk.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

@AndroidEntryPoint
class SessionActivity : BaseSessionActivity(), CoroutineScope {
    companion object {
        const val SOCKET_TEST = "Hello!"
        const val INIT_Socket = "init"
        const val STUDENT_ATTENDANCE = "attendance request"
        const val PROFESSOR_ATTENDANCE = "attendance response"
        const val STUDENT_UNDERSTANDING = "understanding request"
        const val PROFESSOR_UNDERSTANDING = "understanding response"
        const val STUDENT_QUIZ = "quiz request"
        const val PROFESSOR_QUIZ = "quiz response"
    }

    private lateinit var audioRawDataUtil: AudioRawDataUtil
    private lateinit var zoomCanvas: ZoomVideoSDKVideoView
    private lateinit var rawDataRenderer: RawDataRenderer

    private val viewModel: SessionViewModel by viewModels()

    override val sessionLeaveProfessor: () -> Unit by lazy { { viewModel.leaveSession() } }

    @Inject
    lateinit var socket: Socket

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val socketConnectListener = Emitter.Listener {
        Timber.tag("Socket").d("Connect Listener!")

        val initObject = JSONObject().apply {
            put("userType", if (UserInfoUtil.type == TYPE_PROFESSOR) "T" else "S")
            put("room", sessionName)
        }
        socket.emit(INIT_Socket, initObject)
    }

    private val socketDisconnectListener = Emitter.Listener {
        Timber.tag("Socket").d("Disconnect Listener!")
    }

    private val socketAttendanceRequestListener = Emitter.Listener {
        Timber.tag("Socket").d("Attendance Request to student")

        launch(coroutineContext) {
            MaterialAlertDialogBuilder(this@SessionActivity)
                .setMessage("출석 체크를 해주세요.")
                .setCancelable(false)
                .setPositiveButton("확인") { _, _ ->
                    viewModel.postAttendance()
                }
                .show()
        }
    }

    private val socketAttendanceResponseListener = Emitter.Listener {
        launch(coroutineContext) {

        }
    }

    private val socketUnderstandingRequestListener = Emitter.Listener {
        Timber.tag("Socket").d("${it[0]}")

        val understandId = JSONObject(it[0].toString())["understandId"]

        viewModel.understandingId = understandId as Int

        launch(coroutineContext) {
            val dialogBinding = DialogUnderstandingBinding.inflate(layoutInflater)

            val dialog = MaterialAlertDialogBuilder(this@SessionActivity)
                .setView(dialogBinding.root)
                .setCancelable(false)
                .create()

            dialogBinding.dialogUnderstandingO.setOnClickListener {
                viewModel.postUnderStandingStudent(sessionName, "O")
                dialog.dismiss()
            }

            dialogBinding.dialogUnderstandingX.setOnClickListener {
                viewModel.postUnderStandingStudent(sessionName, "X")
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private val socketUnderstandingResponseListener = Emitter.Listener {
        launch(coroutineContext) {

        }
    }

    private val socketQuizRequestListener = Emitter.Listener {
        val json = JSONObject(it[0].toString())
        launch(coroutineContext) {
            val dialogBinding = DialogQuizBinding.inflate(layoutInflater)

            val dialog = MaterialAlertDialogBuilder(this@SessionActivity)
                .setView(dialogBinding.root)
                .setCancelable(false)
                .create()

            dialogBinding.dialogQuizTitle.text = json["question"] as String

            val layoutParams =
                LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                    .apply {
                        setMargins(0, 8, 0, 8)
                    }

            val radioButtonList = Array(5) {
                RadioButton(this@SessionActivity).apply {
                    text = json["answer${it + 1}"] as String
                    id = it + 100 + 1
                    this.layoutParams = layoutParams
                }
            }

            for (radioButton in radioButtonList)
                dialogBinding.dialogQuizList.addView(radioButton)

            dialogBinding.dialogQuizSubmitButton.setOnClickListener {
                val checkId = dialogBinding.dialogQuizList.checkedRadioButtonId

                // 선택되지 않았을 경우
                if (checkId == 0) {
                    viewModel.setToastMessage("정답을 선택해 주세요.")
                } else {
                    viewModel.postQuizStudent(json["quizId"] as Int, checkId - 100)
                    dialog.dismiss()
                }
            }

            dialog.show()
        }
    }

    private val socketQuizResponseListener = Emitter.Listener {
        launch(coroutineContext) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioRawDataUtil = AudioRawDataUtil(this)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        addSocketListener()

        if (UserInfoUtil.type == TYPE_PROFESSOR)
            initProfessorView()

        viewModel.professorUnderstandingResponse.observe(this) { understanding ->
            understanding ?: return@observe

            val understandingString = """
                이해 O : ${understanding.yes}
                이해 X : ${understanding.no}
            """.trimIndent()
            DialogUtil.createDialog(
                this,
                understandingString,
                positiveText = "확인",
                positiveAction = { }
            )
        }

        viewModel.professorQuizResponse.observe(this) { quiz ->
            quiz ?: return@observe

            val quizString = """
                정답자 수 : ${quiz.correctNum}
                오답자 수 : ${quiz.incorrectNum}
            """.trimIndent()
            DialogUtil.createDialog(
                this,
                quizString,
                positiveText = "확인",
                positiveAction = { }
            )
        }

        viewModel.isLoading.observe(this) {
            if (it) {
                DialogUtil.onLoadingDialog(this)
            } else {
                DialogUtil.offLoadingDialog()
            }
        }

        viewModel.toastMessage.observe(this) {
            if (it != "") {
                ToastUtil.shortToast(this, it)
                viewModel.setToastMessage()
            }
        }

        viewModel.isLeave.observe(this) {
            if (it) {
                Timber.tag("SessionLeave").d("$it")
                sessionLeaveAction()

                super.releaseResource()
                val ret = ZoomVideoSDK.getInstance().leaveSession(true)
                Timber.d("leaveSession ret = $ret")
            }
        }
    }

    private fun initProfessorView() {
        binding.requestAttendance.setOnClickListener {
            viewModel.postAttendance()
        }

        binding.requestUnderstanding.setOnClickListener {
            viewModel.postUnderStandingProfessor()
            binding.responseUnderstanding.visibility = View.VISIBLE
        }

        binding.responseUnderstanding.setOnClickListener {
            viewModel.getUnderStandingProfessor()
        }

        binding.requestQuiz.setOnClickListener {
            createQuizDialog()
        }

        binding.responseQuiz.setOnClickListener {
            viewModel.getQuizProfessor()
        }
    }

    private fun createQuizDialog() {
        val quizBinding = DialogCreateQuizBinding.inflate(layoutInflater)

        val builder = MaterialAlertDialogBuilder(this)
            .setView(quizBinding.root)
            .create()

        quizBinding.createQuizButton.setOnClickListener {
            if (quizBinding.createQuizQuestion.text.toString().isEmpty() ||
                quizBinding.createQuizAnswer1.text.toString().isEmpty() ||
                quizBinding.createQuizAnswer2.text.toString().isEmpty() ||
                quizBinding.createQuizAnswer3.text.toString().isEmpty() ||
                quizBinding.createQuizAnswer4.text.toString().isEmpty() ||
                quizBinding.createQuizAnswer5.text.toString().isEmpty() ||
                quizBinding.createQuizRadioGroup.checkedRadioButtonId == -1
            ) {
                viewModel.setToastMessage("공백 없이 모두 입력해주세요.")
                return@setOnClickListener
            }

            val requestDto = QuizRequestDto(
                question = quizBinding.createQuizQuestion.text.toString(),
                answer1 = quizBinding.createQuizAnswer1.text.toString(),
                answer2 = quizBinding.createQuizAnswer2.text.toString(),
                answer3 = quizBinding.createQuizAnswer3.text.toString(),
                answer4 = quizBinding.createQuizAnswer4.text.toString(),
                answer5 = quizBinding.createQuizAnswer5.text.toString(),
                answer = when (quizBinding.createQuizRadioGroup.checkedRadioButtonId) {
                    R.id.quizRadio1 -> 1
                    R.id.quizRadio2 -> 2
                    R.id.quizRadio3 -> 3
                    R.id.quizRadio4 -> 4
                    R.id.quizRadio5 -> 5
                    else -> 0
                }
            )

            viewModel.postQuizProfessor(requestDto)
            binding.responseQuiz.visibility = View.VISIBLE
            builder.dismiss()
        }

        builder.show()
    }

    override fun parseIntent() {
        super.parseIntent()

        intent.extras?.let {
            val lectureId = it.getInt("lectureId", 0)
            val lessonId = it.getInt("lessonId", 0)

            viewModel.setId(lectureId, lessonId, sessionName)
        }
    }

    private fun addSocketListener() {
        Timber.tag("Socket").d("addSocketListener()")

        socket.on(Socket.EVENT_CONNECT, socketConnectListener)
        socket.on(Socket.EVENT_DISCONNECT, socketDisconnectListener)

        if (UserInfoUtil.type == TYPE_PROFESSOR) {
            socket.on(PROFESSOR_ATTENDANCE, socketAttendanceResponseListener)
            socket.on(PROFESSOR_UNDERSTANDING, socketUnderstandingResponseListener)
            socket.on(PROFESSOR_QUIZ, socketQuizResponseListener)
        } else {
            socket.on(STUDENT_ATTENDANCE, socketAttendanceRequestListener)
            socket.on(STUDENT_UNDERSTANDING, socketUnderstandingRequestListener)
            socket.on(STUDENT_QUIZ, socketQuizRequestListener)
        }

        Timber.tag("Socket").d("connect")
        socket.connect()
    }

    override fun onSessionJoin() {
        super.onSessionJoin()
        startMeetingService()
    }

    private fun startMeetingService() {
        val intent = Intent(
            this, NotificationService::class.java
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopMeetingService() {
        stopService(
            Intent(this, NotificationService::class.java)
        )
    }

    override fun onSessionLeave() {
        super.onSessionLeave()
        if (UserInfoUtil.type == TYPE_STUDENT) {
            sessionLeaveAction()
        }
    }

    private fun sessionLeaveAction() {
        finish()
        audioRawDataUtil.unSubscribe()
        shareToolbar?.destroy()

        removeSocketListener()
    }

    private fun removeSocketListener() {
        Timber.tag("Socket").d("disconnect")
        socket.disconnect()

        Timber.tag("Socket").d("removeSocketListener()")
        // socket clear
        socket.off()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMeetingService()
        handler.removeCallbacks(runnable)
    }

    override fun initView() {
        super.initView()

        binding.bigVideoContain.setOnClickListener(onEmptyContentClick)
        binding.chatList.setOnClickListener(onEmptyContentClick)
    }

    private val onEmptyContentClick = object : View.OnClickListener {
        override fun onClick(v: View) {
            if (zoom.isInSession.not())
                return

            Timber.tag("onEmptyContentClick")
                .d("${actionBarBinding.actionBar.visibility == View.VISIBLE}")
            val isShow = actionBarBinding.actionBar.visibility == View.VISIBLE
            toggleView(isShow.not())
        }
    }

    private fun changeResolution() {
        if (renderType == RENDER_TYPE_OPENGLES) {
            var resolution = Random.nextInt(3)
            resolution++
            if (resolution > ZoomVideoSDKVideoResolution.VideoResolution_360P.value) {
                resolution = 0
            }

            val size = ZoomVideoSDKVideoResolution.fromValue(resolution)

            Timber.d("changeResolution : $size")

            if (currentShareUser == null && mActiveUser != null) {
                mActiveUser!!.videoPipe.subscribe(size, rawDataRenderer)
            }
        }
    }

    override fun onItemClick() {
        super.onItemClick()
        if (zoom.isInSession.not())
            return

        val isShow = actionBarBinding.actionBar.visibility == View.VISIBLE
        toggleView(isShow.not())
    }

    override fun toggleView(show: Boolean) {
        if (show.not()) {
            if (binding.chatInputLayout.isKeyBoardShow()) {
                binding.chatInputLayout.dismissChat(true)
                return
            }
        }

        actionBarBinding.actionBar.visibility = if (show) View.VISIBLE else View.GONE
        if (UserInfoUtil.type == TYPE_PROFESSOR)
            binding.sessionProfessor.visibility = if (show) View.VISIBLE else View.GONE
        binding.chatList.visibility = if (show) View.VISIBLE else View.GONE
        Timber.tag("toggleView").d(
            """
            Show : $show
            ActionBar visibility : ${actionBarBinding.actionBar.visibility}
        """.trimIndent()
        )
    }

    override fun initMeeting() {
        zoom.addListener(this)

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        )

        if (renderType == RENDER_TYPE_ZOOMRENDERER) {
            zoomCanvas = ZoomVideoSDKVideoView(this, renderWithSurfaceView.not())
            binding.bigVideoContain.addView(zoomCanvas, 0, params)
        } else {
            rawDataRenderer = RawDataRenderer(this)
            binding.bigVideoContain.addView(rawDataRenderer, 0, params)
        }

        val mySelf = zoom.session.mySelf
        subscribeVideoByUser(mySelf)
        refreshFps()
    }

    private val runnable = Runnable {
        mActiveUser?.let { activeUser ->
            updateFps(
                if (activeUser == currentShareUser)
                    activeUser.shareStatisticInfo
                else
                    activeUser.videoStatisticInfo
            )
            Timber.tag("FpsRunnable").d("updateFps, ${textFps.visibility == View.VISIBLE}")
        }
        Timber.tag("FpsRunnable").d("runnable!")
    }

    private fun refreshFps() {
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, 500)
    }

    override fun onClickSwitchShare(view: View) {
        currentShareUser?.let { currentShareUser ->
            updateVideoAvatar(true)
            subscribeShareByUser(currentShareUser)
            selectAndScrollToUser(mActiveUser)
        }
    }

    override fun unSubscribe() {
        currentShareUser?.let { currentShareUser ->
            if (renderType == RENDER_TYPE_ZOOMRENDERER) {
                currentShareUser.videoCanvas.unSubscribe(zoomCanvas)
                currentShareUser.shareCanvas.unSubscribe(zoomCanvas)
            } else {
                currentShareUser.videoPipe.unSubscribe(rawDataRenderer)
            }
        }

        mActiveUser?.let { mActiveUser ->
            if (renderType == RENDER_TYPE_ZOOMRENDERER) {
                mActiveUser.videoCanvas.unSubscribe(zoomCanvas)
                mActiveUser.shareCanvas.unSubscribe(zoomCanvas)
            } else {
                mActiveUser.videoPipe.unSubscribe(rawDataRenderer)
            }
        }
    }

    override fun subscribeVideoByUser(user: ZoomVideoSDKUser) {
        if (renderType == RENDER_TYPE_ZOOMRENDERER) {
            var aspect = ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_LetterBox
            if (zoom.isInSession)
                aspect = ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_Original
            currentShareUser?.shareCanvas?.unSubscribe(zoomCanvas)

            user.videoCanvas.unSubscribe(zoomCanvas)
            val ret = user.videoCanvas.subscribe(zoomCanvas, aspect)
            if (ret != ZoomVideoSDKErrors.Errors_Success) {
                ToastUtil.shortToast(this, "subscribe error : $ret")
            }
        } else {
            if (zoom.isInSession) {
                rawDataRenderer.setVideoAspectModel(RawDataRenderer.VideoAspect_Original)
            } else {
                rawDataRenderer.setVideoAspectModel(RawDataRenderer.VideoAspect_Full_Filled)
            }

            currentShareUser?.sharePipe?.unSubscribe(rawDataRenderer)
            user.videoPipe.unSubscribe(rawDataRenderer)

            val ret = user.videoPipe.subscribe(
                ZoomVideoSDKVideoResolution.VideoResolution_360P,
                rawDataRenderer
            )
            if (ret != ZoomVideoSDKErrors.Errors_Success) {
                ToastUtil.shortToast(this, "subscribe error : $ret")
            }
        }
        mActiveUser = user

        user.videoStatus?.let {
            updateVideoAvatar(user.videoStatus.isOn)
        }

        currentShareUser?.let {
            binding.btnViewShare.visibility = View.VISIBLE
        } ?: run { binding.btnViewShare.visibility = View.GONE }
    }

    override fun subscribeShareByUser(user: ZoomVideoSDKUser) {
        if (renderType == RENDER_TYPE_ZOOMRENDERER) {
            mActiveUser?.let { mActiveUser ->
                mActiveUser.videoCanvas.unSubscribe(zoomCanvas)
                mActiveUser.shareCanvas.unSubscribe(zoomCanvas)
            }
            user.shareCanvas.subscribe(
                zoomCanvas,
                ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_Original
            )
        } else {
            rawDataRenderer.setVideoAspectModel(RawDataRenderer.VideoAspect_Original)
            rawDataRenderer.subscribe(user, ZoomVideoSDKVideoResolution.VideoResolution_720P, true)
        }
        mActiveUser = user
        binding.btnViewShare.visibility = View.GONE
    }

    private fun updateVideoAvatar(isOn: Boolean) {
        if (isOn) {
            binding.videoOffTips.visibility = View.GONE
        } else {
            binding.videoOffTips.visibility = View.VISIBLE
            textFps.visibility = View.GONE
            binding.videoOffTips.setImageResource(R.drawable.zm_conf_no_avatar)
        }
    }

    override fun onUserLeave(
        userHelper: ZoomVideoSDKUserHelper?,
        userList: MutableList<ZoomVideoSDKUser>
    ) {
        super.onUserLeave(userHelper, userList)
        if (mActiveUser == null || userList.contains(mActiveUser)) {
            subscribeVideoByUser(session.mySelf)
            selectAndScrollToUser(session.mySelf)
        }
    }

    override fun onUserVideoStatusChanged(
        videoHelper: ZoomVideoSDKVideoHelper?,
        userList: MutableList<ZoomVideoSDKUser>
    ) {
        super.onUserVideoStatusChanged(videoHelper, userList)

        mActiveUser?.let { activeUser ->
            if (userList.contains(activeUser))
                updateVideoAvatar(activeUser.videoStatus.isOn)
        }
    }

    override fun onStartShareView() {
        super.onStartShareView()
        if (renderType == RENDER_TYPE_ZOOMRENDERER) {
            mActiveUser?.videoCanvas?.unSubscribe(zoomCanvas)
        } else {
            rawDataRenderer.unSubscribe()
        }

        userVideoAdapter.clear(false)
    }

    override fun onUserShareStatusChanged(
        shareHelper: ZoomVideoSDKShareHelper?,
        userInfo: ZoomVideoSDKUser,
        status: ZoomVideoSDKShareStatus
    ) {
        super.onUserShareStatusChanged(shareHelper, userInfo, status)

        if (status == ZoomVideoSDKShareStatus.ZoomVideoSDKShareStatus_Start) {
            if (userInfo != session.mySelf) {
                subscribeShareByUser(userInfo)
                updateVideoAvatar(true)
                selectAndScrollToUser(userInfo)
            } else {
                if (zoom.shareHelper.isScreenSharingOut.not()) {
                    unSubscribe()
                    userVideoAdapter.clear(false)
                }
            }
        } else if (status == ZoomVideoSDKShareStatus.ZoomVideoSDKShareStatus_Stop) {
            currentShareUser = null
            subscribeVideoByUser(userInfo)

            if (userVideoAdapter.itemCount == 0)
                userVideoAdapter.addAll()

            selectAndScrollToUser(userInfo)
        }
    }

    override fun onBackPressed() {}
}