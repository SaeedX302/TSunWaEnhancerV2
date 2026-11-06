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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Message Edit History Hook:
 * Shows "edited" indicators with the original message preview when a message
 * is edited.
 *
 * TODO (reverse-engineer against WhatsApp 2.26.44.xx DEX):
 *  - Find the method that handles message edits.
 *  - Capture the original text before the edit is applied and store it in
 *    {@link #editHistory} keyed by message ID.
 *  - Hook the message display path to show the original when "edited" is tapped.
 */
public class MessageEditHistoryHook extends BaseFeature {

    private static final String TAG = "[WAEX][MessageEditHistory]";
    private static final String PREF_ENABLED = "message_edit_history_enabled";

    // messageId -> original text before last edit
    private final Map<String, String> editHistory = new LinkedHashMap<>();

    public MessageEditHistoryHook(@NonNull Context context,
                                  @NonNull ClassLoader classLoader,
                                  @NonNull SharedPreferences prefs) {
        super(context, classLoader, prefs);
    }

    @Override
    public void hook() throws Throwable {
        if (!isEnabled(PREF_ENABLED, false)) return;
        hookMessageEdit();
    }

    private void hookMessageEdit() {
        // TODO: Replace "waex_message_edit_flow" with the real WhatsApp edit
        // message method discovered on the 2.26.44.xx DEX.
        Method editMethod = DexSearchEngine.getInstance().findMethodWithCache(
                context,
                classLoader,
                "waex_message_edit_flow",
                (bridge, loader) -> {
                    MethodData md = bridge.findMethod(FindMethod.create()
                            .matcher(MethodMatcher.create()
                                    .addUsingString("edit", StringMatchType.Contains)
                                    .addUsingString("message", StringMatchType.Contains)
                            )
                    ).firstOrNull();
                    return md != null ? md.getMethodInstance(loader) : null;
                }
        );

        if (editMethod != null) {
            XposedBridge.hookMethod(editMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    try {
                        // TODO: Extract message ID and original text; store original:
                        //   editHistory.put(msgId, originalText);
                    } catch (Throwable t) {
                        XposedBridge.log(TAG + " Error in edit hook: " + t.getMessage());
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public String getName() {
        return "Message Edit History";
    }
}