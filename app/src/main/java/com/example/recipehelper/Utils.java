package com.example.recipehelper;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static String secondsToTimeString(int timeSeconds) {
        StringBuilder sb = new StringBuilder();
        int seconds = timeSeconds % 60;
        int minutes = (timeSeconds / 60) % 60;
        int hours = timeSeconds / 3600;
        if (hours > 0) {
            sb.append(String.format("%d", hours)).append(String.format(":%02d", minutes));
        } else if(minutes > 0){
            sb.append(String.format("%d", minutes));
        }
        sb.append(String.format(":%02d", seconds));
        return sb.toString();
    }

    public static int timeStringToSeconds(String timeString){
        String[] vals = timeString.split(":");
        int sum = 0;
        int mult = 1;
        for(int i = vals.length - 1; i > -1; i--){
            try {
                sum += Integer.parseInt(vals[i]) * mult;
                mult *= 60;
            } catch(NumberFormatException e) {
                return -1;
            }
        }
        return sum;
    }

    public static boolean parseTimerString(String timerString, List<Integer> outList) {
        outList.clear();
        String[] vals = timerString.split(",");
        for(String val : vals){
            try {
                outList.add(Integer.parseInt(val));
            } catch(NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    public static String ingsOrStepsToString(List<String> itemList) {
        StringBuilder sb = new StringBuilder();
        for(String str : itemList){
            sb.append(str).append("\n");
        }
        if(sb.length() > 0){
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public static String timersToString(List<Integer> timerList) {
        StringBuilder sb = new StringBuilder();
        for(int time : timerList){
            sb.append(time).append(",");
        }
        if(sb.length() > 0){
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public static int getTotalTimeFromTimers(String timerString){
        String[] vals = timerString.split(",");
        int sum = 0;
        for(String val : vals){
            try {
                sum += Integer.parseInt(val);
            } catch(NumberFormatException e) {
                return 0;
            }
        }
        return sum;
    }
}
