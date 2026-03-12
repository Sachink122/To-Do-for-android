package com.todoapp.ui.calendar;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.todoapp.R;
import com.todoapp.data.model.Task;
import com.todoapp.databinding.FragmentCalendarBinding;
import com.todoapp.ui.adapters.TaskAdapter;
import com.todoapp.ui.viewmodel.TaskListViewModel;
import com.todoapp.util.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment displaying a calendar view with tasks for the selected date.
 * Dates with tasks are shown as chips below the calendar.
 */
@AndroidEntryPoint
public class CalendarFragment extends Fragment implements TaskAdapter.TaskClickListener {

    private FragmentCalendarBinding binding;
    private TaskListViewModel viewModel;
    private TaskAdapter taskAdapter;
    private Calendar selectedDate;
    private List<Task> allTasks = new ArrayList<>();
    private Map<Long, Integer> taskCountByDate = new TreeMap<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use activity-scoped ViewModel to share data with TaskListFragment
        viewModel = new ViewModelProvider(requireActivity()).get(TaskListViewModel.class);
        selectedDate = Calendar.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViews();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh when coming back to this fragment
        filterTasksForSelectedDate();
    }

    private void setupViews() {
        // RecyclerView setup
        taskAdapter = new TaskAdapter(this);
        binding.recyclerTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerTasks.setAdapter(taskAdapter);

        // Calendar setup
        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateSelectedDateText();
            filterTasksForSelectedDate();
        });

        // Initialize with today
        updateSelectedDateText();
    }

    private void updateSelectedDateText() {
        String dateText = DateUtils.formatDateFull(selectedDate.getTimeInMillis());
        binding.textSelectedDate.setText(dateText);
    }

    private void observeViewModel() {
        // Observe all tasks (not filtered by date yet)
        viewModel.getAllTasksWithSubtasks().observe(getViewLifecycleOwner(), tasksWithSubtasks -> {
            if (tasksWithSubtasks != null) {
                allTasks.clear();
                for (int i = 0; i < tasksWithSubtasks.size(); i++) {
                    allTasks.add(tasksWithSubtasks.get(i).getTask());
                }
                updateTaskDateIndicators();
                filterTasksForSelectedDate();
            }
        });
    }

    private void updateTaskDateIndicators() {
        taskCountByDate.clear();
        binding.taskDatesContainer.removeAllViews();
        
        // Count tasks per date
        for (Task task : allTasks) {
            if (task.getDueDate() != null) {
                long dateKey = normalizeToMidnight(task.getDueDate());
                taskCountByDate.put(dateKey, taskCountByDate.getOrDefault(dateKey, 0) + 1);
            }
        }
        
        if (taskCountByDate.isEmpty()) {
            binding.taskDatesScroll.setVisibility(View.GONE);
            return;
        }
        
        binding.taskDatesScroll.setVisibility(View.VISIBLE);
        
        // Add label
        TextView label = new TextView(requireContext());
        label.setText("📅 Tasks on: ");
        label.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_600));
        label.setTextSize(12);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        labelParams.rightMargin = 8;
        label.setLayoutParams(labelParams);
        binding.taskDatesContainer.addView(label);
        
        // Create chips for dates with tasks
        SimpleDateFormat chipFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
        
        for (Map.Entry<Long, Integer> entry : taskCountByDate.entrySet()) {
            long dateTimestamp = entry.getKey();
            int taskCount = entry.getValue();
            
            Chip chip = new Chip(requireContext());
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(dateTimestamp);
            
            String dateText = chipFormat.format(cal.getTime());
            chip.setText(dateText + " (" + taskCount + ")");
            chip.setTextSize(12);
            chip.setChipMinHeight(32 * getResources().getDisplayMetrics().density);
            
            // Check if this date is today
            if (isSameDay(dateTimestamp, System.currentTimeMillis())) {
                chip.setChipBackgroundColorResource(R.color.priority_medium);
                chip.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            } else if (dateTimestamp < System.currentTimeMillis()) {
                // Overdue
                chip.setChipBackgroundColorResource(R.color.priority_high);
                chip.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            } else {
                chip.setChipBackgroundColorResource(R.color.primary);
                chip.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            }
            
            // Highlight if selected
            if (isSameDay(dateTimestamp, selectedDate.getTimeInMillis())) {
                chip.setTypeface(null, Typeface.BOLD);
                chip.setChipStrokeWidth(2 * getResources().getDisplayMetrics().density);
                chip.setChipStrokeColorResource(R.color.on_background);
            }
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.rightMargin = (int) (8 * getResources().getDisplayMetrics().density);
            chip.setLayoutParams(params);
            
            // Click to navigate to date
            chip.setOnClickListener(v -> {
                Calendar clickedCal = Calendar.getInstance();
                clickedCal.setTimeInMillis(dateTimestamp);
                
                selectedDate.setTimeInMillis(dateTimestamp);
                binding.calendarView.setDate(dateTimestamp, true, true);
                updateSelectedDateText();
                filterTasksForSelectedDate();
                updateTaskDateIndicators(); // Refresh to show selection
            });
            
            binding.taskDatesContainer.addView(chip);
        }
    }

    private long normalizeToMidnight(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private void filterTasksForSelectedDate() {
        if (allTasks.isEmpty()) {
            // Try to get from filtered tasks as fallback
            List<Task> filteredList = viewModel.getFilteredTasks().getValue();
            if (filteredList != null) {
                allTasks.clear();
                allTasks.addAll(filteredList);
            }
        }

        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : allTasks) {
            if (task.getDueDate() != null && isSameDay(task.getDueDate(), selectedDate.getTimeInMillis())) {
                filteredTasks.add(task);
            }
        }

        taskAdapter.submitList(new ArrayList<>(filteredTasks));
        updateEmptyState(filteredTasks.isEmpty());
    }

    private boolean isSameDay(long timestamp1, long timestamp2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(timestamp1);
        
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(timestamp2);
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerTasks.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onTaskClick(Task task) {
        NavController navController = Navigation.findNavController(requireView());
        Bundle args = new Bundle();
        args.putLong("taskId", task.getId());
        navController.navigate(R.id.action_calendar_to_taskDetail, args);
    }

    @Override
    public void onTaskCheckChanged(Task task, boolean isCompleted) {
        viewModel.toggleTaskCompleted(task);
    }

    @Override
    public void onStarClick(Task task) {
        viewModel.toggleImportant(task.getId());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
