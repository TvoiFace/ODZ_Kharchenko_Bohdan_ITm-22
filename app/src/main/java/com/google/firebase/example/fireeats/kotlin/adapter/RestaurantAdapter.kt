package com.google.firebase.example.fireeats.kotlin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.example.fireeats.R
import com.google.firebase.example.fireeats.databinding.ItemHotelBinding
import com.google.firebase.example.fireeats.kotlin.model.Hotel
import com.google.firebase.example.fireeats.kotlin.util.HotelUtil
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

/**
 * RecyclerView adapter for a list of Restaurants.
 */
open class RestaurantAdapter(query: Query, private val listener: OnRestaurantSelectedListener) :
    FirestoreAdapter<RestaurantAdapter.ViewHolder>(query) {

    interface OnRestaurantSelectedListener {

        fun onRestaurantSelected(restaurant: DocumentSnapshot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHotelBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), listener)
    }

    class ViewHolder(val binding: ItemHotelBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            snapshot: DocumentSnapshot,
            listener: OnRestaurantSelectedListener?,
        ) {
        
            val hotel = snapshot.toObject<Hotel>() ?: return

            val resources = binding.root.resources

            // Load image
            Glide.with(binding.hotelItemImage.context)
                .load(hotel.photo)
                .into(binding.hotelItemImage)

            val numRatings: Int = hotel.numRatings

            binding.hotelItemName.text = hotel.name
            binding.hotelItemRating.rating = hotel.avgRating.toFloat()
            binding.hotelItemCity.text = hotel.city
            binding.hotelItemCategory.text = hotel.category
            binding.hotelItemNumRatings.text = resources.getString(
                R.string.fmt_num_ratings,
                numRatings,
            )
            binding.hotelItemPrice.text = HotelUtil.getPriceString(hotel)

            // Click listener
            binding.root.setOnClickListener {
                listener?.onRestaurantSelected(snapshot)
            }
        }
    }
}
