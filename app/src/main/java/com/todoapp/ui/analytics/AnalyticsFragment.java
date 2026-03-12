package com.todoapp.ui.analytics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.todoapp.databinding.FragmentAnalyticsBinding;
import com.todoapp.ui.viewmodel.TaskListViewModel;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment displaying task analytics and statistics.
 */
@AndroidEntryPoint
public class AnalyticsFragment extends Fragment {

    private FragmentAnalyticsBinding binding;
    private TaskListViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use activity-scoped ViewModel to share data with TaskListFragment
        viewModel = new ViewModelProvider(requireActivity()).get(TaskListViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAnalyticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeStats();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh stats when fragment becomes visible
        viewModel.refreshStats();
    }

    private void observeStats() {
        viewModel.getTaskStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats == null) return;

            int total = stats.getTotalTasks();
            int completed = stats.getCompletedTasks();
            int pending = stats.getPendingTasks();
            int overdue = stats.getOverdueTasks();

            // Update counts
            binding.textTotalCount.setText(String.valueOf(total));
            binding.textCompletedCount.setText(String.valueOf(completed));
            binding.textPendingCount.setText(String.valueOf(pending));
            binding.textOverdueCount.setText(String.valueOf(overdue));

            // Update completion rate
            int completionRate = total > 0 ? (completed * 100) / total : 0;
            binding.textCompletionPercent.setText(completionRate + "%");
            binding.progressCompletion.setProgress(completionRate);

            // Update activity counts
            binding.textTodayCompleted.setText(String.valueOf(stats.getCompletedToday()));
            binding.textWeekCompleted.setText(String.valueOf(stats.getCompletedThisWeek()));
            binding.textMonthCompleted.setText(String.valueOf(stats.getCompletedThisMonth()));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
