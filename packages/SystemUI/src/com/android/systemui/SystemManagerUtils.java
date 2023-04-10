/*
 * Copyright (C) 2019 Descendant
 * Copyright (C) 2023 the RisingOS Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.power.Boost;
import android.hardware.power.Mode;
import android.location.LocationManager;
import android.os.Handler;
import android.os.UserHandle;
import android.os.PowerManagerInternal;
import android.provider.Settings;

import com.android.internal.util.spark.SparkUtils.SystemManagerController;
import com.android.server.LocalServices;
import com.android.systemui.Dependency;

import java.util.ArrayList;
import java.util.List;

public class SystemManagerUtils {
    static String TAG = "SystemManagerUtils";

    static Handler h = new Handler();
    static Runnable mStartManagerInstance;
    static Runnable mStopManagerInstance;
    static SystemManagerController mSysManagerController;
    static List<ActivityManager.RunningAppProcessInfo> RunningServices;
    static ActivityManager localActivityManager;
    static final long IDLE_TIME_NEEDED = 20000;

    public static void initSystemManager(Context context) {
   	mSysManagerController = new SystemManagerController(context);

        mStartManagerInstance = new Runnable() {
            public void run() {
                    idleModeHandler(true);
                    killBackgroundProcesses(context);
            }
        };

        mStopManagerInstance = new Runnable() {
            public void run() {
                cancelIdleService();
            }
        };
    }

    public static void startIdleService(Context context) {
        if (IDLE_TIME_NEEDED > timeBeforeAlarm(context) && timeBeforeAlarm(context) != 0) {
            h.postDelayed(mStartManagerInstance,100);
        } else {
            h.postDelayed(mStartManagerInstance,IDLE_TIME_NEEDED /*10ms*/);
        }
        if (timeBeforeAlarm(context) != 0) {
            h.postDelayed(mStopManagerInstance,(timeBeforeAlarm(context) - 900000));
        }
    }

    public static void cacheCleaner(PackageManager pm) {
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : apps) {
            pm.deleteApplicationCacheFiles(packageInfo.packageName,null);
        }
    }

    public static void idleModeHandler(boolean idle) {
        PowerManagerInternal mLocalPowerManager = LocalServices.getService(PowerManagerInternal.class);
        if (mLocalPowerManager != null) {
          mLocalPowerManager.setPowerMode(Mode.DEVICE_IDLE, idle);
        }
    }

    public static void runtimePowerModeHandler(boolean awake, int mode) {
        PowerManagerInternal mLocalPowerManager = LocalServices.getService(PowerManagerInternal.class);
        if (mLocalPowerManager != null) {
            switch (mode) {
            	case 0:
              	    // reset power modes
                    mLocalPowerManager.setPowerMode(Mode.LOW_POWER, false);
                    mLocalPowerManager.setPowerMode(Mode.SUSTAINED_PERFORMANCE, false);
                    mLocalPowerManager.setPowerMode(Mode.INTERACTIVE, false);
                    mLocalPowerManager.setPowerMode(Mode.FIXED_PERFORMANCE, false);
                    break;
                case 1:
              	    // low power
                    mLocalPowerManager.setPowerMode(Mode.LOW_POWER, awake);
                    break;
                case 2:
                    // sustained performance
                    mLocalPowerManager.setPowerMode(Mode.SUSTAINED_PERFORMANCE, awake);
                    break;
                case 3:
                    // interactive
                    mLocalPowerManager.setPowerMode(Mode.INTERACTIVE, awake);
                    break;
                case 4:
                    // aggressive
                    mLocalPowerManager.setPowerMode(Mode.FIXED_PERFORMANCE, awake);
                    break;
                default:
                    break;
            }
        }
    }

    public static void cancelIdleService() {
        h.removeCallbacks(mStartManagerInstance);
        onScreenWake();
    }

     public static void boostingServiceHandler(boolean enable, int boostingLevel) {
        PowerManagerInternal mLocalPowerManager = LocalServices.getService(PowerManagerInternal.class);
        if (mLocalPowerManager != null) {
            switch (boostingLevel) {
            	case 0:
              	    // reset power modes
                    mLocalPowerManager.setPowerMode(Mode.SUSTAINED_PERFORMANCE, false);
                    mLocalPowerManager.setPowerMode(Mode.INTERACTIVE, false);
                    mLocalPowerManager.setPowerMode(Mode.FIXED_PERFORMANCE, false);
                    break;
                case 1:
              	    // low
                    mLocalPowerManager.setPowerMode(Mode.SUSTAINED_PERFORMANCE, enable);
                    break;
                case 2:
              	    // moderate
                    mLocalPowerManager.setPowerMode(Mode.INTERACTIVE, enable);
                    break;
                case 3:
                    // agrressive
                    mLocalPowerManager.setPowerMode(Mode.FIXED_PERFORMANCE, enable);
                    break;
                default:
                    break;
          }
        }
     }

    public static void onScreenWake() {
        h.removeCallbacks(mStopManagerInstance);
        idleModeHandler(false);
        PowerManagerInternal mLocalPowerManager = LocalServices.getService(PowerManagerInternal.class);
        if (mLocalPowerManager != null) {
          mLocalPowerManager.setPowerBoost(Boost.DISPLAY_UPDATE_IMMINENT, 200);
        }
    }

    public static long timeBeforeAlarm(Context context) {
        AlarmManager.AlarmClockInfo info =
                ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE)).getNextAlarmClock();
        if (info != null) {
            long alarmTime = info.getTriggerTime();
            long realTime = alarmTime - System.currentTimeMillis();
            return realTime;
        } else {
            return 0;
        }
    }

    public static void killBackgroundProcesses(Context context) {
        localActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        RunningServices = localActivityManager.getRunningAppProcesses();
        for (int i=0; i < RunningServices.size(); i++) {
            if (!RunningServices.get(i).pkgList[0].toString().toLowerCase().contains("com.android.") &&
                !RunningServices.get(i).pkgList[0].toString().toLowerCase().contains("com.spark") &&
                !RunningServices.get(i).pkgList[0].toString().toLowerCase().equals("android") &&
                !RunningServices.get(i).pkgList[0].toString().toLowerCase().contains("launcher") &&
                !RunningServices.get(i).pkgList[0].toString().toLowerCase().contains("ims") &&
                !RunningServices.get(i).pkgList[0].toString().toLowerCase().contains("messaging") &&
                RunningServices.get(i).pkgList[0].toString().toLowerCase().contains("camera") &&
                RunningServices.get(i).pkgList[0].toString().toLowerCase().contains("settings") &&
                RunningServices.get(i).pkgList[0].toString().toLowerCase().contains("gms") &&
                RunningServices.get(i).pkgList[0].toString().toLowerCase().contains("google")) {
                    localActivityManager.killBackgroundProcesses(RunningServices.get(i).pkgList[0].toString());
            }
        }
    }

}
