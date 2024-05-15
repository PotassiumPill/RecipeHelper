package com.example.recipehelper;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class EditStepsTimersFragment extends Fragment {

    private static final String ARG_STEPS = "steps";
    private static final String ARG_TIMERS = "timers";

    private ArrayList<String> mSteps;
    private ArrayList<Integer> mTimers;

    private View mView;
    private FloatingActionButton addStepTimerButton;

    EditStepsTimersListAdapter adapter;



    public EditStepsTimersFragment() {}

    public static EditStepsTimersFragment newInstance(ArrayList<String> steps, ArrayList<Integer> timers) {
        EditStepsTimersFragment fragment = new EditStepsTimersFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_STEPS, steps);
        args.putIntegerArrayList(ARG_TIMERS, timers);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSteps = getArguments().getStringArrayList(ARG_STEPS);
            mTimers = getArguments().getIntegerArrayList(ARG_TIMERS);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_edit_steps_timers, container, false);

        addStepTimerButton = mView.findViewById(R.id.add_step_timer_button);
        addStepTimerButton.setOnClickListener((view) -> {
            //click item
            createPopUp(adapter.getItemCount(), false);
        });
        RecyclerView recyclerView = mView.findViewById(R.id.steps_timers_recyclerview);
        adapter = new EditStepsTimersListAdapter(mView.getContext(), mSteps, mTimers);
        adapter.setOnItemClickListener((view, position) -> {
            //click item
            createPopUp(position, true);
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(mView.getContext()));

        ItemTouchHelper helper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        0) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        int previousPos = viewHolder.getAdapterPosition();
                        int currentPos = target.getAdapterPosition();
                        String step = mSteps.remove(previousPos);
                        mSteps.add(currentPos, step);
                        int time = mTimers.remove(previousPos);
                        mTimers.add(currentPos, time);
                        adapter.notifyItemMoved(previousPos, currentPos);
                        return true;
                    }
                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
                                         int direction) {}
                });
        helper.attachToRecyclerView(recyclerView);
        return mView;
    }

    public ArrayList<String> getSteps() {
        return mSteps;
    }

    public ArrayList<Integer> getTimers() {
        return mTimers;
    }

    public void createPopUp(int position, boolean forEdit) {
        AlertDialog.Builder popupBuilder = new AlertDialog.Builder(mView.getContext());
        if(!forEdit) {
            popupBuilder.setTitle(R.string.add_step_popup_title);
        }
        View popupView = getActivity().getLayoutInflater().inflate(R.layout.edit_step_timer_popup_layout, null);
        EditText stepEditText = popupView.findViewById(R.id.edit_text_step);
        EditText timerHourEditText = popupView.findViewById(R.id.edit_timer_hour);
        EditText timerMinuteEditText = popupView.findViewById(R.id.edit_timer_minute);
        EditText timerSecondEditText = popupView.findViewById(R.id.edit_timer_second);
        if(forEdit) {
            stepEditText.setText(adapter.getStepAtPosition(position));
            int time = adapter.getTimeAtPosition(position);
            if(time/3600 > 0){
                timerHourEditText.setText(String.format("%02d",time / 3600));
            }
            if(time / 60 > 0){
                timerMinuteEditText.setText(String.format("%02d",(time / 60) % 60));
            }
            if(time > 0) {
                timerSecondEditText.setText(String.format("%02d", time % 60));
            }
        }

        Button applyEdits = popupView.findViewById(R.id.apply_step_edit_button);
        if(!forEdit){
            applyEdits.setText(R.string.add);
        }
        Button cancel = popupView.findViewById(R.id.step_cancel_button);
        ImageButton delete = popupView.findViewById(R.id.step_delete_button);
        if(!forEdit) {
            delete.setVisibility(View.GONE);
            stepEditText.setHint(R.string.add_step_hint);
        } else {
            delete.setVisibility(View.VISIBLE);
        }
        popupBuilder.setView(popupView);
        AlertDialog alertDialog = popupBuilder.create();
        alertDialog.show();

        if(forEdit) {
            applyEdits.setOnClickListener((view) -> {
                if (!(stepEditText.getText().toString().trim().equals(""))) {
                    mSteps.set(position, stepEditText.getText().toString().trim());
                    mTimers.set(position, getTimeFromEditText(timerHourEditText, timerMinuteEditText, timerSecondEditText));
                    adapter.notifyItemChanged(position);
                    alertDialog.dismiss();
                }
            });
            delete.setOnClickListener((view) -> {
                String step = mSteps.remove(position);
                int timer = mTimers.remove(position);
                adapter.notifyItemRemoved(position);
                alertDialog.dismiss();
                Snackbar.make(mView ,R.string.step_deleted_snackbar,
                        Snackbar.LENGTH_LONG).setAction(R.string.undo, v -> {
                    mSteps.add(position, step);
                    mTimers.add(position, timer);
                    adapter.notifyItemInserted(position);
                }).show();
            });
        } else {
            applyEdits.setOnClickListener((view) -> {
                if (!(stepEditText.getText().toString().trim().equals(""))) {
                    mSteps.add(stepEditText.getText().toString().trim());
                    mTimers.add(getTimeFromEditText(timerHourEditText, timerMinuteEditText, timerSecondEditText));
                    adapter.notifyItemInserted(position + 1);
                    alertDialog.dismiss();
                }
            });
        }
        cancel.setOnClickListener((view) -> alertDialog.dismiss());
    }

    private int getTimeFromEditText(EditText etHour, EditText etMinute, EditText etSecond) {
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        try {
            hours = Integer.parseInt(etHour.getText().toString());
        } catch (NumberFormatException ignored) {}
        try {
            minutes = Integer.parseInt(etMinute.getText().toString());
        } catch (NumberFormatException ignored) {}
        try {
            seconds = Integer.parseInt(etSecond.getText().toString());
        } catch (NumberFormatException ignored) {}
        return (hours * 3600) + (minutes * 60) + seconds;
    }
}