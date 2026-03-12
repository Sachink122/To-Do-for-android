package com.todoapp.ui.adapters;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.todoapp.data.model.Category;
import com.todoapp.databinding.ItemCategoryBinding;

/**
 * RecyclerView adapter for displaying categories with task counts.
 */
public class CategoryAdapter extends ListAdapter<Category, CategoryAdapter.CategoryViewHolder> {

    private final CategoryClickListener listener;

    public interface CategoryClickListener {
        void onCategoryClick(Category category);
        void onCategoryDeleteClick(Category category);
    }

    public CategoryAdapter(CategoryClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Category> DIFF_CALLBACK = new DiffUtil.ItemCallback<Category>() {
        @Override
        public boolean areItemsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                   oldItem.getColor().equals(newItem.getColor());
        }
    };

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = getItem(position);
        holder.bind(category);
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryBinding binding;

        public CategoryViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Category category) {
            binding.textCategoryName.setText(category.getName());
            
            // Color indicator
            GradientDrawable colorDrawable = (GradientDrawable) binding.colorIndicator.getBackground();
            colorDrawable.setColor(category.getColorInt());

            // Task count - would need to be passed in or fetched
            // For now, just show placeholder
            binding.textTaskCount.setText("");

            // Click listeners
            binding.cardCategory.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category);
                }
            });

            binding.btnMoreOptions.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryDeleteClick(category);
                }
            });
        }
    }
}
