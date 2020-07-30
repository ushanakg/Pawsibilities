package com.example.pawsibilities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pawsibilities.databinding.ActivityLoginBinding;
import com.example.pawsibilities.databinding.ActivitySignupBinding;
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
                signupUser(username, password);
            }
        });

        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void signupUser(final String username, final String password) {
        // Create the ParseUser
        ParseUser user = new ParseUser();
        // Set core properties
        user.setUsername(username);
        user.setPassword(password);
        // Invoke signUpInBackground
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Sign up failed", e);
                    Toasty.error(SignupActivity.this, "Sign up failed!", Toast.LENGTH_SHORT).show();
                } else {
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