package com.android.keyguard.clock;

import android.app.WallpaperManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.UserHandle;
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
import android.provider.Settings;
import android.content.Context;
import android.graphics.Typeface;
import java.util.TimeZone;

public class SparkClockController implements ClockPlugin {
    private ClockLayout mBigClockView;
    private final SysuiColorExtractor mColorExtractor;
    private float mDarkAmount;
    private TextClock mDateClock;
    private final LayoutInflater mLayoutInflater;
    private TextClock mTimeClock1;
    private TextClock mTimeClock2;
    private TextClock mTimeClock3;
    private TextClock mDateClockBig;
    private TextClock mTimeClock4;
    private TextClock mTimeClock5;
    private final ViewPreviewer mRenderer = new ViewPreviewer();
    private final Resources mResources;
    private ClockLayout mView;
    private Context mContext;
    
    public String getName() {
        return "Spark";
    }

    
    public String getTitle() {
        return "Spark";
    }

    
    public void setColorPalette(boolean z, int[] iArr) {
    }

    
    public void setStyle(Paint.Style style) {
    }

    
    public boolean shouldShowStatusArea() {
        return false;
    }

    
    public boolean usesPreferredY() {
        return true;
    }

    
    public SparkClockController(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor, Context context) {
        mResources = resources;
        mLayoutInflater = layoutInflater;
        mContext = mLayoutInflater.getContext();
        mColorExtractor = sysuiColorExtractor;
    }

    private void createViews() {
        mView = (ClockLayout) mLayoutInflater.inflate(R.layout.digital_clock_sparklsclock1, (ViewGroup) null);
        mBigClockView = (ClockLayout) mLayoutInflater.inflate(R.layout.digital_clock_sparklsclock1_big, (ViewGroup) null);
        mDateClock = (TextClock) mView.findViewById(R.id.date);
        mTimeClock1 = (TextClock) mView.findViewById(R.id.timeclock1);
        mTimeClock2 = (TextClock) mView.findViewById(R.id.timeclock2);
        mTimeClock3 = (TextClock) mView.findViewById(R.id.timeclock3);
        mDateClockBig = (TextClock) mBigClockView.findViewById(R.id.date);
        mTimeClock4 = (TextClock) mBigClockView.findViewById(R.id.timeclock1);
        mTimeClock5 = (TextClock) mBigClockView.findViewById(R.id.timeclock2);
    }

    
    public void onDestroyView() {
        mView = null;
        mTimeClock1 = null;
        mTimeClock2 = null;
        mTimeClock3 = null;
        mDateClock = null;
    }

    
    public Bitmap getThumbnail() {
        return BitmapFactory.decodeResource(mResources, R.drawable.default_thumbnail);
    }

    public Bitmap getPreview(int width, int height) {

        // Use the big clock view for the preview
        View view = getBigClockView();

        // Initialize state of plugin before generating preview.
        setDarkAmount(1f);
        ColorExtractor.GradientColors colors = mColorExtractor.getColors(
                WallpaperManager.FLAG_LOCK);
        setColorPalette(colors.supportsDarkText(), colors.getColorPalette());
        onTimeTick();

        return mRenderer.createPreview(view, width, height);
    }
    
    public View getView() {
        if (mView == null) {
            createViews();
        }
        return mView;
    }

    
    public View getBigClockView() {
        return mBigClockView;
    }

    
    public int getPreferredY(int totalheight) {
        return totalheight / 6;
    }

    
    public void setTextColor(int color) {
        TextClock textClock = mTimeClock1;
        TextClock textClock2 = mDateClock;
        TextClock textClock3 = mTimeClock2;
        TextClock textClock4 = mTimeClock3;
        TextClock textClock5 = mDateClockBig;
        TextClock textClock6 = mTimeClock4;
        TextClock textClock7 = mTimeClock5;

        int i2 = -1;
        boolean isCustomColorEnabled = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                Settings.Secure.KG_CUSTOM_CLOCK_COLOR_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        int customClockColor = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                Settings.Secure.KG_CUSTOM_CLOCK_COLOR, 0x92FFFFFF, UserHandle.USER_CURRENT);
        if (isCustomColorEnabled) {
            textClock.setTextColor(customClockColor);
            textClock2.setTextColor(customClockColor);
            textClock3.setTextColor(customClockColor);
            textClock4.setTextColor(customClockColor);
            textClock5.setTextColor(customClockColor);
            textClock6.setTextColor(customClockColor);
            textClock7.setTextColor(customClockColor);
        } else {
        textClock.setTextColor(mDarkAmount < 0.5f ? Utils.getColorAttrDefaultColor(textClock.getContext(), R.attr.wallpaperTextColorAccent) : -1);
        if (mDarkAmount < 0.5f) {
            i2 = Utils.getColorAttrDefaultColor(textClock2.getContext(), R.attr.wallpaperTextColorAccent);
        }
        textClock2.setTextColor(i2);
      }
    }
    
    public void onTimeTick() {
        mView.onTimeChanged();
        mBigClockView.onTimeChanged();
        mDateClock.refreshTime();
        mTimeClock1.refreshTime();
        mTimeClock2.refreshTime();
        mTimeClock3.refreshTime();
    }

    @Override
    public void setTypeface(Typeface tf) {
        mDateClock.setTypeface(tf);
        mTimeClock1.setTypeface(tf);
        mTimeClock2.setTypeface(tf);
        mTimeClock3.setTypeface(tf);
        mDateClockBig.setTypeface(tf);
        mTimeClock4.setTypeface(tf);
        mTimeClock5.setTypeface(tf);
    }


    public void setDarkAmount(float darkAmount) {
        mView.setDarkAmount(darkAmount);
        mDarkAmount = darkAmount;
        TextClock textClock = mTimeClock1;
        int i = (darkAmount > 0.5f ? 1 : (darkAmount == 0.5f ? 0 : -1));
        int i2 = -1;
        textClock.setTextColor(i < 0 ? Utils.getColorAttrDefaultColor(textClock.getContext(), R.attr.wallpaperTextColorAccent) : -1);
        TextClock textClock2 = mDateClock;
        if (i < 0) {
            i2 = Utils.getColorAttrDefaultColor(textClock2.getContext(), R.attr.wallpaperTextColorAccent);
        }
        textClock2.setTextColor(i2);
    }

    
    public void onTimeZoneChanged(TimeZone timeZone) {
        onTimeTick();
    }
}
