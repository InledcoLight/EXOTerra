package com.inledco.exoterra.uvbbuddy;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseActivity;

public class UvbMainActivity extends BaseActivity {
    private BottomNavigationView uvbmain_bnv;

    private AnimalViewModel mAnimalViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
        initEvent();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_uvbmain;
    }

    @Override
    protected void initView() {
        uvbmain_bnv = findViewById(R.id.uvbmain_bnv);
    }

    @Override
    protected void initData() {
        UvbDatabase.loadDatabaseFromAssets(getAssets(), "animal_light_relations");

        replaceFragment(R.id.uvbmain_fl, new UvbAnimalsFragment());
        uvbmain_bnv.setSelectedItemId(R.id.uvbmain_bnv_dashboard);

        mAnimalViewModel = ViewModelProviders.of(this).get(AnimalViewModel.class);
    }

    @Override
    protected void initEvent() {
        uvbmain_bnv.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem menuItem) {

            }
        });
        uvbmain_bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.uvbmain_bnv_home:
                        finish();
                        break;
                    case R.id.uvbmain_bnv_dashboard:
                        replaceFragment(R.id.uvbmain_fl, new UvbAnimalsFragment());
                        break;
                    case R.id.uvbmain_bnv_pref:
                        break;
                }
                return true;
            }
        });
    }
}
