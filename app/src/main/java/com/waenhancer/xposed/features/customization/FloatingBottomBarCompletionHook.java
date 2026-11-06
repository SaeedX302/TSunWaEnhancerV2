package com.waenhancer.xposed.features.customization;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import com.waenhancer.xposed.core.BaseFeature;
import de.robv.android.xposed.XposedBridge;

/**
 * Floating Bottom Bar Completion Hook:
 * Completes the upcoming WhatsApp floating bottom bar feature (tabs, padding,
 * selector color, hide tab labels) hinted at in the changelog.
 *
 * TODO (reverse-engineer against WhatsApp 2.26.44.xx DEX):
 *  - Find the bottom navigation / tab bar class in WhatsApp's UI.
 *  - Hook its layout/measure pass to apply custom padding, selector color,
 *    and tab label visibility per the existing FloatingBottomBarHook preferences.
 */
public class FloatingBottomBarCompletionHook extends BaseFeature {

    private static final String TAG = "[WAEX][FloatingBottomBarCompletion]";
    private static final String PREF_ENABLED = "floating_bottom_bar_completion_enabled";

    public FloatingBottomBarCompletionHook(@NonNull Context context,
                                           @NonNull ClassLoader classLoader,
                                           @NonNull SharedPreferences prefs) {
        super(context, classLoader, prefs);
    }

    @Override
    public void hook() throws Throwable {
        if (!isEnabled(PREF_ENABLED, false)) return;
        hookBottomBarLayout();
    }

    private void hookBottomBarLayout() {
        // TODO: Replace "waex_bottom_bar_flow" with the real WhatsApp bottom
        // navigation bar layout method discovered on the 2.26.44.xx DEX.
        XposedBridge.log(TAG + " Floating bottom bar completion hook registered " +
                "(awaiting WhatsApp 2.26.44.xx class/method mapping)");
    }

    @NonNull
    @Override
    public String getName() {
        return "Floating Bottom Bar Completion";
    }
}