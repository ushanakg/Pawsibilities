package com.example.pawsibilities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.pawsibilities.databinding.ActivityMainBinding;
import com.example.pawsibilities.fragments.MapsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mainBinding;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        fragmentManager = getSupportFragmentManager();

        mainBinding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;

                switch(item.getItemId()) {
                    case R.id.action_profile:
                        //TODO: replace this with profile fragment
                        fragment = new MapsFragment();
                        break;
                    case R.id.action_map:
                    default:
                        fragment = new MapsFragment();
                        break;
                }
                fragmentManager.beginTransaction().replace(mainBinding.flContainer.getId(), fragment, "MainActivity").commit();
                return true;
            }
        });

        mainBinding.bottomNavigation.setSelectedItemId(R.id.action_map);

    }

}