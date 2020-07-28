package com.example.pawsibilities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;

import com.example.pawsibilities.databinding.ActivityMainBinding;
import com.example.pawsibilities.fragments.MapsFragment;
import com.example.pawsibilities.fragments.ProfileFragment;
import com.example.pawsibilities.fragments.TagListFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mainBinding;
    private FragmentManager fragmentManager;
    private PagerAdapter adapter;
    private FabButtonClickListener fabButtonClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        fragmentManager = getSupportFragmentManager();

        adapter = new PagerAdapter(fragmentManager);
        mainBinding.vpPager.setAdapter(adapter);

        mainBinding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.action_profile:
                        mainBinding.vpPager.setCurrentItem(PagerAdapter.PROFILE);
                        break;
                    case R.id.action_tag_list:
                        mainBinding.vpPager.setCurrentItem(PagerAdapter.TAG_LIST);
                        break;
                    case R.id.action_map:
                    default:
                        mainBinding.vpPager.setCurrentItem(PagerAdapter.MAP);
                        break;
                }
                return true;
            }
        });

        // fab animation called when page transitions detected
        final ViewPager.OnPageChangeListener lstnr = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == PagerAdapter.MAP) {
                    fabEnterReveal();
                } else {
                    fabExitReveal();
                }
            }
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageScrollStateChanged(int state) {}
        };
        mainBinding.vpPager.addOnPageChangeListener(lstnr);

        // fab click listener
        mainBinding.fabCreateTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabButtonClickListener.onFabClicked();
            }
        });

        // set first page to map and ensure associated animations occur
        mainBinding.vpPager.setCurrentItem(PagerAdapter.MAP);
        mainBinding.vpPager.post(new Runnable() { // use runnable to make sure page is instantiated first
            @Override
            public void run() { lstnr.onPageSelected(PagerAdapter.MAP); }
        });
    }

    public void setListener(FabButtonClickListener listener){
        fabButtonClickListener = listener;
    }

    private void fabEnterReveal() {
        // get the center for the clipping circle
        int cx = mainBinding.fabCreateTag.getMeasuredWidth() / 2;
        int cy = mainBinding.fabCreateTag.getMeasuredHeight() / 2;

        int finalRadius = mainBinding.fabCreateTag.getWidth() / 2;
        Animator anim = ViewAnimationUtils.createCircularReveal(mainBinding.fabCreateTag, cx, cy, 0, finalRadius);

        // make the view visible and start the animation
        mainBinding.fabCreateTag.setVisibility(View.VISIBLE);
        anim.start();
    }

    private void fabExitReveal() {
        // get the center for the clipping circle
        int cx = mainBinding.fabCreateTag.getMeasuredWidth() / 2;
        int cy = mainBinding.fabCreateTag.getMeasuredHeight() / 2;

        int initialRadius = mainBinding.fabCreateTag.getWidth() / 2;
        Animator anim = ViewAnimationUtils.createCircularReveal(mainBinding.fabCreateTag, cx, cy, initialRadius, 0);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mainBinding.fabCreateTag.setVisibility(View.INVISIBLE);
            }
        });
        anim.start();
    }

    // listener interface for floating action button click
    public interface FabButtonClickListener {
        void onFabClicked();
    }
}