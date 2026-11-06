package com.waenhancer.xposed.features.conversation;

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
 * Pin Limit Bypass Hook:
 * Allows pinning more than the default limit (3) of chats.
 *
 * TODO (reverse-engineer against WhatsApp 2.26.44.xx DEX):
 *  - Find the method that enforces the maximum pinned chat count.
 *  - Replace the placeholder search string below with the real method signature.
 *  - Hook it to return a higher limit or bypass the check entirely.
 */
public class PinLimitBypassHook extends BaseFeature {

    private static final String TAG = "[WAEX][PinLimitBypass]";
    private static final String PREF_ENABLED = "pin_limit_bypass_enabled";

    public PinLimitBypassHook(@NonNull Context context,
                              @NonNull ClassLoader classLoader,
                              @NonNull SharedPreferences prefs) {
        super(context, classLoader, prefs);
    }

    @Override
    public void hook() throws Throwable {
        if (!isEnabled(PREF_ENABLED, false)) return;
        hookPinnedLimit();
    }

    private void hookPinnedLimit() {
        // TODO: Replace "waex_pin_limit_flow" with the real WhatsApp method
        // that returns/enforces the pinned chat limit, discovered on the 2.26.44.xx DEX.
        Method limitMethod = DexSearchEngine.getInstance().findMethodWithCache(
                context,
                classLoader,
                "waex_pin_limit_flow",
                (bridge, loader) -> {
                    MethodData md = bridge.findMethod(FindMethod.create()
                            .matcher(MethodMatcher.create()
                                    .addUsingString("pin", StringMatchType.Contains)
                                    .addUsingString("limit", StringMatchType.Contains)
                            )
                    ).firstOrNull();
                    return md != null ? md.getMethodInstance(loader) : null;
                }
        );

        if (limitMethod != null) {
            XposedBridge.hookMethod(limitMethod, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    // TODO: Override the returned limit to allow more pinned chats.
                    // Example: param.setResult(99);
                }
            });
        }
    }

    @NonNull
    @Override
    public String getName() {
        return "Pin Limit Bypass";
    }
}