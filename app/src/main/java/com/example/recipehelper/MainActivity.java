package com.example.recipehelper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String EXTRA_NEW_RECIPE = "com.example.recipehelper.MainActivity.extra.NEW_RECIPE";
    public static final String EXTRA_RECIPE = "com.example.recipehelper.MainActivity.extra.RECIPE";
    public static final String EXTRA_INGREDIENTS = "com.example.recipehelper.MainActivity.extra.INGREDIENTS";
    public static final String EXTRA_STEPS = "com.example.recipehelper.MainActivity.extra.STEPS";
    public static final String EXTRA_TIMERS = "com.example.recipehelper.MainActivity.extra.TIMERS";
    public static final String EXTRA_PREP = "com.example.recipehelper.MainActivity.extra.PREP";
    public static final String EXTRA_TYPE = "com.example.recipehelper.MainActivity.extra.TYPE";

    private static final int REQUEST_NOTIFICATIONS = 0;

    RecipeViewModel mRecipeViewModel;
    RecipeListAdapter adapter;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    ActivityResultLauncher<Intent> mStartEditActivityForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getResultCode() == RESULT_OK){
            Intent intent = result.getData();
            if(intent == null) return;
            //get edited data
            boolean isNewRecipe = intent.getBooleanExtra(EXTRA_NEW_RECIPE, true);
            String recipe = intent.getStringExtra(EXTRA_RECIPE);
            String ingredients = intent.getStringExtra(EXTRA_INGREDIENTS);
            String steps = intent.getStringExtra(EXTRA_STEPS);
            String timers = intent.getStringExtra(EXTRA_TIMERS);
            int prepTime = intent.getIntExtra(EXTRA_PREP, 0);
            int recipeType = intent.getIntExtra(EXTRA_TYPE, Recipe.RecipeType.UNSPECIFIED);
            if(isNewRecipe) {
                mRecipeViewModel.insert(new Recipe(recipe, ingredients, steps, timers, prepTime, recipeType));
                Toast.makeText(MainActivity.this, getResources().getString(R.string.new_recipe_toast, recipe), Toast.LENGTH_SHORT).show();
            } else {
                mRecipeViewModel.changeIngredients(ingredients, recipe);
                mRecipeViewModel.changeSteps(steps, recipe);
                mRecipeViewModel.changeTimers(timers, recipe);
                mRecipeViewModel.changePrepTime(prepTime, recipe);
                mRecipeViewModel.changeRecipeType(recipeType, recipe);
                Toast.makeText(MainActivity.this, getResources().getString(R.string.saved_recipe_toast, recipe), Toast.LENGTH_SHORT).show();
            }
        }
    });

    ActivityResultLauncher<Intent> mStartRecipeActivityForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getResultCode() == RESULT_OK){
            Intent intent = result.getData();
            if(intent == null) return;
            //get edited data
        }
    });

    @Override
    protected void onStop() {
        super.onStop();
        if(mAccelerometer != null) {
            mSensorManager.unregisterListener(mShakeDetector);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAccelerometer != null) {
            mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Random random = new Random();

        mRecipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.recipe_recycleview);
        adapter = new RecipeListAdapter(this);
        adapter.setOnItemClickListener((view, position) -> {
            Log.d(TAG, "clicked: " + position);
            //click to start new recipe
            Recipe recipe = adapter.getRecipeAtPosition(position);
            Intent intent = new Intent(MainActivity.this, StartRecipeActivity.class);
            intent.putExtra(StartRecipeActivity.EXTRA_NOTIFICATION, false);
            intent.putExtra(StartRecipeActivity.EXTRA_RECIPE, recipe.getRecipe());
            intent.putExtra(StartRecipeActivity.EXTRA_INGREDIENTS, recipe.getIngredients());
            intent.putExtra(StartRecipeActivity.EXTRA_STEPS, recipe.getSteps());
            intent.putExtra(StartRecipeActivity.EXTRA_TIMERS, recipe.getTimers());
            mStartRecipeActivityForResult.launch(intent);
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecipeViewModel.getAllRecipes().observe(this, adapter::setRecipes);

        ItemTouchHelper helper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }
                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
                                         int direction) {
                        int position = viewHolder.getAdapterPosition();
                        //refreshes item to reappear
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemInserted(position);
                        Recipe recipe = adapter.getRecipeAtPosition(position);
                        //item swiped opens alert dialog for editing or deleting
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                        alertDialogBuilder.setTitle(getResources().getString(R.string.edit_or_delete_dialog, recipe.getRecipe()));
                        if((getResources().getConfiguration().uiMode &
                                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES){
                            alertDialogBuilder.setIcon(R.drawable.ic_action_edit_icon_dark);
                        } else {
                            alertDialogBuilder.setIcon(R.drawable.ic_action_edit_icon);
                        }
                        alertDialogBuilder.setPositiveButton(R.string.delete, (arg0, arg1) -> {
                            //delete
                            mRecipeViewModel.deleteRecipe(recipe);
                            Snackbar.make(findViewById(R.id.recipe_recycleview) , getResources().getString(R.string.delete_recipe_snackbar, recipe.getRecipe()), Snackbar.LENGTH_LONG)
                                    .setAction(R.string.undo, view ->
                                            mRecipeViewModel.insert(recipe)).show();
                        });
                        alertDialogBuilder.setNegativeButton(R.string.edit, (dialog, which) -> {
                            //edit
                            Intent intent = new Intent(MainActivity.this, EditRecipeActivity.class);
                            intent.putExtra(EditRecipeActivity.EXTRA_NEW_RECIPE, false);
                            intent.putExtra(EditRecipeActivity.EXTRA_RECIPE, recipe.getRecipe());
                            intent.putExtra(EditRecipeActivity.EXTRA_INGREDIENTS, recipe.getIngredients());
                            intent.putExtra(EditRecipeActivity.EXTRA_STEPS, recipe.getSteps());
                            intent.putExtra(EditRecipeActivity.EXTRA_TIMERS, recipe.getTimers());
                            intent.putExtra(EditRecipeActivity.EXTRA_PREP, recipe.getPrepTime());
                            intent.putExtra(EditRecipeActivity.EXTRA_TYPE, recipe.getRecipeType());
                            mStartEditActivityForResult.launch(intent);
                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                });
        helper.attachToRecyclerView(recyclerView);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if(mSensorManager != null) {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(() -> {
            //on shake, get random recipe
            Recipe recipe = adapter.getRecipeAtPosition(random.nextInt(adapter.getItemCount()));
            Intent intent = new Intent(MainActivity.this, StartRecipeActivity.class);
            intent.putExtra(StartRecipeActivity.EXTRA_NOTIFICATION, false);
            intent.putExtra(StartRecipeActivity.EXTRA_RECIPE, recipe.getRecipe());
            intent.putExtra(StartRecipeActivity.EXTRA_INGREDIENTS, recipe.getIngredients());
            intent.putExtra(StartRecipeActivity.EXTRA_STEPS, recipe.getSteps());
            intent.putExtra(StartRecipeActivity.EXTRA_TIMERS, recipe.getTimers());
            mStartRecipeActivityForResult.launch(intent);
        });

        if(savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_DENIED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.POST_NOTIFICATIONS)){
                Snackbar.make(findViewById(R.id.recipe_recycleview), R.string.notifs_on_request, Snackbar.LENGTH_LONG)
                        .setAction(R.string.ok, (view) -> {
                            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATIONS);
                        }).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATIONS);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add) {
            createNewRecipePopup();
            return true;
        } else if (id == R.id.action_delete_all){
            //delete all
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setTitle(R.string.delete_all_recipes_title);
            if((getResources().getConfiguration().uiMode &
                    Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES){
                alertDialogBuilder.setIcon(R.drawable.ic_action_warning_dark);
            } else {
                alertDialogBuilder.setIcon(R.drawable.ic_action_warning);
            }
            alertDialogBuilder.setMessage(R.string.delete_all_recipes_subtitle);
            alertDialogBuilder.setPositiveButton(R.string.delete, (arg0, arg1) -> {
                //delete
                mRecipeViewModel.deleteAll();
                Toast.makeText(MainActivity.this, R.string.all_recipes_deleted, Toast.LENGTH_SHORT).show();
            });
            alertDialogBuilder.setNegativeButton(R.string.cancel, (dialog, which) -> {});
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void createNewRecipePopup() {
        AlertDialog.Builder popupBuilder = new AlertDialog.Builder(MainActivity.this);
        popupBuilder.setTitle(R.string.new_recipe_dialog_title);
        View popupView = getLayoutInflater().inflate(R.layout.new_recipe_popup_layout, null);
        EditText recipeEditText = popupView.findViewById(R.id.edit_text_recipe);
        Button addRecipe = popupView.findViewById(R.id.apply_new_recipe_button);
        Button cancel = popupView.findViewById(R.id.recipe_cancel_button);
        popupBuilder.setView(popupView);
        AlertDialog alertDialog = popupBuilder.create();
        alertDialog.show();

        addRecipe.setOnClickListener((view) -> {
            if (!(recipeEditText.getText().toString().trim().equals(""))) {
                Intent intent = new Intent(MainActivity.this, EditRecipeActivity.class);
                intent.putExtra(EditRecipeActivity.EXTRA_NEW_RECIPE, true);
                intent.putExtra(EditRecipeActivity.EXTRA_RECIPE, recipeEditText.getText().toString().trim());
                mStartEditActivityForResult.launch(intent);
                alertDialog.dismiss();
            }
        });

        cancel.setOnClickListener((view) -> alertDialog.dismiss());
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        HashMap<String, Integer> perms = new HashMap<>();
        switch(requestCode){
            case REQUEST_NOTIFICATIONS:
                perms.put(android.Manifest.permission.POST_NOTIFICATIONS, PackageManager.PERMISSION_GRANTED);
                if(grantResults.length > 0){
                    for(int i = 0; i < permissions.length; i++){
                        perms.put(permissions[i], grantResults[i]);
                    }
                    if(perms.get(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, R.string.notifications_enabled, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.notifications_disabled, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }

    }

}