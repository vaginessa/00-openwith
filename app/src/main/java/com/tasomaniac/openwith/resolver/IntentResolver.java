package com.tasomaniac.openwith.resolver;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.tasomaniac.openwith.PerActivity;
import com.tasomaniac.openwith.rx.SchedulingStrategy;
import com.tasomaniac.openwith.util.Intents;
import com.tasomaniac.openwith.util.ResolverInfos;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;

@PerActivity
class IntentResolver {

    private final PackageManager packageManager;
    private final SchedulingStrategy schedulingStrategy;
    private final ResolveListGrouper resolveListGrouper;
    private final Intent sourceIntent;
    private final CallerPackage callerPackage;

    @Nullable private Data data;
    private Listener listener = Listener.NO_OP;
    private Disposable disposable;

    @Inject
    IntentResolver(PackageManager packageManager,
                   SchedulingStrategy schedulingStrategy,
                   Intent sourceIntent,
                   CallerPackage callerPackage,
                   ResolveListGrouper resolveListGrouper) {
        this.packageManager = packageManager;
        this.schedulingStrategy = schedulingStrategy;
        this.sourceIntent = sourceIntent;
        this.callerPackage = callerPackage;
        this.resolveListGrouper = resolveListGrouper;
    }

    void bind(Listener listener) {
        this.listener = listener;

        if (data == null) {
            resolve();
        } else {
            listener.onIntentResolved(data);
        }
    }

    public void unbind() {
        this.listener = Listener.NO_OP;
    }

    Intent getSourceIntent() {
        return sourceIntent;
    }

    void resolve() {
        disposable = Observable.fromCallable(this::doResolve)
                .compose(schedulingStrategy.forObservable())
                .subscribe(data -> {
                    this.data = data;
                    listener.onIntentResolved(data);
                });
    }

    void release() {
        disposable.dispose();
    }

    private Data doResolve() {
        int flag = SDK_INT >= M ? PackageManager.MATCH_ALL : PackageManager.MATCH_DEFAULT_ONLY;
        List<ResolveInfo> currentResolveList = new ArrayList<>(packageManager.queryIntentActivities(sourceIntent, flag));
        if (Intents.isHttp(sourceIntent) && SDK_INT >= M) {
            addBrowsersToList(currentResolveList);
        }

        callerPackage.removeFrom(currentResolveList);

        List<DisplayResolveInfo> resolved = groupResolveList(currentResolveList);
        return new Data(resolved, resolveListGrouper.filteredItem, resolveListGrouper.showExtended);
    }

    private List<DisplayResolveInfo> groupResolveList(List<ResolveInfo> currentResolveList) {
        if (currentResolveList.isEmpty()) {
            return Collections.emptyList();
        }
        return resolveListGrouper.groupResolveList(currentResolveList);
    }

    private void addBrowsersToList(List<ResolveInfo> list) {
        final int initialSize = list.size();

        for (ResolveInfo browser : queryBrowsers()) {
            boolean browserFound = false;

            for (int i = 0; i < initialSize; i++) {
                ResolveInfo info = list.get(i);

                if (ResolverInfos.equals(info, browser)) {
                    browserFound = true;
                    break;
                }
            }

            if (!browserFound) {
                list.add(browser);
            }
        }
    }

    private List<ResolveInfo> queryBrowsers() {
        Intent browserIntent = new Intent()
                .setAction(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(Uri.parse("http:"));
        return packageManager.queryIntentActivities(browserIntent, 0);
    }

    static class Data {
        final List<DisplayResolveInfo> resolved;
        @Nullable final DisplayResolveInfo filteredItem;
        final boolean showExtended;

        Data(List<DisplayResolveInfo> resolved, @Nullable DisplayResolveInfo filteredItem, boolean showExtended) {
            this.resolved = resolved;
            this.filteredItem = filteredItem;
            this.showExtended = showExtended;
        }

        boolean isEmpty() {
            return totalCount() == 0;
        }

        int totalCount() {
            return resolved.size() + (filteredItem != null ? 1 : 0);
        }
    }

    interface Listener {

        void onIntentResolved(Data data);

        Listener NO_OP = data -> {
            // no-op
        };

    }
}
