package com.example.recipehelper;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "recipe_table")
public class Recipe {

    public static class RecipeType {
        public static final int BREAKFAST = 0;
        public static final int LUNCH = 1;
        public static final int DINNER = 2;
        public static final int SNACK = 3;
        public static final int DESSERT = 4;
        public static final int DRINK = 5;
        public static final int UNSPECIFIED = 6;
    }
    @PrimaryKey
    @NonNull
    @ColumnInfo(name="recipe_name")
    private String mRecipe;

    @NonNull
    @ColumnInfo(name="recipe_ingredients")
    private String mIngredients;
    @NonNull
    @ColumnInfo(name="recipe_steps")
    private String mSteps;
    @NonNull
    @ColumnInfo(name="recipe_timers")
    private String mTimers;
    @ColumnInfo(name="recipe_prep_time")
    private int mPrepTime;
    @ColumnInfo(name="recipe_type")
    private int mRecipeType;

    public Recipe(){
        this.mRecipe = " ";
        this.mIngredients = " ";
        this.mSteps = " ";
        this.mTimers = " ";
        this.mPrepTime = 0;
        this.mRecipeType = 0;
    }
    public Recipe(@NonNull String recipe_name, @NonNull String recipe_ingredients, @NonNull String recipe_steps, @NonNull String recipe_timers, int recipe_prep_time, int recipe_type){
        this.mRecipe = recipe_name;
        this.mIngredients = recipe_ingredients;
        this.mSteps = recipe_steps;
        this.mTimers = recipe_timers;
        this.mPrepTime = recipe_prep_time;
        this.mRecipeType = recipe_type;
    }
    public String getRecipe() { return mRecipe; }
    public String getIngredients() { return mIngredients; }
    public String getSteps() { return mSteps; }
    public String getTimers() { return mTimers; }

    public int getPrepTime() { return mPrepTime; }
    public int getRecipeType() {return mRecipeType; }


    public void setRecipe(String recipe_name) { mRecipe = recipe_name;}
    public void setIngredients(String recipe_ingredients) { mIngredients = recipe_ingredients; }
    public void setSteps(String recipe_steps) { mSteps = recipe_steps; }
    public void setTimers(String recipe_timers) { mTimers = recipe_timers; }

    public void setPrepTime(int recipe_prep_time) { mPrepTime = recipe_prep_time; }
    public void setRecipeType(int recipe_type) { mRecipeType = recipe_type; }

}
