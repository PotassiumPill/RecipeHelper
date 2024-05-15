package com.example.recipehelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EditStepsTimersListAdapter extends RecyclerView.Adapter<EditStepsTimersListAdapter.EditStepsTimersViewHolder> {

    public interface Listener {
        void onClick(View view, int position);
    }
    private Listener onItemClickListener;

    private final LayoutInflater mInflater;
    private List<String> mSteps;
    private List<Integer> mTimers;

    public EditStepsTimersListAdapter(Context context, List<String> steps, List<Integer> timers){
        mInflater = LayoutInflater.from(context);
        this.mSteps = steps;
        this.mTimers = timers;
    }

    @NonNull
    @Override
    public EditStepsTimersListAdapter.EditStepsTimersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_steps_timers_item_layout, parent, false);
        return new EditStepsTimersViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EditStepsTimersListAdapter.EditStepsTimersViewHolder holder, int position) {
        if (mSteps != null) {
            String current = mSteps.get(position);
            holder.stepTextView.setText(current);
            if(mTimers.size() < position + 1) {
                mTimers.add(0);
            }
        } else {
            // Covers the case of data not being ready yet.
            holder.stepTextView.setText("");
        }
    }

    @Override
    public int getItemCount() {
        if (mSteps == null) {
            return 0;
        }
        return mSteps.size();
    }

    public String getStepAtPosition(int position) {
        return mSteps.get(position);
    }

    public int getTimeAtPosition(int position) {
        return mTimers.get(position);
    }

    class EditStepsTimersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView stepTextView;

        public EditStepsTimersViewHolder(@NonNull View itemView) {
            super(itemView);
            stepTextView = itemView.findViewById(R.id.edit_step_text_view);
            stepTextView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int mPosition = getLayoutPosition();
            if(onItemClickListener != null){
                onItemClickListener.onClick(view, mPosition);
            }
        }
    }

    public void setOnItemClickListener(EditStepsTimersListAdapter.Listener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public void removeOnItemClickListener(){
        this.onItemClickListener = null;
    }
}
