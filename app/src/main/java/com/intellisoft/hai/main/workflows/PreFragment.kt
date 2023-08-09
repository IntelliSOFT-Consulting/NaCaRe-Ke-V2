package com.intellisoft.hai.main.workflows

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.intellisoft.hai.databinding.FragmentPreBinding
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.listeners.OnFragmentInteractionListener
import com.intellisoft.hai.room.MainViewModel
import com.intellisoft.hai.room.PrePostOperativeData
import com.intellisoft.hai.util.AppUtils.controlData
import com.intellisoft.hai.util.AppUtils.generateUuid

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PreFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PreFragment : Fragment() {
    private lateinit var formatterClass: FormatterClass
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: FragmentPreBinding
    private val pre_operation = HashSet<String>()
    private val post_operation = HashSet<String>()
    private var mListener: OnFragmentInteractionListener? = null
    private lateinit var reason: String
    private lateinit var drain: String
    private lateinit var implant: String
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPreBinding.inflate(layoutInflater)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        formatterClass = FormatterClass()
        controlListeners()
        binding.btnSubmit.apply {
            setOnClickListener {
                if (validate()) {
                    val user = formatterClass.getSharedPref("username", requireContext())
                    if (user != null) {
                        val patient = formatterClass.getSharedPref("patient", requireContext())
                        val preLabels = pre_operation.toTypedArray()
                        val preString = preLabels.joinToString(", ")
                        val postLabels = post_operation.toTypedArray()
                        val postString = postLabels.joinToString(", ")
                        val pre_other = binding.edtPreOther.text?.toString()
                        val post_other = binding.edtPostOther.text?.toString()
                        val pre = binding.edtPreNonOther.text?.toString()
                        val post = binding.edtPostNonOther.text?.toString()
                        val res = binding.edtReasonOther.text?.toString()
                        val drain_loc = binding.edtDrain.text?.toString()
                        val type = binding.edtImplantType.text?.toString()
                        val ceased =
                            if (binding.radioButtonAntibioticsCeasedNo.isChecked) "No" else "Yes"
                        val antibiotic =
                            if (binding.radioButtonNoAntibioticWithDrain.isChecked) "No" else "Yes"

                        val data =
                            PrePostOperativeData(
                                userId = user,
                                patientId = patient.toString(),
                                encounterId = generateUuid(),
                                pre_antibiotic_prophylaxis = preString,
                                pre_antibiotic_prophylaxis_other = pre_other.toString(),
                                pre_other_antibiotic_given = pre.toString(),
                                antibiotics_ceased = ceased,
                                post_antibiotic_prophylaxis = postString,
                                post_antibiotic_prophylaxis_other = post_other.toString(),
                                post_other_antibiotic_given = post.toString(),
                                post_reason = getData(binding.postOpAntibioticReasonRadioGroup),
                                post_reason_other = res.toString(),
                                drain_inserted = getData(binding.drainInsertedRadioGroup),
                                drain_location = drain_loc.toString(),
                                drain_antibiotic = antibiotic,
                                implant_used = getData(binding.implantUsedRadioGroup),
                                implant_other = type.toString()
                            )
                        val added = mainViewModel.addPrePostOperativeData(data)
                        if (added) {
                            Toast.makeText(
                                requireContext(),
                                "Record Successfully saved",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            mListener?.nextFragment(PostFragment())

                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Encountered problems saving data",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please check user account",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        }
        handleClicks()
        handleVisibles()
        return binding.root
    }

    private fun getData(parent: RadioGroup): String {
        for (i in 0 until parent.childCount) {
            val radioButton = parent.getChildAt(i) as RadioButton
            if (radioButton.isChecked) {
                // Get the text of the selected RadioButton
                reason = radioButton.text.toString()
                break
            }
        }
        return reason;
    }

    private fun controlListeners() {


        controlData(
            binding.edtImplantType,
            binding.implantTypeHolder,
            "Please provide input",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        controlData(
            binding.edtDrain,
            binding.drainHolder,
            "Please provide input",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        controlData(
            binding.edtReasonOther,
            binding.reasonOtherHolder,
            "Please provide input",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        controlData(
            binding.edtPreOther,
            binding.preOtherHolder,
            "Please provide input",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        controlData(
            binding.edtPostOther,
            binding.postOtherHolder,
            "Please provide input",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        controlData(
            binding.edtPreNonOther,
            binding.preNonOtherHolder,
            "Please provide input",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
        controlData(
            binding.edtPostNonOther,
            binding.postNonOtherHolder,
            "Please provide input",
            hasMin = false,
            hasMax = false,
            min = 0,
            max = 0
        )
    }

    private fun handleVisibles() {
        binding.drainInsertedRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.radioButtonDrainInserted.id) {
                binding.drainHolder.visibility = View.VISIBLE
            } else {
                binding.drainHolder.visibility = View.GONE
            }
        }
        binding.implantUsedRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.radioButtonOtherImplant.id) {
                binding.implantTypeHolder.visibility = View.VISIBLE
            } else {
                binding.implantTypeHolder.visibility = View.GONE
            }
        }
        binding.postOpAntibioticReasonRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.radioButtonOtherReason.id) {
                binding.reasonOtherHolder.visibility = View.VISIBLE
            } else {
                binding.reasonOtherHolder.visibility = View.GONE
            }
        }
        binding.checkBoxOtherPre.apply {
            setOnCheckedChangeListener { _, isChecked ->
                binding.preOtherHolder.isVisible = isChecked
                updatePreOperation(binding.checkBoxOtherPre.text.toString(), isChecked)
                if (!isChecked) {
                    binding.edtPreOther.text?.clear()
                }
            }
        }
        binding.checkBoxOtherPost.apply {
            setOnCheckedChangeListener { _, isChecked ->
                binding.postOtherHolder.isVisible = isChecked
                updatePostOperation(binding.checkBoxOtherPost.text.toString(), isChecked)
                if (!isChecked) {
                    binding.edtPostOther.text?.clear()
                }
            }
        }

    }


    private fun handleClicks() {
        binding.checkBoxNoneGiven.setOnCheckedChangeListener { _, isChecked ->
            updatePreOperation(binding.checkBoxNoneGiven.text.toString(), isChecked)
        }
        binding.checkBoxGentamicin.setOnCheckedChangeListener { _, isChecked ->
            updatePreOperation(binding.checkBoxGentamicin.text.toString(), isChecked)
        }
        binding.checkBoxAmoxiclav.setOnCheckedChangeListener { _, isChecked ->
            updatePreOperation(binding.checkBoxAmoxiclav.text.toString(), isChecked)
        }
        binding.checkBoxCiprofloxacin.setOnCheckedChangeListener { _, isChecked ->
            updatePreOperation(binding.checkBoxCiprofloxacin.text.toString(), isChecked)
        }
        binding.checkBoxCefazolin.setOnCheckedChangeListener { _, isChecked ->
            updatePreOperation(binding.checkBoxCefazolin.text.toString(), isChecked)
        }
        binding.checkBoxCloxacillin.setOnCheckedChangeListener { _, isChecked ->
            updatePreOperation(binding.checkBoxCloxacillin.text.toString(), isChecked)
        }
        binding.checkBoxVancomycin.setOnCheckedChangeListener { _, isChecked ->
            updatePreOperation(binding.checkBoxVancomycin.text.toString(), isChecked)
        }
        binding.checkBoxMetronidazole.setOnCheckedChangeListener { _, isChecked ->
            updatePreOperation(binding.checkBoxMetronidazole.text.toString(), isChecked)
        }
        binding.checkBoxPenicillin.setOnCheckedChangeListener { _, isChecked ->
            updatePreOperation(binding.checkBoxPenicillin.text.toString(), isChecked)
        }
        binding.checkBoxCeftriaxone.setOnCheckedChangeListener { _, isChecked ->
            updatePreOperation(binding.checkBoxCeftriaxone.text.toString(), isChecked)
        }
        binding.checkBoxCefuroxime.setOnCheckedChangeListener { _, isChecked ->
            updatePreOperation(binding.checkBoxCefuroxime.text.toString(), isChecked)
        }
        binding.checkBoxOtherPre.setOnCheckedChangeListener { _, isChecked ->
            updatePreOperation(binding.checkBoxOtherPre.text.toString(), isChecked)
        }
        /*Post Data*/

        binding.checkBoxNoneGivenPost.setOnCheckedChangeListener { _, isChecked ->
            updatePostOperation(binding.checkBoxNoneGivenPost.text.toString(), isChecked)
        }
        binding.checkBoxGentamicinPost.setOnCheckedChangeListener { _, isChecked ->
            updatePostOperation(binding.checkBoxGentamicinPost.text.toString(), isChecked)
        }
        binding.checkBoxAmoxiclavPost.setOnCheckedChangeListener { _, isChecked ->
            updatePostOperation(binding.checkBoxAmoxiclavPost.text.toString(), isChecked)
        }
        binding.checkBoxCiprofloxacinPost.setOnCheckedChangeListener { _, isChecked ->
            updatePostOperation(binding.checkBoxCiprofloxacinPost.text.toString(), isChecked)
        }
        binding.checkBoxCefazolinPost.setOnCheckedChangeListener { _, isChecked ->
            updatePostOperation(binding.checkBoxCefazolinPost.text.toString(), isChecked)
        }
        binding.checkBoxCloxacillinPost.setOnCheckedChangeListener { _, isChecked ->
            updatePostOperation(binding.checkBoxCloxacillinPost.text.toString(), isChecked)
        }
        binding.checkBoxVancomycinPost.setOnCheckedChangeListener { _, isChecked ->
            updatePostOperation(binding.checkBoxVancomycinPost.text.toString(), isChecked)
        }
        binding.checkBoxMetronidazolePost.setOnCheckedChangeListener { _, isChecked ->
            updatePostOperation(binding.checkBoxMetronidazolePost.text.toString(), isChecked)
        }
        binding.checkBoxPenicillinPost.setOnCheckedChangeListener { _, isChecked ->
            updatePostOperation(binding.checkBoxPenicillinPost.text.toString(), isChecked)
        }
        binding.checkBoxCeftriaxonePost.setOnCheckedChangeListener { _, isChecked ->
            updatePostOperation(binding.checkBoxCeftriaxonePost.text.toString(), isChecked)
        }
        binding.checkBoxCefuroximePost.setOnCheckedChangeListener { _, isChecked ->
            updatePostOperation(binding.checkBoxCefuroximePost.text.toString(), isChecked)
        }
        binding.checkBoxOtherPost.setOnCheckedChangeListener { _, isChecked ->
            updatePostOperation(binding.checkBoxOtherPost.text.toString(), isChecked)
        }

        binding.postOpAntibioticReasonRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            reason = selectedRadioButton.text.toString()
        }
        binding.drainInsertedRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            drain = selectedRadioButton.text.toString()
        }
        binding.implantUsedRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            implant = selectedRadioButton.text.toString()
        }
    }

    private fun updatePreOperation(string: String, checked: Boolean) {
        if (checked) {
            pre_operation.add(string)
        } else {
            pre_operation.remove(string)
        }
    }

    private fun updatePostOperation(string: String, checked: Boolean) {
        if (checked) {
            post_operation.add(string)
        } else {
            post_operation.remove(string)
        }
    }

    private fun validate(): Boolean {
        val pre = pre_operation.toTypedArray()
        if (pre.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "please select pre operation antibotic",
                Toast.LENGTH_SHORT
            )
                .show()
            return false
        }
        if (binding.checkBoxOtherPre.isChecked) {
            val pre_other = binding.edtPreOther.text?.toString()
            if (pre_other.isNullOrEmpty()) {
                binding.preOtherHolder.error = "Please provide input"
                binding.edtPreOther.requestFocus()
                return false
            }
        }
        val o = binding.edtPreNonOther.text?.toString()
        if (o.isNullOrEmpty()) {
            binding.preNonOtherHolder.error = "Please provide input"
            binding.edtPreNonOther.requestFocus()
            return false
        }
        if (binding.antibioticsCeasedRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select if ceased",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        val post = post_operation.toTypedArray()
        if (post.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "please select post operation antibotic",
                Toast.LENGTH_SHORT
            )
                .show()
            return false
        }
        if (binding.checkBoxOtherPost.isChecked) {
            val post_other = binding.edtPostOther.text?.toString()
            if (post_other.isNullOrEmpty()) {
                binding.postOtherHolder.error = "Please provide input"
                binding.edtPostOther.requestFocus()
                return false
            }
        }
        val p = binding.edtPostNonOther.text?.toString()
        if (p.isNullOrEmpty()) {
            binding.postNonOtherHolder.error = "Please provide input"
            binding.edtPostNonOther.requestFocus()
            return false
        }
        if (binding.postOpAntibioticReasonRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select reason",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.radioButtonOtherReason.isChecked) {
            val res = binding.edtReasonOther.text?.toString()
            if (res.isNullOrEmpty()) {
                binding.reasonOtherHolder.error = "Please provide input"
                binding.edtReasonOther.requestFocus()
                return false
            }
        }
        if (binding.drainInsertedRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select if drain inserted",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.radioButtonDrainInserted.isChecked) {
            val dr = binding.edtDrain.text?.toString()
            if (dr.isNullOrEmpty()) {
                binding.drainHolder.error = "Please provide input"
                binding.edtDrain.requestFocus()
                return false
            }
        }
        if (binding.antibioticGivenWithDrainRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select if given in presence of drain",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.implantUsedRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select if implant used",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.radioButtonOtherImplant.isChecked) {
            val cc = binding.edtImplantType.text?.toString()
            if (cc.isNullOrEmpty()) {
                binding.implantTypeHolder.error = "Please provide input"
                binding.edtImplantType.requestFocus()
                return false
            }
        }

        return true
    }

}