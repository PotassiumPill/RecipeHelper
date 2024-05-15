package com.example.recipehelper;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class EditIngredientsFragment extends Fragment {

    private static final String ARG_INGREDIENTS = "ingredients";
    private static final String ARG_PREP_TIME = "prepTime";

    private ArrayList<String> mIngredients;
    private int mPrepTime;

    private View mView;
    private EditText editPrepTimeHour;
    private EditText editPrepTimeMinute;
    private EditText editPrepTimeSecond;
    private FloatingActionButton addIngredientButton;

    EditIngredientsListAdapter adapter;

    public EditIngredientsFragment() {}

    public static EditIngredientsFragment newInstance(ArrayList<String> ingredients, int prepTime) {
        EditIngredientsFragment fragment = new EditIngredientsFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_INGREDIENTS, ingredients);
        args.putInt(ARG_PREP_TIME, prepTime);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIngredients = getArguments().getStringArrayList(ARG_INGREDIENTS);
            mPrepTime = getArguments().getInt(ARG_PREP_TIME);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_edit_ingredients, container, false);
        editPrepTimeHour = mView.findViewById(R.id.edit_prep_time_hour);
        if(mPrepTime/3600 > 0){
            editPrepTimeHour.setText(String.format("%02d",mPrepTime / 3600));
        }
        editPrepTimeMinute = mView.findViewById(R.id.edit_prep_time_minute);
        if(mPrepTime / 60 > 0){
            editPrepTimeMinute.setText(String.format("%02d",(mPrepTime / 60) % 60));
        }
        editPrepTimeSecond = mView.findViewById(R.id.edit_prep_time_second);
        if(mPrepTime > 0) {
            editPrepTimeSecond.setText(String.format("%02d", mPrepTime % 60));
        }
        addIngredientButton = mView.findViewById(R.id.add_ingredient_button);
        addIngredientButton.setOnClickListener((view) -> {
            //click item
            createPopUp(adapter.getItemCount(), false);
        });
        RecyclerView recyclerView = mView.findViewById(R.id.ingredients_recycle_view);
        adapter = new EditIngredientsListAdapter(mView.getContext(), mIngredients);
        adapter.setOnItemClickListener((view, position) -> {
            //click item
            createPopUp(position, true);
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(mView.getContext()));
        return mView;
    }

    public int getPrepTime() {
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        try {
            hours = Integer.parseInt(editPrepTimeHour.getText().toString());
        } catch (NumberFormatException ignored) {}
        try {
            minutes = Integer.parseInt(editPrepTimeMinute.getText().toString());
        } catch (NumberFormatException ignored) {}
        try {
            seconds = Integer.parseInt(editPrepTimeSecond.getText().toString());
        } catch (NumberFormatException ignored) {}
        return (hours * 3600) + (minutes * 60) + seconds;
    }

    public ArrayList<String> getIngredients() {
        return mIngredients;
    }

    public void createPopUp(int position, boolean forEdit) {
        AlertDialog.Builder popupBuilder = new AlertDialog.Builder(mView.getContext());
        if(!forEdit) {
            popupBuilder.setTitle(R.string.add_ingredient_popup_title);
        }
        View popupView = getActivity().getLayoutInflater().inflate(R.layout.edit_ingredient_popup_layout, null);
        EditText ingredientEditText = popupView.findViewById(R.id.edit_text_ingredient);
        if(forEdit) {
            ingredientEditText.setText(adapter.getIngredientAtPosition(position));
        }
        Button applyEdits = popupView.findViewById(R.id.apply_ingredient_edit_button);
        if(!forEdit){
            applyEdits.setText(R.string.add);
        }
        Button cancel = popupView.findViewById(R.id.ingredient_cancel_button);
        ImageButton delete = popupView.findViewById(R.id.ingredient_delete_button);
        if(!forEdit) {
            delete.setVisibility(View.GONE);
            ingredientEditText.setHint(R.string.add_ingredient_hint);
        } else {
            delete.setVisibility(View.VISIBLE);
        }
        popupBuilder.setView(popupView);
        AlertDialog alertDialog = popupBuilder.create();
        alertDialog.show();

        if(forEdit) {
            applyEdits.setOnClickListener((view) -> {
                if (!(ingredientEditText.getText().toString().trim().equals(""))) {
                    mIngredients.set(position, ingredientEditText.getText().toString().trim());
                    adapter.notifyItemChanged(position);
                    alertDialog.dismiss();
                }
            });
            delete.setOnClickListener((view) -> {
                String ingredient = mIngredients.remove(position);
                adapter.notifyItemRemoved(position);
                alertDialog.dismiss();
                Snackbar.make(mView ,R.string.ingredient_deleted_snackbar,
                        Snackbar.LENGTH_LONG).setAction(R.string.undo, v -> {
                    mIngredients.add(position, ingredient);
                    adapter.notifyItemInserted(position);
                }).show();
            });
        } else {
            applyEdits.setOnClickListener((view) -> {
                if (!(ingredientEditText.getText().toString().trim().equals(""))) {
                    mIngredients.add(ingredientEditText.getText().toString().trim());
                    adapter.notifyItemInserted(position + 1);
                    alertDialog.dismiss();
                }
            });
        }
        cancel.setOnClickListener((view) -> alertDialog.dismiss());
    }


}