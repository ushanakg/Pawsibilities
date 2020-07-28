package com.example.pawsibilities.fragments;

import android.location.Location;
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
import com.example.pawsibilities.databinding.FragmentEditTagBinding;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;

public class EditTagDialogFragment extends CircularRevealDialogFragment {

    private static final String TAG = "EditTagDialogFragment";
    private static final String KEY_TAG = "Tag";
    private FragmentEditTagBinding binding;
    private Tag tag;
    private ParseGeoPoint userLocation;

    public EditTagDialogFragment() {
        // Required empty public constructor
    }


    public static EditTagDialogFragment newInstance(Tag tag) {
        EditTagDialogFragment fragment = new EditTagDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_TAG, tag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEditTagBinding.inflate(inflater, container, false);
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
            Glide.with(getContext()).load(image.getUrl())
                    .circleCrop()
                    .into(binding.ivPhoto);
        }
        binding.tvName.setText(tag.getName());
        binding.tvTimeAgo.setText("Updated " + tag.getRelativeTimeAgo());

        // dropdown for location
        ArrayList<String> lst = new ArrayList<>();
        lst.add("My location");
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>
                (getContext(), android.R.layout.simple_spinner_item, lst);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spDistance.setAdapter(locationAdapter);
        locationAdapter.add(tag.distanceFrom(userLocation) + " mi away");
        locationAdapter.notifyDataSetChanged();
        binding.spDistance.setSelection(1);

        // dropdown for directions
        ArrayAdapter<CharSequence> directionAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.directions_array, android.R.layout.simple_spinner_item);
        directionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spDirection.setAdapter(directionAdapter);
        binding.spDirection.setSelection(directionAdapter.getPosition(tag.getDirection()));

        binding.tvActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tag.setActive(false);
                sendBackResult();
            }
        });

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tag.setName(binding.tvName.getText().toString());
                tag.setDirection(binding.spDirection.getSelectedItem().toString());
                if (binding.spDistance.getSelectedItem().equals("My location")) {
                    tag.setLocation(userLocation);
                }
                sendBackResult();
            }
        });
    }

    public void sendBackResult() {
        EditTagDialogFragment.EditTagDialogListener listener = (EditTagDialogFragment.EditTagDialogListener) getTargetFragment();
        listener.onFinishEditDialog(tag);
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Defines the listener interface
    public interface EditTagDialogListener {
        void onFinishEditDialog(Tag newTag);
    }
}