package com.example.recipehelper.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.recipehelper.R;
import com.example.recipehelper.Utils;
import com.example.recipehelper.activity.StartRecipeActivity;
import com.example.recipehelper.database.Recipe;
import com.example.recipehelper.database.RecipeDao;
import com.example.recipehelper.database.RecipeRoomDatabase;

import java.util.Calendar;
import java.util.List;

public class NewAppWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new NewAppRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class NewAppRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    List<Recipe> recipes;
    RecipeDao mRecipeDao;
    private Context mContext;
    private int mAppWidgetId;

    public NewAppRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        RecipeRoomDatabase db = RecipeRoomDatabase.getDatabase(mContext);
        mRecipeDao = db.recipeDao();
    }

    @Override
    public void onDataSetChanged() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int recipeType;
        if(hour >= 5 && hour < 11) {
            recipeType = Recipe.RecipeType.BREAKFAST;
        } else if(hour >= 11 && hour < 14) {
            recipeType = Recipe.RecipeType.LUNCH;
        } else if(hour >= 17 && hour < 22) {
            recipeType = Recipe.RecipeType.DINNER;
        } else {
            recipeType = Recipe.RecipeType.SNACK;
        }
        recipes = mRecipeDao.getAllRecipesOfType(recipeType);
        if(hour == 11) {
            recipes.addAll(mRecipeDao.getAllRecipesOfType(Recipe.RecipeType.BREAKFAST));
        }
        if(hour == 10) {
            recipes.addAll(mRecipeDao.getAllRecipesOfType(Recipe.RecipeType.LUNCH));
        }
        if(hour >= 18) {
            recipes.addAll(mRecipeDao.getAllRecipesOfType(Recipe.RecipeType.DRINK));
        }
        if(hour >= 16) {
            recipes.addAll(mRecipeDao.getAllRecipesOfType(Recipe.RecipeType.DESSERT));
        }
        if(hour == 13 || hour == 17) {
            recipes.addAll(mRecipeDao.getAllRecipesOfType(Recipe.RecipeType.SNACK));
        }
    }

    @Override
    public void onDestroy() {
        recipes.clear();
    }

    @Override
    public int getCount() {
        if(recipes != null) {
            return recipes.size();
        }
        return 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_adapter_layout);
        Recipe current = recipes.get(position);
        rv.setTextViewText(R.id.recipe_text_view, current.getRecipe());
        rv.setTextViewText(R.id.time_text_view,
                mContext.getResources().getString(R.string.recipe_time_description,
                Utils.secondsToTimeString(current.getPrepTime()),
                Utils.secondsToTimeString(Utils.getTotalTimeFromTimers(current.getTimers()) + current.getPrepTime())
        ));

        Intent fillinIntent = new Intent();
        fillinIntent.putExtra(StartRecipeActivity.EXTRA_NOTIFICATION, false);
        fillinIntent.putExtra(StartRecipeActivity.EXTRA_RECIPE, current.getRecipe());
        fillinIntent.putExtra(StartRecipeActivity.EXTRA_INGREDIENTS, current.getIngredients());
        fillinIntent.putExtra(StartRecipeActivity.EXTRA_STEPS, current.getSteps());
        fillinIntent.putExtra(StartRecipeActivity.EXTRA_TIMERS, current.getTimers());
        rv.setOnClickFillInIntent(R.id.recipe_text_view, fillinIntent);
        rv.setOnClickFillInIntent(R.id.time_text_view, fillinIntent);
        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
