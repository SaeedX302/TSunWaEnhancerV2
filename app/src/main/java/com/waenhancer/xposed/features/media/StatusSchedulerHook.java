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
import java.util.concurrent.ConcurrentHashMap;

/**
 * Status Scheduler Hook:
 * Schedules status posts to go live at a chosen time.
 *
 * TODO (reverse-engineer against WhatsApp 2.26.44.xx DEX):
 *  - Find the method that uploads/publishes a status story.
 *  - When a status is marked as scheduled, defer the upload and enqueue it
 *    for later dispatch at the scheduled time.
 */
public class StatusSchedulerHook extends BaseFeature {

    private static final String TAG = "[WAEX][StatusScheduler]";
    private static final String PREF_ENABLED = "status_scheduler_enabled";

    // Scheduled statuses awaiting publication: key = status id, value = run-at epoch ms
    private final ConcurrentHashMap<String, Long> scheduledStatuses = new ConcurrentHashMap<>();

    public StatusSchedulerHook(@NonNull Context context,
                               @NonNull ClassLoader classLoader,
                               @NonNull SharedPreferences prefs) {
        super(context, classLoader, prefs);
    }

    @Override
    public void hook() throws Throwable {
        if (!isEnabled(PREF_ENABLED, false)) return;
        hookStatusPublish();
    }

    private void hookStatusPublish() {
        // TODO: Replace "waex_status_publish_flow" with the real WhatsApp
        // status publish/upload method discovered on the 2.26.44.xx DEX.
        Method publishMethod = DexSearchEngine.getInstance().findMethodWithCache(
                context,
                classLoader,
                "waex_status_publish_flow",
                (bridge, loader) -> {
                    MethodData md = bridge.findMethod(FindMethod.create()
                            .matcher(MethodMatcher.create()
                                    .addUsingString("status", StringMatchType.Contains)
                                    .addUsingString("send", StringMatchType.Contains)
                            )
                    ).firstOrNull();
                    return md != null ? md.getMethodInstance(loader) : null;
                }
        );

        if (publishMethod != null) {
            XposedBridge.hookMethod(publishMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    try {
                        // TODO: If scheduled, store in scheduledStatuses and block:
                        //   param.setResult(null);
                        //   scheduledStatuses.put(statusId, scheduledAt);
                    } catch (Throwable t) {
                        XposedBridge.log(TAG + " Error in status hook: " + t.getMessage());
                    }
                }
            });
        }
    }

    /**
     * Called by a background scheduler to publish due statuses.
     */
    public void publishDueStatuses(long now) {
        scheduledStatuses.forEach((statusId, runAt) -> {
            if (runAt <= now) {
                // TODO: re-invoke the real status publish path for statusId
                scheduledStatuses.remove(statusId);
            }
        });
    }

    @NonNull
    @Override
    public String getName() {
        return "Status Scheduler";
    }
}