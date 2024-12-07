package com.example.recipehelper.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Recipe recipe);

    @Query("DELETE FROM recipe_table")
    void deleteAll();

    @Delete
    void deleteRecipe(Recipe recipe);

    @Query("SELECT * from recipe_table ORDER BY recipe_type ASC, recipe_name ASC")
    LiveData<List<Recipe>> getAllRecipes();

    @Query("SELECT * from recipe_table ORDER BY " +
            "CASE WHEN recipe_type = 0 THEN :breakfast_order " +
            "WHEN recipe_type = 1 THEN :lunch_order " +
            "WHEN recipe_type = 2 THEN :dinner_order " +
            "WHEN recipe_type = 3 THEN :snack_order " +
            "WHEN recipe_type = 4 THEN :dessert_order " +
            "WHEN recipe_type = 5 THEN :drink_order " +
            "WHEN recipe_type = 6 THEN :unspecified_order END ASC, recipe_name ASC")
    LiveData<List<Recipe>> getAllRecipesOrdered(int breakfast_order, int lunch_order, int dinner_order,
                                                int snack_order, int dessert_order, int drink_order,
                                                int unspecified_order);

    @Query("SELECT * from recipe_table where recipe_type = :recipeType ORDER BY recipe_name")
    List<Recipe> getAllRecipesOfType(int recipeType);

    @Query("SELECT * from recipe_table LIMIT 1")
    Recipe[] getAnyRecipe();


    @Query("UPDATE recipe_table SET recipe_ingredients = :ingredients WHERE recipe_name = :recipe")
    void changeIngredients(String ingredients, String recipe);

    @Query("UPDATE recipe_table SET recipe_steps = :steps WHERE recipe_name = :recipe")
    void changeSteps(String steps, String recipe);

    @Query("UPDATE recipe_table SET recipe_timers = :timers WHERE recipe_name = :recipe")
    void changeTimers(String timers, String recipe);

    @Query("UPDATE recipe_table SET recipe_prep_time = :prepTime WHERE recipe_name = :recipe")
    void changePrepTime(int prepTime, String recipe);
    @Query("UPDATE recipe_table SET recipe_type = :recipeType WHERE recipe_name = :recipe")
    void changeRecipeType(int recipeType, String recipe);

}
