package com.todoapp.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.todoapp.R;
import com.todoapp.data.model.Task;
import com.todoapp.databinding.FragmentDeletedTasksBinding;
import com.todoapp.ui.viewmodel.TaskListViewModel;
import com.todoapp.util.DateUtils;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment to display and manage deleted tasks (trash).
 */
@AndroidEntryPoint
public class DeletedTasksFragment extends Fragment implements DeletedTaskAdapter.DeletedTaskClickListener {

    private FragmentDeletedTasksBinding binding;
    private TaskListViewModel viewModel;
    private DeletedTaskAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TaskListViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDeletedTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViews();
        setupMenu();
        observeDeletedTasks();
    }

    private void setupViews() {
        adapter = new DeletedTaskAdapter(this);
        binding.recyclerDeletedTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerDeletedTasks.setAdapter(adapter);
    }

    private void setupMenu() {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_deleted_tasks, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_empty_trash) {
                    showEmptyTrashConfirmation();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void observeDeletedTasks() {
        viewModel.getDeletedTasks().observe(getViewLifecycleOwner(), this::displayDeletedTasks);
    }

    private void displayDeletedTasks(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.recyclerDeletedTasks.setVisibility(View.GONE);
        } else {
            binding.emptyState.setVisibility(View.GONE);
            binding.recyclerDeletedTasks.setVisibility(View.VISIBLE);
            adapter.submitList(tasks);
        }
    }

    @Override
    public void onRestoreClick(Task task) {
        viewModel.restoreTask(task.getId());
        Toast.makeText(requireContext(), R.string.task_restored, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeletePermanentlyClick(Task task) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_permanently)
                .setMessage(getString(R.string.delete_task_confirmation))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.permanentlyDeleteTask(task.getId());
                    Toast.makeText(requireContext(), R.string.task_permanently_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showEmptyTrashConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.empty_trash)
                .setMessage(R.string.empty_trash_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.emptyTrash();
                    Toast.makeText(requireContext(), R.string.trash_emptied, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
