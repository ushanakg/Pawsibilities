package com.example.pawsibilities.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.pawsibilities.LoginActivity;
import com.example.pawsibilities.R;
import com.example.pawsibilities.databinding.FragmentProfileBinding;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment {

    private static final String KEY_PROFILE = "profile";
    private static final String TAG = "ProfileFragment";
    private ParseUser currentUser;
    private FragmentProfileBinding profileBinding;
    private final static int PICK_PHOTO_CODE = 1046;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        profileBinding = FragmentProfileBinding.inflate(inflater, container, false);
        return profileBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = ParseUser.getCurrentUser();

        // display user info
        profileBinding.tvUsername.setText(currentUser.getUsername());
        ParseFile profile = currentUser.getParseFile(KEY_PROFILE);
        if (profile != null) {
            Glide.with(getContext()).load(profile.getUrl()).circleCrop().into(profileBinding.ivProfile);
        }

        profileBinding.ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPickPhoto(view);
            }
        });

        // log out button
        profileBinding.btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser.logOut();

                // send user back to log in screen
                Intent i = new Intent(getContext(), LoginActivity.class);
                startActivity(i);
                getActivity().finish();
            }
        });
    }

    // Trigger gallery selection for a photo
    private void onPickPhoto(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();

            // set photo in image view
            Glide.with(getContext()).load(photoUri).circleCrop().into(profileBinding.ivProfile);

            Bitmap bmp = loadFromUri(photoUri);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 50, stream);
            byte[] byteArray = stream.toByteArray();

            // save photo to user in Parse database
            final ParseFile pf = new ParseFile("profile_pic.jpg", byteArray);
            pf.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        saveUserProfile(pf);
                    }
                }
            });
        }
    }

    private void saveUserProfile(ParseFile parseFile) {
        currentUser.put(KEY_PROFILE, parseFile);
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "saving user profile photo failed", e);
                } else {
                    Toast.makeText(getContext(), "Updated!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            if(Build.VERSION.SDK_INT > 27){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
}




