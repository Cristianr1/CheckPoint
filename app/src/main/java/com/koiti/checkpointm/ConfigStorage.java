package com.koiti.checkpointm;

import android.content.Context;
import android.content.SharedPreferences;

public class ConfigStorage {

    /**
     * Constructor
     */
    public ConfigStorage(){}

    /**
     * Integer data persists
     */
    public void save(int value, String key, Context context) {
        SharedPreferences settings = context.getSharedPreferences("KEY_DATA", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * String data persists
     */
    public void save(String value, String key, Context context) {
        SharedPreferences settings = context.getSharedPreferences("KEY_DATA", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Boolean data persists
     */
    public void save(Boolean value, String key, Context context) {
        SharedPreferences settings = context.getSharedPreferences("KEY_DATA", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * @return integer corresponding to the key defValue: 0
     */
    public int getValueInt(String key, Context context) {
        SharedPreferences settings = context.getSharedPreferences("KEY_DATA", 0);
        return settings.getInt(key, 0);
    }

    /**
     * @return string corresponding to the key defValue: empty
     */
    public String getValueString(String key, Context context) {
        SharedPreferences settings = context.getSharedPreferences("KEY_DATA", 0);
        return settings.getString(key, "");
    }

    /**
     * @return boolena corresponding to the key defValue: false
     */
    public Boolean getValueBoolean(String key, Context context) {
        SharedPreferences settings = context.getSharedPreferences("KEY_DATA", 0);
        return settings.getBoolean(key, false);
    }
}
