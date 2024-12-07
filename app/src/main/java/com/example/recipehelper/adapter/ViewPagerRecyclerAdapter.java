package com.example.recipehelper.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipehelper.R;
import com.example.recipehelper.Utils;

import java.util.List;

public class ViewPagerRecyclerAdapter extends RecyclerView.Adapter<ViewPagerRecyclerAdapter.ViewPagerRecyclerViewHolder>{

    public interface Listener {
        void onClick(View view, int position, boolean hasTime);
    }
    private Listener onItemClickListener;
    private final LayoutInflater mInflater;

    private List<String> mSteps;
    private List<Integer> mTimers;

    public ViewPagerRecyclerAdapter(Context context, List<String> steps, List<Integer> timers) {
        mInflater = LayoutInflater.from(context);
        this.mSteps = steps;
        this.mTimers = timers;
    }
    @NonNull
    @Override
    public ViewPagerRecyclerAdapter.ViewPagerRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.viewpager_step_item_layout, parent, false);
        return new ViewPagerRecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewPagerRecyclerAdapter.ViewPagerRecyclerViewHolder holder, int position) {
        if (mSteps != null) {
            String current = mSteps.get(position);
            holder.stepName.setText(current);
        } else {
            // Covers the case of data not being ready yet.
            holder.stepName.setText("");
        }
        if (mTimers != null) {
            int curTime = mTimers.get(position);
            if(curTime == 0){
                holder.tvTimer.setVisibility(View.GONE);
                if(position == getItemCount() - 1){
                    holder.startTimer.setText(R.string.finish_recipe);
                } else {
                    holder.startTimer.setText(R.string.next_step_no_time);
                }
            } else {
                holder.tvTimer.setVisibility(View.VISIBLE);
                holder.startTimer.setText(R.string.start_timer_button_text);
                holder.tvTimer.setText(Utils.secondsToTimeString(curTime));
            }
        } else {
            // Covers the case of data not being ready yet.
            holder.tvTimer.setVisibility(View.GONE);
            holder.startTimer.setVisibility(View.GONE);
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

    class ViewPagerRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView stepName;
        private final Button startTimer;
        private final TextView tvTimer;

        public ViewPagerRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            stepName = itemView.findViewById(R.id.step_name);
            startTimer = itemView.findViewById(R.id.button_timer_begin);
            startTimer.setOnClickListener(this);
            tvTimer = itemView.findViewById(R.id.tv_timer);
        }

        @Override
        public void onClick(View view) {
            int mPosition = getLayoutPosition();
            if(onItemClickListener != null){
                onItemClickListener.onClick(view, mPosition, getTimeAtPosition(mPosition) > 0);
            }
        }
    }

    public void setOnItemClickListener(Listener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public void removeOnItemClickListener(){
        this.onItemClickListener = null;
    }
}
