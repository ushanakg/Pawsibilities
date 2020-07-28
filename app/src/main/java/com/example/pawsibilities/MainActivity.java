package com.example.pawsibilities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.animation.Animator;
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

        mainBinding.vpPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if (position == PagerAdapter.MAP) {
                    mainBinding.fabCreateTag.show();
                } else {
                    mainBinding.fabCreateTag.hide();
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        mainBinding.fabCreateTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabButtonClickListener.onFabClicked();
            }
        });
    }

    public void setListener(FabButtonClickListener listener){
        fabButtonClickListener = listener;
    }

    // listener interface for floating action button click
    public interface FabButtonClickListener {
        void onFabClicked();
    }
}