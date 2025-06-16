package com.example.huddleup;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class Z_MainActivity extends BaseActivity {

    private ChipNavigationBar bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.z_activity_main);

        bottomNav = findViewById(R.id.bottom_nav_chip);

        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, windowInsets) -> {
            int bottomInset = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            v.setPadding(0, 0, 0, bottomInset);
            return windowInsets;
        });

        if (savedInstanceState == null) {

            bottomNav.setItemSelected(R.id.nav_manage, true);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new Z_EventListFragment()).commit();

        }

        bottomNav.setOnItemSelectedListener(id -> {
            boolean isSameActivity = false;
            Intent intent = null;

            if (id == R.id.nav_events) {
                intent = new Intent(Z_MainActivity.this, N_EventKu.class);
            } else if (id == R.id.nav_manage) {
                isSameActivity = true;
                showEventListFragment();
            } else if (id == R.id.nav_favorites) {
                intent = new Intent(Z_MainActivity.this, D_WishlistActivity.class);
            } else if (id == R.id.nav_notifications) {
                intent = new Intent(Z_MainActivity.this, J_NotificationActivity.class);
            }

            if (!isSameActivity && intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
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