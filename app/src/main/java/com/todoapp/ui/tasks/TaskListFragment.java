package com.todoapp.ui.tasks;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.todoapp.R;
import com.todoapp.data.model.Task;
import com.todoapp.databinding.FragmentTaskListBinding;
import com.todoapp.ui.adapters.TaskAdapter;
import com.todoapp.ui.viewmodel.TaskListViewModel;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment displaying the main task list with filtering and swipe actions.
 */
@AndroidEntryPoint
public class TaskListFragment extends Fragment implements TaskAdapter.TaskClickListener {

    private FragmentTaskListBinding binding;
    private TaskListViewModel viewModel;
    private TaskAdapter taskAdapter;
    private boolean isSearchVisible = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use activity-scoped ViewModel to share data with AnalyticsFragment
        viewModel = new ViewModelProvider(requireActivity()).get(TaskListViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTaskListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupSwipeRefresh();
        setupFilterChips();
        setupSearch();
        setupMenu();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh stats when fragment becomes visible
        viewModel.refreshStats();
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(this);
        binding.recyclerTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerTasks.setAdapter(taskAdapter);

        // Setup swipe actions
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeCallback());
        itemTouchHelper.attachToRecyclerView(binding.recyclerTasks);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            // Refresh is automatic with LiveData, just stop the indicator
            binding.swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setupFilterChips() {
        binding.chipAll.setOnClickListener(v -> viewModel.setFilter(TaskListViewModel.FilterType.ALL));
        binding.chipToday.setOnClickListener(v -> viewModel.setFilter(TaskListViewModel.FilterType.TODAY));
        binding.chipUpcoming.setOnClickListener(v -> viewModel.setFilter(TaskListViewModel.FilterType.THIS_WEEK));
        binding.chipCompleted.setOnClickListener(v -> viewModel.setFilter(TaskListViewModel.FilterType.COMPLETED));
        binding.chipOverdue.setOnClickListener(v -> viewModel.setFilter(TaskListViewModel.FilterType.OVERDUE));
    }

    private void setupSearch() {
        // Initially hide search card
        binding.searchCard.setVisibility(View.GONE);
        
        // Search text change listener
        binding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                viewModel.setSearchQuery(query);
                binding.btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Clear search button
        binding.btnClearSearch.setOnClickListener(v -> {
            binding.editSearch.setText("");
            viewModel.setSearchQuery("");
        });
    }

    private void toggleSearch() {
        isSearchVisible = !isSearchVisible;
        if (isSearchVisible) {
            binding.searchCard.setVisibility(View.VISIBLE);
            binding.editSearch.requestFocus();
        } else {
            binding.searchCard.setVisibility(View.GONE);
            binding.editSearch.setText("");
            viewModel.setSearchQuery("");
        }
    }

    private void setupMenu() {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                // Don't inflate menu here - let Activity handle it
                // Just ensure menu items are visible for this fragment
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.action_search) {
                    toggleSearch();
                    return true;
                } else if (id == R.id.action_sort) {
                    showSortDialog();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void showSortDialog() {
        String[] sortOptions = {
            getString(R.string.sort_date_created),
            getString(R.string.sort_due_date),
            getString(R.string.sort_priority),
            getString(R.string.
                    sort_alphabetically)
        };

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.sort_by)
            .setItems(sortOptions, (dialog, which) -> {
                TaskListViewModel.SortType sortType;
                switch (which) {
                    case 1:
                        sortType = TaskListViewModel.SortType.DATE_DUE;
                        break;
                    case 2:
                        sortType = TaskListViewModel.SortType.PRIORITY;
                        break;
                    case 3:
                        sortType = TaskListViewModel.SortType.ALPHABETICAL;
                        break;
                    default:
                        sortType = TaskListViewModel.SortType.DATE_CREATED;
                        break;
                }
                viewModel.setSort(sortType);
            })
            .show();
    }

