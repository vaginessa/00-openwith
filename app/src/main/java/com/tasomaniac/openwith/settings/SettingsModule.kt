package com.tasomaniac.openwith.settings

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.LOLLIPOP
import com.tasomaniac.openwith.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet

@Module
class SettingsModule {

  @Provides
  fun clipboardManager(app: Application) = app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

  @Provides
  @ElementsIntoSet
  fun settings(
      clipboard: ClipboardSettings,
      general: GeneralSettings,
      display: DisplaySettings,
      other: OtherSettings
  ) = setOf(clipboard, general, display, other)

  @Provides
  @ElementsIntoSet
  fun usageAccessSettings(settings: UsageAccessSettings) = setOf(settings, condition = SDK_INT >= LOLLIPOP)

  @Provides
  @ElementsIntoSet
  fun debugSettings(settings: DebugSettings) = setOf(settings, condition = BuildConfig.DEBUG)

  private fun setOf(vararg settings: Settings, condition: Boolean) = if (condition) setOf(*settings) else setOf()

}
