package com.intellisoft.nacare.listeners

import androidx.fragment.app.Fragment

interface OnFragmentInteractionListener {
    fun launchAction()
    fun nextFragment(fragment: Fragment)
}