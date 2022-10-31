package com.example.hydrowatch.ui.statistics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UsageViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
//        value = "Usage"
    }
    val text: LiveData<String> = _text
}