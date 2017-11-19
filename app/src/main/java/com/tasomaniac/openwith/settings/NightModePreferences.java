package com.tasomaniac.openwith.settings;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatDelegate;

import com.tasomaniac.openwith.R;

import javax.inject.Inject;

public class NightModePreferences {

    private final SharedPreferences sharedPreferences;
    private final Resources resources;
    private final String key;
    private final String defaultValue;

    @Inject
    public NightModePreferences(SharedPreferences sharedPreferences, Resources resources) {
        this.sharedPreferences = sharedPreferences;
        this.resources = resources;
        this.key = resources.getString(R.string.pref_key_night_mode);
        this.defaultValue = Mode.OFF.stringVale(resources);
    }

    public void updateDefaultNightMode() {
        AppCompatDelegate.setDefaultNightMode(getMode().delegate);
    }

    @StringRes
    public int getSelectedEntry() {
        return getMode().entry;
    }

    public Mode getMode() {
        String value = sharedPreferences.getString(key, defaultValue);
        return Mode.fromValue(resources, value);
    }

    public enum Mode {
        OFF(R.string.pref_value_night_mode_off, R.string.pref_entry_night_mode_off, AppCompatDelegate.MODE_NIGHT_NO),
        ON(R.string.pref_value_night_mode_on, R.string.pref_entry_night_mode_on, AppCompatDelegate.MODE_NIGHT_YES),
        AUTO(R.string.pref_value_night_mode_auto, R.string.pref_entry_night_mode_auto, AppCompatDelegate.MODE_NIGHT_AUTO);

        @StringRes private final int value;
        private final int entry;
        private final int delegate;

        Mode(@StringRes int value, @StringRes int entry, int delegate) {
            this.value = value;
            this.entry = entry;
            this.delegate = delegate;
        }

        public String stringVale(Resources resources) {
            return resources.getString(value);
        }

        static Mode fromValue(Resources resources, String value) {
            for (Mode mode : Mode.values()) {
                if (mode.stringVale(resources).equals(value)) {
                    return mode;
                }
            }
            return null;
        }
    }
}
