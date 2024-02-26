package com.capture.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capture.app.model.CodeValuePair
import com.capture.app.model.CodeValuePairPatient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResponseViewModel : ViewModel() {

    private val _mutableListLiveData = MutableLiveData<MutableList<CodeValuePair>>().apply {
        value = mutableListOf() // Initial value is an empty mutable list
    }

    private val _mutableListLiveDataPatient =
        MutableLiveData<MutableList<CodeValuePairPatient>>().apply {
            value = mutableListOf() // Initial value is an empty mutable list
        }


    // Expose the LiveData as an immutable LiveData to observers
    val mutableListLiveData: LiveData<MutableList<CodeValuePair>> = _mutableListLiveData
    val mutableListLiveDataPatient: LiveData<MutableList<CodeValuePairPatient>> =
        _mutableListLiveDataPatient

    private val _additionalInformationSaved = MutableLiveData<Boolean>().apply {
        value = false // Initial value is a false
    }
    val additionalInformationSaved: LiveData<Boolean> = _additionalInformationSaved


    fun updatePatientDetails(boolean: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _additionalInformationSaved.postValue(boolean)
            }
        }
    }

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