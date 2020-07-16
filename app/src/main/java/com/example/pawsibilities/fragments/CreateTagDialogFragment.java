package com.example.pawsibilities.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pawsibilities.R;
import com.example.pawsibilities.Tag;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditTagDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateTagDialogFragment extends DialogFragment {

    public static final String KEY_TAG = "Tag";
    private Tag tag;

    public CreateTagDialogFragment() {
        // Required empty public constructor
    }


    public static CreateTagDialogFragment newInstance(Tag tag) {
        CreateTagDialogFragment fragment = new CreateTagDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_TAG, tag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_tag, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            this.tag = getArguments().getParcelable(KEY_TAG);
        }

        // TODO fill in overlay's functionality
    }
}