package com.example.recipehelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CheckboxListAdapter extends RecyclerView.Adapter<CheckboxListAdapter.CheckboxViewHolder> {

    private final LayoutInflater mInflater;
    private List<String> mIngredients;

    private boolean[] mChecked;

    public CheckboxListAdapter(Context context, List<String> ingredients, boolean[] checked){
        mInflater = LayoutInflater.from(context);
        this.mIngredients = ingredients;
        this.mChecked = checked;
    }

    @NonNull
    @Override
    public CheckboxListAdapter.CheckboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_checklist_item_layout, parent, false);
        return new CheckboxViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckboxListAdapter.CheckboxViewHolder holder, int position) {
        if (mIngredients != null) {
            String current = mIngredients.get(position);
            boolean curChecked = mChecked[position];
            holder.ingredientCheckBox.setText(current);
            holder.ingredientCheckBox.setChecked(curChecked);
        } else {
            // Covers the case of data not being ready yet.
            holder.ingredientCheckBox.setText("");
        }
    }

    @Override
    public int getItemCount() {
        if (mIngredients == null) {
            return 0;
        }
        return mIngredients.size();
    }

    class CheckboxViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {

        private final CheckBox ingredientCheckBox;

        public CheckboxViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientCheckBox = itemView.findViewById(R.id.checkbox_item);
            ingredientCheckBox.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            int mPosition = getLayoutPosition();
            mChecked[mPosition] = b;
        }
    }

}
