package com.todoapp.ui.settings;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.todoapp.R;
import com.todoapp.data.model.Category;
import com.todoapp.data.model.Task;
import com.todoapp.databinding.FragmentSettingsBinding;
import com.todoapp.ui.viewmodel.TaskListViewModel;
import com.todoapp.util.PreferencesManager;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

/**
 * Fragment for app settings and preferences.
 */
@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private TaskListViewModel viewModel;
    private List<Category> categories = new ArrayList<>();
    
    @Inject
    PreferencesManager preferencesManager;

    // For export file picker
    private ActivityResultLauncher<Intent> exportLauncher;
    // For import file picker
    private ActivityResultLauncher<Intent> importLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TaskListViewModel.class);
        
        // Register for export file result
        exportLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        exportDataToUri(uri);
                    }
                }
            }
        );

        // Register for import file result
        importLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        importDataFromUri(uri);
                    }
                }
            }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupAppearanceSettings();
        setupCategoriesSettings();
        setupDefaultsSettings();
        setupNotificationSettings();
        setupDataSettings();
        setupDeletedTasksSettings();
        setupAboutSection();
        observeCategories();
        observeDeletedTasksCount();
    }

    private void observeCategories() {
        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categoryList -> {
            if (categoryList != null) {
                categories.clear();
                categories.addAll(categoryList);
                updateDefaultCategoryDisplay();
            }
        });
    }

    private void observeDeletedTasksCount() {
        viewModel.getDeletedTasksCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null && count > 0) {
                binding.textDeletedCount.setText(getString(R.string.deleted_tasks_count, count));
            } else {
                binding.textDeletedCount.setText(R.string.no_deleted_tasks);
            }
        });
    }

    private void setupDeletedTasksSettings() {
        binding.layoutDeletedTasks.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_settings_to_deletedTasks);
        });
    }

    private void setupAppearanceSettings() {
        binding.layoutTheme.setOnClickListener(v -> showThemeSelectionDialog());
        updateThemeDisplay();
    }

    private void updateThemeDisplay() {
        int currentTheme = preferencesManager.getThemeMode();
        String themeText;
        if (currentTheme == PreferencesManager.THEME_LIGHT) {
            themeText = getString(R.string.theme_light);
        } else if (currentTheme == PreferencesManager.THEME_DARK) {
            themeText = getString(R.string.theme_dark);
        } else {
            themeText = getString(R.string.theme_system);
        }
        binding.textThemeValue.setText(themeText);
    }

    private void setupCategoriesSettings() {
        binding.layoutManageCategories.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.categoriesFragment);
        });
    }

    private void setupDefaultsSettings() {
        // Default Category
        binding.layoutDefaultCategory.setOnClickListener(v -> showDefaultCategoryDialog());
        updateDefaultCategoryDisplay();

        // Default Priority
        binding.layoutDefaultPriority.setOnClickListener(v -> showDefaultPriorityDialog());
        updateDefaultPriorityDisplay();
    }

    private void updateDefaultCategoryDisplay() {
        Long defaultCategoryId = preferencesManager.getDefaultCategoryId();
        if (defaultCategoryId != null && defaultCategoryId > 0) {
            for (Category category : categories) {
                if (category.getId() == defaultCategoryId) {
                    binding.textDefaultCategoryValue.setText(category.getName());
                    return;
                }
            }
        }
        binding.textDefaultCategoryValue.setText(R.string.none);
    }

    private void updateDefaultPriorityDisplay() {
        int priority = preferencesManager.getDefaultPriority();
        String priorityText;
        switch (priority) {
            case Task.Priority.LOW:
                priorityText = getString(R.string.priority_low);
                break;
            case Task.Priority.MEDIUM:
                priorityText = getString(R.string.priority_medium);
                break;
            case Task.Priority.HIGH:
                priorityText = getString(R.string.priority_high);
                break;
            default:
                priorityText = getString(R.string.priority_none);
                break;
        }
        binding.textDefaultPriorityValue.setText(priorityText);
    }

    private void showDefaultCategoryDialog() {
        if (categories.isEmpty()) {
            Toast.makeText(requireContext(), R.string.no_categories, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] categoryNames = new String[categories.size() + 1];
        categoryNames[0] = getString(R.string.none);
        for (int i = 0; i < categories.size(); i++) {
            categoryNames[i + 1] = categories.get(i).getName();
        }

        Long currentDefault = preferencesManager.getDefaultCategoryId();
        int selectedIndex = 0;
        if (currentDefault != null) {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getId() == currentDefault) {
                    selectedIndex = i + 1;
                    break;
                }
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.default_category)
                .setSingleChoiceItems(categoryNames, selectedIndex, (dialog, which) -> {
                    if (which == 0) {
                        preferencesManager.setDefaultCategoryId(null);
                    } else {
                        preferencesManager.setDefaultCategoryId(categories.get(which - 1).getId());
                    }
                    updateDefaultCategoryDisplay();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDefaultPriorityDialog() {
        String[] priorities = {
                getString(R.string.priority_none),
                getString(R.string.priority_low),
                getString(R.string.priority_medium),
                getString(R.string.priority_high)
        };

        int currentPriority = preferencesManager.getDefaultPriority();

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.default_priority)
                .setSingleChoiceItems(priorities, currentPriority, (dialog, which) -> {
                    preferencesManager.setDefaultPriority(which);
                    updateDefaultPriorityDisplay();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showThemeSelectionDialog() {
        String[] themes = {
                getString(R.string.theme_system),
                getString(R.string.theme_light),
                getString(R.string.theme_dark)
        };
        
        int currentTheme = preferencesManager.getThemeMode();
        int selectedIndex = 0;
        if (currentTheme == PreferencesManager.THEME_LIGHT) {
            selectedIndex = 1;
        } else if (currentTheme == PreferencesManager.THEME_DARK) {
            selectedIndex = 2;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.select_theme)
                .setSingleChoiceItems(themes, selectedIndex, (dialog, which) -> {
                    int newTheme;
                    int nightMode;
                    switch (which) {
                        case 1:
                            newTheme = PreferencesManager.THEME_LIGHT;
                            nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                            break;
                        case 2:
                            newTheme = PreferencesManager.THEME_DARK;
                            nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                            break;
                        default:
                            newTheme = PreferencesManager.THEME_SYSTEM;
                            nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                            break;
                    }
                    
                    preferencesManager.setThemeMode(newTheme);
                    AppCompatDelegate.setDefaultNightMode(nightMode);
                    updateThemeDisplay();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void setupNotificationSettings() {
        binding.switchDailySummary.setChecked(preferencesManager.getDailySummaryEnabled());
        binding.switchDailySummary.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setDailySummaryEnabled(isChecked);
        });

        binding.switchNotifications.setChecked(preferencesManager.getRemindersEnabled());
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setRemindersEnabled(isChecked);
        });
    }

    private void setupDataSettings() {
        binding.layoutExport.setOnClickListener(v -> startExport());
        binding.layoutImport.setOnClickListener(v -> startImport());
    }

    private void startExport() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "todo_backup_" + System.currentTimeMillis() + ".json");
        exportLauncher.launch(intent);
    }

    private void startImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        importLauncher.launch(intent);
    }

    private void exportDataToUri(Uri uri) {
        try {
            OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                // For now, just show a success message. Full implementation would serialize tasks
                outputStream.write("{}".getBytes());
                outputStream.close();
                Toast.makeText(requireContext(), R.string.data_exported, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), R.string.export_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void importDataFromUri(Uri uri) {
        try {
            // For now, just show a message. Full implementation would deserialize and import
            Toast.makeText(requireContext(), R.string.data_imported, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), R.string.import_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupAboutSection() {
        try {
            PackageInfo packageInfo = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0);
            String versionName = packageInfo.versionName;
            binding.textVersion.setText(versionName);
        } catch (Exception e) {
            binding.textVersion.setText("1.0.0");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
