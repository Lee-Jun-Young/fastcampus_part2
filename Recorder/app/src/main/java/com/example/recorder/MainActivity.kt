package com.example.recorder

import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_main.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private val requiresPermissions = arrayOf(
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val recordingFilePath: String by lazy {
        "${externalCacheDir?.absolutePath}/recording.3gp"
    }

    private var recorder: MediaRecorder?= null
    private var player: MediaPlayer?= null
    private var state = State.BEFORE_RECORDING
        set(value) {
            field = value
            btn_reset.isEnabled = value == State.AFTER_RECORDING || value == State.ON_PLAYING
            recordButton.updateIconWithState(value)
        }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) 
        setContentView(R.layout.activity_main)

        requestAudioPermission()
        initViews()
        bindViews()
        initVariables()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val audioRecordPermissionGranted = requestCode == REQUEST_RECORD_AUDIO_PERMISSION &&
                grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

        if(!audioRecordPermissionGranted){
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestAudioPermission(){
        requestPermissions(requiresPermissions, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    private fun initViews(){
        recordButton.updateIconWithState(state)
    }

    private fun bindViews(){
        soundVisualizerView.onRequestCurrentAmplitude = {
            recorder?.maxAmplitude ?: 0
        }
        btn_reset.setOnClickListener {
            stopPlaying()
            soundVisualizerView.clearVisualization()
            tv_recordTime.clearCountTime()
            state = State.BEFORE_RECORDING
        }
        recordButton.setOnClickListener {
            when(state){
                State.BEFORE_RECORDING -> {
                    startRecording()
                }
                State.ON_RECORDING -> {
                    stopRecording()
                }
                State.AFTER_RECORDING -> {
                    startPlaying()
                }
                State.ON_PLAYING -> {
                    stopPlaying()
                }
            }
        }
    }

    private fun startRecording(){
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(recordingFilePath)
            prepare()
        }
        recorder?.start()
        soundVisualizerView.startVisualizing(false)
        tv_recordTime.startCountUp()
        state = State.ON_RECORDING
    }

    private fun stopRecording(){
        recorder?.run {
            stop()
            release()
        }
        recorder = null
        soundVisualizerView.stopVisualizing()
        tv_recordTime.stopCountUp()
        state = State.AFTER_RECORDING
    }

    private fun startPlaying(){
        player = MediaPlayer().apply {
            setDataSource(recordingFilePath)
            prepare()
        }
        player?.setOnCompletionListener {
            stopPlaying()
            state = State.AFTER_RECORDING
        }
        player?.start()
        soundVisualizerView.startVisualizing(true)
        tv_recordTime.startCountUp()
        state = State.ON_PLAYING
    }

    private fun stopPlaying(){
        player?.release()
        player = null
        soundVisualizerView.stopVisualizing()
        tv_recordTime.stopCountUp()
        state = State.AFTER_RECORDING
    }

    private fun initVariables(){
        state = State.BEFORE_RECORDING
    }

    companion object{
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 201
    }

}