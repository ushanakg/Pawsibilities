package com.example.pawsibilities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pawsibilities.databinding.ActivitySignupBinding;
import com.example.pawsibilities.fragments.ProfileFragment;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import es.dmoral.toasty.Toasty;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = SignupActivity.class.getSimpleName();

    private ActivitySignupBinding signupBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        signupBinding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(signupBinding.getRoot());

        signupBinding.etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

        signupBinding.btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = signupBinding.etUsername.getText().toString();
                String password = signupBinding.etPassword.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    Toasty.warning(SignupActivity.this, "Invalid username/password", Toast.LENGTH_SHORT).show();
                    return;
                }
                signupUser(username, password);
            }
        });

        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void signupUser(final String username, final String password) {
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.put(ProfileFragment.NUM_TAGS_DROPPED, 0);

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Sign up failed", e);
                    Toasty.error(SignupActivity.this, "Sign up failed!", Toast.LENGTH_SHORT).show();
                } else {

                    // go back to login and sign user in
                    Intent i = new Intent();
                    i.putExtra("username", username);
                    i.putExtra("password", password);
                    setResult(RESULT_OK, i);
                    finish();
                }
            }
        });
    }
}