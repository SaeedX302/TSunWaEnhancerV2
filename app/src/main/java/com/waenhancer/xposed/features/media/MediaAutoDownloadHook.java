package com.waenhancer.xposed.features.media;

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
 * Media Auto-Download Controls Hook:
 * Per-chat auto-download settings (Wi-Fi vs mobile vs roaming) for photos,
 * videos, audio, and documents.
 *
 * TODO (reverse-engineer against WhatsApp 2.26.44.xx DEX):
 *  - Find the method that decides whether media should be auto-downloaded.
 *  - Hook it to apply per-chat auto-download preferences.
 */
public class MediaAutoDownloadHook extends BaseFeature {

    private static final String TAG = "[WAEX][MediaAutoDownload]";
    private static final String PREF_ENABLED = "media_auto_download_enabled";

    public MediaAutoDownloadHook(@NonNull Context context,
                                 @NonNull ClassLoader classLoader,
                                 @NonNull SharedPreferences prefs) {
        super(context, classLoader, prefs);
    }

    @Override
    public void hook() throws Throwable {
        if (!isEnabled(PREF_ENABLED, false)) return;
        hookAutoDownloadDecision();
    }

    private void hookAutoDownloadDecision() {
        // TODO: Replace "waex_media_auto_download_flow" with the real WhatsApp
        // auto-download decision method discovered on the 2.26.44.xx DEX.
        Method downloadMethod = DexSearchEngine.getInstance().findMethodWithCache(
                context,
                classLoader,
                "waex_media_auto_download_flow",
                (bridge, loader) -> {
                    MethodData md = bridge.findMethod(FindMethod.create()
                            .matcher(MethodMatcher.create()
                                    .addUsingString("download", StringMatchType.Contains)
                                    .addUsingString("auto", StringMatchType.Contains)
                            )
                    ).firstOrNull();
                    return md != null ? md.getMethodInstance(loader) : null;
                }
        );

        if (downloadMethod != null) {
            XposedBridge.hookMethod(downloadMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    // TODO: Inspect media type + network type; apply per-chat
                    // preference to allow or block auto-download:
                    //   param.setResult(false); // block
                }
            });
        }
    }

    @NonNull
    @Override
    public String getName() {
        return "Media Auto-Download Controls";
    }
}