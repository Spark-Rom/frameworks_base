/**
 * Copyright (C) 2021 ExtendedUI
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

package com.android.systemui.biometrics;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.WindowManager;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.UserHandle;
import android.text.TextUtils;
import com.android.systemui.Dependency;
import com.android.systemui.spark.OmniSettingsService;
import java.io.FileDescriptor;
import com.android.systemui.R;

public class FODIconView extends ImageView implements OmniSettingsService.OmniSettingsObserver {
    private AnimationDrawable iconAnim;
    private boolean mIsFODIconAnimated;
    private boolean mIsKeyguard;
    private final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
    private int mPositionX;
    private int mPositionY;
    private int mSize;
    private final WindowManager mWindowManager;
    private BitmapDrawable mCustomImage;
    private Handler mHandler;

    private int mSelectedAnim;
    private final int[] ANIMATION_STYLES_NAMES = {
        R.drawable.fod_icon_aod_anim,
        R.drawable.zaid_oneui_fod,
        R.drawable.oneui2_fod,
        R.drawable.oos_fod_animated,
        R.drawable.fod_spark_animated
    };

    private int mSelectedIcon;
    private final int[] ICON_STYLES = {
        R.drawable.fod_icon_default,
        R.drawable.fod_icon_default_0,
        R.drawable.fod_icon_default_1,
        R.drawable.fod_icon_default_2,
        R.drawable.fod_icon_default_3,
        R.drawable.fod_icon_default_4,
        R.drawable.fod_icon_madness,
        R.drawable.fod_icon_arc_reactor,
        R.drawable.fod_icon_zaid1,
        R.drawable.fod_icon_zaid2,
        R.drawable.fod_icon_zaid3,
        R.drawable.fod_icon_spark,
        R.drawable.fod_icon_glow_circle,
        R.drawable.fod_icon_neon_arc,
        R.drawable.fod_icon_neon_arc_gray,
        R.drawable.fod_icon_neon_circle_pink,
        R.drawable.fod_icon_neon_triangle,
        R.drawable.fod_icon_whatever,
        R.drawable.fod_icon_unfunnyguy,
        R.drawable.fod_icon_zaid4,
        R.drawable.fod_icon_zaid5,
        R.drawable.fod_icon_sun_metro,
        R.drawable.fod_icon_sammy,
        R.drawable.fod_icon_op_white,
        R.drawable.fod_icon_zaid6,
        R.drawable.fod_icon_light,
        R.drawable.fod_icon_gxzw,
        R.drawable.fod_icon_transparent
    };

    public FODIconView(Context context, int i, int i2, int i3) {
        super(context);
        this.mPositionX = i2;
        this.mPositionY = i3;
        this.mSize = i;
        this.mWindowManager = (WindowManager) context.getSystemService(WindowManager.class);
        setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        context.getResources();
        WindowManager.LayoutParams layoutParams = this.mParams;
        int i4 = this.mSize;
        layoutParams.height = i4;
        layoutParams.width = i4;
        layoutParams.format = -3;
        layoutParams.packageName = "android";
        layoutParams.type = 2020;
        layoutParams.flags = 264;
        layoutParams.gravity = 51;
        layoutParams.setTitle("Fingerprint on display icon");
        this.mWindowManager.addView(this, this.mParams);
        boolean x =  Settings.System.getInt(getContext().getContentResolver(), "custom_fingerprint_icon", 0) != 0;
        boolean z = Settings.System.getInt(getContext().getContentResolver(), "fod_icon_animation", 0) != 0;
        this.mIsFODIconAnimated = z;
        if (z) {
            mCustomSettingsObserver.observe();
            mCustomSettingsObserver.update();
            setBackgroundResource(ANIMATION_STYLES_NAMES[mSelectedAnim]);
            this.iconAnim = (AnimationDrawable) getBackground();
        } else if (!x) {
            setCustomIcon();
        mCustomSettingsObserver.observe();
        mCustomSettingsObserver.update();
        } else {
            setImageResource(ICON_STYLES[mSelectedIcon]);
        }
        hide();

        mCustomSettingsObserver.observe();
        mCustomSettingsObserver.update();
    }

    private CustomSettingsObserver mCustomSettingsObserver = new CustomSettingsObserver(mHandler);
    private class CustomSettingsObserver extends ContentObserver {

        CustomSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.FOD_ICON),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.FOD_ICON_ANIM_TYPE),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor(
                   Settings.System.OMNI_CUSTOM_FP_ICON),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            boolean z = Settings.System.getInt(getContext().getContentResolver(), "fod_icon_animation", 0) != 0;
            boolean x = Settings.System.getInt(getContext().getContentResolver(), "custom_fingerprint_icon", 0) != 0;
            mIsFODIconAnimated = z;
            if (uri.equals(Settings.System.getUriFor(Settings.System.FOD_ICON)) ||
                    uri.equals(Settings.System.getUriFor(Settings.System.FOD_ICON_ANIM_TYPE)) ||
                    uri.equals(Settings.System.getUriFor(Settings.System.OMNI_CUSTOM_FP_ICON))) {
                updateStyle(z);
            }
        }

        public void update() {
            boolean x = Settings.System.getInt(getContext().getContentResolver(), "custom_fingerprint_icon", 0) != 0;
            boolean z = Settings.System.getInt(getContext().getContentResolver(), "fod_icon_animation", 0) != 0;
            mIsFODIconAnimated = z;
            updateStyle(z);
            updateStyle(x);
        }
    }

    public void updateStyle(boolean isEnabled) {
        Dependency.get(OmniSettingsService.class).addStringObserver(this, Settings.System.OMNI_CUSTOM_FP_ICON);
        mSelectedIcon = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.FOD_ICON, 0);
        mSelectedAnim = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.FOD_ICON_ANIM_TYPE, 0);
    }

    public void hide() {
        setVisibility(8);
        if (this.iconAnim != null && this.mIsFODIconAnimated) {
            clearAnimation();
            this.iconAnim.stop();
            this.iconAnim.selectDrawable(0);
        }
    }

    public void show() {
        setIsAnimationEnabled(Settings.System.getInt(getContext().getContentResolver(), "fod_icon_animation", 0) != 0);
        setVisibility(0);
        AnimationDrawable animationDrawable = this.iconAnim;
        if (animationDrawable != null && this.mIsFODIconAnimated && this.mIsKeyguard) {
            animationDrawable.start();
        }
    }

    public void updatePosition(int i, int i2) {
        this.mPositionX = i;
        this.mPositionY = i2;
        WindowManager.LayoutParams layoutParams = this.mParams;
        layoutParams.x = i;
        layoutParams.y = i2;
        this.mWindowManager.updateViewLayout(this, layoutParams);
    }

    public void setIsAnimationEnabled(boolean z) {
            boolean x = Settings.System.getInt(getContext().getContentResolver(), "custom_fingerprint_icon", 0) != 0;
        this.mIsFODIconAnimated = z;
        if (z) {
            setImageResource(0);
            mCustomSettingsObserver.observe();
            mCustomSettingsObserver.update();
            setBackgroundResource(ANIMATION_STYLES_NAMES[mSelectedAnim]);
            this.iconAnim = (AnimationDrawable) getBackground();
            return;
        } else if (!x) {
        setCustomIcon();
            mCustomSettingsObserver.observe();
            mCustomSettingsObserver.update();
        setBackgroundResource(0);
        } else {
        setBackgroundResource(0);
        setImageResource(ICON_STYLES[mSelectedIcon]);
    }
 }
    private void setCustomIcon(){
        if (mCustomImage != null) {
            setImageDrawable(mCustomImage);
        } else {
        setImageResource(ICON_STYLES[mSelectedIcon]);
        }
    }

    @Override
    public void onStringSettingChanged(String key, String customIconURI) {
        if (!TextUtils.isEmpty(customIconURI)) {
            loadCustomImage(customIconURI);
        } else {
            mCustomImage = null;
        }
    }

    private void loadCustomImage(String customIconURI) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContext().getContentResolver().openFileDescriptor(Uri.parse(customIconURI), "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            mCustomImage = new BitmapDrawable(getResources(), image);
        }
        catch (Exception e) {
            mCustomImage = null;
        }
    }

    public void setIsKeyguard(boolean z) {
        this.mIsKeyguard = z;
        if (this.mIsKeyguard || !this.mIsFODIconAnimated) {
            setBackgroundTintList(null);
        } else {
            setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#807B7E")));
        }
    }
}
