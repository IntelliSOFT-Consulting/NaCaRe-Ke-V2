package com.intellisoft.nacare.main.ui.cases

interface FilterBottomSheetListener {
    fun onStatusClicked(status: String)
    fun onDateClick()
    fun onDateRangeClicked()

}