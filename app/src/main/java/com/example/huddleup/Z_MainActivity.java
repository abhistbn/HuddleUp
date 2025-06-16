package com.example.huddleup;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class Z_MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.z_activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new Z_EventListFragment())
                    .commit();
        }
    }

    @Override
    protected int getNavigationMenuItemId() {
        return R.id.nav_manage;
    }
    public void showEventListFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new Z_EventListFragment())
                .commit();
    }

    public void showEventFormFragment(@Nullable Z_EventP2 event) {
        Z_EventFormFragment formFragment = new Z_EventFormFragment();
        if (event != null) {
            Bundle args = new Bundle();
            args.putString("EVENT_ID", event.getId());
            args.putString("EVENT_NAME", event.getName());
            args.putString("EVENT_IMAGE_URL", event.getImageUrl());
            args.putString("EVENT_DATE", event.getDate());
            args.putString("EVENT_TIME", event.getTime());
            args.putString("EVENT_LOCATION", event.getLocation());
            args.putString("EVENT_ABOUT", event.getAbout());
            formFragment.setArguments(args);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, formFragment)
                .addToBackStack(null)
                .commit();
    }
}