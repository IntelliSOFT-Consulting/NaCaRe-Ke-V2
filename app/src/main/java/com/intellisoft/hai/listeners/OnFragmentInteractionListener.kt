package com.intellisoft.hai.listeners

import androidx.fragment.app.Fragment

interface OnFragmentInteractionListener {
    fun launchAction()
    fun nextFragment(fragment: Fragment)
}