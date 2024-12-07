package com.example.recipehelper.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.recipehelper.adapter.CheckboxListAdapter;
import com.example.recipehelper.notifications.NotificationsManager;
import com.example.recipehelper.R;
import com.example.recipehelper.fragment.TimerFragment;
import com.example.recipehelper.Utils;
import com.example.recipehelper.adapter.ViewPagerRecyclerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;

public class StartRecipeActivity extends AppCompatActivity implements TimerFragment.Listener {
    private static final String TAG = StartRecipeActivity.class.getSimpleName();
    public static final String EXTRA_RECIPE = "com.example.recipehelper.activity.StartRecipeActivity.extra.RECIPE";
    public static final String EXTRA_INGREDIENTS = "com.example.recipehelper.activity.StartRecipeActivity.extra.INGREDIENTS";
    public static final String EXTRA_STEPS = "com.example.recipehelper.activity.StartRecipeActivity.extra.STEPS";
    public static final String EXTRA_TIMERS = "com.example.recipehelper.activity.StartRecipeActivity.extra.TIMERS";
    public static final String EXTRA_NOTIFICATION = "com.example.recipehelper.activity.StartRecipeActivity.extra.NOTIFICATION";
    public static final String ACTION_PAUSE_TIMER = "com.example.recipehelper.action.PAUSE_TIMER";
    public static final String ACTION_RESUME_TIMER = "com.example.recipehelper.action.RESUME_TIMER";
    public static final String ACTION_STOP_TIMER = "com.example.recipehelper.action.STOP_TIMER";

    private static final String START_RECIPE_CHECKED_KEY = "com.example.recipehelper.activity.StartRecipeActivity.start_recipe_checked";
    private static final String START_RECIPE_BUTTON_CLICKED_KEY = "com.example.recipehelper.activity.StartRecipeActivity.start_recipe_button_clicked";
    private static final String START_TIMER_BUTTON_CLICKED_KEY = "com.example.recipehelper.activity.StartRecipeActivity.start_timer_button_clicked";
    private static final String ADAPTER_POSITION_KEY = "com.example.recipehelper.activity.StartRecipeActivity.adapter_position";
    private static final String FRAGMENT_TAG = "TimerFragmentTag";
    private String recipeName;
    private ArrayList<String> ingredients;
    private boolean[] checked;
    private ArrayList<String> steps;
    private ArrayList<Integer> timers;

    Button beginButton;
    CheckboxListAdapter adapter;
    ViewPager2 stepsViewPager;

    ViewPagerRecyclerAdapter pagerAdapter;

    FrameLayout fragmentHolder;

    FragmentManager timerFragManager;

    NotificationsManager notificationsManager;


