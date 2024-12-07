package com.example.recipehelper.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.Executors;

@Database(entities = {Recipe.class}, version = 6, exportSchema = false)
public abstract class RecipeRoomDatabase extends RoomDatabase {

    private static final boolean POPULATE_DUMMY_RECIPES = true;

    private static volatile RecipeRoomDatabase INSTANCE;

    public abstract RecipeDao recipeDao();

    private static final RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){
                @Override
                public void onOpen (@NonNull SupportSQLiteDatabase db){
                    super.onOpen(db);
                    Executors.newSingleThreadExecutor().submit(() -> {
                        if(POPULATE_DUMMY_RECIPES && INSTANCE.recipeDao().getAnyRecipe().length < 1){
                            INSTANCE.recipeDao().deleteAll();
                            //dummy data insert
                            INSTANCE.recipeDao().insert(new Recipe("Sunny Side Up Egg",
                                    "1 egg\n1 tbsp butter\nPinch of salt\nPinch of pepper",
                                    "Add butter to pan on medium until melted.\nCrack egg softly onto melted butter, cook until opaque.\nSeason with salt and pepper and enjoy!",
                                    "0,180,0", 30, Recipe.RecipeType.BREAKFAST));
                            INSTANCE.recipeDao().insert(new Recipe("Grilled Cheese Sandwich",
                                    "2 slices bread\n1-2 tbsp butter\n3-5 slices cheddar cheese, or shredded",
                                    "Add butter to pan on medium high heat until melted.\nCover outside of bread slices with butter.\nAdd bread slices to pan, buttered side down." +
                                            "Then fry until fragrant and slightly toasted.\nOnce toasted, cover one slice with cheese and add the empty slice on top.\n" +
                                            "Fry each side for 1-2 minutes until cheese melted and bread is golden-brown.\nRemove from pan and serve with your favorite side and drink!",
                                    "0,0,60,0,120,0", 120, Recipe.RecipeType.LUNCH));
                            INSTANCE.recipeDao().insert(new Recipe("Pan-Seared Salmon",
                                    "4 salmon filets (6 oz)\n1 tbsp garlic Powder\n1 tbsp dried basil\n1 tsp salt\n1 lemon\n2 tbsp butter",
                                    "Wash and dry salmon filets.\nCombine all dry ingredients together in a bowl.\nCover salmon filets with mixture, rubbing to coat evenly.\nAdd butter to pan on medium-high heat and melt.\n" +
                                            "Add filets and cook for 5 minutes, or until salmon appears darker halfway through.\nFlip and fry the other side of the filet for 5 minutes, or until darkened all the way through.\n" +
                                            "Serve with lemon slices and carb of choice!",
                                    "0,0,0,0,300,300,0", 300, Recipe.RecipeType.DINNER));
                            INSTANCE.recipeDao().insert(new Recipe("Trash Snack Mix",
                                    "2 cups Rice Chex\n2 cups Corn Chex\n2 cups Cheerios\n2 cups Bugles\n1 cup Fritos\n1 cup oyster crackers\n1 cup small pretzels\n1 cup mixed nuts\n3/4 cup softened butter\n1/4 cup worcestershire sauce\n2 tsp chili powder\n1 tsp ground cumin\n3/4 tsp garlic powder\n1/2 tsp onion powder\n1/2 tsp seasoning salt\n1/8 tsp cayenne pepper to taste",
                                    "Preheat oven to 250 degrees F.\nCombine all dry snack ingredients in large bowl.\n" +
                                            "Add all seasoning/powdered ingredients into medium-sized microwave-safe glass.\n" +
                                            "Cover bowl with wax paper and cook in microwave for 1 minute until butter is melted.\n" +
                                            "Remove from microwave and whisk to combine ingredients.\n" +
                                            "Pour seasoning over snack ingredients. Stir to coat evenly, then spread evenly into a large baking pan.\n" +
                                            "Bake for 1 hour, stirring every 15 minutes.\n" +
                                            "Once done baking, remove and add mix onto several layers of paper towels to cool. Once cooled, store in an airtight container for up to one week.",
                                    "0,0,0,60,0,0,3600,0", 600, Recipe.RecipeType.SNACK));
                            INSTANCE.recipeDao().insert(new Recipe("Old-Fashioned",
                                    "2 oz bourbon/rye whiskey\n0.25 oz gum syrup (or syrup of choice)\n2 dashes orange bitters\n2 dashes Angostura bitters\n1 orange or lemon peel",
                                    "Add all ingredients except citrus peel to mixing glass with ice.\n" +
                                            "Stir about 30 rotations, or 10 seconds.\n" +
                                            "Strain drink into glass with new large ice cube. Express citrus peel on surface of drink and place as a garnish.",
                                    "0,10,0", 30, Recipe.RecipeType.DRINK));
                            INSTANCE.recipeDao().insert(new Recipe("3 Ingredient Peanut Butter Cookies",
                                    "1/2 cup (128g) unsweetened peanut butter\n1/4 cup (59mL) maple syrup\n50g quick oats",
                                    "Preheat oven to 350 F (177 C), then line a large baking sheet with parchment paper or silicone baking mat.\n" +
                                            "Add all ingredients to a large mixing bowl and stir until well incorporated.\n" +
                                            "Make 1 tbsp scoops from the dough, rolling with the palms of your hands if necessary to create round balls. Add each ball to the baking sheet and press to flatten, about 1/4 in thick.\n" +
                                            "Bake cookies in oven for 10 minutes or until the surface is cooked and golden.\n" +
                                            "Remove from oven and let cookies cool on baking sheet.",
                                    "0,0,0,600,0",300, Recipe.RecipeType.DESSERT));
                        }
                    });
                }
            };

    public static RecipeRoomDatabase getDatabase(final Context context) {
        if(INSTANCE == null){
            synchronized (RecipeRoomDatabase.class){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            RecipeRoomDatabase.class,
                            "recipe_database")
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
