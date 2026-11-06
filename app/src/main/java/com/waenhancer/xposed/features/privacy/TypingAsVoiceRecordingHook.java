package com.waenhancer.xposed.features.privacy;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import com.waenhancer.xposed.core.BaseFeature;
import com.waenhancer.xposed.core.devkit.DexCacheManager;
import com.waenhancer.xposed.core.devkit.DexSearchEngine;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;
import java.lang.reflect.Method;

/**
 * Typing as Voice Recording Indicator:
 * When you type a message, WhatsApp normally shows "typing…" to the contact.
 * This feature converts the outgoing typing indicator into a voice-recording
 * indicator, so the contact sees the mic / recording icon instead of typing.
 *
 * It also keeps the real voice-recording indicator active when you actually
 * record a voice message.
 *
 * Presence states used by WhatsApp's HandleMeComposing/sendComposing:
 *   0 = typing / composing
 *   1 = recording audio
 *   2 = clear / stopped
 *
 * TODO (reverse-engineer against WhatsApp 2.26.44.xx DEX):
 *  - Confirm the exact method name / class for sending composing/recording presence.
 *  - Confirm the integer state values (0/1/2) for the target version.
 */
public class TypingAsVoiceRecordingHook extends BaseFeature {

    private static final String TAG = "[WAEX][TypingAsVoiceRecording]";
    private static final String PREF_ENABLED = "typing_as_voice_recording_enabled";

    // Presence state constants observed in WhatsApp's composing flow.
    private static final int STATE_TYPING = 0;
    private static final int STATE_RECORDING = 1;
    private static final int STATE_CLEAR = 2;

    public TypingAsVoiceRecordingHook(@NonNull Context context,
                                      @NonNull ClassLoader classLoader,
                                      @NonNull SharedPreferences prefs) {
        super(context, classLoader, prefs);
    }

    @Override
    public void hook() throws Throwable {
        if (!isEnabled(PREF_ENABLED, false)) return;

        DexCacheManager dexCache = DexCacheManager.getInstance(context);
        DexSearchEngine engine = DexSearchEngine.getInstance();

        Method composingMethod = dexCache.getMethod(classLoader, "waex_typing_as_voice_recording_method", () -> {
            var bridge = engine.getBridge();
            if (bridge == null) {
                engine.initialize(context);
                bridge = engine.getBridge();
            }
            if (bridge == null) return null;

            MethodData data = bridge.findMethod(
                    FindMethod.create().matcher(
                            MethodMatcher.create()
                                    .addUsingString("HandleMeComposing/sendComposing", StringMatchType.Contains)
                    )
            ).firstOrNull();

            if (data != null) {
                return data.getMethodInstance(classLoader);
            }
            return null;
        });

        if (composingMethod != null) {
            XposedBridge.hookMethod(composingMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args == null || param.args.length == 0) return;

                    int state = -1;

                    // Scan arguments for the integer presence state.
                    for (Object arg : param.args) {
                        if (arg instanceof Integer) {
                            state = (Integer) arg;
                            break;
                        }
                    }

                    if (state == STATE_TYPING) {
                        // Convert typing (0) into recording (1) so the contact sees
                        // the mic/recording indicator instead of "typing…".
                        for (int i = 0; i < param.args.length; i++) {
                            if (param.args[i] instanceof Integer) {
                                param.args[i] = STATE_RECORDING;
                                break;
                            }
                        }
                        XposedBridge.log(TAG + " Spoofed typing as voice-recording indicator");
                    }
                    // When actually recording voice (STATE_RECORDING), leave it as-is
                    // so the real recording indicator still shows.
                }
            });
        } else {
            XposedBridge.log("[WAEX] Could not locate 'HandleMeComposing/sendComposing' method via DexKit.");
        }
    }

    @NonNull
    @Override
    public String getName() {
        return "Typing as Voice Recording Indicator";
    }
}