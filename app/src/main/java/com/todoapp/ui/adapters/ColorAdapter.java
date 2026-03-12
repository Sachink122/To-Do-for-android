package com.todoapp.ui.adapters;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.todoapp.R;
import com.todoapp.databinding.ItemColorBinding;

/**
 * RecyclerView adapter for color picker in category dialogs.
 */
public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder> {

    private final String[] colors;
    private String selectedColor;
    private final OnColorSelectedListener listener;

    public interface OnColorSelectedListener {
        void onColorSelected(String color);
    }

    public ColorAdapter(String[] colors, String selectedColor, OnColorSelectedListener listener) {
        this.colors = colors;
        this.selectedColor = selectedColor;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemColorBinding binding = ItemColorBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ColorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        String color = colors[position];
        holder.bind(color);
    }

    @Override
    public int getItemCount() {
        return colors.length;
    }

    class ColorViewHolder extends RecyclerView.ViewHolder {
        private final ItemColorBinding binding;

        public ColorViewHolder(ItemColorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String color) {
            // Set circle color
            GradientDrawable drawable = (GradientDrawable) binding.colorCircle.getBackground();
            drawable.setColor(android.graphics.Color.parseColor(color));

            // Show checkmark if selected
            boolean isSelected = color.equals(selectedColor);
            binding.iconCheck.setVisibility(isSelected ? android.view.View.VISIBLE : android.view.View.GONE);

            // Add selection indicator
            if (isSelected) {
                drawable.setStroke(4, ContextCompat.getColor(binding.getRoot().getContext(), R.color.white));
            } else {
                drawable.setStroke(0, android.graphics.Color.TRANSPARENT);
            }

            // Click listener
            binding.getRoot().setOnClickListener(v -> {
                if (!color.equals(selectedColor)) {
                    String previousColor = selectedColor;
                    selectedColor = color;
                    
                    // Notify change for previous and new selection
                    for (int i = 0; i < colors.length; i++) {
                        if (colors[i].equals(previousColor) || colors[i].equals(color)) {
                            notifyItemChanged(i);
                        }
                    }
                    
                    if (listener != null) {
                        listener.onColorSelected(color);
                    }
                }
            });
        }
    }
}
