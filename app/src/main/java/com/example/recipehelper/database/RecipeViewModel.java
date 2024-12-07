package com.example.recipehelper.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class RecipeViewModel extends AndroidViewModel {

    private RecipeRepository mRepository;

    private LiveData<List<Recipe>> mAllRecipes;

    public RecipeViewModel(@NonNull Application application) {
        super(application);
        mRepository = new RecipeRepository(application);
        mAllRecipes = mRepository.getAllRecipes();
    }

    public LiveData<List<Recipe>> getAllRecipes() { return mAllRecipes; }

    public void insert(Recipe recipe) { mRepository.insert(recipe); }

    public void deleteAll() { mRepository.deleteAll(); }

    public void deleteRecipe(Recipe recipe) { mRepository.deleteRecipe(recipe); }

    public void changeIngredients(String ingredients, String recipe) {
        mRepository.changeIngredients(ingredients, recipe);
    }

    public void changeSteps(String steps, String recipe) {
        mRepository.changeSteps(steps, recipe);
    }

    public void changeTimers(String timers, String recipe) {
        mRepository.changeTimers(timers, recipe);
    }

    public void changePrepTime(int prepTime, String recipe) {
        mRepository.changePrepTime(prepTime, recipe);
    }

    public void changeRecipeType(int recipeType, String recipe) {
        mRepository.changeRecipeType(recipeType, recipe);
    }

}
