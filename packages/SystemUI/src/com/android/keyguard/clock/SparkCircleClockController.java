package com.android.keyguard.clock;

import android.app.WallpaperManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.os.UserHandle;
import android.content.Context;
import android.provider.Settings;
import android.graphics.Typeface;

import java.util.TimeZone;

public class SparkCircleClockController implements ClockPlugin {
    private ClockLayout mBigClockView;
    private final SysuiColorExtractor mColorExtractor;
    private float mDarkAmount;
    private final LayoutInflater mLayoutInflater;
    private final ViewPreviewer mRenderer = new ViewPreviewer();
    private final Resources mResources;
    private ClockLayout mView;
    private TextClock mTextClock1;
    private TextClock mTextClock2;
    private TextClock mTextClock3;
    private TextClock mTextClock4;
    private TextClock mTextClock5;
    private TextClock mTextClock1Big;
    private TextClock mTextClock2Big;
    private TextClock mTextClock3Big;
    private TextClock mTextClock4Big;
    private TextClock mTextClock5Big;
    private Context mContext;

    
    public String getName() {
        return "SparkCircle";
    }

    
    public String getTitle() {
        return "SparkCircle";
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

    
    public SparkCircleClockController(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor, Context context) {
        mResources = resources;
        mLayoutInflater = layoutInflater;
        mContext = mLayoutInflater.getContext();
        mColorExtractor = sysuiColorExtractor;
    }

    private void createViews() {
        mView = (ClockLayout) mLayoutInflater.inflate(R.layout.digital_clock_SparkCircleClock, (ViewGroup) null);
        mBigClockView = (ClockLayout) mLayoutInflater.inflate(R.layout.digital_clock_SparkCircleClock_big, (ViewGroup) null);
        mTextClock1 = (TextClock) mView.findViewById(R.id.textclock1);
        mTextClock2 = (TextClock) mView.findViewById(R.id.textclock2);
        mTextClock3 = (TextClock) mView.findViewById(R.id.textclock3);
        mTextClock4 = (TextClock) mView.findViewById(R.id.textclock4);
        mTextClock5 = (TextClock) mView.findViewById(R.id.textclock5);
        mTextClock1Big = (TextClock) mBigClockView.findViewById(R.id.textclock1);
        mTextClock2Big = (TextClock) mBigClockView.findViewById(R.id.textclock2);
        mTextClock3Big = (TextClock) mBigClockView.findViewById(R.id.textclock3);
        mTextClock4Big = (TextClock) mBigClockView.findViewById(R.id.textclock4);
        mTextClock5Big = (TextClock) mBigClockView.findViewById(R.id.textclock5);

    }


    @Override
    public void setTypeface(Typeface tf) {
        mTextClock1.setTypeface(tf);
        mTextClock2.setTypeface(tf);
        mTextClock3.setTypeface(tf);
        mTextClock4.setTypeface(tf);
        mTextClock5.setTypeface(tf);
        mTextClock1Big.setTypeface(tf);
        mTextClock2Big.setTypeface(tf);
        mTextClock3Big.setTypeface(tf);
        mTextClock4Big.setTypeface(tf);
        mTextClock5Big.setTypeface(tf);
    }

    public void setTextColor(int color) {
        TextClock textClock1 = mTextClock1;
        TextClock textClock2 = mTextClock2;
        TextClock textClock3 = mTextClock3;
        TextClock textClock4 = mTextClock4;
        TextClock textClock5 = mTextClock5;
        TextClock textClock6 = mTextClock1Big;
        TextClock textClock7 = mTextClock2Big;
        TextClock textClock8 = mTextClock3Big;
        TextClock textClock9 = mTextClock4Big;
        TextClock textClock10 = mTextClock5Big;

        int i2 = -1;
        boolean isCustomColorEnabled = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                Settings.Secure.KG_CUSTOM_CLOCK_COLOR_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        int customClockColor = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                Settings.Secure.KG_CUSTOM_CLOCK_COLOR, 0x92FFFFFF, UserHandle.USER_CURRENT);
        if (isCustomColorEnabled) {
            textClock1.setTextColor(customClockColor);
            textClock2.setTextColor(customClockColor);
            textClock3.setTextColor(customClockColor);
            textClock4.setTextColor(customClockColor);
            textClock5.setTextColor(customClockColor);
            textClock6.setTextColor(customClockColor);
            textClock7.setTextColor(customClockColor);
            textClock8.setTextColor(customClockColor);
            textClock9.setTextColor(customClockColor);
            textClock10.setTextColor(customClockColor);
         }
    }

    public void onDestroyView() {
        mView = null;
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
        return totalheight / 4;
    }

    

    public void onTimeTick() {
        mView.onTimeChanged();
        mBigClockView.onTimeChanged();
    }

    
    public void setDarkAmount(float darkAmount) {
        mView.setDarkAmount(darkAmount);
        mDarkAmount = darkAmount;
        int i = (darkAmount > 0.5f ? 1 : (darkAmount == 0.5f ? 0 : -1));
        int i2 = -1;
    }

    
    public void onTimeZoneChanged(TimeZone timeZone) {
        onTimeTick();
    }
}
