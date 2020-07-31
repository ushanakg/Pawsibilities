package com.example.pawsibilities.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.pawsibilities.R;
import com.example.pawsibilities.Tag;
import com.example.pawsibilities.databinding.FragmentCreateTagBinding;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;

import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;

public class CreateTagDialogFragment extends CircularRevealDialogFragment {

    private static final String TAG = "CreateTagDialogFragment";
    private static final String KEY_TAG = "Tag";
    private FragmentCreateTagBinding binding;
    private ParseGeoPoint userLocation;
    private Tag tag;

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 23;
    private File photoFile;
    private static final String photoFileName = "createphoto.jpg";

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
        binding = FragmentCreateTagBinding.inflate(inflater, container, false);
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

        binding.cvPhotoWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });

        if (userLocation.equals(tag.getLocation())) {
            binding.tvDistance.setText("Your current location");
        } else {
            binding.tvDistance.setText(tag.distanceFrom(userLocation) + " miles away");
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.directions_array, R.layout.direction_spinner_item);
        adapter.setDropDownViewResource(R.layout.direction_spinner_item);
        binding.spDirection.setAdapter(adapter);
        binding.spDirection.setSelection(0);

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (photoFile == null) {
                    Toasty.warning(getContext(), "Photo isn't provided", Toasty.LENGTH_SHORT).show();
                    return;
                }

                tag.setPhoto(new ParseFile(photoFile));
                String name = binding.etName.getText().toString();
                if (name.isEmpty()) {
                    name = "Unnamed";
                }
                tag.setName(name);
                tag.setDirection((String) binding.spDirection.getSelectedItem());

                sendBackResult();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap takenImage = rotateBitmapOrientation(photoFile.getAbsolutePath());

                Glide.with(getContext())
                        .load(takenImage)
                        .transform(new CenterCrop(), new RoundedCorners(100))
                        .into(binding.ivPhoto);
            } else {
                Toasty.warning(getContext(), "No photo taken", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public static File getPhotoFileUri(Context context, String fileName) {
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        File mediaStorageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = CreateTagDialogFragment.getPhotoFileUri(getContext(), photoFileName);

        // wrap File object into a content provider
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    public static Bitmap rotateBitmapOrientation(String photoFilePath) {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        // Read EXIF Data
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        // Rotate Bitmap
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        // Return result
        return rotatedBitmap;
    }

    public void sendBackResult() {
        CreateTagDialogListener listener = (CreateTagDialogListener) getTargetFragment();
        listener.onFinishCreateDialog(tag);
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Defines the listener interface
    public interface CreateTagDialogListener {
        void onFinishCreateDialog(Tag newTag);
    }
}