package com.example.recipehelper;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.List;

public class RecipeListAdapter extends RecyclerView.Adapter<RecipeListAdapter.RecipeViewHolder> {

    public interface Listener {
        void onClick(View view, int position);
    }
    private Listener onItemClickListener;

    private final LayoutInflater mInflater;
    private List<Recipe> mRecipes;

    public RecipeListAdapter(Context context){
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_recipe_item_layout, parent, false);
        return new RecipeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeListAdapter.RecipeViewHolder holder, int position) {
        if (mRecipes != null) {
            Recipe current = mRecipes.get(position);
            holder.recipeTextView.setText(current.getRecipe());
            holder.timeTextView.setText(mInflater.getContext().getResources().getString(R.string.recipe_time_description,
                    Utils.secondsToTimeString(current.getPrepTime()),
                    Utils.secondsToTimeString(Utils.getTotalTimeFromTimers(current.getTimers()) + current.getPrepTime())
            ));
            if(current.getRecipeType() == Recipe.RecipeType.UNSPECIFIED) {
                holder.recipeTypeIcon.setVisibility(View.GONE);
            } else {
                holder.recipeTypeIcon.setVisibility(View.VISIBLE);
                switch(current.getRecipeType()){
                    case Recipe.RecipeType.BREAKFAST:
                        holder.recipeTypeIcon.setImageResource(R.drawable.baseline_breakfast_24);
                        holder.recipeTypeIcon.setColorFilter(Color.parseColor("#FFD54F"));
                        break;
                    case Recipe.RecipeType.LUNCH:
                        holder.recipeTypeIcon.setImageResource(R.drawable.baseline_lunch_24);
                        holder.recipeTypeIcon.setColorFilter(Color.parseColor("#EF5350"));
                        break;
                    case Recipe.RecipeType.DINNER:
                        holder.recipeTypeIcon.setImageResource(R.drawable.baseline_dinner_24);
                        holder.recipeTypeIcon.setColorFilter(Color.parseColor("#66BB6A"));
                        break;
                    case Recipe.RecipeType.SNACK:
                        holder.recipeTypeIcon.setImageResource(R.drawable.baseline_snack_24);
                        holder.recipeTypeIcon.setColorFilter(Color.parseColor("#FF7043"));
                        break;
                    case Recipe.RecipeType.DESSERT:
                        holder.recipeTypeIcon.setImageResource(R.drawable.baseline_dessert_24);
                        holder.recipeTypeIcon.setColorFilter(Color.parseColor("#F48FB1"));
                        break;
                    case Recipe.RecipeType.DRINK:
                        holder.recipeTypeIcon.setImageResource(R.drawable.baseline_drink_24);
                        holder.recipeTypeIcon.setColorFilter(Color.parseColor("#4FC3F7"));
                        break;
                }
            }
        } else {
            // Covers the case of data not being ready yet.
            holder.recipeTextView.setText("");
        }
    }

    void setRecipes(List<Recipe> recipes) {
        mRecipes = recipes;
        notifyDataSetChanged();
        Intent intent = new Intent(mInflater.getContext(), NewAppWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int[] ids = AppWidgetManager.getInstance(mInflater.getContext()).getAppWidgetIds(
                new ComponentName(mInflater.getContext(), NewAppWidget.class));
        if(ids != null && ids.length > 0) {
            AppWidgetManager.getInstance(mInflater.getContext()).notifyAppWidgetViewDataChanged(ids, R.id.recipe_container);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            mInflater.getContext().sendBroadcast(intent);
        }
    }

    @Override
    public int getItemCount() {
        if (mRecipes == null) {
            return 0;
        }
        return mRecipes.size();
    }

    public Recipe getRecipeAtPosition(int position) {
        return mRecipes.get(position);
    }


    class RecipeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView recipeTextView;
        private final TextView timeTextView;
        private final ImageView recipeTypeIcon;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeTextView = itemView.findViewById(R.id.recipe_text_view);
            recipeTextView.setOnClickListener(this);
            timeTextView = itemView.findViewById(R.id.time_text_view);
            timeTextView.setOnClickListener(this);
            recipeTypeIcon = itemView.findViewById(R.id.recipe_type_icon);
        }

        @Override
        public void onClick(View view) {
            int mPosition = getLayoutPosition();
            if(onItemClickListener != null){
                onItemClickListener.onClick(view, mPosition);
            }
        }
    }

    public void setOnItemClickListener(Listener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public void removeOnItemClickListener(){
        this.onItemClickListener = null;
    }

}
