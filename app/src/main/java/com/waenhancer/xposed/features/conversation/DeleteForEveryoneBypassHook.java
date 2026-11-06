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
 * Delete for Everyone Time Limit Bypass Hook:
 * Removes the ~2-day restriction on "Delete for Everyone" so messages can be
 * revoked at any time.
 *
 * TODO (reverse-engineer against WhatsApp 2.26.44.xx DEX):
 *  - Find the method that checks whether a message is within the revoke window.
 *  - Replace the placeholder search string below with the real method signature.
 *  - Hook it to always return true (within window) or bypass the check.
 */
public class DeleteForEveryoneBypassHook extends BaseFeature {

    private static final String TAG = "[WAEX][DeleteForEveryoneBypass]";
    private static final String PREF_ENABLED = "delete_for_everyone_bypass_enabled";

    public DeleteForEveryoneBypassHook(@NonNull Context context,
                                       @NonNull ClassLoader classLoader,
                                       @NonNull SharedPreferences prefs) {
        super(context, classLoader, prefs);
    }

    @Override
    public void hook() throws Throwable {
        if (!isEnabled(PREF_ENABLED, false)) return;
        hookRevokeWindowCheck();
    }

    private void hookRevokeWindowCheck() {
        // TODO: Replace "waex_revoke_window_flow" with the real WhatsApp method
        // that enforces the delete-for-everyone time limit, discovered on the 2.26.44.xx DEX.
        Method windowMethod = DexSearchEngine.getInstance().findMethodWithCache(
                context,
                classLoader,
                "waex_revoke_window_flow",
                (bridge, loader) -> {
                    MethodData md = bridge.findMethod(FindMethod.create()
                            .matcher(MethodMatcher.create()
                                    .addUsingString("revoke", StringMatchType.Contains)
                                    .addUsingString("everyone", StringMatchType.Contains)
                            )
                    ).firstOrNull();
                    return md != null ? md.getMethodInstance(loader) : null;
                }
        );

        if (windowMethod != null) {
            XposedBridge.hookMethod(windowMethod, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    // TODO: Force the result to indicate the message is revocable.
                    // Example: param.setResult(true);
                }
            });
        }
    }

    @NonNull
    @Override
    public String getName() {
        return "Delete for Everyone Time Limit Bypass";
    }
}