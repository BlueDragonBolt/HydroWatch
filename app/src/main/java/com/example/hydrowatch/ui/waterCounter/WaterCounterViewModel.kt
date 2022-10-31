package com.example.hydrowatch.ui.waterCounter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WaterCounterViewModel : ViewModel() {
    private var _counter = MutableLiveData<Int>().apply {
        value = 0
    }
    private var _text = MutableLiveData<String>().apply {
        value = "You've used ${_counter.value}L of water"
    }
    val text: LiveData<String> = _text
    val counter: LiveData<Int> = _counter
    fun incrementCounter() {
        _counter.value = _counter.value!! + 1
    }
    fun resetCounter() {
        _counter.value = 0
    }

}