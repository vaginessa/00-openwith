package com.tasomaniac.openwith.settings;

import android.app.backup.BackupManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import com.tasomaniac.openwith.BuildConfig;
import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.browser.PreferredBrowserActivity;
import com.tasomaniac.openwith.data.Analytics;
import com.tasomaniac.openwith.data.prefs.BooleanPreference;
import com.tasomaniac.openwith.data.prefs.UsageAccess;
import com.tasomaniac.openwith.intro.IntroActivity;
import com.tasomaniac.openwith.preferred.PreferredAppsActivity;
import com.tasomaniac.openwith.util.Intents;
import com.tasomaniac.openwith.util.Utils;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

    @Inject Analytics analytics;
    @Inject
    @UsageAccess
    BooleanPreference usageAccessPref;

    private PreferenceCategory usageStatsPreferenceCategory;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSupportInjection.inject(this);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_general);
        addPreferencesFromResource(R.xml.pref_others);

        findPreference(R.string.pref_key_about).setOnPreferenceClickListener(this);
        findPreference(R.string.pref_key_preferred).setOnPreferenceClickListener(this);
        findPreference(R.string.pref_key_browser).setOnPreferenceClickListener(this);
        findPreference(R.string.pref_key_open_source).setOnPreferenceClickListener(this);
        findPreference(R.string.pref_key_contact).setOnPreferenceClickListener(this);

        if (BuildConfig.DEBUG) {
            new DebugPreferences(this).setup();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        if (SDK_INT >= LOLLIPOP) {
            setupUsagePreference();
        }
        setupVersionPreference();
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupUsagePreference() {
        boolean usageAccessGiven = Utils.isUsageStatsEnabled(getActivity());

        if (usageAccessGiven) {
            if (usageStatsPreferenceCategory != null) {
                getPreferenceScreen().removePreference(usageStatsPreferenceCategory);
                usageStatsPreferenceCategory = null;
            }
        } else {
            if (usageStatsPreferenceCategory == null) {
                addPreferencesFromResource(R.xml.pref_usage);
                usageStatsPreferenceCategory = (PreferenceCategory) findPreference(R.string.pref_key_category_usage);

                Preference usageStatsPreference = findPreference(R.string.pref_key_usage_stats);
                usageStatsPreference.setOnPreferenceClickListener(this);

                //Set title and summary in red font.
                usageStatsPreference.setTitle(coloredErrorString(R.string.pref_title_usage_stats));
                usageStatsPreference.setSummary(coloredErrorString(R.string.pref_summary_usage_stats));
                usageStatsPreference.setWidgetLayoutResource(R.layout.preference_widget_error);
            }
        }

        if (usageAccessPref.get() != usageAccessGiven) {
            usageAccessPref.set(usageAccessGiven);

            analytics.sendEvent(
                    "Usage Access",
                    "Access Given",
                    Boolean.toString(usageAccessGiven)
            );
        }
    }

    private void setupVersionPreference() {
        StringBuilder version = new StringBuilder(BuildConfig.VERSION_NAME);
        if (BuildConfig.DEBUG) {
            version.append(" (")
                    .append(BuildConfig.VERSION_CODE)
                    .append(")");
        }
        Preference preference = findPreference(R.string.pref_key_version);
        preference.setSummary(version);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        if (getString(R.string.pref_key_about).equals(preference.getKey())) {
            startActivity(new Intent(getActivity(), IntroActivity.class));
        } else if (getString(R.string.pref_key_preferred).equals(preference.getKey())) {
            startActivity(new Intent(getActivity(), PreferredAppsActivity.class));
        } else if (getString(R.string.pref_key_browser).equals(preference.getKey())) {
            startActivity(new Intent(getActivity(), PreferredBrowserActivity.class));
        } else if (getString(R.string.pref_key_usage_stats).equals(preference.getKey())) {
            onUsageAccessClick(preference);
        } else if (getString(R.string.pref_key_open_source).equals(preference.getKey())) {
            displayLicensesDialogFragment();
        } else if (getString(R.string.pref_key_contact).equals(preference.getKey())) {
            startContactEmailChooser();
        }

        analytics.sendEvent(
                "Preference",
                "Item Click",
                preference.getKey()
        );
        return true;
    }

    private void onUsageAccessClick(Preference preference) {
        boolean settingsOpened = Intents.maybeStartUsageAccessSettings(getActivity());

        if (!settingsOpened) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.error)
                    .setMessage(R.string.error_usage_access_not_found)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();

            preference.setSummary(R.string.error_usage_access_not_found);
        }
    }

    private void displayLicensesDialogFragment() {
        LicensesDialogFragment.newInstance().show(getFragmentManager(), "LicensesDialog");
    }

    private void startContactEmailChooser() {
        ShareCompat.IntentBuilder.from(getActivity())
                .addEmailTo("Said Tahsin Dane <tasomaniac+openlinkwith@gmail.com>")
                .setSubject(getString(R.string.app_name))
                .setType("message/rfc822")
                .startChooser();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        new BackupManager(getActivity()).dataChanged();
    }

    private Preference findPreference(@StringRes int keyResource) {
        return findPreference(getString(keyResource));
    }

    private CharSequence coloredErrorString(@StringRes int stringRes) {
        SpannableString errorSpan = new SpannableString(getString(stringRes));
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(
                ContextCompat.getColor(getContext(), R.color.error_color));
        errorSpan.setSpan(colorSpan, 0, errorSpan.length(), 0);
        return errorSpan;
    }
}
