package com.example.recipehelper;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TimerFragment extends Fragment {

    private static final String TAG = TimerFragment.class.getSimpleName();
    private static final String TIME_KEY = "com.example.recipehelper.TimerFragment.time";
    private static final String MAX_TIME_KEY = "com.example.recipehelper.TimerFragment.max_time";
    private static final String PAUSED_KEY = "com.example.recipehelper.TimerFragment.paused";
    private static final String ARG_TIME = "time";

    public enum ButtonClicked {
        STOP, PAUSE, START
    }

    public interface Listener {
        void onClick(ButtonClicked buttonClicked);
        void onTick(int newTime);
        void onTimeUp();
        void onTimeClose(int time);
        void onRestoreNotificationProgress(int maxTime);
    }
    private Listener onFragmentActivityListener;

    View mView;
    AtomicInteger atomicTime = new AtomicInteger(0);
    TimerProgressView timer;
    Button stopButton;
    Button pauseButton;
    Handler main;
    ScheduledExecutorService scheduler;
    boolean paused;

    public static TimerFragment newInstance(int time) {
        TimerFragment fragment = new TimerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TIME, time);
        fragment.setArguments(args);
        return fragment;
    }


    public TimerFragment() {}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        main = new Handler(Looper.getMainLooper());
        mView = inflater.inflate(R.layout.fragment_timer, container, false);
        timer = mView.findViewById(R.id.tv_time_left);
        pauseButton = mView.findViewById(R.id.pause_timer_button);
        paused = false;
        if(savedInstanceState != null) {
            atomicTime = new AtomicInteger(savedInstanceState.getInt(TIME_KEY));
            timer.setCurrentTime(atomicTime.get(), false);
            timer.setMaxTime(savedInstanceState.getInt(MAX_TIME_KEY));
            timer.setCloseTimeThreshold(Math.min(timer.getMaxTime() / 6, 120));
            if(onFragmentActivityListener != null) {
                onFragmentActivityListener.onRestoreNotificationProgress(timer.getMaxTime());
            }
            if(savedInstanceState.getBoolean(PAUSED_KEY)) {
                paused = true;
                pauseButton.setText(getResources().getString(R.string.resume_timer_label));
            } else {
                if (atomicTime.get() > 0) {
                    resumeTimer();
                }
            }
        }
        timer.setOnTimerCloseListener((time) -> {
            if(onFragmentActivityListener != null) {
                onFragmentActivityListener.onTimeClose(atomicTime.get());
            }
        });
        stopButton = mView.findViewById(R.id.finish_timer_button);
        stopButton.setOnClickListener((view) ->
            stopTimer());
        pauseButton.setOnClickListener((view) -> {
            if(paused) {
                if(onFragmentActivityListener != null) {
                    onFragmentActivityListener.onClick(ButtonClicked.START);
                }
                resumeTimer();
            } else {
                pauseTimer();
            }
        });
        return mView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof Listener)
            onFragmentActivityListener = (Listener) context;

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TIME_KEY, atomicTime.get());
        outState.putBoolean(PAUSED_KEY, paused);
        outState.putInt(MAX_TIME_KEY, timer.getMaxTime());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(scheduler != null){
            scheduler.shutdownNow();
        }
        onFragmentActivityListener = null;
    }

    public void pauseTimer() {
        paused = true;
        pauseButton.setText(getResources().getString(R.string.resume_timer_label));
        if(scheduler != null){
            scheduler.shutdownNow();
        }
        if(onFragmentActivityListener != null){
            onFragmentActivityListener.onClick(ButtonClicked.PAUSE);
        }
    }

    public void resumeTimer() {
        paused = false;
        pauseButton.setText(getResources().getString(R.string.pause_timer_label));
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            int newTime = atomicTime.decrementAndGet();
            main.post(() ->
                    timer.decrementTimer());
            if(newTime < 1) {
                if(onFragmentActivityListener != null){
                    main.post(() ->
                        onFragmentActivityListener.onTimeUp());
                }
                scheduler.shutdown();
            } else if(onFragmentActivityListener != null) {
                main.post(() ->
                    onFragmentActivityListener.onTick(newTime));
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    public void stopTimer() {
        if(scheduler != null){
            scheduler.shutdownNow();
        }
        if(onFragmentActivityListener != null){
            onFragmentActivityListener.onClick(ButtonClicked.STOP);
        }
        atomicTime.set(0);
    }

    public void startTimer(int time) {
        atomicTime = new AtomicInteger(time);
        timer.setCurrentTime(time, false);
        timer.setMaxTime(time);
        timer.setCloseTimeThreshold(Math.min(time / 6, 120));
        resumeTimer();
    }

    public int getTime() {
        return atomicTime.get();
    }

    public int getMaxTime() {
        return timer.getMaxTime();
    }
}