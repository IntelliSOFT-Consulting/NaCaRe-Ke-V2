package com.intellisoft.nacare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.nacare.helper_class.Person
import com.intellisoft.nacare.room.EventData
import com.nacare.ke.capture.R

class PersonAdapter(
    private val people: List<Person>,
    private val click: (Person) -> Unit
) :
    RecyclerView.Adapter<PersonAdapter.PersonViewHolder>() {

    class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val yearTextView: TextView = itemView.findViewById(R.id.yearTextView)
        val firstNameTextView: TextView = itemView.findViewById(R.id.firstNameTextView)
        val middleNameTextView: TextView = itemView.findViewById(R.id.middleNameTextView)
        val lastNameTextView: TextView = itemView.findViewById(R.id.lastNameTextView)
        val documentTextView: TextView = itemView.findViewById(R.id.documentTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_person, parent, false)
        return PersonViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val person = people[position]

        holder.yearTextView.text = person.year.toString()
        holder.firstNameTextView.text = person.firstName
        holder.middleNameTextView.text = person.middleName
        holder.lastNameTextView.text = person.lastName
        holder.documentTextView.text = person.document
        holder.itemView.setOnClickListener {
            click(person)
        }
    }

    override fun getItemCount(): Int {
        return people.size
    }
}