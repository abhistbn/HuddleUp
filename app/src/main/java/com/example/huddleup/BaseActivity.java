package com.example.huddleup;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public abstract class BaseActivity extends AppCompatActivity {

    protected ChipNavigationBar bottomNav;

    protected abstract int getNavigationMenuItemId();

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        setupBottomNavigationBar();
    }

    protected void setupBottomNavigationBar() {
        bottomNav = findViewById(R.id.bottom_nav_chip);
        if (bottomNav == null) {
            return;
        }

        bottomNav.setItemSelected(getNavigationMenuItemId(), true);

        bottomNav.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int id) {
                if (id != getNavigationMenuItemId()) {
                    Intent intent = null;
                    if (id == R.id.nav_events) {
                        intent = new Intent(BaseActivity.this, N_EventKu.class);
                    } else if (id == R.id.nav_manage) {
                        intent = new Intent(BaseActivity.this, Z_MainActivity.class);
                    } else if (id == R.id.nav_favorites) {
                        intent = new Intent(BaseActivity.this, D_WishlistActivity.class);
                    } else if (id == R.id.nav_notifications) {
                        intent = new Intent(BaseActivity.this, J_NotificationActivity.class);
                    }

                    if (intent != null) {
                        // Menggunakan kombinasi flag yang lebih andal
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        // Memberikan animasi transisi yang mulus
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                }
            }
        });
    }
}