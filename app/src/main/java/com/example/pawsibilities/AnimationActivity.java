package com.example.pawsibilities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;

import com.example.pawsibilities.databinding.ActivityAnimationBinding;
import com.parse.ParseUser;

public class AnimationActivity extends AppCompatActivity {

    private ActivityAnimationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnimationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setElevation(0);

            if (ParseUser.getCurrentUser() != null) {
                Intent i = new Intent(AnimationActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }

            binding.animationView.setImageAssetsFolder("res/lottie/");
            binding.animationView.loop(false);

            binding.animationView.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) { }

                @Override
                public void onAnimationEnd(Animator animator) {
                    goLoginActivity();
                }

                @Override
                public void onAnimationCancel(Animator animator) { }

                @Override
                public void onAnimationRepeat(Animator animator) { }
            });

            binding.animationView.playAnimation();
        } else {
            goLoginActivity();
        }
    }

    private void goLoginActivity() {
        Intent i = new Intent(AnimationActivity.this, LoginActivity.class);
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(AnimationActivity.this,
                android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        startActivity(i, bundle);
        finish();
    }
}