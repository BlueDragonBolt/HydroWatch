package com.example.hydrowatch.ui.medals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MedalsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Medals!"
    }
    val text: LiveData<String> = _text
}