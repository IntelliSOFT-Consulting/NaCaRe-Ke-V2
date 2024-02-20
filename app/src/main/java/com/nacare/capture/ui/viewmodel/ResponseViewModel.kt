package com.nacare.capture.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nacare.capture.model.CodeValuePair
import com.nacare.capture.model.CodeValuePairPatient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResponseViewModel : ViewModel() {

    private val _mutableListLiveData = MutableLiveData<MutableList<CodeValuePair>>().apply {
        value = mutableListOf() // Initial value is an empty mutable list
    }

    private val _mutableListLiveDataPatient = MutableLiveData<MutableList<CodeValuePairPatient>>().apply {
        value = mutableListOf() // Initial value is an empty mutable list
    }

    // Expose the LiveData as an immutable LiveData to observers
    val mutableListLiveData: LiveData<MutableList<CodeValuePair>> = _mutableListLiveData
    val mutableListLiveDataPatient: LiveData<MutableList<CodeValuePairPatient>> = _mutableListLiveDataPatient


    fun populateRelevantData(searchParameters: ArrayList<CodeValuePair>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _mutableListLiveData.postValue(searchParameters)
            }
        }
    }

    fun populateRelevantPatientData(searchParameters: ArrayList<CodeValuePairPatient>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _mutableListLiveDataPatient.postValue(searchParameters)
            }
        }
    }
}