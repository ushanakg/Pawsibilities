package com.example.pawsibilities.fragments;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.example.pawsibilities.R;

public class CircularRevealDialogFragment extends DialogFragment {

    private static final String TAG = "CircularRevealDialogFragment";
    private static final int animationLength = 400; // in milliseconds

    // takes in root view to animate and whether or not to also animate view background
    protected void setUpOnLayoutListener(final View viewToAnimate, final boolean animateEntireFragment) {
        viewToAnimate.addOnLayoutChangeListener(
                new View.OnLayoutChangeListener() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onLayoutChange(
                            View v,
                            int left,
                            int top,
                            int right,
                            int bottom,
                            int oldLeft,
                            int oldTop,
                            int oldRight,
                            int oldBottom) {
                        v.removeOnLayoutChangeListener(this);
                        if (animateEntireFragment) {
                            setUpForShowAnimation(viewToAnimate);
                        } else {
                            animateShowingFragment(viewToAnimate);
                        }
                    }
                });
    }

    // sets up dialog to animate entire fragment (not just contents)
    private void setUpForShowAnimation(final View root) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(root).setCancelable(false);

        Dialog dialog = getDialog();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
        animateShowingFragment(root);
    }

    // animation where view is revealed circularly from bottom right corner
    protected void animateShowingFragment(View viewToAnimate) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            // get the center for the clipping circle
            int cx = viewToAnimate.getWidth();
            int cy = viewToAnimate.getHeight();

            // get the final radius for the clipping circle
            float finalRadius = (float) Math.hypot(cx, cy);

            Animator anim =
                    ViewAnimationUtils.createCircularReveal(viewToAnimate, cx, cy, 0f, finalRadius);
            anim.setDuration(animationLength);
            anim.setInterpolator(new FastOutSlowInInterpolator());

            startColorAnimation(
                    viewToAnimate,
                    getResources().getColor(R.color.orange),
                    getResources().getColor(R.color.dusty_blue),
                    animationLength);
            // make the view visible and start the animation
            viewToAnimate.setVisibility(View.VISIBLE);
            anim.start();
        } else {
            viewToAnimate.setVisibility(View.VISIBLE);
        }
    }

    // animate expanding circle
    private void startColorAnimation(
            final View viewToAnimate, @ColorInt int startColor, @ColorInt int endColor, int duration) {
        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(startColor, endColor);
        anim.setEvaluator(new ArgbEvaluator());
        anim.addUpdateListener(
                new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        viewToAnimate.setBackgroundColor((Integer) valueAnimator.getAnimatedValue());
                    }
                });
        anim.setDuration(duration);
        anim.start();
    }
}
