package com.akhila.amaps;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.LatLng;


public class MainActivity extends AppCompatActivity  {

    private static final String FAB_VISIBLE_KEY = "restart_fab_visible";
    private LatLng org,dest;
    private NavFrag navfrag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_AppCompat_Light_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent=getIntent();
       org=new LatLng(intent.getDoubleExtra("orglat",0.0),intent.getDoubleExtra("orglang",0.0));
        dest=new LatLng(intent.getDoubleExtra("destlat",0.0),intent.getDoubleExtra("destlang",0.0));
        Toast.makeText(getApplicationContext(),String.valueOf(org.getLatitude()),Toast.LENGTH_SHORT).show();
        navfrag=new NavFrag();
        Bundle bundle = new Bundle();
        bundle.putDouble("orglat",intent.getDoubleExtra("orglat",0.0));
        bundle.putDouble("destlat",intent.getDoubleExtra("destlat",0.0));
        bundle.putDouble("orglang",intent.getDoubleExtra("orglang",0.0));
        bundle.putDouble("destlang",intent.getDoubleExtra("destlang",0.0));
        navfrag.setArguments(bundle);

        initializeNavigationViewFragment(savedInstanceState);




    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean isVisible = savedInstanceState.getBoolean(FAB_VISIBLE_KEY);
        int visibility = isVisible ? View.VISIBLE : View.INVISIBLE;

    }
    public void showNavigationFab() {

    }

    public void showPlaceholderFragment() {
        replaceFragment(new PlaceholderFragment());
    }

    private void initializeNavigationViewFragment(@Nullable Bundle savedInstanceState) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.disallowAddToBackStack();
            transaction.add(R.id.navigation_fragment_frame, navfrag).commit();
        }
    }

    private void replaceFragment(Fragment newFragment) {
        String tag = String.valueOf(newFragment.getId());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.disallowAddToBackStack();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        int fadeInAnimId = android.support.v7.appcompat.R.anim.abc_fade_in;
        int fadeOutAnimId = android.support.v7.appcompat.R.anim.abc_fade_out;
        transaction.setCustomAnimations(fadeInAnimId, fadeOutAnimId, fadeInAnimId, fadeOutAnimId);
        transaction.replace(R.id.navigation_fragment_frame, newFragment, tag);
        transaction.commit();
    }

}
