package com.myapplication;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.myapplication.fragments.HomeFragment;
import com.myapplication.fragments.MapFragment;
import com.myapplication.fragments.MapBottomFragment;
import com.myapplication.fragments.UserFragment;
import com.tmapmobility.tmap.tmapsdk.ui.fragment.NavigationFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setItemIconTintList(getResources().getColorStateList(R.color.navigation_item_icon_tint));
        bottomNavigationView.setItemTextColor(getResources().getColorStateList(R.color.navigation_item_color));

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                switch (item.getItemId()) {
                    case R.id.home:
                        selectedFragment = new HomeFragment();
                        break;
                    case R.id.map:
                        selectedFragment = new MapFragment();
                        break;
                    case R.id.user:
                        selectedFragment = new UserFragment();
                        break;
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }

                return true;
            }
        });

        // 기본 화면 설정
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    /**
     * 안내시작 버튼을 눌렀을 때 map_bottom 활성화
     */
    public void showMapBottomView() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // MapBottomFragment를 동적으로 추가
        MapBottomFragment mapBottomFragment = new MapBottomFragment();
        transaction.add(R.id.fragment_container, mapBottomFragment, "MapBottomFragment");
        transaction.commit();
    }
}
