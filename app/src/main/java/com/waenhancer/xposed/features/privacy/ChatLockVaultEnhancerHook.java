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
 * Chat Lock / Vault Enhancer Hook:
 * Adds vault enhancements such as a fake/decoy chat list, decoy PIN, or pattern
 * lock for vaulted chats.
 *
 * TODO (reverse-engineer against WhatsApp 2.26.44.xx DEX):
 *  - Find the locked-chats / vault entry point (activity or fragment).
 *  - Hook its unlock flow to support a decoy PIN that shows a fake chat list.
 */
public class ChatLockVaultEnhancerHook extends BaseFeature {

    private static final String TAG = "[WAEX][ChatLockVault]";
    private static final String PREF_ENABLED = "chat_lock_vault_enhancer_enabled";

    public ChatLockVaultEnhancerHook(@NonNull Context context,
                                     @NonNull ClassLoader classLoader,
                                     @NonNull SharedPreferences prefs) {
        super(context, classLoader, prefs);
    }

    @Override
    public void hook() throws Throwable {
        if (!isEnabled(PREF_ENABLED, false)) return;
        hookVaultUnlock();
    }

    private void hookVaultUnlock() {
        // TODO: Replace "waex_vault_unlock_flow" with the real WhatsApp vault
        // unlock method discovered on the 2.26.44.xx DEX.
        Method unlockMethod = DexSearchEngine.getInstance().findMethodWithCache(
                context,
                classLoader,
                "waex_vault_unlock_flow",
                (bridge, loader) -> {
                    MethodData md = bridge.findMethod(FindMethod.create()
                            .matcher(MethodMatcher.create()
                                    .addUsingString("locked", StringMatchType.Contains)
                                    .addUsingString("unlock", StringMatchType.Contains)
                            )
                    ).firstOrNull();
                    return md != null ? md.getMethodInstance(loader) : null;
                }
        );

        if (unlockMethod != null) {
            XposedBridge.hookMethod(unlockMethod, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    // TODO: After successful unlock, check if decoy PIN was used;
                    // if so, show a fake chat list instead of the real vault.
                }
            });
        }
    }

    @NonNull
    @Override
    public String getName() {
        return "Chat Lock / Vault Enhancer";
    }
}