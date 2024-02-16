package com.nacare.capture.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nacare.capture.model.CodeValuePair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResponseViewModel : ViewModel() {

    private val _mutableListLiveData = MutableLiveData<MutableList<CodeValuePair>>().apply {
        value = mutableListOf() // Initial value is an empty mutable list
    }

    // Expose the LiveData as an immutable LiveData to observers
    val mutableListLiveData: LiveData<MutableList<CodeValuePair>> = _mutableListLiveData


    fun populateRelevantData(searchParameters: ArrayList<CodeValuePair>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _mutableListLiveData.postValue(searchParameters)
            }
        }
    }
}