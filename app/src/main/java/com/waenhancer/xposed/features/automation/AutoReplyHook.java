package com.waenhancer.xposed.features.automation;

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
 * Auto-Reply / Away Message Hook:
 * Sends rule-based automatic replies to specific contacts or groups.
 *
 * TODO (reverse-engineer against WhatsApp 2.26.44.xx DEX):
 *  - Find the method that handles incoming messages (e.g. a message receipt or
 *    conversation onMessage callback).
 *  - When an incoming message matches a rule, invoke WhatsApp's send path with
 *    the configured auto-reply text.
 */
public class AutoReplyHook extends BaseFeature {

    private static final String TAG = "[WAEX][AutoReply]";
    private static final String PREF_ENABLED = "auto_reply_enabled";

    public AutoReplyHook(@NonNull Context context,
                         @NonNull ClassLoader classLoader,
                         @NonNull SharedPreferences prefs) {
        super(context, classLoader, prefs);
    }

    @Override
    public void hook() throws Throwable {
        if (!isEnabled(PREF_ENABLED, false)) return;
        hookIncomingMessage();
    }

    private void hookIncomingMessage() {
        // TODO: Replace "waex_incoming_message_flow" with the real WhatsApp
        // incoming-message handler method discovered on the 2.26.44.xx DEX.
        Method incomingMethod = DexSearchEngine.getInstance().findMethodWithCache(
                context,
                classLoader,
                "waex_incoming_message_flow",
                (bridge, loader) -> {
                    MethodData md = bridge.findMethod(FindMethod.create()
                            .matcher(MethodMatcher.create()
                                    .addUsingString("receive", StringMatchType.Contains)
                                    .addUsingString("message", StringMatchType.Contains)
                            )
                    ).firstOrNull();
                    return md != null ? md.getMethodInstance(loader) : null;
                }
        );

        if (incomingMethod != null) {
            XposedBridge.hookMethod(incomingMethod, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        // TODO: Extract sender JID and message text from param.args.
                        // If a matching auto-reply rule exists, invoke the send path
                        // with the configured reply text.
                    } catch (Throwable t) {
                        XposedBridge.log(TAG + " Error in incoming hook: " + t.getMessage());
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public String getName() {
        return "Auto-Reply / Away Message";
    }
}