package com.intellisoft.nacare.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class NetworkViewModel() : ViewModel() {

    private val _booleanData = MutableLiveData<Boolean>()
    val booleanData: LiveData<Boolean>
        get() = _booleanData

    init {
        // Initialize your boolean data, if needed
        _booleanData.value = false
    }

    fun setBooleanValue(newValue: Boolean) {
        _booleanData.value = newValue
    }
}