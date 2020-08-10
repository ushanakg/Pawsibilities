package com.example.pawsibilities.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.pawsibilities.LoginActivity;
import com.example.pawsibilities.R;
import com.example.pawsibilities.Tag;
import com.example.pawsibilities.databinding.FragmentProfileBinding;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    public static final String KEY_PROFILE = "profile";
    private static final String TAG = "ProfileFragment";
    private GestureDetector detector;
    private ParseUser user;
    private FragmentProfileBinding profileBinding;

    private final static int PICK_PHOTO_CODE = 1046;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 23;
    private File photoFile;
    private static final String photoFileName = "profile_pic.jpg";

    public final static String NUM_TAGS_DROPPED = "numTagsDropped";
    public final static String KEY_RADIUS = "radius";

    private TextWatcher watcher;

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

        detector = new GestureDetector(getContext(), new GestureListener());
        user = ParseUser.getCurrentUser();

        watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().isEmpty()) {
                    user.put(KEY_RADIUS, 0);
                } else {
                    user.put(KEY_RADIUS, Double.parseDouble(editable.toString()));
                }
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "saving radius failed", e);
                            Toasty.error(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
            }
        };

        queryUser();

        profileBinding.tvUsername.setText(user.getUsername());
        ParseFile profile = user.getParseFile(KEY_PROFILE);
        if (profile != null) {
            Glide.with(getContext())
                    .load(profile.getUrl())
                    .transform(new CenterCrop(), new RoundedCorners(150))
                    .into(profileBinding.ivProfile);
        }

        profileBinding.ivProfile.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return detector.onTouchEvent(motionEvent); //custom gesture listener will handle event
            }
        });

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

        queryTag();
    }


    private void queryUser() {
        try {
            user = user.fetchIfNeeded();
            profileBinding.tvLocation.setText(parseGeoPointToDMS(user.getParseGeoPoint(MapsFragment.KEY_LOCATION)));
            profileBinding.etRadius.setText(user.getDouble(KEY_RADIUS) + "");
            profileBinding.tvNumTagsDropped.setText(Integer.toString(user.getInt(NUM_TAGS_DROPPED)));
            profileBinding.etRadius.addTextChangedListener(watcher);

        } catch (ParseException e) {
            Toasty.warning(getContext(), "Data is not up to date", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "user not updated", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        queryUser();
    }

    private String parseGeoPointToDMS(ParseGeoPoint p) {
        String latitude = decimalToDMS(p.getLatitude());
        String longitude = decimalToDMS(p.getLongitude());
        return String.format("%s N, %s W", latitude, longitude);
    }

    // converts longitude/latitude from decimal format to Degrees, Minutes, Seconds
    private String decimalToDMS(double d) {
        d = Math.abs(d);
        int degree = (int) d;
        d = (d - degree) * 60;
        int minute = (int) d;
        d = (d - minute) * 60;
        int second = (int) d;
        return String.format("%d\u00B0 %d\u2032 %d\u2033", degree, minute, second);
    }

    // queries nearest tag and sets text in tvNearestTag
    private void queryTag() {
        ParseQuery<Tag> query = ParseQuery.getQuery(Tag.class);
        query.setLimit(1);
        query.whereNear(Tag.KEY_LOCATION, user.getParseGeoPoint(MapsFragment.KEY_LOCATION));
        query.whereEqualTo(Tag.KEY_ACTIVE, true);
        query.findInBackground(new FindCallback<Tag>() {
            @Override
            public void done(List<Tag> queried, ParseException e) {
                if (e != null) {
                    profileBinding.tvNearestTag.setText(getString(R.string.blank));
                    Log.e(TAG, "issue with getting Tags", e);
                    return;
                }
                profileBinding.tvNearestTag.setText(
                        queried.get(0).distanceFrom(user.getParseGeoPoint(MapsFragment.KEY_LOCATION))
                                + getString(R.string.mi));
            }
        });
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = CreateTagDialogFragment.getPhotoFileUri(getContext(), photoFileName);

        // wrap File object into a content provider
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
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

        Bitmap bmp = null;
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();
            bmp = loadFromUri(photoUri);
        }

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                bmp = CreateTagDialogFragment.rotateBitmapOrientation(photoFile.getAbsolutePath());
            } else {
                Toasty.warning(getContext(), "No photo taken", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (bmp != null) {
            bmp = bmp.copy(Bitmap.Config.ARGB_8888, true);
            // set image into profile pic view
            Glide.with(getContext())
                    .load(bmp)
                    .transform(new CenterCrop(), new RoundedCorners(100))
                    .into(profileBinding.ivProfile);

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
        } else {
            Toasty.warning(getContext(), "No photo selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserProfile(ParseFile parseFile) {
        user.put(KEY_PROFILE, parseFile);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "saving user profile photo failed", e);
                    Toasty.error(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toasty.success(getContext(), "Updated!", Toast.LENGTH_SHORT).show();
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

    // Gesture listener to handle single and double tap gestures
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            profileBinding.ivProfile.setColorFilter(Color.parseColor("#77000000"));
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            profileBinding.ivProfile.clearColorFilter();
            onPickPhoto(profileBinding.ivProfile);
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            profileBinding.ivProfile.clearColorFilter();
            // 0 on the first tap, 1 on the second, resets if enough time has passed
            // so that two spaced out taps do not count as a double tap
            if (e.getAction() == 1) {
                launchCamera();
            }
            return super.onDoubleTapEvent(e);
        }
    }
}




