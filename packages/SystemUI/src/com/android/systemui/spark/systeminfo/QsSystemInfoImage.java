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
import android.widget.ImageView;
import com.android.systemui.R;
import android.view.View;

public class QsSystemInfoImage extends ImageView {

    private Context mContext;
    private SettingsObserver settingsObserver;
    private boolean isSupported;
    private boolean mShowIcon;
    private int mSystemInfoMode;

    public QsSystemInfoImage(Context context) {
        super(context);
        init(context);
    }

    public QsSystemInfoImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public QsSystemInfoImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        settingsObserver = new SettingsObserver(new Handler());
        settingsObserver.observe();
        isSupported = getAvailability();
        mSystemInfoMode = getQsSystemInfoMode();
        mShowIcon = getShouldShowIcon();
        updateSystemInfoImage();
    }

    private int getQsSystemInfoMode() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QS_SYSTEM_INFO, 0);
    }

    private boolean getShouldShowIcon() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QS_SYSTEM_INFO_ICON, 1) == 1;
    }

    private boolean getAvailability() {
        return mContext.getResources().
                getBoolean(com.android.internal.R.bool.config_supportSystemInfo);
    }

    public void updateSystemInfoImage() {
       if (mSystemInfoMode != 0 && isSupported && mShowIcon) {
          setVisibility(View.VISIBLE);
          switch (mSystemInfoMode) {
              case 1:
                  setImageDrawable(mContext.getDrawable(R.drawable.ic_cpu_thermometer));
                  break;
              case 2:
                  setImageDrawable(mContext.getDrawable(R.drawable.ic_batt_thermometer));
                  break;
              case 3:
                  setImageDrawable(mContext.getDrawable(R.drawable.ic_speed_gpu));
                  break;
              case 4:
                  setImageDrawable(mContext.getDrawable(R.drawable.ic_memory_gpu));
                  break;
          }
       } else {
          setVisibility(View.GONE);
       }
    }

    class SettingsObserver extends ContentObserver {

        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.QS_SYSTEM_INFO), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.QS_SYSTEM_INFO_ICON), false, this);

            updateSystemInfoImage();
        }

        @Override
        public void onChange(boolean selfChange) {
            updateSystemInfoImage();
        }
    }
}
