package com.waenhancer.xposed.features.privacy;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import com.waenhancer.xposed.core.BaseFeature;
import de.robv.android.xposed.XposedBridge;
import java.util.Calendar;

/**
 * DND / Do-Not-Disturb Schedule Hook:
 * Auto-enables DND mode (and related privacy modes) during configured hours.
 *
 * This feature does not hook WhatsApp internals directly; instead it manages the
 * shared DND preference used by {@link DndModeHook} based on a time schedule.
 *
 * TODO:
 *  - Read start/end time from preferences.
 *  - Use a periodic check (AlarmManager / handler thread) to toggle
 *    prefs boolean "dnd_mode" (used by DndModeHook) during the scheduled window.
 */
public class DndScheduleHook extends BaseFeature {

    private static final String TAG = "[WAEX][DndSchedule]";
    private static final String PREF_ENABLED = "dnd_schedule_enabled";
    private static final String PREF_START_HOUR = "dnd_schedule_start_hour";
    private static final String PREF_END_HOUR = "dnd_schedule_end_hour";
    private static final String PREF_DND = "dnd_mode"; // shared with DndModeHook

    public DndScheduleHook(@NonNull Context context,
                           @NonNull ClassLoader classLoader,
                           @NonNull SharedPreferences prefs) {
        super(context, classLoader, prefs);
    }

    @Override
    public void hook() throws Throwable {
        if (!isEnabled(PREF_ENABLED, false)) return;
        applySchedule();
    }

    private void applySchedule() {
        try {
            int startHour = prefs.getInt(PREF_START_HOUR, 22);
            int endHour = prefs.getInt(PREF_END_HOUR, 8);
            int nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

            boolean inWindow;
            if (startHour < endHour) {
                inWindow = nowHour >= startHour && nowHour < endHour;
            } else { // overnight window, e.g. 22 -> 8
                inWindow = nowHour >= startHour || nowHour < endHour;
            }

            prefs.edit().putBoolean(PREF_DND, inWindow).apply();
            XposedBridge.log(TAG + " DND schedule applied: inWindow=" + inWindow);
        } catch (Throwable t) {
            XposedBridge.log(TAG + " Error applying DND schedule: " + t.getMessage());
        }
    }

    @NonNull
    @Override
    public String getName() {
        return "DND Schedule";
    }
}