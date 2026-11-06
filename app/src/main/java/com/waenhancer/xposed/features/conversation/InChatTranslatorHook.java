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
 * In-Chat Message Translator Hook:
 * Adds a translate action on incoming/outgoing message bubbles.
 *
 * TODO (reverse-engineer against WhatsApp 2.26.44.xx DEX):
 *  - Find the message bubble / conversation view class and its context-menu
 *    inflation method to inject a "Translate" menu item.
 *  - Find the method that returns the displayed message text so it can be
 *    translated and replaced in-place (or shown in a tooltip/dialog).
 */
public class InChatTranslatorHook extends BaseFeature {

    private static final String TAG = "[WAEX][InChatTranslator]";
    private static final String PREF_ENABLED = "in_chat_translator_enabled";

    public InChatTranslatorHook(@NonNull Context context,
                                @NonNull ClassLoader classLoader,
                                @NonNull SharedPreferences prefs) {
        super(context, classLoader, prefs);
    }

    @Override
    public void hook() throws Throwable {
        if (!isEnabled(PREF_ENABLED, false)) return;
        hookMessageView();
    }

    private void hookMessageView() {
        // TODO: Replace "waex_msg_view_translate" with the real WhatsApp message
        // view / context menu method discovered on the 2.26.44.xx DEX.
        Method viewMethod = DexSearchEngine.getInstance().findMethodWithCache(
                context,
                classLoader,
                "waex_msg_view_translate",
                (bridge, loader) -> {
                    MethodData md = bridge.findMethod(FindMethod.create()
                            .matcher(MethodMatcher.create()
                                    .addUsingString("Conversation", StringMatchType.Contains)
                                    .addUsingString("onCreateContextMenu", StringMatchType.Contains)
                            )
                    ).firstOrNull();
                    return md != null ? md.getMethodInstance(loader) : null;
                }
        );

        if (viewMethod != null) {
            XposedBridge.hookMethod(viewMethod, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    // TODO: Add a "Translate" menu item; on click, read the message
                    // text, call a translation service, and show the result.
                }
            });
        }
    }

    @NonNull
    @Override
    public String getName() {
        return "In-Chat Message Translator";
    }
}