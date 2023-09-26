package pl.devadam.fiszki

import android.app.Application
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

open class TTSViewModel(application: Application) : AndroidViewModel(application) {

    private val _textToSpeech = MutableLiveData<TextToSpeech>()
    val textToSpeech: LiveData<TextToSpeech> get() = _textToSpeech

    private val _voices = MutableLiveData<Set<Voice>>()
    val voices: LiveData<Set<Voice>> get() = _voices

    fun getTTSVoicesNames(): List<String> {

        if (_textToSpeech.value == null || _textToSpeech.value!!.voices == null)
            return emptyList()

        val localVoices = _textToSpeech.value!!.voices!!.filter { it.name.endsWith("-language") }
        // TODO: fallback if voices are not ending with -language

        return localVoices.map { it.name }
    }

    protected fun initializeTTS(application: Application) {
        _textToSpeech.value = TextToSpeech(application) { status ->
            if (status != TextToSpeech.SUCCESS) {
                // Handle TTS initialization failure
                // Log or show an error message
                println("TTS failed")
                return@TextToSpeech
            }

            _voices.value = _textToSpeech.value?.voices
        }
    }

    override fun onCleared() {

        _textToSpeech.value?.shutdown()

        super.onCleared()
    }
}