package com.example.recipehelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class EditRecipeActivity extends AppCompatActivity {
    private static final String TAG = EditRecipeActivity.class.getSimpleName();

    public static final String EXTRA_NEW_RECIPE = "com.example.recipehelper.EditRecipeActivity.extra.NEW_RECIPE";
    public static final String EXTRA_RECIPE = "com.example.recipehelper.EditRecipeActivity.extra.RECIPE";
    public static final String EXTRA_INGREDIENTS = "com.example.recipehelper.EditRecipeActivity.extra.INGREDIENTS";
    public static final String EXTRA_STEPS = "com.example.recipehelper.EditRecipeActivity.extra.STEPS";
    public static final String EXTRA_TIMERS = "com.example.recipehelper.EditRecipeActivity.extra.TIMERS";
    public static final String EXTRA_PREP = "com.example.recipehelper.EditRecipeActivity.extra.PREP";
    public static final String EXTRA_TYPE = "com.example.recipehelper.EditRecipeActivity.extra.TYPE";
    private boolean isNewRecipe;
    private String recipeName;
    private ArrayList<String> ingredients;
    private ArrayList<String> steps;
    private ArrayList<Integer> timers;
    private int prepTime;
    private int recipeType;

    private TextView tvRecipe;
    private Spinner recipeSpinner;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FragmentStateAdapter pagerAdapter;

    private FloatingActionButton fabSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        Intent intent = getIntent();
        isNewRecipe = intent.getBooleanExtra(EXTRA_NEW_RECIPE, true);
        recipeName = intent.getStringExtra(EXTRA_RECIPE);
        if(!isNewRecipe) {
            ingredients = new ArrayList<>(Arrays.asList(intent.getStringExtra(EXTRA_INGREDIENTS).split("\n")));
            if (ingredients.size() == 1 && ingredients.get(0).equals("")) {
                ingredients.clear();
            }
            steps = new ArrayList<>(Arrays.asList(intent.getStringExtra(EXTRA_STEPS).split("\n")));
            if (steps.size() == 1 && steps.get(0).equals("")) {
                steps.clear();
            }
            String timersString = intent.getStringExtra(EXTRA_TIMERS);
            timers = new ArrayList<>();
            if (!Utils.parseTimerString(timersString, timers)) {
                timers.clear();
            }
            prepTime = intent.getIntExtra(EXTRA_PREP, 0);
            recipeType = intent.getIntExtra(EXTRA_TYPE, Recipe.RecipeType.UNSPECIFIED);
        } else {
            setTitle(R.string.edit_recipe_activity_alternate_label);
            ingredients = new ArrayList<>();
            steps = new ArrayList<>();
            timers = new ArrayList<>();
            prepTime = 0;
            recipeType = Recipe.RecipeType.UNSPECIFIED;
        }

        tvRecipe = findViewById(R.id.tv_recipe_name_editing);
        tvRecipe.setText(recipeName);

        recipeSpinner = findViewById(R.id.recipe_type_spinner);
        String[] array = getResources().getStringArray(R.array.recipe_type_array);
        ArrayAdapter<CharSequence> spinnerAdapter = new ArrayAdapter<CharSequence>(this, R.layout.spinner_layout, array) {
            @Override
            public int getCount() {
                return array.length - 1;
            }
        };
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        recipeSpinner.setAdapter(spinnerAdapter);
        if(isNewRecipe) {
            recipeSpinner.setSelection(array.length - 1);
        } else {
            //set selection
            recipeSpinner.setSelection(recipeType);
        }
        recipeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                recipeType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        fabSave = findViewById(R.id.save_button);
        fabSave.setOnClickListener((view) -> {
            //save button click
            EditIngredientsFragment ingFragment = (EditIngredientsFragment)
                    getSupportFragmentManager()
                            .findFragmentByTag("f" + pagerAdapter.getItemId(0));
            if(ingFragment != null) {
                prepTime = ingFragment.getPrepTime();
                ingredients = ingFragment.getIngredients();
            }
            EditStepsTimersFragment stepTimerFragment = (EditStepsTimersFragment)
                    getSupportFragmentManager()
                            .findFragmentByTag("f" + pagerAdapter.getItemId(1));
            if(stepTimerFragment != null) {
                steps = stepTimerFragment.getSteps();
                timers = stepTimerFragment.getTimers();
            }
            if(recipeType >= recipeSpinner.getCount()){
                recipeType = Recipe.RecipeType.UNSPECIFIED;
            }
            Intent outIntent = new Intent();
            outIntent.putExtra(MainActivity.EXTRA_NEW_RECIPE, isNewRecipe);
            outIntent.putExtra(MainActivity.EXTRA_RECIPE, recipeName);
            outIntent.putExtra(MainActivity.EXTRA_INGREDIENTS, Utils.ingsOrStepsToString(ingredients));
            outIntent.putExtra(MainActivity.EXTRA_STEPS, Utils.ingsOrStepsToString(steps));
            outIntent.putExtra(MainActivity.EXTRA_TIMERS, Utils.timersToString(timers));
            outIntent.putExtra(MainActivity.EXTRA_PREP, prepTime);
            outIntent.putExtra(MainActivity.EXTRA_TYPE, recipeType);
            setResult(RESULT_OK, outIntent);
            finish();
        });

        tabLayout = findViewById(R.id.edit_recipe_tabs);
        TabLayout.Tab tab1 = tabLayout.newTab();
        tab1.setText(R.string.ingredients_tab);
        tabLayout.addTab(tab1);
        TabLayout.Tab tab2 = tabLayout.newTab();
        tab2.setText(R.string.steps_tab);
        tabLayout.addTab(tab2);

        viewPager = findViewById(R.id.edit_recipe_view_pager);
        pagerAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), getLifecycle(),
                ingredients, steps, timers, prepTime);
        viewPager.setAdapter(pagerAdapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(isNewRecipe){
            if(recipeType <= Recipe.RecipeType.UNSPECIFIED) {
                unsavedChangesPopup();
                return;
            }
            EditIngredientsFragment ingFragment = (EditIngredientsFragment)
                    getSupportFragmentManager()
                            .findFragmentByTag("f" + pagerAdapter.getItemId(0));
            if (ingFragment != null && (ingFragment.getPrepTime() > 0 ||
                    ingFragment.getIngredients().size() > 0)) {
                unsavedChangesPopup();
                return;
            }
            EditStepsTimersFragment stepTimerFragment = (EditStepsTimersFragment)
                    getSupportFragmentManager()
                            .findFragmentByTag("f" + pagerAdapter.getItemId(1));
            if (stepTimerFragment != null && (stepTimerFragment.getSteps().size() > 0 ||
                    stepTimerFragment.getTimers().size() > 0)) {
                unsavedChangesPopup();
                return;
            }
        } else {
            Intent init = getIntent();
            if (init.getIntExtra(EXTRA_TYPE, Recipe.RecipeType.UNSPECIFIED) != recipeType) {
                unsavedChangesPopup();
                return;
            }
            EditIngredientsFragment ingFragment = (EditIngredientsFragment)
                    getSupportFragmentManager()
                            .findFragmentByTag("f" + pagerAdapter.getItemId(0));
            if (ingFragment != null && (prepTime != ingFragment.getPrepTime() ||
                    !init.getStringExtra(EXTRA_INGREDIENTS)
                            .equals(Utils.ingsOrStepsToString(ingFragment.getIngredients())))) {
                unsavedChangesPopup();
                return;
            }
            EditStepsTimersFragment stepTimerFragment = (EditStepsTimersFragment)
                    getSupportFragmentManager()
                            .findFragmentByTag("f" + pagerAdapter.getItemId(1));
            if (stepTimerFragment != null && (!init.getStringExtra(EXTRA_STEPS)
                    .equals(Utils.ingsOrStepsToString(stepTimerFragment.getSteps())) ||
                    !init.getStringExtra(EXTRA_TIMERS)
                            .equals(Utils.timersToString(stepTimerFragment.getTimers())))) {
                unsavedChangesPopup();
                return;
            }
        }
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        super.onBackPressed();
    }

    private void unsavedChangesPopup() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.warning);
        if((getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES){
            alertDialogBuilder.setIcon(R.drawable.ic_action_warning_dark);
        } else {
            alertDialogBuilder.setIcon(R.drawable.ic_action_warning);
        }
        alertDialogBuilder.setMessage(R.string.unsaved_changes_message);
        alertDialogBuilder.setPositiveButton(R.string.exit_without_saving, (arg0, arg1) -> {
            //exit without saving
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        });
        alertDialogBuilder.setNegativeButton(R.string.cancel, (dialog, which) -> {});
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}
