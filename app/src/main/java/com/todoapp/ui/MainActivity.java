package com.todoapp.ui;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.todoapp.R;
import com.todoapp.databinding.ActivityMainBinding;
import com.todoapp.ui.notifications.NotificationsDialogFragment;
import com.todoapp.ui.profile.ProfileDialogFragment;
import com.todoapp.util.PreferencesManager;

import java.io.File;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Main activity hosting the navigation component and bottom navigation.
 * Uses single-activity architecture with multiple fragments.
 */
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject
    PreferencesManager preferencesManager;

    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before super.onCreate (read directly from SharedPreferences)
        applyThemeFromPrefs();
        
        super.onCreate(savedInstanceState);
        
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setupNavigation();
        setupFab();
    }

    private void applyThemeFromPrefs() {
        // Read directly from SharedPreferences since Hilt injection hasn't happened yet
        SharedPreferences prefs = getSharedPreferences("todo_app_prefs", MODE_PRIVATE);
        int themeMode = prefs.getInt("theme_mode", 0);
        switch (themeMode) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
    }

    private void setupNavigation() {
        // Set up toolbar
        setSupportActionBar(binding.toolbar);
        
        // Get NavController from NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            
            // Define top-level destinations (no back button)
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.taskListFragment,
                    R.id.calendarFragment,
                    R.id.analyticsFragment,
                    R.id.settingsFragment
            ).build();
            
            // Set up action bar with NavController
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            
            // Set up bottom navigation
            BottomNavigationView bottomNav = binding.bottomNavigation;
            NavigationUI.setupWithNavController(bottomNav, navController);
            
            // Listen for destination changes to show/hide FAB and bottom nav
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                handleDestinationChange(destination);
            });
        }
    }

    private void handleDestinationChange(NavDestination destination) {
        int destinationId = destination.getId();
        
        // Update toolbar title based on destination
        updateToolbarTitle(destinationId);
        
        // Refresh menu when destination changes
        invalidateOptionsMenu();
        
        // Show/hide FAB based on destination
        if (destinationId == R.id.taskListFragment) {
            binding.fabAddTask.show();
            binding.bottomNavigation.setVisibility(View.VISIBLE);
        } else if (destinationId == R.id.addEditTaskFragment || 
                   destinationId == R.id.taskDetailFragment) {
            binding.fabAddTask.hide();
            binding.bottomNavigation.setVisibility(View.GONE);
        } else if (destinationId == R.id.calendarFragment ||
                   destinationId == R.id.categoriesFragment ||
                   destinationId == R.id.analyticsFragment ||
                   destinationId == R.id.settingsFragment) {
            binding.fabAddTask.hide();
            binding.bottomNavigation.setVisibility(View.VISIBLE);
        }
        
        // Update toolbar visibility
        binding.appBarLayout.setVisibility(View.VISIBLE);
    }

    private void updateToolbarTitle(int destinationId) {
        String title;
        if (destinationId == R.id.taskListFragment) {
            title = getString(R.string.tasks);
        } else if (destinationId == R.id.calendarFragment) {
            title = getString(R.string.calendar);
        } else if (destinationId == R.id.analyticsFragment) {
            title = getString(R.string.analytics);
        } else if (destinationId == R.id.settingsFragment) {
            title = getString(R.string.settings);
        } else if (destinationId == R.id.addEditTaskFragment) {
            title = getString(R.string.add_task);
        } else if (destinationId == R.id.taskDetailFragment) {
            title = getString(R.string.tasks);
        } else if (destinationId == R.id.categoriesFragment) {
            title = getString(R.string.categories);
        } else if (destinationId == R.id.deletedTasksFragment) {
            title = getString(R.string.deleted_tasks);
        } else {
            title = getString(R.string.app_name);
        }
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private void setupFab() {
        binding.fabAddTask.setOnClickListener(v -> {
            // Navigate to add task fragment using global action
            navController.navigate(R.id.action_global_addEditTask);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.optionsMenu = menu;
        updateProfileIcon();
        return true;
    }
    
    private void updateProfileIcon() {
        if (optionsMenu == null) return;
        
        MenuItem profileItem = optionsMenu.findItem(R.id.action_profile);
        if (profileItem == null) return;
        
        // Check for local profile image first
        String localImagePath = preferencesManager.getLocalProfileImagePath();
        if (!TextUtils.isEmpty(localImagePath)) {
            File imageFile = new File(localImagePath);
            if (imageFile.exists()) {
                loadProfileIconFromFile(profileItem, imageFile);
                return;
            }
        }
        
        // Check for Google profile photo
        String googlePhotoUrl = preferencesManager.getUserPhotoUrl();
        if (!TextUtils.isEmpty(googlePhotoUrl)) {
            loadProfileIconFromUrl(profileItem, googlePhotoUrl);
            return;
        }
        
        // Use default icon
        profileItem.setIcon(R.drawable.ic_profile);
    }
    
    private void loadProfileIconFromFile(MenuItem profileItem, File imageFile) {
        Glide.with(this)
            .asBitmap()
            .load(imageFile)
            .signature(new ObjectKey(imageFile.lastModified()))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .circleCrop()
            .into(new CustomTarget<Bitmap>(96, 96) {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                    drawable.setCircular(true);
                    profileItem.setIcon(drawable);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    profileItem.setIcon(R.drawable.ic_profile);
                }
            });
    }
    
    private void loadProfileIconFromUrl(MenuItem profileItem, String url) {
        Glide.with(this)
            .asBitmap()
            .load(url)
            .circleCrop()
            .into(new CustomTarget<Bitmap>(96, 96) {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                    drawable.setCircular(true);
                    profileItem.setIcon(drawable);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    profileItem.setIcon(R.drawable.ic_profile);
                }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update profile icon when returning to activity (e.g., after sign-in)
        updateProfileIcon();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_search) {
            // Handle search - dispatch to current fragment if it's TaskListFragment
            return false; // Let fragment handle it
        } else if (id == R.id.action_notifications) {
            showNotificationsDialog();
            return true;
        } else if (id == R.id.action_profile) {
            showProfileDialog();
            return true;
        } else if (id == R.id.action_sort) {
            // Let fragment handle it
            return false;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showProfileDialog() {
        ProfileDialogFragment profileDialog = ProfileDialogFragment.newInstance();
        profileDialog.setOnDismissListener(() -> updateProfileIcon());
        profileDialog.show(getSupportFragmentManager(), "profile_dialog");
    }

    private void showNotificationsDialog() {
        NotificationsDialogFragment notificationsDialog = NotificationsDialogFragment.newInstance();
        notificationsDialog.show(getSupportFragmentManager(), "notifications_dialog");
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * Get the NavController for use by fragments
     */
    public NavController getNavController() {
        return navController;
    }

    /**
     * Show or hide the FAB programmatically
     */
    public void setFabVisible(boolean visible) {
        if (visible) {
            binding.fabAddTask.show();
        } else {
            binding.fabAddTask.hide();
        }
    }

    /**
     * Show or hide the bottom navigation
     */
    public void setBottomNavVisible(boolean visible) {
        binding.bottomNavigation.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
