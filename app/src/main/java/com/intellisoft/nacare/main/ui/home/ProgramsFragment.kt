package com.intellisoft.nacare.main.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.intellisoft.nacare.core.Sdk
import com.nacare.capture.databinding.FragmentProgramsBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.program.Program


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProgramsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProgramsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private var disposable: Disposable? = null
    private lateinit var binding: FragmentProgramsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProgramsBinding.inflate(inflater, container, false)
        programData()
        return binding.root

    }

    private fun programData() {
//        val adapter = ProgramsAdapter(this)
//      bin  recyclerView.setAdapter(adapter)

//        disposable = Sdk.d2().organisationUnitModule().organisationUnits().getUids()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .map(this::getPrograms)
//            .subscribe { programs ->
//                programs?.observe(viewLifecycleOwner) { programPagedList ->
//                    //                    adapter.submitList(programPagedList)
//                    Log.e("TAG", "Data Comes Here $programPagedList")
//
//                }
//            }
    }

//    private fun getPrograms(organisationUnitUids: List<String>): LiveData<PagedList<Program>>? {
//        return Sdk.d2().programModule().programs()
//            .byOrganisationUnitList(organisationUnitUids)
//            .orderByName(RepositoryScope.OrderByDirection.ASC)
//            .getPaged(20)
//    }

//    override fun onDestroy() {
//        super.onDestroy()
//        disposable?.dispose()
//    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProgramsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProgramsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}