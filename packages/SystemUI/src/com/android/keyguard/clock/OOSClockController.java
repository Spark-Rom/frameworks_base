package com.android.keyguard.clock;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextClock;
import com.android.internal.colorextraction.ColorExtractor;
import com.android.settingslib.Utils;
import com.android.systemui.R;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.plugins.ClockPlugin;
import java.util.TimeZone;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.UserHandle;
import android.provider.Settings;
import android.content.Context;

public class OOSClockController implements ClockPlugin {
    private ClockLayout mBigView;
    private int mColor;
    private final SysuiColorExtractor mColorExtractor;
    private TextClock mDate;
    private TextClock mDay;
    private TextClock mDateBig;
    private TextClock mDayBig;
    private final LayoutInflater mLayoutInflater;
    private final ViewPreviewer mRenderer = new ViewPreviewer();
    private final Resources mResources;
    private TextClock mTimeClockBig;
    private TextClock mTimeClockAccentedBig;
    private TextClock mTimeClock;
    private TextClock mTimeClockAccented;
    private ClockLayout mView;
    private Context mContext;

    public String getName() {
        return "oos";
    }

    public String getTitle() {
        return "OxygenOS";
    }
    
    public boolean usesPreferredY() {
        return false;
    }
    
    public void setStyle(Paint.Style style) {
    }
    
    public boolean shouldShowStatusArea() {
        return false;
    }

    public OOSClockController(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor, Context context) {
        mResources = resources;
        mLayoutInflater = layoutInflater;
        mContext = mLayoutInflater.getContext();
        mColorExtractor = sysuiColorExtractor;
    }

    private void createViews() {
        mBigView = (ClockLayout) mLayoutInflater.inflate(R.layout.digital_clock_oos_big, (ViewGroup) null);
        mView = (ClockLayout) mLayoutInflater.inflate(R.layout.digital_clock_oos, (ViewGroup) null);
        mTimeClock = (TextClock) mView.findViewById(R.id.time_clock);
        mTimeClockAccented = (TextClock) mView.findViewById(R.id.time_clock_accented);
        mDay = (TextClock) mView.findViewById(R.id.clock_day);
        mDate = (TextClock) mView.findViewById(R.id.timedate);
        mTimeClockBig = (TextClock) mBigView.findViewById(R.id.time_clock);
        mTimeClockAccentedBig = (TextClock) mBigView.findViewById(R.id.time_clock_accented);
        mDayBig = (TextClock) mBigView.findViewById(R.id.clock_day);
        mDateBig = (TextClock) mBigView.findViewById(R.id.timedate);

    }

    @Override
    public void setTypeface(Typeface tf) {
        mTimeClock.setTypeface(tf);
        mTimeClockAccented.setTypeface(tf);
        mDay.setTypeface(tf);
        mDate.setTypeface(tf);
        mTimeClockAccentedBig.setTypeface(tf);
        mDateBig.setTypeface(tf);

    }
    public void onDestroyView() {
        mView = null;
        mTimeClock = null;
        mDay = null;
        mDate = null;
        mTimeClockAccented = null;
    }
    
    public Bitmap getThumbnail() {
        return BitmapFactory.decodeResource(mResources, R.drawable.default_thumbnail);
    }

    public Bitmap getPreview(int width, int height) {

        View inflate = getBigClockView();

        ColorExtractor.GradientColors colors = this.mColorExtractor.getColors(2);
        setColorPalette(colors.supportsDarkText(), colors.getColorPalette());
        onTimeTick();
        return mRenderer.createPreview(inflate, width, height);
    }

    public View getView() {
        if (mView == null) {
            createViews();
        }
        return mView;
    }

    public View getBigClockView() {
        return mBigView;
    }

    public int getPreferredY(int totalHeight) {
        return totalHeight / 6;
    }

    public void setTextColor(int color) {
        boolean isCustomColorEnabled = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                Settings.Secure.KG_CUSTOM_CLOCK_COLOR_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        int customClockColor = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                Settings.Secure.KG_CUSTOM_CLOCK_COLOR, 0x92FFFFFF, UserHandle.USER_CURRENT);
        if (isCustomColorEnabled) {
        mTimeClock.setTextColor(customClockColor);
        mDay.setTextColor(customClockColor);
        mDate.setTextColor(customClockColor);
        mTimeClockAccented.setTextColor(customClockColor);
        mTimeClockAccentedBig.setTextColor(customClockColor);
        mDateBig.setTextColor(customClockColor);
        } else {
        mTimeClock.setTextColor(color);
        mDay.setTextColor(color);
        mDate.setTextColor(color);
        mTimeClockAccented.setTextColor(color);
        mTimeClockAccentedBig.setTextColor(color);
        mDateBig.setTextColor(color);
        }
        mColor = color;
    }

    public void setColorPalette(boolean supportsDarkText, int[] colorPalette) {
        if (colorPalette == null || colorPalette.length == 0) {
            return;
        }
        final int accentColor = colorPalette[Math.max(0, colorPalette.length - 5)];
    }

    
    public void onTimeTick() {
        mView.onTimeChanged();
        mBigView.onTimeChanged();
        mTimeClock.refreshTime();
        mTimeClockAccented.refreshTime();
        mDay.refreshTime();
        mDate.refreshTime();
        setTextColor(mColor);
    }

    public void setDarkAmount(float darkAmount) {
        ClockLayout clockLayout = mView;
        if (clockLayout != null) {
            clockLayout.setDarkAmount(darkAmount);
        }
        int i = (darkAmount > 0.5f ? 1 : (darkAmount == 0.5f ? 0 : -1));
        int i2 = -1;
        if (i < 0) {
            i2 = Utils.getColorAttrDefaultColor(mTimeClock.getContext(), R.attr.wallpaperTextColorAccent);
        }
        mColor = i2;
    }

    public void onTimeZoneChanged(TimeZone timeZone) {
        onTimeTick();
    }
}
