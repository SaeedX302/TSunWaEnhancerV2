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
import java.util.concurrent.ConcurrentHashMap;

/**
 * Message Scheduler Hook:
 * Schedules outgoing messages to be sent at a specific future time.
 *
 * TODO (reverse-engineer against WhatsApp 2.26.44.xx DEX):
 *  - Identify the class/method responsible for sending outgoing messages
 *    (e.g. a SendMessage / MessageSender / Conversation send flow).
 *  - Replace the placeholder search string below with the real method signature.
 *  - Wire the scheduler into that send path: when a message is marked as
 *    scheduled, defer the real send and enqueue it in {@link #scheduledMessages}.
 */
public class MessageSchedulerHook extends BaseFeature {

    private static final String TAG = "[WAEX][MessageScheduler]";
    private static final String PREF_ENABLED = "msg_scheduler_enabled";

    // Scheduled messages awaiting dispatch: key = unique message id, value = run-at epoch ms
    private final ConcurrentHashMap<String, Long> scheduledMessages = new ConcurrentHashMap<>();

    public MessageSchedulerHook(@NonNull Context context,
                                @NonNull ClassLoader classLoader,
                                @NonNull SharedPreferences prefs) {
        super(context, classLoader, prefs);
    }

    @Override
    public void hook() throws Throwable {
        if (!isEnabled(PREF_ENABLED, false)) return;
        hookOutgoingSend();
    }

    private void hookOutgoingSend() {
        // TODO: Replace "waex_msg_send_flow" with the real WhatsApp send-message method
        // discovered via DexSearchEngine on the 2.26.44.xx DEX.
        Method sendMethod = DexSearchEngine.getInstance().findMethodWithCache(
                context,
                classLoader,
                "waex_msg_send_flow",
                (bridge, loader) -> {
                    MethodData md = bridge.findMethod(FindMethod.create()
                            .matcher(MethodMatcher.create()
                                    .addUsingString("sendMessage", StringMatchType.Contains)
                            )
                    ).firstOrNull();
                    return md != null ? md.getMethodInstance(loader) : null;
                }
        );

        if (sendMethod != null) {
            XposedBridge.hookMethod(sendMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    try {
                        // TODO: Inspect param.args to extract the message text and
                        // scheduled timestamp (if the UI sets one). If scheduled,
                        // store in scheduledMessages and block the immediate send:
                        //   param.setResult(null); // defer
                        //   scheduledMessages.put(msgId, scheduledAt);
                    } catch (Throwable t) {
                        XposedBridge.log(TAG + " Error in send hook: " + t.getMessage());
                    }
                }
            });
        }
    }

    /**
     * Called by a background scheduler (TODO: implement via AlarmManager / WorkManager
     * or a handler thread) to dispatch due scheduled messages.
     */
    public void dispatchDueMessages(long now) {
        scheduledMessages.forEach((msgId, runAt) -> {
            if (runAt <= now) {
                // TODO: re-invoke the real send path for msgId
                scheduledMessages.remove(msgId);
            }
        });
    }

    @NonNull
    @Override
    public String getName() {
        return "Message Scheduler";
    }
}