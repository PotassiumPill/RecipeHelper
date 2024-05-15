package com.example.recipehelper;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecipeRepository {

    private RecipeDao mRecipeDao;
    private LiveData<List<Recipe>> mAllRecipes;

    private ExecutorService executorService;

    public RecipeRepository(Application application) {
        RecipeRoomDatabase db = RecipeRoomDatabase.getDatabase(application);
        mRecipeDao = db.recipeDao();
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if(hour >= 5 && hour < 11) {
            mAllRecipes = mRecipeDao.getAllRecipesOrdered(1,2,5,3,4,6,7);
        } else if(hour == 11){
            mAllRecipes = mRecipeDao.getAllRecipesOrdered(2,1,5,3,4,6,7);
        }else if(hour >= 12 && hour < 14) {
            mAllRecipes = mRecipeDao.getAllRecipesOrdered(4,1,3,2,5,6,7);
        } else if(hour >= 14 && hour < 17) {
            mAllRecipes = mRecipeDao.getAllRecipesOrdered(5,4,2,1,3,6,7);
        } else if(hour >= 17 && hour < 19) {
            mAllRecipes = mRecipeDao.getAllRecipesOrdered(6,5,1,3,2,4,7);
        } else if(hour >= 19 && hour < 22) {
            mAllRecipes = mRecipeDao.getAllRecipesOrdered(5,4,1,6,2,3,7);
        }else if(hour >= 22) {
            mAllRecipes = mRecipeDao.getAllRecipesOrdered(6,5,4,2,3,1,7);
        } else {
            mAllRecipes = mRecipeDao.getAllRecipesOrdered(2,4,5,1,3,6,7);
        }
//        mAllRecipes = mRecipeDao.getAllRecipes();
        executorService = Executors.newSingleThreadExecutor();
    }

    LiveData<List<Recipe>> getAllRecipes() {
        return mAllRecipes;
    }

    public void insert(final Recipe recipe) {
        executorService.submit(() -> mRecipeDao.insert(recipe));
    }

    public void deleteAll() {
        executorService.submit(() -> mRecipeDao.deleteAll());
    }

    public void deleteRecipe(final Recipe recipe) {
        executorService.submit(() -> mRecipeDao.deleteRecipe(recipe));
    }

    public void changeIngredients(final String ingredients, final String recipe) {
        executorService.submit(() -> mRecipeDao.changeIngredients(ingredients, recipe));
    }

    public void changeSteps(final String steps, final String recipe) {
        executorService.submit(() -> mRecipeDao.changeSteps(steps, recipe));
    }

    public void changeTimers(final String timers, final String recipe) {
        executorService.submit(() -> mRecipeDao.changeTimers(timers, recipe));
    }

    public void changePrepTime(final int prepTime, final String recipe) {
        executorService.submit(() -> mRecipeDao.changePrepTime(prepTime, recipe));
    }

    public void changeRecipeType(final int recipeType, final String recipe) {
        executorService.submit(() -> mRecipeDao.changeRecipeType(recipeType, recipe));
    }

}
