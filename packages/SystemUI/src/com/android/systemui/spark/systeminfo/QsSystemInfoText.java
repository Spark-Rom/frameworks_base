package com.android.systemui.spark.systeminfo;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import com.android.systemui.util.AutoMarqueeTextView;
import android.view.View;
import com.android.systemui.R;
import android.content.res.Resources;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

public class QsSystemInfoText extends AutoMarqueeTextView {

    private Context mContext;
    private SettingsObserver settingsObserver;
    private boolean isSupported;
    private int mSystemInfoMode;
    private String mSysCPUTemp;
    private String mSysBatTemp;
    private String mSysGPUFreq;
    private String mSysGPULoad;
    private int mSysCPUTempMultiplier;
    private int mSysBatTempMultiplier;

    public QsSystemInfoText(Context context) {
        super(context);
        init(context);
    }

    public QsSystemInfoText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public QsSystemInfoText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        settingsObserver = new SettingsObserver(new Handler());
        settingsObserver.observe();
        isSupported = getAvailability();
        mSystemInfoMode = getQsSystemInfoMode();
        updateSystemInfoText();
    }

    private int getQsSystemInfoMode() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QS_SYSTEM_INFO, 0);
    }

    private boolean getAvailability() {
        return mContext.getResources()
                .getBoolean(com.android.internal.R.bool.config_supportSystemInfo);
    }

    public void updateSystemInfoText() {
        if (mSystemInfoMode != 0 && isSupported) {
            updateSysInfoResources();
            String systemInfoText = "";
            int defaultMultiplier = 1;
            switch (mSystemInfoMode) {
                case 1:
                    systemInfoText = getSystemInfo(mSysCPUTemp, mSysCPUTempMultiplier, "\u2103", true);
                    break;
                case 2:
                    systemInfoText = getSystemInfo(mSysBatTemp, mSysBatTempMultiplier, "\u2103", true);
                    break;
                case 3:
                    systemInfoText = getSystemInfo(mSysGPUFreq, defaultMultiplier, "Mhz", true);
                    break;
                case 4:
                    systemInfoText = getGPUBusy();
                    break;
            }
            if (systemInfoText != " " && systemInfoText != null && !systemInfoText.isEmpty()) {
                setText(systemInfoText);
                setVisibility(View.VISIBLE);
            }
        } else {
            setVisibility(View.GONE);
        }
    }

    public static boolean fileExists(String fileName) {
        final File file = new File(fileName);
        return file.exists();
    }

    private static String readOneLine(String fname) {
        BufferedReader br;
        String line = null;
        try {
            br = new BufferedReader(new FileReader(fname), 512);
            try {
                line = br.readLine();
            } finally {
                br.close();
            }
        } catch (Exception e) {
            return null;
        }
        return line;
    }

    public static boolean isValueNumeric(String string) {
    	int intValue;

        if(string == null || string.equals("")) {
            return false;
        }

        try {
            intValue = Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
        }
        return false;
    }


    private String getGPUBusy() {
    	String gpuBusyValue;
        if (!mSysGPULoad.isEmpty() && fileExists(mSysGPULoad)) {
            gpuBusyValue = readOneLine(mSysGPULoad);
            if (gpuBusyValue == null) {
              gpuBusyValue = " ";
            }
            return gpuBusyValue;
        }
        return " ";
    }

   private String getSystemInfo(String sysPath, int multiplier, String unit, boolean returnFormatted) {
   	String formattedValue;
   	String value;

        if (!sysPath.isEmpty() && fileExists(sysPath)) {
            value = readOneLine(sysPath);
            if (value == null) {
              value = " ";
            }
            if (isValueNumeric(value)) {
              formattedValue = String.format("%s", Integer.parseInt(value) / multiplier) + unit;
            } else {
              formattedValue = " ";
              value = " ";
            }
            return returnFormatted ? formattedValue : value;
        }
        return " ";

    }

    private void updateSysInfoResources(){
        Resources resources = mContext.getResources();
        mSysCPUTemp = resources.getString(
                  com.android.internal.R.string.config_sysCPUTemp);
        mSysBatTemp = resources.getString(
                  com.android.internal.R.string.config_sysBatteryTemp);
        mSysGPUFreq = resources.getString(
                  com.android.internal.R.string.config_sysGPUFreq);
        mSysGPULoad = resources.getString(
                  com.android.internal.R.string.config_sysGPULoad);
        mSysCPUTempMultiplier = resources.getInteger(
                  com.android.internal.R.integer.config_sysCPUTempMultiplier);
        mSysBatTempMultiplier = resources.getInteger(
                  com.android.internal.R.integer.config_sysBatteryTempMultiplier);
    }

    class SettingsObserver extends ContentObserver {

        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.QS_SYSTEM_INFO), false, this);
            updateSystemInfoText();
        }

        @Override
        public void onChange(boolean selfChange) {
            updateSystemInfoText();
        }
    }

}
