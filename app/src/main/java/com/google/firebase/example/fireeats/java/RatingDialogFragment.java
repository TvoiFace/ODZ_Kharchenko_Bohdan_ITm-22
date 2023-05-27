package com.google.firebase.example.fireeats.java;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.fireeats.R;
import com.google.firebase.example.fireeats.databinding.DialogRatingBinding;
import com.google.firebase.example.fireeats.java.model.Rating;


public class RatingDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "RatingDialog";

    private DialogRatingBinding mBinding;
    
    interface RatingListener {

        void onRating(Rating rating);

    }

    private RatingListener mRatingListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DialogRatingBinding.inflate(inflater, container, false);

        mBinding.hotelFormButton.setOnClickListener(this);
        mBinding.hotelFormCancel.setOnClickListener(this);

        return mBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getParentFragment() instanceof RatingListener) {
            mRatingListener = (RatingListener) getParentFragment();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    private void onSubmitClicked(View view) {
        Rating rating = new Rating(
                FirebaseAuth.getInstance().getCurrentUser(),
                mBinding.hotelFormRating.getRating(),
                mBinding.hotelFormText.getText().toString());

        if (mRatingListener != null) {
            mRatingListener.onRating(rating);
        }

        dismiss();
    }

    private void onCancelClicked(View view) {
        dismiss();
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.hotelFormButton) {
            onSubmitClicked(v);
        } else if (viewId == R.id.hotelFormCancel) {
            onCancelClicked(v);
        }
    }

}
