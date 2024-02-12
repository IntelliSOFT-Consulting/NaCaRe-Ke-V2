package com.imeja.nacare_live.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.imeja.nacare_live.R
import com.imeja.nacare_live.data.FormatterClass
import com.imeja.nacare_live.holders.PersonViewHolder
import com.imeja.nacare_live.model.ExpandableItem


class ExpandableListAdapter(
    personList: List<ExpandableItem>, context: Context
) :
    RecyclerView.Adapter<PersonViewHolder>() {
    private val personList: List<ExpandableItem>
    private val context: Context
    private var radioGroup: RadioGroup? = null
    private val currentPosition = 0

    init {
        this.personList = personList
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.list_layout_tracked, parent, false)
        return PersonViewHolder(v)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val data: ExpandableItem = personList[position]
        holder.textViewName.text = data.groupName
        holder.linearLayout.visibility = View.GONE
        if (currentPosition == position) {
            holder.lnLinearLayout.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.selected
                )
            )
            holder.rotationImageView.rotation = 0f
            holder.linearLayout.visibility = View.VISIBLE
        } else {
            holder.linearLayout.visibility = View.GONE
        }
//        holder.itemView.setOnClickListener { view: View? ->
//            currentPosition = position
//            notifyDataSetChanged()
//        }
//        if (data.getChildItems() != null) {
//            holder.smallTextView.text =
//                countResponded(data.getChildItems()) + "/" + data.getChildItems().size()
//            holder.linearLayout.removeAllViews()
//            for (trackedEntityAttribute in data.getChildItems()) {
//                createSearchFields(
//                    holder.linearLayout, trackedEntityAttribute,
//                    extractCurrentAttributeValue(trackedEntityAttribute),
//                    extractElementAttributes(trackedEntityAttribute)
//                )
//            }
//        }
//        if (data.getDataElements() != null) {
//            holder.smallTextView.text = "0/" + data.getDataElements().size()
//            holder.linearLayout.removeAllViews()
//            for (dataElement in data.getDataElements()) {
//                createSearchFieldsDataElement(
//                    holder.linearLayout,
//                    dataElement,
//                    extractCurrentValue(
//                        data.getProgramUid(), data.getProgramStageUid(), data.getSelectedOrgUnit(),
//                        data.getSelectedTei(), dataElement
//                    ), extractElementAttribute(dataElement)
//                )
//            }
//        }
//        val inflater = LayoutInflater.from(context)
//        val itemView = inflater.inflate(
//            R.layout.item_submit_cancel,
//            holder.linearLayout,
//            false
//        ) as LinearLayout
//        holder.linearLayout.addView(itemView)
//        val submitButton = itemView.findViewById<MaterialButton>(R.id.btn_proceed)
//        submitButton.setOnClickListener { v: View? ->
//            val builder: AlertDialog.Builder = Builder(context)
//            val inflater2 = LayoutInflater.from(context)
//            val customView: View =
//                inflater2.inflate(R.layout.custom_layout_confirm, null)
//            builder.setView(customView)
//            val alertDialog: AlertDialog = builder.create()
//            val tvTitle =
//                customView.findViewById<TextView>(R.id.tv_title)
//            val tvMessage =
//                customView.findViewById<TextView>(R.id.tv_message)
//            val noButton = customView.findViewById<MaterialButton>(R.id.no_button)
//            val yesButton = customView.findViewById<MaterialButton>(R.id.yes_button)
//            tvTitle.setText(R.string.submit_form)
//            tvMessage.text =
//                "Are you sure you want to save?\nYou will npt be able to edit this patient info once saved"
//            yesButton.setOnClickListener { c: View? -> alertDialog.dismiss() }
//            noButton.setOnClickListener { c: View? -> alertDialog.dismiss() }
//            alertDialog.show()
//        }
//        val nextButton = itemView.findViewById<MaterialButton>(R.id.btn_cancel)
//        nextButton.setOnClickListener { v: View? -> }
    }


    private fun getSharedPref(key: String): String? {
        return FormatterClass().getSharedPref(key, context)
    }


    override fun getItemCount(): Int {
        return personList.size
    }


}