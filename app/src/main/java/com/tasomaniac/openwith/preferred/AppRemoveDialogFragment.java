package com.tasomaniac.openwith.preferred;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Html;

import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.resolver.DisplayResolveInfo;

public class AppRemoveDialogFragment extends AppCompatDialogFragment {

    private static final String EXTRA_INFO = "EXTRA_INFO";

    static AppRemoveDialogFragment newInstance(DisplayResolveInfo info) {
        AppRemoveDialogFragment fragment = new AppRemoveDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_INFO, info);
        fragment.setArguments(args);
        return fragment;
    }

    private Callbacks callbacks;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        callbacks = null;
        super.onDetach();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DisplayResolveInfo info = getInfo();
        CharSequence message = appRemoveDialogContentFrom(getResources(), info);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.preferred_remove_title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> callbacks.onAppRemoved(info))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private DisplayResolveInfo getInfo() {
        final DisplayResolveInfo info = getArguments().getParcelable(EXTRA_INFO);
        if (info == null) {
            throw new IllegalArgumentException("Use newInstance method to create the Dialog");
        }
        return info;
    }

    private static CharSequence appRemoveDialogContentFrom(Resources resources, DisplayResolveInfo info) {
        String content = resources.getString(
                R.string.preferred_remove_message,
                info.displayLabel(),
                info.extendedInfo(),
                info.extendedInfo()
        );
        //noinspection deprecation
        return Html.fromHtml(content);
    }

    interface Callbacks {
        void onAppRemoved(DisplayResolveInfo info);
    }
}
