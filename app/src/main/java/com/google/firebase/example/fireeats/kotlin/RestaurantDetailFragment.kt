package com.google.firebase.example.fireeats.kotlin

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.example.fireeats.R
import com.google.firebase.example.fireeats.databinding.FragmentHotelDetailBinding
import com.google.firebase.example.fireeats.kotlin.adapter.RatingAdapter
import com.google.firebase.example.fireeats.kotlin.model.Rating
import com.google.firebase.example.fireeats.kotlin.model.Hotel
import com.google.firebase.example.fireeats.kotlin.util.HotelUtil
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class RestaurantDetailFragment :
    Fragment(),
    EventListener<DocumentSnapshot>,
    RatingDialogFragment.RatingListener {

    private var ratingDialog: RatingDialogFragment? = null

    private lateinit var binding: FragmentHotelDetailBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var restaurantRef: DocumentReference
    private lateinit var ratingAdapter: RatingAdapter

    private var restaurantRegistration: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHotelDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get restaurant ID from extras
        val restaurantId = RestaurantDetailFragmentArgs.fromBundle(requireArguments()).keyRestaurantId

        // Initialize Firestore
        firestore = Firebase.firestore

        // Get reference to the restaurant
        restaurantRef = firestore.collection("restaurants").document(restaurantId)

        // Get ratings
        val ratingsQuery = restaurantRef
            .collection("ratings")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)

        // RecyclerView
        ratingAdapter = object : RatingAdapter(ratingsQuery) {
            override fun onDataChanged() {
                if (itemCount == 0) {
                    binding.recyclerRatings.visibility = View.GONE
                    binding.viewEmptyRatings.visibility = View.VISIBLE
                } else {
                    binding.recyclerRatings.visibility = View.VISIBLE
                    binding.viewEmptyRatings.visibility = View.GONE
                }
            }
        }
        binding.recyclerRatings.layoutManager = LinearLayoutManager(context)
        binding.recyclerRatings.adapter = ratingAdapter

        ratingDialog = RatingDialogFragment()

        binding.hotelButtonBack.setOnClickListener { onBackArrowClicked() }
        binding.fabShowRatingDialog.setOnClickListener { onAddRatingClicked() }
    }

    public override fun onStart() {
        super.onStart()

        ratingAdapter.startListening()
        restaurantRegistration = restaurantRef.addSnapshotListener(this)
    }

    public override fun onStop() {
        super.onStop()

        ratingAdapter.stopListening()

        restaurantRegistration?.remove()
        restaurantRegistration = null
    }

    /**
     * Listener for the Restaurant document ([.restaurantRef]).
     */
    override fun onEvent(snapshot: DocumentSnapshot?, e: FirebaseFirestoreException?) {
        if (e != null) {
            Log.w(TAG, "restaurant:onEvent", e)
            return
        }

        snapshot?.let {
            val hotel = snapshot.toObject<Hotel>()
            if (hotel != null) {
                onRestaurantLoaded(hotel)
            }
        }
    }

    private fun onRestaurantLoaded(hotel: Hotel) {
        binding.hotelName.text = hotel.name
        binding.hotelRating.rating = hotel.avgRating.toFloat()
        binding.hotelNumRatings.text = getString(R.string.fmt_num_ratings, hotel.numRatings)
        binding.hotelCity.text = hotel.city
        binding.hotelCategory.text = hotel.category
        binding.hotelPrice.text = HotelUtil.getPriceString(hotel)

        // Background image
        Glide.with(binding.hotelImage.context)
            .load(hotel.photo)
            .into(binding.hotelImage)
    }

    private fun onBackArrowClicked() {
        findNavController().popBackStack()
    }

    private fun onAddRatingClicked() {
        ratingDialog?.show(childFragmentManager, RatingDialogFragment.TAG)
    }

    override fun onRating(rating: Rating) {
        // In a transaction, add the new rating and update the aggregate totals
        addRating(restaurantRef, rating)
            .addOnSuccessListener(requireActivity()) {
                Log.d(TAG, "Rating added")

                // Hide keyboard and scroll to top
                hideKeyboard()
                binding.recyclerRatings.smoothScrollToPosition(0)
            }
            .addOnFailureListener(requireActivity()) { e ->
                Log.w(TAG, "Add rating failed", e)

                // Show failure message and hide keyboard
                hideKeyboard()
                Snackbar.make(
                    requireView().findViewById(android.R.id.content),
                    "Failed to add rating",
                    Snackbar.LENGTH_SHORT,
                ).show()
            }
    }

    private fun addRating(restaurantRef: DocumentReference, rating: Rating): Task<Void> {
        // Create reference for new rating, for use inside the transaction
        val ratingRef = restaurantRef.collection("ratings").document()

        // In a transaction, add the new rating and update the aggregate totals
        return firestore.runTransaction { transaction ->
            val hotel = transaction.get(restaurantRef).toObject<Hotel>()
                ?: throw Exception("Restaurant not found at ${restaurantRef.path}")

            // Compute new number of ratings
            val newNumRatings = hotel.numRatings + 1

            // Compute new average rating
            val oldRatingTotal = hotel.avgRating * hotel.numRatings
            val newAvgRating = (oldRatingTotal + rating.rating) / newNumRatings

            // Set new restaurant info
            hotel.numRatings = newNumRatings
            hotel.avgRating = newAvgRating

            // Commit to Firestore
            transaction.set(restaurantRef, hotel)
            transaction.set(ratingRef, rating)

            null
        }
    }

    private fun hideKeyboard() {
        val view = requireActivity().currentFocus
        if (view != null) {
            (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    companion object {

        private const val TAG = "RestaurantDetail"

        const val KEY_RESTAURANT_ID = "key_restaurant_id"
    }
}