    private void observeViewModel() {
        viewModel.getFilteredTasks().observe(getViewLifecycleOwner(), tasks -> {
            taskAdapter.submitList(tasks);
            updateEmptyState(tasks == null || tasks.isEmpty());
        });

        viewModel.getCurrentFilter().observe(getViewLifecycleOwner(), filter -> {
            updateFilterChips(filter);
        });

        viewModel.getTaskStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                binding.textPendingCount.setText(String.valueOf(stats.getPendingTasks()));
                binding.textCompletedCount.setText(String.valueOf(stats.getCompletedTasks()));
                binding.textOverdueCount.setText(String.valueOf(stats.getOverdueTasks()));
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerTasks.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void updateFilterChips(TaskListViewModel.FilterType filter) {
        binding.chipAll.setChecked(filter == TaskListViewModel.FilterType.ALL);
        binding.chipToday.setChecked(filter == TaskListViewModel.FilterType.TODAY);
        binding.chipUpcoming.setChecked(filter == TaskListViewModel.FilterType.THIS_WEEK);
        binding.chipCompleted.setChecked(filter == TaskListViewModel.FilterType.COMPLETED);
        binding.chipOverdue.setChecked(filter == TaskListViewModel.FilterType.OVERDUE);
    }

    @Override
    public void onTaskClick(Task task) {
        NavController navController = Navigation.findNavController(requireView());
        Bundle args = new Bundle();
        args.putLong("taskId", task.getId());
        navController.navigate(R.id.action_taskList_to_taskDetail, args);
    }

    @Override
    public void onTaskCheckChanged(Task task, boolean isCompleted) {
        viewModel.toggleTaskCompleted(task);
    }

    @Override
    public void onStarClick(Task task) {
        // Toggle important flag via ViewModel to persist to database
        viewModel.toggleImportant(task.getId());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Swipe callback for completing/deleting tasks
     */
    private class SwipeCallback extends ItemTouchHelper.SimpleCallback {
        private final ColorDrawable completeBackground = new ColorDrawable(Color.parseColor("#4CAF50"));
        private final ColorDrawable deleteBackground = new ColorDrawable(Color.parseColor("#F44336"));
        private final Drawable checkIcon;
        private final Drawable deleteIcon;

        public SwipeCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            checkIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check);
            deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            Task task = taskAdapter.getTaskAt(position);
            
            if (task == null) return;

            if (direction == ItemTouchHelper.RIGHT) {
                // Complete task
                viewModel.toggleTaskCompleted(task);
                Snackbar.make(binding.getRoot(), R.string.task_completed, Snackbar.LENGTH_SHORT).show();
            } else if (direction == ItemTouchHelper.LEFT) {
                // Delete task
                viewModel.deleteTask(task);
                Snackbar.make(binding.getRoot(), R.string.task_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> {
                            // Undo would require implementing task restore
                        })
                        .show();
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            View itemView = viewHolder.itemView;
            int iconMargin = (itemView.getHeight() - 24) / 2;

            if (dX > 0) {
                // Swiping right - complete
                completeBackground.setBounds(itemView.getLeft(), itemView.getTop(),
                        (int) (itemView.getLeft() + dX), itemView.getBottom());
                completeBackground.draw(c);

                if (checkIcon != null) {
                    int iconTop = itemView.getTop() + iconMargin;
                    int iconBottom = iconTop + 24;
                    int iconLeft = itemView.getLeft() + iconMargin;
                    int iconRight = iconLeft + 24;
                    checkIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    checkIcon.draw(c);
                }
            } else if (dX < 0) {
                // Swiping left - delete
                deleteBackground.setBounds((int) (itemView.getRight() + dX), itemView.getTop(),
                        itemView.getRight(), itemView.getBottom());
                deleteBackground.draw(c);

                if (deleteIcon != null) {
                    int iconTop = itemView.getTop() + iconMargin;
                    int iconBottom = iconTop + 24;
                    int iconRight = itemView.getRight() - iconMargin;
                    int iconLeft = iconRight - 24;
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    deleteIcon.draw(c);
                }
            }
        }
    }
}
