package com.intellisoft.nacare.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.intellisoft.nacare.room.EventData


class NetworkViewModel() : ViewModel() {
    private val _eventData = MutableLiveData<EventData>()

    private val _booleanData = MutableLiveData<Boolean>()
    val booleanData: LiveData<Boolean>
        get() = _booleanData
    val eventData: LiveData<EventData>
        get() = _eventData
    init {
        // Initialize your boolean data, if needed
        _booleanData.value = false
    }

    fun updateData(newData: EventData) {
        _eventData.value = newData
    }

    fun setBooleanValue(newValue: Boolean) {
        _booleanData.value = newValue
    }
}