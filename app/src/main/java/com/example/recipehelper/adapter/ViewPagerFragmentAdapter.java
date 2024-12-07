package com.example.recipehelper.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.recipehelper.fragment.EditIngredientsFragment;
import com.example.recipehelper.fragment.EditStepsTimersFragment;

import java.util.ArrayList;

public class ViewPagerFragmentAdapter extends FragmentStateAdapter {
    private ArrayList<String> mIngredients;
    private ArrayList<String> mSteps;
    private ArrayList<Integer> mTimers;
    private int mPrepTime;

    @Override
    public long getItemId(int position) {
        return position;
    }

    public ViewPagerFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle,
                                    ArrayList<String> ingredients,
                                    ArrayList<String> steps,
                                    ArrayList<Integer> timers,
                                    int prepTime) {
        super(fragmentManager, lifecycle);
        mIngredients = ingredients;
        mSteps = steps;
        mTimers = timers;
        mPrepTime = prepTime;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return EditIngredientsFragment.newInstance(mIngredients, mPrepTime);
            case 1:
                return EditStepsTimersFragment.newInstance(mSteps, mTimers);
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
