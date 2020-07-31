package com.example.pawsibilities.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.pawsibilities.R;
import com.example.pawsibilities.Tag;
import com.example.pawsibilities.databinding.FragmentDetailedTagBinding;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;


public class DetailedTagDialogFragment extends CircularRevealDialogFragment {

    private static final String TAG = "DetailedTagDialogFragment";
    private static final String KEY_TAG = "Tag";
    private FragmentDetailedTagBinding binding;
    private Tag tag;
    private ParseGeoPoint userLocation;

    public DetailedTagDialogFragment() {
        // Required empty public constructor
    }


    public static DetailedTagDialogFragment newInstance(Tag tag) {
        DetailedTagDialogFragment fragment = new DetailedTagDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_TAG, tag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDetailedTagBinding.inflate(inflater, container, false);
        setUpOnLayoutListener(binding.getRoot(), true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            this.tag = getArguments().getParcelable(KEY_TAG);
            this.userLocation = ParseUser.getCurrentUser().getParseGeoPoint(MapsFragment.KEY_LOCATION);
        }

        ParseFile image = tag.getPhoto();
        if (image != null) {
            Glide.with(getContext())
                    .load(image.getUrl())
                    .transform(new CenterCrop(), new RoundedCorners(100))
                    .into(binding.ivPhoto);
        }
        binding.tvName.setText(tag.getName());
        binding.tvName.setSelected(true);
        binding.tvTimeAgo.setText("Updated " + tag.getRelativeTimeAgo());
        binding.tvDistance.setText(tag.distanceFrom(userLocation) + " miles away");
        binding.tvDirection.setText("Heading: " + tag.getDirection());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}