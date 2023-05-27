package com.google.firebase.example.fireeats.java;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.fireeats.R;
import com.google.firebase.example.fireeats.databinding.FragmentMainBinding;
import com.google.firebase.example.fireeats.java.adapter.HotelAdapter;
import com.google.firebase.example.fireeats.java.model.Hotel;
import com.google.firebase.example.fireeats.java.model.Rating;
import com.google.firebase.example.fireeats.java.util.RatingUtil;
import com.google.firebase.example.fireeats.java.util.HotelUtil;
import com.google.firebase.example.fireeats.java.viewmodel.MainActivityViewModel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.Collections;
import java.util.List;

public class MainFragment extends Fragment implements
        FilterDialogFragment.FilterListener,
        HotelAdapter.OnHotelSelectedListener, View.OnClickListener,
        MenuProvider {

    private static final String TAG = "MainActivity";

    private static final int LIMIT = 50;

    private FragmentMainBinding mBinding;

    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private FilterDialogFragment mFilterDialog;
    private HotelAdapter mAdapter;

    private MainActivityViewModel mViewModel;

    private MenuHost menuHost;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentMainBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.filterBar.setOnClickListener(this);
        mBinding.buttonClearFilter.setOnClickListener(this);

        // MenuProvider
        menuHost = requireActivity();
        menuHost.addMenuProvider(this);

        // View model
        mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        // Firestore
        mFirestore = FirebaseFirestore.getInstance();


        mQuery = mFirestore.collection("hotels")
                .orderBy("avgRating", Query.Direction.DESCENDING)
                .limit(LIMIT);

        // RecyclerView
        mAdapter = new HotelAdapter(mQuery, this) {
            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mBinding.recyclerHotels.setVisibility(View.GONE);
                    mBinding.viewEmpty.setVisibility(View.VISIBLE);
                } else {
                    mBinding.recyclerHotels.setVisibility(View.VISIBLE);
                    mBinding.viewEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(mBinding.getRoot(),
                        "Error", Snackbar.LENGTH_LONG).show();
            }
        };

        mBinding.recyclerHotels.setLayoutManager(new LinearLayoutManager(requireContext()));
        mBinding.recyclerHotels.setAdapter(mAdapter);

        // Filter Dialog
        mFilterDialog = new FilterDialogFragment();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Start sign in if necessary
        if (shouldStartSignIn()) {
            startSignIn();
            return;
        }

        // Apply filters
        onFilter(mViewModel.getFilters());

        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem item) {
        //Due to bump in Java version, we can not use view ids in switch
        //(see: http://tools.android.com/tips/non-constant-fields), so we
        //need to use if/else:

        int itemId = item.getItemId();
        if (itemId == R.id.menu_add_items) {
            onAddItemsClicked();
            return true;
        } else if (itemId == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(requireContext());
            startSignIn();
            return true;
        }
        return false;
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        mViewModel.setIsSigningIn(false);

        if (result.getResultCode() != Activity.RESULT_OK) {
            if (response == null) {
                // User pressed the back button.
                requireActivity().finish();
            } else if (response.getError() != null
                    && response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                showSignInErrorDialog(R.string.message_no_network);
            } else {
                showSignInErrorDialog(R.string.message_unknown);
            }
        }
    }

    public void onFilterClicked() {
        // Show the dialog containing filter options
        mFilterDialog.show(getChildFragmentManager(), FilterDialogFragment.TAG);
    }

    public void onClearFilterClicked() {
        mFilterDialog.resetFilters();

        onFilter(Filters.getDefault());
    }

    @Override
    public void onHotelSelected(DocumentSnapshot hotel) {
        // Go to the details page for the selected hotels
        MainFragmentDirections.ActionMainFragmentToRestaurantDetailFragment action = MainFragmentDirections
                .actionMainFragmentToRestaurantDetailFragment(hotel.getId());

        NavHostFragment.findNavController(this)
                .navigate(action);
    }

    @Override
    public void onFilter(Filters filters) {
        // Construct query basic query
        Query query = mFirestore.collection("hotels");

        // Category (equality filter)
        if (filters.hasCategory()) {
            query = query.whereEqualTo(Hotel.FIELD_CATEGORY, filters.getCategory());
        }

        // City (equality filter)
        if (filters.hasCity()) {
            query = query.whereEqualTo(Hotel.FIELD_CITY, filters.getCity());
        }

        // Price (equality filter)
        if (filters.hasPrice()) {
            query = query.whereEqualTo(Hotel.FIELD_PRICE, filters.getPrice());
        }

        // Sort by (orderBy with direction)
        if (filters.hasSortBy()) {
            query = query.orderBy(filters.getSortBy(), filters.getSortDirection());
        }

        // Limit items
        query = query.limit(LIMIT);

        // Update the query
        mAdapter.setQuery(query);

        // Set header
        mBinding.textCurrentSearch.setText(HtmlCompat.fromHtml(filters.getSearchDescription(requireContext()),
                HtmlCompat.FROM_HTML_MODE_LEGACY));
        mBinding.textCurrentSortBy.setText(filters.getOrderDescription(requireContext()));

        // Save filters
        mViewModel.setFilters(filters);
    }

    private boolean shouldStartSignIn() {
        return (!mViewModel.getIsSigningIn() && FirebaseAuth.getInstance().getCurrentUser() == null);
    }

    private void startSignIn() {
        // Sign in with FirebaseUI
        ActivityResultLauncher<Intent> signinLauncher = requireActivity()
                .registerForActivityResult(new FirebaseAuthUIActivityResultContract(),
                        this::onSignInResult
                );

        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(Collections.singletonList(
                        new AuthUI.IdpConfig.EmailBuilder().build()))
                .setIsSmartLockEnabled(false)
                .build();

        signinLauncher.launch(intent);
        mViewModel.setIsSigningIn(true);
    }

    private void onAddItemsClicked() {
        // Add a bunch of random hotels
        WriteBatch batch = mFirestore.batch();
        for (int i = 0; i < 10; i++) {
            DocumentReference restRef = mFirestore.collection("hotels").document();

            // Create random hotels / ratings
            Hotel randomHotel = HotelUtil.getRandom(requireContext());
            List<Rating> randomRatings = RatingUtil.getRandomList(randomHotel.getNumRatings());
            randomHotel.setAvgRating(RatingUtil.getAverageRating(randomRatings));

            // Add hotel
            batch.set(restRef, randomHotel);

            // Add ratings to subcollection
            for (Rating rating : randomRatings) {
                batch.set(restRef.collection("ratings").document(), rating);
            }
        }

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Write batch done.");
                } else {
                    Log.w(TAG, "write batch failed.", task.getException());
                }
            }
        });
    }

    private void showSignInErrorDialog(@StringRes int message) {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_sign_in_error)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.option_retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                      startSignIn();
                    }
                })
                .setNegativeButton(R.string.option_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requireActivity().finish();
                    }
                }).create();

        dialog.show();
    }

    @Override
    public void onClick(View v) {
        //Due to bump in Java version, we can not use view ids in switch
        //(see: http://tools.android.com/tips/non-constant-fields), so we
        //need to use if/else:

        int viewId = v.getId();
        if (viewId == R.id.filterBar) {
            onFilterClicked();
        } else if (viewId == R.id.buttonClearFilter) {
            onClearFilterClicked();
        }
    }
}
