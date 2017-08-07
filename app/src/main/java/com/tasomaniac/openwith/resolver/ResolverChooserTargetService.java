package com.tasomaniac.openwith.resolver;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.service.chooser.ChooserTarget;
import android.service.chooser.ChooserTargetService;

import com.tasomaniac.openwith.App;
import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.ShareToOpenWith;
import com.tasomaniac.openwith.data.Analytics;

import java.util.Collections;
import java.util.List;

@TargetApi(Build.VERSION_CODES.M)
public class ResolverChooserTargetService extends ChooserTargetService {

    @Override
    public List<ChooserTarget> onGetChooserTargets(ComponentName targetActivityName,
                                                   IntentFilter matchedFilter) {
        sendAnalyticsEvent();

        ComponentName componentName = new ComponentName( this, ShareToOpenWith.class);

        return Collections.singletonList(new ChooserTarget(
                getString(R.string.open_with),
                Icon.createWithResource(this, R.mipmap.ic_launcher_main),
                0.2f,
                componentName,
                createBundleExtra()
        ));
    }

    private static Bundle createBundleExtra() {
        Bundle extras = new Bundle(1);
        extras.putBoolean(ShareToOpenWith.EXTRA_FROM_DIRECT_SHARE, true);
        return extras;
    }

    private void sendAnalyticsEvent() {
        analytics().sendEvent(
                "Direct Share",
                "Shown",
                "true"
        );
    }

    private Analytics analytics() {
        return ((App) getApplicationContext()).component().analytics();
    }
}
