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
 * Bulk Message Selector Hook:
 * Enhanced multi-select for messages/media with actions like "forward to many".
 *
 * TODO (reverse-engineer against WhatsApp 2.26.44.xx DEX):
 *  - Find the conversation list view / selection mode entry point.
 *  - Hook selection callbacks to expose bulk actions (forward to many, delete many,
 *    etc.) beyond WhatsApp's default limits.
 */
public class BulkMessageSelectorHook extends BaseFeature {

    private static final String TAG = "[WAEX][BulkSelector]";
    private static final String PREF_ENABLED = "bulk_message_selector_enabled";

    public BulkMessageSelectorHook(@NonNull Context context,
                                   @NonNull ClassLoader classLoader,
                                   @NonNull SharedPreferences prefs) {
        super(context, classLoader, prefs);
    }

    @Override
    public void hook() throws Throwable {
        if (!isEnabled(PREF_ENABLED, false)) return;
        hookSelectionMode();
    }

    private void hookSelectionMode() {
        // TODO: Replace "waex_bulk_select_flow" with the real WhatsApp message
        // selection mode method discovered on the 2.26.44.xx DEX.
        Method selectMethod = DexSearchEngine.getInstance().findMethodWithCache(
                context,
                classLoader,
                "waex_bulk_select_flow",
                (bridge, loader) -> {
                    MethodData md = bridge.findMethod(FindMethod.create()
                            .matcher(MethodMatcher.create()
                                    .addUsingString("select", StringMatchType.Contains)
                                    .addUsingString("message", StringMatchType.Contains)
                            )
                    ).firstOrNull();
                    return md != null ? md.getMethodInstance(loader) : null;
                }
        );

        if (selectMethod != null) {
            XposedBridge.hookMethod(selectMethod, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    // TODO: Inject bulk-action menu items into the selection bar.
                }
            });
        }
    }

    @NonNull
    @Override
    public String getName() {
        return "Bulk Message Selector";
    }
}