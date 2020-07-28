package com.example.pawsibilities.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.bumptech.glide.Glide;
import com.example.pawsibilities.R;
import com.example.pawsibilities.Tag;
import com.example.pawsibilities.databinding.FragmentDetailedTagDialogBinding;
import com.example.pawsibilities.databinding.FragmentEditTagBinding;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.ArrayList;

public class DetailedTagDialogFragment extends DialogFragment {

    private static final String TAG = "DetailedTagDialogFragment";
    private static final String KEY_TAG = "Tag";
    private FragmentDetailedTagDialogBinding binding;
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
        binding = FragmentDetailedTagDialogBinding.inflate(inflater, container, false);
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
            Glide.with(getContext()).load(image.getUrl())
                    .circleCrop()
                    .into(binding.ivPhoto);
        }
        binding.tvName.setText(tag.getName());
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