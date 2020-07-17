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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.pawsibilities.R;
import com.example.pawsibilities.Tag;
import com.example.pawsibilities.databinding.FragmentEditTagBinding;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.SaveCallback;

public class EditTagDialogFragment extends DialogFragment {

    private static final String TAG = "EditTagDialogFragment";
    public static final String KEY_TAG = "Tag";
    public static final String KEY_Location = "Location";
    private FragmentEditTagBinding binding;
    private Tag tag;
    private ParseGeoPoint userLocation;

    public EditTagDialogFragment() {
        // Required empty public constructor
    }


    public static EditTagDialogFragment newInstance(Tag tag, Location currentLocation) {
        EditTagDialogFragment fragment = new EditTagDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_TAG, tag);
        args.putParcelable(KEY_Location, currentLocation);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEditTagBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            this.tag = getArguments().getParcelable(KEY_TAG);
            Location l = (Location) getArguments().get(KEY_Location);
            this.userLocation = new ParseGeoPoint(l.getLatitude(), l.getLongitude());
        }

        ParseFile profile = tag.getPhoto();
        if (profile != null) {
            Glide.with(getContext()).load(profile.getUrl())
                    .transform(new RoundedCorners(150))
                    .into(binding.ivPhoto);
        }

        binding.tvName.setText(tag.getName());
        binding.tvTimeAgo.setText("Updated " + tag.getRelativeTimeAgo());
        binding.tvDistance.setText(tag.distanceFrom(userLocation) + " miles away");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.directions_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spDirection.setAdapter(adapter);
        binding.spDirection.setSelection(adapter.getPosition(tag.getDirection()));

        binding.tvActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tag.setActive(false);
                tag.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Toast.makeText(getContext(), "Unable to update", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Unable to mark tag outdated", e);
                        } else {
                            Toast.makeText(getContext(), "Updated!", Toast.LENGTH_SHORT).show();
                            dismiss();
                        }
                    }
                });
            }
        });

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tag.setName(binding.tvName.getText().toString());
                tag.setDirection(binding.spDirection.getSelectedItem().toString());
                tag.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Toast.makeText(getContext(), "Unable to update", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Unable to mark tag outdated", e);
                        } else {
                            Toast.makeText(getContext(), "Updated!", Toast.LENGTH_SHORT).show();
                            dismiss();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}