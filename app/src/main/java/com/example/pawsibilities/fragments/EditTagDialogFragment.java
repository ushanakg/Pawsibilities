package com.example.pawsibilities.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.pawsibilities.R;
import com.example.pawsibilities.Tag;
import com.example.pawsibilities.databinding.FragmentEditTagBinding;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.ArrayList;

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
            Glide.with(getContext())
                    .load(image.getUrl())
                    .transform(new CenterCrop(), new RoundedCorners(100))
                    .into(binding.ivPhoto);
        }
        binding.tvName.setText(tag.getName());
        binding.tvName.setSelected(true);
        binding.tvTimeAgo.setText("Updated " + tag.getRelativeTimeAgo());

        // dropdown for location
        ArrayList<String> lst = new ArrayList<>();
        lst.add(getString(R.string.yourlocation));
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>
                (getContext(), R.layout.location_spinner_item, lst);
        locationAdapter.setDropDownViewResource(R.layout.location_spinner_item);
        binding.spDistance.setAdapter(locationAdapter);
        locationAdapter.add(tag.distanceFrom(userLocation) + getString(R.string.miaway));
        locationAdapter.notifyDataSetChanged();
        binding.spDistance.setSelection(1);

        // dropdown for directions
        ArrayAdapter<CharSequence> directionAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.directions_array, R.layout.direction_spinner_item);
        directionAdapter.setDropDownViewResource(R.layout.direction_spinner_item);
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
                tag.setDirection(binding.spDirection.getSelectedItem().toString());
                if (binding.spDistance.getSelectedItem().equals(getString(R.string.yourlocation))) {
                    tag.setLocation(userLocation);
                }
                sendBackResult();
            }
        });

        ParseUser user = tag.getDroppedBy();
        try {
            user.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ParseFile profile = user.getParseFile(ProfileFragment.KEY_PROFILE);
        if (profile != null) {
            Glide.with(getContext())
                    .load(profile.getUrl())
                    .circleCrop()
                    .into(binding.ivUserProfile);
        }
        binding.tvUsername.setText(user.getString("username"));
    }

    public void sendBackResult() {
        EditTagDialogFragment.EditTagDialogListener listener =
                (EditTagDialogFragment.EditTagDialogListener) getTargetFragment();
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