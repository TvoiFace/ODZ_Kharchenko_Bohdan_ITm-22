package com.google.firebase.example.fireeats.java.adapter;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.example.fireeats.R;
import com.google.firebase.example.fireeats.databinding.ItemHotelBinding;
import com.google.firebase.example.fireeats.java.model.Hotel;
import com.google.firebase.example.fireeats.java.util.HotelUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;


public class HotelAdapter extends FirestoreAdapter<HotelAdapter.ViewHolder> {

    public interface OnHotelSelectedListener {

        void onHotelSelected(DocumentSnapshot hotel);

    }

    private OnHotelSelectedListener mListener;

    public HotelAdapter(Query query, OnHotelSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(ItemHotelBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ItemHotelBinding binding;

        public ViewHolder(ItemHotelBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ViewHolder(View itemView) {
            super(itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnHotelSelectedListener listener) {

            Hotel hotel = snapshot.toObject(Hotel.class);
            Resources resources = itemView.getResources();

            // Load image
            Glide.with(binding.hotelItemImage.getContext())
                    .load(hotel.getPhoto())
                    .into(binding.hotelItemImage);

            binding.hotelItemName.setText(hotel.getName());
            binding.hotelItemRating.setRating((float) hotel.getAvgRating());
            binding.hotelItemCity.setText(hotel.getCity());
            binding.hotelItemCategory.setText(hotel.getCategory());
            binding.hotelItemNumRatings.setText(resources.getString(R.string.fmt_num_ratings,
                    hotel.getNumRatings()));
            binding.hotelItemPrice.setText(HotelUtil.getPriceString(hotel));

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onHotelSelected(snapshot);
                    }
                }
            });
        }

    }
}
