package com.capture.app.data

import android.app.Application
import android.content.Context
import android.util.Log
import com.capture.app.room.Converters
import com.capture.app.room.MainViewModel

class PermissionManager {
    fun hadWriteAccess(context: Context): Boolean {
        val viewModel = MainViewModel(context.applicationContext as Application)
        var canWrite = false
        FormatterClass().deleteSharedPref("canWrite", context)
        //load facility program
        val data = viewModel.loadSingleProgram(context, "facility")
        if (data != null) {
            try {
                // get current user
                val userData = FormatterClass().getSharedPref("user_data", context)
                if (userData != null) {
                    val refinedData = Converters().fromJsonUser(userData)
                    val userGroups = refinedData.userGroups.map { it.id }
                    val converters = Converters().fromJson(data.jsonData)
                    val program = converters.programs.firstOrNull()
                    if (program != null) {
                        if (userGroups.isNotEmpty()) {
                            val isUserFound =
                                program.userGroupAccesses.any { access -> userGroups.contains(access.id) }
                            if (isUserFound) {
                                // If user is found, you can proceed to check specific rights
                                val permission = program.userGroupAccesses.find { userGroupAccess ->
                                    userGroups.contains(userGroupAccess.id)
                                }
                                if (permission != null) {
                                    val containsW = permission.access.getOrNull(3) == 'w'
                                    if (containsW) {
                                        canWrite = true
                                        FormatterClass().saveSharedPref("canWrite", "true", context)
                                    }
                                }
                            }
                        } else {
                            canWrite = true
                            FormatterClass().saveSharedPref("canWrite", "true", context)
                        }
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return canWrite

    }
}