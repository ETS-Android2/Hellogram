package com.example.hellogram;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.example.hellogram.Fragments.HomeFragment;
import com.example.hellogram.Fragments.NotificationFragment;
import com.example.hellogram.Fragments.ProfileFragment;
import com.example.hellogram.Fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Fragment selectorFragment;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {

            switch(item.getItemId()){
                case R.id.nav_home:
                    selectorFragment = new HomeFragment();
                    break;

                case R.id.nav_search:
                    selectorFragment = new SearchFragment();
                    break;

                case R.id.nav_add:
                    selectorFragment = null;
                    startActivity(new Intent(MainActivity.this, PostActivity.class));
                    break;

                case R.id.nav_heart:
                    selectorFragment = new NotificationFragment();
                    break;

                case R.id.nav_profile:
                    selectorFragment = new ProfileFragment();
                    break;
            }

            if (selectorFragment != null){
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectorFragment).commit();
            }

            return true;
        });

        Bundle intent = getIntent().getExtras();
        if (intent != null){
            String profileId = intent.getString("publisherId");

            getSharedPreferences("PROFILE", MODE_PRIVATE).edit().putString("profileId", profileId).apply();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        }
        else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }
    }
}
