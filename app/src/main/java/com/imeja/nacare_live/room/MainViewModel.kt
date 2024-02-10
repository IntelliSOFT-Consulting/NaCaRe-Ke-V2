package com.imeja.nacare_live.room

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MainRepository

    init {
        val roomDao = MainDatabase.getDatabase(application).roomDao()
        repository = MainRepository(roomDao)
    }

    fun addIndicators(data: ProgramData) {

        repository.addIndicators(data)
    }

    fun loadPrograms(context: Context) = runBlocking {
        repository.loadPrograms(context)

    }

    fun deletePrograms() = runBlocking {
        repository.deletePrograms()
    }

    fun loadSingleProgram(context: Context, userId: String) = runBlocking {
        repository.loadSingleProgram(context, userId)
    }
}