    final BroadcastReceiver mNotifReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            TimerFragment tf = (TimerFragment) timerFragManager.findFragmentByTag(FRAGMENT_TAG);
            switch(action) {
                case ACTION_PAUSE_TIMER:
                    //pause timer
                    if (tf != null) {
                        tf.pauseTimer();
                    }
                    break;
                case ACTION_STOP_TIMER:
                    //stop timer
                    if (tf != null) {
                        tf.stopTimer();
                    }
                    break;
                case ACTION_RESUME_TIMER:
                    if(tf != null) {
                        notificationsManager.updateTimerNotification(tf.getTime());
                        tf.resumeTimer();
                    }
                default:
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ACTION_STOP_TIMER);
        filter.addAction(ACTION_PAUSE_TIMER);
        filter.addAction(ACTION_RESUME_TIMER);
        registerReceiver(mNotifReceiver, filter);
    }



    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onsaveinstancestate");
        outState.putBooleanArray(START_RECIPE_CHECKED_KEY, checked);
        outState.putBoolean(START_RECIPE_BUTTON_CLICKED_KEY, beginButton.getVisibility() == View.GONE);
        outState.putBoolean(START_TIMER_BUTTON_CLICKED_KEY, fragmentHolder.getVisibility() == View.VISIBLE);
        outState.putInt(ADAPTER_POSITION_KEY, stepsViewPager.getCurrentItem());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if(!intent.getBooleanExtra(EXTRA_NOTIFICATION, false) && !intent.getStringExtra(EXTRA_RECIPE).equals(recipeName)){
            recipeName = intent.getStringExtra(EXTRA_RECIPE);
            setTitle(recipeName);
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

            checked = new boolean[ingredients.size()];
            RecyclerView recyclerView = findViewById(R.id.checkbox_recycler_view);
            adapter = new CheckboxListAdapter(this, ingredients, checked);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            pagerAdapter = new ViewPagerRecyclerAdapter(this, steps, timers);
            pagerAdapter.setOnItemClickListener((view, position, hasTime) -> {
                notificationsManager.cancelTimeUpNotification();
                //start timer fragment
                if(hasTime) {
                    stepsViewPager.setVisibility(View.GONE);
                    fragmentHolder.setVisibility(View.VISIBLE);
                    TimerFragment tf = (TimerFragment) timerFragManager.findFragmentByTag(FRAGMENT_TAG);
                    if (tf != null) {
                        tf.startTimer(pagerAdapter.getTimeAtPosition(position));
                    }
                    notificationsManager.sendTimerNotification(position, pagerAdapter.getTimeAtPosition(position));
                } else {
                    if(stepsViewPager.getCurrentItem() == pagerAdapter.getItemCount() - 1){
                        //finish
                        createFinishPopup();
                    } else {
                        stepsViewPager.setCurrentItem(stepsViewPager.getCurrentItem() + 1);
                    }
                }
            });
            stepsViewPager.setAdapter(pagerAdapter);

            TimerFragment tf = (TimerFragment) timerFragManager.findFragmentByTag(FRAGMENT_TAG);
            if (tf != null) {
                tf.stopTimer();
            }

            beginButton.setVisibility(View.VISIBLE);
            stepsViewPager.setVisibility(View.GONE);
            fragmentHolder.setVisibility(View.GONE);

            notificationsManager.cancelTimeUpNotification();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_recipe);

        Intent intent = getIntent();
        if(intent.getBooleanExtra(EXTRA_NOTIFICATION, false)){
            Intent onStrayNotification = new Intent(StartRecipeActivity.this, MainActivity.class);
            startActivity(onStrayNotification);
            finish();
            return;
        }
        recipeName = intent.getStringExtra(EXTRA_RECIPE);
        setTitle(recipeName);
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


        beginButton = findViewById(R.id.begin_button);
        beginButton.setOnClickListener((view) -> {
            //BEGIN BUTTON
            if(arrayHasFalse(checked)){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(StartRecipeActivity.this);
                alertDialogBuilder.setMessage(R.string.unchecked_ingredients_warning);
                alertDialogBuilder.setTitle(R.string.warning);
                if((getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES){
                    alertDialogBuilder.setIcon(R.drawable.ic_action_warning_dark);
                } else {
                    alertDialogBuilder.setIcon(R.drawable.ic_action_warning);
                }
                alertDialogBuilder.setPositiveButton(R.string.yes, (arg0, arg1) -> {
                    //continue
                    beginButton.setVisibility(View.GONE);
                    stepsViewPager.setVisibility(View.VISIBLE);
                });
                alertDialogBuilder.setNegativeButton(R.string.no, (dialog, which) -> {});
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            } else {
                beginButton.setVisibility(View.GONE);
                stepsViewPager.setVisibility(View.VISIBLE);
            }
        });

        stepsViewPager = findViewById(R.id.recipe_steps_view_pager);

        fragmentHolder = findViewById(R.id.time_fragment_frame);

        if(savedInstanceState != null) {
            checked = savedInstanceState.getBooleanArray(START_RECIPE_CHECKED_KEY);
            if(savedInstanceState.getBoolean(START_RECIPE_BUTTON_CLICKED_KEY)){
                beginButton.setVisibility(View.GONE);
                if(savedInstanceState.getBoolean(START_TIMER_BUTTON_CLICKED_KEY)){
                    stepsViewPager.setVisibility(View.GONE);
                    fragmentHolder.setVisibility(View.VISIBLE);
                } else {
                    fragmentHolder.setVisibility(View.GONE);
                    stepsViewPager.setVisibility(View.VISIBLE);
                }
            } else {
                beginButton.setVisibility(View.VISIBLE);
                stepsViewPager.setVisibility(View.GONE);
                fragmentHolder.setVisibility(View.GONE);
            }
        } else {
            checked = new boolean[ingredients.size()];
        }

        notificationsManager = new NotificationsManager(this);

        RecyclerView recyclerView = findViewById(R.id.checkbox_recycler_view);
        adapter = new CheckboxListAdapter(this, ingredients, checked);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        pagerAdapter = new ViewPagerRecyclerAdapter(this, steps, timers);
        pagerAdapter.setOnItemClickListener((view, position, hasTime) -> {
            notificationsManager.cancelTimeUpNotification();
            //start timer fragment
            if(hasTime) {
                stepsViewPager.setVisibility(View.GONE);
                fragmentHolder.setVisibility(View.VISIBLE);
                TimerFragment tf = (TimerFragment) timerFragManager.findFragmentByTag(FRAGMENT_TAG);
                if (tf != null) {
                    tf.startTimer(pagerAdapter.getTimeAtPosition(position));
                }
                notificationsManager.sendTimerNotification(position, pagerAdapter.getTimeAtPosition(position));
            } else {
                if(stepsViewPager.getCurrentItem() == pagerAdapter.getItemCount() - 1){
                    //finish
                    createFinishPopup();
                } else {
                    stepsViewPager.setCurrentItem(stepsViewPager.getCurrentItem() + 1);
                }
            }
        });
        stepsViewPager.setAdapter(pagerAdapter);

        if(savedInstanceState != null) {
            notificationsManager.setStepPosition(savedInstanceState.getInt(ADAPTER_POSITION_KEY));
        }

        timerFragManager = getSupportFragmentManager();
        if(savedInstanceState == null){
            timerFragManager.beginTransaction()
                    .add(R.id.time_fragment_frame, new TimerFragment(), FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                if (id == R.id.action_show_all_steps){
                    allStepsPopup(beginButton.getVisibility() != View.VISIBLE, stepsViewPager.getCurrentItem());
                    return true;
                }
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        notificationsManager.cancelTimerNotification();
        notificationsManager.cancelTimeUpNotification();
        super.onBackPressed();
    }

    @Override
    public void onClick(TimerFragment.ButtonClicked buttonClicked) {
        TimerFragment tf = (TimerFragment) timerFragManager.findFragmentByTag(FRAGMENT_TAG);
        switch(buttonClicked){
            case STOP:
                fragmentHolder.setVisibility(View.GONE);
                stepsViewPager.setVisibility(View.VISIBLE);
                notificationsManager.cancelTimerNotification();
                break;
            case PAUSE:
                if(tf != null) {
                    notificationsManager.updateTimerOnPauseNotification(tf.getTime());
                }
                break;
            case START:
                if(tf != null) {
                    notificationsManager.updateTimerNotification(tf.getTime());
                }
                break;
        }
    }

    @Override
    public void onTick(int newTime) {
        notificationsManager.updateTimerNotification(newTime);
    }


    @Override
    public void onTimeUp() {
        notificationsManager.cancelTimerNotification();
        notificationsManager.sendTimeUpNotification();
        fragmentHolder.setVisibility(View.GONE);
        stepsViewPager.setVisibility(View.VISIBLE);
        if(stepsViewPager.getCurrentItem() == pagerAdapter.getItemCount() - 1){
            //finish
            createFinishPopup();
        } else {
            stepsViewPager.setCurrentItem(stepsViewPager.getCurrentItem() + 1);
        }
    }

    @Override
    public void onTimeClose(int time) {
        //on time close notification
        notificationsManager.sendTimeCloseNotification(time);
    }

    @Override
    public void onRestoreNotificationProgress(int maxTime) {
        notificationsManager.setMaxTime(maxTime);
    }

    public void createFinishPopup() {
        AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this);
        View popupView = getLayoutInflater().inflate(R.layout.finish_recipe_popup_layout, null);
        Button startOverButton = popupView.findViewById(R.id.start_over_button);
        popupBuilder.setView(popupView);
        AlertDialog alertDialog = popupBuilder.create();
        alertDialog.setOnDismissListener((view) -> {
            beginButton.setVisibility(View.VISIBLE);
            stepsViewPager.setVisibility(View.GONE);
            fragmentHolder.setVisibility(View.GONE);
            stepsViewPager.setCurrentItem(0);
            Arrays.fill(checked, false);
            adapter.notifyItemRangeChanged(0, ingredients.size());
        });
        startOverButton.setOnClickListener((view) -> alertDialog.dismiss());
        alertDialog.show();
    }

    public void allStepsPopup(boolean viewingSteps, int stepNum) {
        AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this);
        View popupView = getLayoutInflater().inflate(R.layout.all_steps_popup_layout, null);
        TextView tv = popupView.findViewById(R.id.all_steps_tv);
        Executors.newSingleThreadExecutor().submit(() -> {
            int i = 0;
            SpannableStringBuilder sb = new SpannableStringBuilder();
            for(String step : steps) {
                if(viewingSteps && i == stepNum){
                    int begin = sb.length();
                    sb.append(String.valueOf(++i)).append(") ").append(step).append("\n");
                    sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), begin,
                            sb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                } else {
                    sb.append(String.valueOf(++i)).append(") ").append(step).append("\n");
                }
            }
            sb.delete(sb.length() - 1, sb.length());
            runOnUiThread(() -> tv.setText(sb));
        });
        tv.setMovementMethod(new ScrollingMovementMethod());
        popupBuilder.setView(popupView);
        AlertDialog alertDialog = popupBuilder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipe_activity, menu);

        return true;
    }

    private boolean arrayHasFalse(boolean[] arr) {
        for(boolean value : arr) {
            if(!value)
                return true;
        }
        return false;
    }
}