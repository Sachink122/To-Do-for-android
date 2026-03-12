package com.todoapp.ui.categories;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.todoapp.R;
import com.todoapp.data.model.Category;
import com.todoapp.databinding.FragmentCategoriesBinding;
import com.todoapp.ui.adapters.CategoryAdapter;
import com.todoapp.ui.adapters.ColorAdapter;
import com.todoapp.ui.viewmodel.CategoryViewModel;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment for managing task categories.
 */
@AndroidEntryPoint
public class CategoriesFragment extends Fragment implements CategoryAdapter.CategoryClickListener {

    private FragmentCategoriesBinding binding;
    private CategoryViewModel viewModel;
    private CategoryAdapter categoryAdapter;

    private static final String[] CATEGORY_COLORS = {
            "#F44336", "#E91E63", "#9C27B0", "#673AB7",
            "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
            "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
            "#FFEB3B", "#FFC107", "#FF9800", "#FF5722"
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoriesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupFab();
        observeViewModel();
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryAdapter(this);
        binding.recyclerCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerCategories.setAdapter(categoryAdapter);
    }

    private void setupFab() {
        binding.fabAddCategory.setOnClickListener(v -> showCategoryDialog(null));
    }

    private void observeViewModel() {
        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryAdapter.submitList(categories);
            updateEmptyState(categories == null || categories.isEmpty());
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerCategories.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showCategoryDialog(@Nullable Category existingCategory) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_category, null);

        EditText editName = dialogView.findViewById(R.id.edit_category_name);
        RecyclerView recyclerColors = dialogView.findViewById(R.id.recycler_colors);

        recyclerColors.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        final String[] selectedColor = {existingCategory != null ? existingCategory.getColor() : CATEGORY_COLORS[0]};

        ColorAdapter colorAdapter = new ColorAdapter(CATEGORY_COLORS, selectedColor[0], color -> {
            selectedColor[0] = color;
        });
        recyclerColors.setAdapter(colorAdapter);

        if (existingCategory != null) {
            editName.setText(existingCategory.getName());
        }

        String title = existingCategory != null ?
                getString(R.string.edit_category) : getString(R.string.add_category);
        String positiveButton = existingCategory != null ?
                getString(R.string.save) : getString(R.string.add);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(positiveButton, (d, which) -> {
                    String name = editName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.error_empty_title, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (existingCategory != null) {
                        existingCategory.setName(name);
                        existingCategory.setColor(selectedColor[0]);
                        viewModel.updateCategory(existingCategory);
                        Toast.makeText(requireContext(), R.string.category_updated, Toast.LENGTH_SHORT).show();
                    } else {
                        viewModel.createCategory(name, selectedColor[0]);
                        Toast.makeText(requireContext(), R.string.category_created, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        dialog.show();
        editName.requestFocus();
    }

    private void showDeleteConfirmation(Category category) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_category)
                .setMessage(getString(R.string.delete_category_confirmation, category.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteCategory(category);
                    Toast.makeText(requireContext(), R.string.category_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onCategoryClick(Category category) {
        showCategoryDialog(category);
    }

    @Override
    public void onCategoryDeleteClick(Category category) {
        showDeleteConfirmation(category);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
