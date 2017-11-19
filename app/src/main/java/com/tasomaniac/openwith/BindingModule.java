package com.tasomaniac.openwith;

import com.tasomaniac.openwith.browser.PreferredBrowserActivity;
import com.tasomaniac.openwith.homescreen.AddToHomeScreen;
import com.tasomaniac.openwith.homescreen.AddToHomeScreenDialogFragment;
import com.tasomaniac.openwith.intro.IntroActivity;
import com.tasomaniac.openwith.preferred.PreferredAppsActivity;
import com.tasomaniac.openwith.redirect.RedirectFixActivity;
import com.tasomaniac.openwith.settings.SettingsActivity;
import com.tasomaniac.openwith.settings.SettingsFragment;
import com.tasomaniac.openwith.settings.SettingsModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
interface BindingModule {

    @ContributesAndroidInjector
    SettingsActivity settingsActivity();

    @ContributesAndroidInjector(modules = SettingsModule.class)
    SettingsFragment settingsFragment();

    @ContributesAndroidInjector
    IntroActivity introActivity();

    @ContributesAndroidInjector
    RedirectFixActivity redirectFixActivity();

    @ContributesAndroidInjector
    ShareToOpenWith shareToOpenWith();

    @ContributesAndroidInjector
    PreferredAppsActivity preferredAppsActivity();

    @ContributesAndroidInjector
    PreferredBrowserActivity preferredBrowserActivity();

    @PerActivity
    @ContributesAndroidInjector
    AddToHomeScreenDialogFragment addToHomeScreenDialogFragment();

    @ContributesAndroidInjector
    AddToHomeScreen addToHomeScreen();
}
