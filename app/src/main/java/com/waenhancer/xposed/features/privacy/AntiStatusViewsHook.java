package com.waenhancer.xposed.features.privacy;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import com.waenhancer.xposed.core.BaseFeature;
import com.waenhancer.xposed.core.devkit.DexSearchEngine;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;
import java.lang.reflect.Method;

/**
 * Anti-Status-Views Hook:
 * Prevents your status views from being recorded while still letting you view
 * statuses anonymously.
 *
 * TODO (reverse-engineer against WhatsApp 2.26.44.xx DEX):
 *  - Identify the method that marks a status as "viewed" / records a view receipt
 *    (e.g. in the Status / Story playback flow).
 *  - Replace the placeholder search string below with the real method signature.
 */
public class AntiStatusViewsHook extends BaseFeature {

    private static final String TAG = "[WAEX][AntiStatusViews]";
    private static final String PREF_ENABLED = "anti_status_views_enabled";

    public AntiStatusViewsHook(@NonNull Context context,
                               @NonNull ClassLoader classLoader,
                               @NonNull SharedPreferences prefs) {
        super(context, classLoader, prefs);
    }

    @Override
    public void hook() throws Throwable {
        if (!isEnabled(PREF_ENABLED, false)) return;
        hookStatusViewRecorder();
    }

    private void hookStatusViewRecorder() {
        // TODO: Replace "waex_status_view_record" with the real WhatsApp method
        // that records a status view, discovered on the 2.26.44.xx DEX.
        Method viewMethod = DexSearchEngine.getInstance().findMethodWithCache(
                context,
                classLoader,
                "waex_status_view_record",
                (bridge, loader) -> {
                    MethodData md = bridge.findMethod(FindMethod.create()
                            .matcher(MethodMatcher.create()
                                    .addUsingString("status", StringMatchType.Contains)
                                    .addUsingString("view", StringMatchType.Contains)
                            )
                    ).firstOrNull();
                    return md != null ? md.getMethodInstance(loader) : null;
                }
        );

        if (viewMethod != null) {
            XposedBridge.hookMethod(viewMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    // Block the view from being recorded
                    param.setResult(null);
                }
            });
        }
    }

    @NonNull
    @Override
    public String getName() {
        return "Anti-Status-Views (Anonymous Status Viewing)";
    }
}