package com.example.voicecareagent.telephony

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.voicecareagent.engine.VoiceAgentConversationEngine
import com.example.voicecareagent.engine.ConversationStep
import com.example.voicecareagent.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

data class ActiveCallState(
    val mission: CallMission? = null,
    val stage: CallStage = CallStage.IDLE,
    val isCallActive: Boolean = false,
    val isTtsSpeaking: Boolean = false,
    val isRepSpeaking: Boolean = false,
    val isListeningForCallee: Boolean = false,
    val isUserTakeover: Boolean = false,
    val isMuted: Boolean = false,
    val durationSeconds: Int = 0,
    val transcript: List<TranscriptEntry> = emptyList(),
    val currentStepIndex: Int = 0,
    val resolutionAmount: Double = 0.0,
    val confirmationCode: String = "",
    val whisperQueue: List<String> = emptyList()
) {
    val formattedDuration: String
        get() {
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            return String.format(Locale.US, "%02d:%02d", mins, secs)
        }
}

class CallVoiceEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var turnCounter = 0

    private val _callState = MutableStateFlow(ActiveCallState())
    val callState: StateFlow<ActiveCallState> = _callState.asStateFlow()

    private var callJob: Job? = null
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (_: Exception) {}
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            tts?.setSpeechRate(0.98f)
            tts?.setPitch(1.02f)
            isTtsReady = true

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _callState.update { it.copy(isTtsSpeaking = true, isRepSpeaking = false, isListeningForCallee = false) }
                }

                override fun onDone(utteranceId: String?) {
                    _callState.update { it.copy(isTtsSpeaking = false) }
                    // When agent finishes speaking in personal mode, immediately start listening to the other person!
                    val current = _callState.value
                    if (current.isCallActive && !current.isUserTakeover && current.mission?.callMode == CallMode.FRIENDS_AND_CONTACTS) {
                        mainHandler.postDelayed({
                            if (_callState.value.isCallActive && !_callState.value.isTtsSpeaking) {
                                startListeningForCallee()
                            }
                        }, 400L)
                    }
                }

                override fun onError(utteranceId: String?) {
                    _callState.update { it.copy(isTtsSpeaking = false) }
                }
            })
        }
    }

    fun startCallMission(mission: CallMission) {
        stopCall()
        turnCounter = 0

        _callState.update {
            ActiveCallState(
                mission = mission,
                stage = CallStage.DIALING,
                isCallActive = true,
                durationSeconds = 0,
                transcript = emptyList()
            )
        }

        // Start call duration timer
        timerJob = scope.launch {
            while (isActive && _callState.value.isCallActive) {
                delay(1000L)
                _callState.update { it.copy(durationSeconds = it.durationSeconds + 1) }
            }
        }

        // Trigger real telephone call through Android Telecom via SIM card
        try {
            val cleanPhone = mission.calleePhoneNumber.replace("[^0-9+]".toRegex(), "")
            if (cleanPhone.isNotBlank() && cleanPhone.length >= 7) {
                val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$cleanPhone")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(callIntent)
            }
        } catch (e: Exception) {
            try {
                val cleanPhone = mission.calleePhoneNumber.replace("[^0-9+]".toRegex(), "")
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
            } catch (_: Exception) {}
        }

        // Route audio through speakerphone for comfortable hands-free duplex
        try {
            audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager?.isSpeakerphoneOn = true
        } catch (_: Exception) {}

        if (mission.callMode == CallMode.FRIENDS_AND_CONTACTS) {
            startFriendsDuplexSession(mission)
        } else {
            startCustomerCareNegotiation(mission)
        }
    }

    /**
     * DUPLEX CONVERSATIONAL MODE:
     * Greets the friend, then WAITS and LISTENS for what they actually say before generating a tailored response!
     */
    private fun startFriendsDuplexSession(mission: CallMission) {
        callJob = scope.launch {
            // Wait 2.5 seconds while cellular line establishes connection
            delay(2500L)
            if (!_callState.value.isCallActive) return@launch

            _callState.update { it.copy(stage = CallStage.CONNECTED) }

            // Initial turn: AI Agent introduces itself and asks for the friend
            val friendName = mission.friendContact?.name ?: mission.calleeDisplayName
            val callerName = mission.customerName
            val initialGreeting = "Hi $friendName! This is $callerName's personal AI voice agent calling for him. Am I speaking with $friendName?"

            speakAgentUtterance(initialGreeting, CallStage.CONVERSING)
            // Once spoken, UtteranceProgressListener.onDone() will automatically start listening!
        }
    }

    /**
     * Starts listening for callee's incoming voice speech via SpeechRecognizer.
     */
    fun startListeningForCallee() {
        if (!_callState.value.isCallActive || _callState.value.isUserTakeover) return

        mainHandler.post {
            try {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(object : RecognitionListener {
                            override fun onReadyForSpeech(params: Bundle?) {
                                _callState.update { it.copy(isListeningForCallee = true) }
                            }

                            override fun onBeginningOfSpeech() {
                                _callState.update { it.copy(isRepSpeaking = true) }
                            }

                            override fun onRmsChanged(rmsdB: Float) {}

                            override fun onBufferReceived(buffer: ByteArray?) {}

                            override fun onEndOfSpeech() {
                                _callState.update { it.copy(isRepSpeaking = false, isListeningForCallee = false) }
                            }

                            override fun onError(error: Int) {
                                _callState.update { it.copy(isRepSpeaking = false, isListeningForCallee = false) }
                                // If speech timeout or no match, re-listen if call is still active
                                if (_callState.value.isCallActive && !_callState.value.isTtsSpeaking) {
                                    mainHandler.postDelayed({
                                        startListeningForCallee()
                                    }, 800L)
                                }
                            }

                            override fun onResults(results: Bundle?) {
                                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                val heardText = matches?.firstOrNull()?.trim() ?: ""
                                if (heardText.isNotBlank()) {
                                    handleCalleeSpeech(heardText)
                                } else if (_callState.value.isCallActive) {
                                    startListeningForCallee()
                                }
                            }

                            override fun onPartialResults(partialResults: Bundle?) {}

                            override fun onEvent(eventType: Int, params: Bundle?) {}
                        })
                    }
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }
                speechRecognizer?.startListening(intent)
                _callState.update { it.copy(isListeningForCallee = true, isRepSpeaking = false) }
            } catch (_: Exception) {
                _callState.update { it.copy(isListeningForCallee = true) }
            }
        }
    }

    /**
     * Receives and processes what the callee actually said, generating a dynamic tailored response!
     */
    fun handleCalleeSpeech(calleeText: String) {
        val mission = _callState.value.mission ?: return
        if (!_callState.value.isCallActive) return

        // 1. Log callee's words to live transcript
        val calleeEntry = TranscriptEntry(
            speaker = TranscriptSpeaker.CONTACT_OR_REP,
            speakerLabel = mission.calleeDisplayName,
            text = calleeText,
            timestamp = VoiceAgentConversationEngine.getCurrentTimestamp()
        )
        _callState.update { it.copy(transcript = it.transcript + calleeEntry, isRepSpeaking = false, isListeningForCallee = false) }

        // 2. Consume any pending user whisper instructions
        val whispers = _callState.value.whisperQueue
        val whisper = whispers.firstOrNull()
        if (whispers.isNotEmpty()) {
            _callState.update { it.copy(whisperQueue = it.whisperQueue.drop(1)) }
        }

        // 3. Generate dynamic contextual response strictly tailored to what they said!
        val (agentReply, nextStage) = VoiceAgentConversationEngine.generateDynamicFriendResponse(
            calleeText = calleeText,
            mission = mission,
            turnNumber = turnCounter,
            whisperGuidance = whisper
        )
        turnCounter++

        // 4. Speak response
        speakAgentUtterance(agentReply, nextStage)
    }

    private fun speakAgentUtterance(text: String, nextStage: CallStage) {
        if (!_callState.value.isCallActive || _callState.value.isUserTakeover || _callState.value.isMuted) {
            _callState.update { it.copy(stage = nextStage) }
            return
        }

        val agentEntry = TranscriptEntry(
            speaker = TranscriptSpeaker.AI_AGENT,
            speakerLabel = "VoiceCare AI Agent",
            text = text,
            timestamp = VoiceAgentConversationEngine.getCurrentTimestamp()
        )
        _callState.update {
            it.copy(
                transcript = it.transcript + agentEntry,
                stage = nextStage,
                isTtsSpeaking = true,
                isListeningForCallee = false
            )
        }

        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utterance_${System.currentTimeMillis()}")
        }
    }

    /**
     * Customer Care negotiation script runner
     */
    private fun startCustomerCareNegotiation(mission: CallMission) {
        val script = VoiceAgentConversationEngine.buildConversationScript(mission)
        callJob = scope.launch {
            for ((index, step) in script.withIndex()) {
                if (!_callState.value.isCallActive) break

                _callState.update {
                    it.copy(
                        stage = step.stage,
                        currentStepIndex = index
                    )
                }

                if (step.ivrText != null) {
                    val ivrEntry = TranscriptEntry(
                        speaker = TranscriptSpeaker.IVR_AUTOMATION,
                        speakerLabel = "${mission.calleeDisplayName} (Phone System)",
                        text = step.ivrText,
                        timestamp = VoiceAgentConversationEngine.getCurrentTimestamp()
                    )
                    _callState.update { it.copy(transcript = it.transcript + ivrEntry) }
                    delay(step.delayMs)
                }

                if (step.otherPartySpeech != null) {
                    _callState.update { it.copy(isRepSpeaking = true) }
                    val repEntry = TranscriptEntry(
                        speaker = TranscriptSpeaker.CONTACT_OR_REP,
                        speakerLabel = "Customer Care Specialist",
                        text = step.otherPartySpeech,
                        timestamp = VoiceAgentConversationEngine.getCurrentTimestamp()
                    )
                    _callState.update { it.copy(transcript = it.transcript + repEntry) }
                    delay(step.delayMs)
                    _callState.update { it.copy(isRepSpeaking = false) }
                }

                val whispers = _callState.value.whisperQueue
                var finalAgentSpeech = step.agentSpeech
                if (whispers.isNotEmpty()) {
                    val whisper = whispers.first()
                    finalAgentSpeech += " (Note: $whisper)"
                    _callState.update { it.copy(whisperQueue = it.whisperQueue.drop(1)) }
                }

                if (!_callState.value.isUserTakeover && !_callState.value.isMuted) {
                    _callState.update { it.copy(isTtsSpeaking = true) }
                    val agentEntry = TranscriptEntry(
                        speaker = TranscriptSpeaker.AI_AGENT,
                        speakerLabel = "VoiceCare AI Agent",
                        text = finalAgentSpeech,
                        timestamp = VoiceAgentConversationEngine.getCurrentTimestamp()
                    )
                    _callState.update { it.copy(transcript = it.transcript + agentEntry) }

                    if (isTtsReady) {
                        tts?.speak(finalAgentSpeech, TextToSpeech.QUEUE_FLUSH, null, "call_speech_${index}")
                    }
                    delay(step.delayMs + 600L)
                    _callState.update { it.copy(isTtsSpeaking = false) }
                } else {
                    delay(step.delayMs)
                }

                if (step.resolutionAmount > 0.0) {
                    _callState.update {
                        it.copy(
                            resolutionAmount = step.resolutionAmount,
                            confirmationCode = step.confirmationCode ?: ""
                        )
                    }
                }
            }

            if (_callState.value.isCallActive) {
                _callState.update { it.copy(stage = CallStage.COMPLETED, isCallActive = false) }
            }
        }
    }

    fun whisperToAgent(whisperText: String) {
        if (whisperText.isBlank()) return
        val whisperEntry = TranscriptEntry(
            speaker = TranscriptSpeaker.USER_WHISPER,
            speakerLabel = "You (Whisper to Agent)",
            text = "🤫 Whisper: ${whisperText.trim()}",
            timestamp = VoiceAgentConversationEngine.getCurrentTimestamp()
        )
        _callState.update {
            it.copy(
                transcript = it.transcript + whisperEntry,
                whisperQueue = it.whisperQueue + whisperText.trim()
            )
        }
    }

    fun toggleTakeover() {
        _callState.update { current ->
            val newTakeover = !current.isUserTakeover
            if (newTakeover) {
                tts?.stop()
                mainHandler.post {
                    speechRecognizer?.stopListening()
                }
            }
            current.copy(isUserTakeover = newTakeover)
        }
    }

    fun toggleMute() {
        _callState.update { current ->
            val newMute = !current.isMuted
            if (newMute) {
                tts?.stop()
                mainHandler.post {
                    speechRecognizer?.stopListening()
                }
            }
            current.copy(isMuted = newMute)
        }
    }

    fun stopCall() {
        callJob?.cancel()
        timerJob?.cancel()
        tts?.stop()
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (_: Exception) {}
        }
        _callState.update {
            it.copy(
                isCallActive = false,
                isTtsSpeaking = false,
                isRepSpeaking = false,
                isListeningForCallee = false,
                stage = if (it.resolutionAmount > 0.0) CallStage.COMPLETED else CallStage.IDLE
            )
        }
    }

    fun clearCall() {
        stopCall()
        _callState.value = ActiveCallState()
    }

    fun openSystemDialer() {
        try {
            val phone = _callState.value.mission?.calleePhoneNumber?.replace("[^0-9+]".toRegex(), "") ?: ""
            if (phone.isNotBlank()) {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
            }
        } catch (_: Exception) {}
    }
}
