package com.rei.flowroomdemo

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.afollestad.recyclical.ViewHolder

data class CartData(
    val id: Int,
    val name: String,
    var qty: Int = 0,
) {
}

class CartViewHolder(itemView: View) : ViewHolder(itemView) {
    val name: TextView = itemView.findViewById(R.id.tvMaterial)
    val plus: ImageView = itemView.findViewById(R.id.plus)
    val minus: ImageView = itemView.findViewById(R.id.minus)
    val qty: TextView = itemView.findViewById(R.id.qty)
}