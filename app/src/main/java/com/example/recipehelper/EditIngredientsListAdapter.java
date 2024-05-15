package com.example.recipehelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EditIngredientsListAdapter extends RecyclerView.Adapter<EditIngredientsListAdapter.EditIngredientsViewHolder> {

    public interface Listener {
        void onClick(View view, int position);
    }
    private Listener onItemClickListener;

    private final LayoutInflater mInflater;
    private List<String> mIngredients;

    public EditIngredientsListAdapter(Context context, List<String> ingredients){
        mInflater = LayoutInflater.from(context);
        this.mIngredients = ingredients;
    }

    @NonNull
    @Override
    public EditIngredientsListAdapter.EditIngredientsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_ingredient_item_layout, parent, false);
        return new EditIngredientsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EditIngredientsListAdapter.EditIngredientsViewHolder holder, int position) {
        if (mIngredients != null) {
            String current = mIngredients.get(position);
            holder.ingredientTextView.setText(current);
        } else {
            // Covers the case of data not being ready yet.
            holder.ingredientTextView.setText("");
        }
    }

    @Override
    public int getItemCount() {
        if (mIngredients == null) {
            return 0;
        }
        return mIngredients.size();
    }

    public String getIngredientAtPosition(int position) {
        return mIngredients.get(position);
    }

    class EditIngredientsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView ingredientTextView;

        public EditIngredientsViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientTextView = itemView.findViewById(R.id.edit_ingredient_text_view);
            ingredientTextView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int mPosition = getLayoutPosition();
            if(onItemClickListener != null){
                onItemClickListener.onClick(view, mPosition);
            }
        }
    }

    public void setOnItemClickListener(EditIngredientsListAdapter.Listener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public void removeOnItemClickListener(){
        this.onItemClickListener = null;
    }
}
