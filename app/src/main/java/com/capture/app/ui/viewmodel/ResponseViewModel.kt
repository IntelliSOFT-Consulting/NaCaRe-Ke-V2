package com.capture.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capture.app.model.CodeValuePair
import com.capture.app.model.CodeValuePairPatient
import com.capture.app.model.ProgramData
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

    private val _mutablePatientUniqueLiveData =
        MutableLiveData<String>().apply {
            value = "" // Initial value is an empty mutable list
        }
   private val _alreadyAnsweredElements =
        MutableLiveData<String>().apply {
            value = "0" // Initial value is an empty mutable list
        }

    // Expose the LiveData as an immutable LiveData to observers
    val mutableListLiveData: LiveData<MutableList<CodeValuePair>> = _mutableListLiveData
    val mutablePatientUniqueLiveData: LiveData<String> = _mutablePatientUniqueLiveData
    val mutableAlreadyAnsweredElements: LiveData<String> = _alreadyAnsweredElements
    val mutableListLiveDataPatient: LiveData<MutableList<CodeValuePairPatient>> =
        _mutableListLiveDataPatient

    private val _additionalInformationSaved = MutableLiveData<Boolean>().apply {
        value = false // Initial value is a false
    }
    val additionalInformationSaved: LiveData<Boolean> = _additionalInformationSaved

    private val _entireFormDisabled = MutableLiveData<Boolean>().apply {
        value = false // Initial value is a false
    }
    val entireFormDisabled: LiveData<Boolean> = _entireFormDisabled


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

    fun disableEntireForm(boolean: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _entireFormDisabled.postValue(boolean)
            }
        }
    }
    fun updateUniqueID(uid: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _mutablePatientUniqueLiveData.postValue(uid)
            }
        }
    }
    fun updateAlreadySaved(uid: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _alreadyAnsweredElements.postValue(uid)
            }
        }
    }


}