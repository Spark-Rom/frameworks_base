/*
* Copyright (C) 2014 The Android Open Source Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package com.android.systemui.spark;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.app.WallpaperInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.palette.graphics.Palette;

import com.android.settingslib.Utils;
import com.android.systemui.R;

public class AmbientText extends FrameLayout {
   private static final boolean DEBUG = false;
   private static final String TAG = "AmbientText";
   private TextView mAmbientText;
   private ValueAnimator mTextAnimator;
   private ValueAnimator mTextEndAnimator;
   private boolean mEnable;
   private WallpaperManager mWallManager;

   public AmbientText(Context context) {
       this(context, null);
   }

   public AmbientText(Context context, AttributeSet attrs) {
       this(context, attrs, 0);
   }

   public AmbientText(Context context, AttributeSet attrs, int defStyleAttr) {
       this(context, attrs, defStyleAttr, 0);
   }

   public AmbientText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
       super(context, attrs, defStyleAttr, defStyleRes);
       if (DEBUG) Log.d(TAG, "new");
   }

   private Runnable mTextUpdate = new Runnable() {
       @Override
       public void run() {
           if (DEBUG) Log.d(TAG, "run");
           animateText(mEnable);
       }
   };

   @Override
   public void draw(Canvas canvas) {
       super.draw(canvas);
       if (DEBUG) Log.d(TAG, "draw");
   }

   public void update() {

      ContentResolver resolver = getContext().getContentResolver();
      TextView textView = (TextView) findViewById(R.id.ambient_text);

      String text = Settings.System.getStringForUser(resolver ,
                      Settings.System.AMBIENT_TEXT_STRING,
                      UserHandle.USER_CURRENT);

      FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) textView.getLayoutParams();

      int align = Settings.System.getIntForUser(resolver,
              Settings.System.AMBIENT_TEXT_ALIGNMENT, 3, UserHandle.USER_CURRENT);

      switch (align) {
          case 0:
            textView.setGravity(Gravity.START|Gravity.TOP);
            lp.gravity = Gravity.START | Gravity.TOP;
            break;
          case 1:
            textView.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
            lp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
            break;
          case 2:
            textView.setGravity(Gravity.START|Gravity.BOTTOM);
            lp.gravity = Gravity.START | Gravity.BOTTOM;
            break;
          case 3:
          default:
            textView.setGravity(Gravity.CENTER);
            lp.gravity = Gravity.CENTER;
            break;
          case 4:
            textView.setGravity(Gravity.END|Gravity.TOP);
            lp.gravity = Gravity.END | Gravity.TOP;
            break;
          case 5:
            textView.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);
            lp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
            break;
          case 6:
            textView.setGravity(Gravity.END|Gravity.BOTTOM);
            lp.gravity = Gravity.END | Gravity.BOTTOM;
            break;
      }
      textView.setLayoutParams(lp);
      textView.setText(text);
      textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, updateTextSize());
      refreshTextFont();

   }

    public void refreshTextFont() {
        final Resources res = getContext().getResources();
        int textFont = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.AMBIENT_TEXT_FONT, 8);
        TextView textView = (TextView) findViewById(R.id.ambient_text);

        switch (textFont) {
        	case 0:
        	Typeface sansSF = Typeface.create("sans-serif", Typeface.NORMAL);
        	textView.setTypeface(sansSF);
        	break;
        	case 1:
        	Typeface accuratist = Typeface.create("accuratist", Typeface.NORMAL);
        	textView.setTypeface(accuratist);
        	break;
        	case 2:
        	Typeface aclonica = Typeface.create("aclonica", Typeface.NORMAL);
        	textView.setTypeface(aclonica);
        	break;
        	case 3:
        	Typeface amarante = Typeface.create("amarante", Typeface.NORMAL);
        	textView.setTypeface(amarante);
        	break;
        	case 4:
        	Typeface bariol = Typeface.create("bariol", Typeface.NORMAL);
        	textView.setTypeface(bariol);
        	break;
        	case 5:
        	Typeface cagliostro = Typeface.create("cagliostro", Typeface.NORMAL);
        	textView.setTypeface(cagliostro);
        	break;
        	case 6:
        	Typeface cocon = Typeface.create("cocon", Typeface.NORMAL);
        	textView.setTypeface(cocon);
        	break;
        	case 7:
        	Typeface comfortaa = Typeface.create("comfortaa", Typeface.NORMAL);
        	textView.setTypeface(comfortaa);
        	break;
        	case 8:
        	Typeface comicsans = Typeface.create("comicsans", Typeface.NORMAL);
        	textView.setTypeface(comicsans);
        	break;
        	case 9:
        	Typeface coolstory = Typeface.create("coolstory", Typeface.NORMAL);
        	textView.setTypeface(coolstory);
        	break;
        	case 10:
        	Typeface exotwo = Typeface.create("exotwo", Typeface.NORMAL);
        	textView.setTypeface(exotwo);
        	break;
        	case 11:
        	Typeface fifa2018 = Typeface.create("fifa2018", Typeface.NORMAL);
        	textView.setTypeface(fifa2018);
        	break;
        	case 12:
        	Typeface fluidsans = Typeface.create("fluid-sans", Typeface.NORMAL);
        	textView.setTypeface(fluidsans);
        	break;
        	case 13:
        	Typeface googlesans = Typeface.create("googlesans", Typeface.NORMAL);
        	textView.setTypeface(googlesans);
        	break;
        	case 14:
        	Typeface grandhotel = Typeface.create("grandhotel", Typeface.NORMAL);
        	textView.setTypeface(grandhotel);
        	break;
        	case 15:
        	Typeface harmonyossans = Typeface.create("harmonyos-sans", Typeface.NORMAL);
        	textView.setTypeface(harmonyossans);
        	break;
        	case 16:
        	Typeface intercustom = Typeface.create("inter_custom", Typeface.NORMAL);
        	textView.setTypeface(intercustom);
        	break;
        	case 17:
        	Typeface jtleonor = Typeface.create("jtleonor", Typeface.NORMAL);
        	textView.setTypeface(jtleonor);
        	break;
        	case 18:
        	Typeface latobold = Typeface.create("lato-bold", Typeface.NORMAL);
        	textView.setTypeface(latobold);
        	break;
        	case 19:
        	Typeface lgsmartgothic = Typeface.create("lgsmartgothic", Typeface.NORMAL);
        	textView.setTypeface(lgsmartgothic);
        	break;
        	case 20:
        	Typeface linotte = Typeface.create("linotte", Typeface.NORMAL);
        	textView.setTypeface(linotte);
        	break;
        	case 21:
        	Typeface misans = Typeface.create("misans", Typeface.NORMAL);
        	textView.setTypeface(misans);
        	break;
        	case 22:
        	Typeface nokiapure = Typeface.create("nokiapure", Typeface.NORMAL);
        	textView.setTypeface(nokiapure);
        	break;
        	case 23:
        	Typeface nothingdot57 = Typeface.create("nothingdot57", Typeface.NORMAL);
        	textView.setTypeface(nothingdot57);
        	break;
        	case 24:
        	Typeface nunitobold = Typeface.create("nunito-bold", Typeface.NORMAL);
        	textView.setTypeface(nunitobold);
        	break;
        	case 25:
        	Typeface opsans = Typeface.create("op-sans", Typeface.NORMAL);
        	textView.setTypeface(opsans);
        	break;
        	case 26:
        	Typeface oneplusslate = Typeface.create("oneplusslate", Typeface.NORMAL);
        	textView.setTypeface(oneplusslate);
        	break;
        	case 27:
        	Typeface opposans = Typeface.create("opposans", Typeface.NORMAL);
        	textView.setTypeface(opposans);
        	break;
        	case 28:
        	Typeface oswaldbold = Typeface.create("oswald-bold", Typeface.NORMAL);
        	textView.setTypeface(oswaldbold);
        	break;
        	case 29:
        	Typeface productsansvh = Typeface.create("productsansvh", Typeface.NORMAL);
        	textView.setTypeface(productsansvh);
        	break;
        	case 30:
        	Typeface quando = Typeface.create("quando", Typeface.NORMAL);
        	textView.setTypeface(quando);
        	break;
        	case 31:
        	Typeface redressed = Typeface.create("redressed", Typeface.NORMAL);
        	textView.setTypeface(redressed);
        	break;
        	case 32:
        	Typeface reemkufi = Typeface.create("reemkufi", Typeface.NORMAL);
        	textView.setTypeface(reemkufi);
        	break;
        	case 33:
        	Typeface robotocondensed = Typeface.create("robotocondensed", Typeface.NORMAL);
        	textView.setTypeface(robotocondensed);
        	break;
        	case 34:
        	Typeface rosemary = Typeface.create("rosemary", Typeface.NORMAL);
        	textView.setTypeface(rosemary);
        	break;
        	case 35:
        	Typeface rubikbold = Typeface.create("rubik-bold", Typeface.NORMAL);
        	textView.setTypeface(rubikbold);
        	break;
        	case 36:
        	Typeface samsungone = Typeface.create("samsungone", Typeface.NORMAL);
        	textView.setTypeface(samsungone);
        	break;
        	case 37:
        	Typeface sanfrancisco = Typeface.create("sanfrancisco", Typeface.NORMAL);
        	textView.setTypeface(sanfrancisco);
        	break;
        	case 38:
        	Typeface simpleday = Typeface.create("simpleday", Typeface.NORMAL);
        	textView.setTypeface(simpleday);
        	break;
        	case 39:
        	Typeface sonysketch = Typeface.create("sonysketch", Typeface.NORMAL);
        	textView.setTypeface(sonysketch);
        	break;
        	case 40:
        	Typeface storopia = Typeface.create("storopia", Typeface.NORMAL);
        	textView.setTypeface(storopia);
        	break;
        	case 41:
        	Typeface surfer = Typeface.create("surfer", Typeface.NORMAL);
        	textView.setTypeface(surfer);
        	break;
        	case 42:
        	Typeface ubuntu = Typeface.create("ubuntu", Typeface.NORMAL);
        	textView.setTypeface(ubuntu);
        	break;
        	case 43:
        	Typeface manrope = Typeface.create("manrope", Typeface.NORMAL);
        	textView.setTypeface(manrope);
        	break;
        	case 44:
        	Typeface notosans = Typeface.create("noto-sans", Typeface.NORMAL);
        	textView.setTypeface(notosans);
        	break;
        	case 45:
        	Typeface recursivecasual = Typeface.create("recursive-casual", Typeface.NORMAL);
        	textView.setTypeface(recursivecasual);
        	break;
        	case 46:
        	Typeface recursive = Typeface.create("recursive", Typeface.NORMAL);
        	textView.setTypeface(recursive);
        	break;
        	case 47:
        	Typeface robotosystem = Typeface.create("roboto-system", Typeface.NORMAL);
        	textView.setTypeface(robotosystem);
        	break;
        	case 48:
        	Typeface sourcesans = Typeface.create("source-sans", Typeface.NORMAL);
        	textView.setTypeface(sourcesans);
        	break;
        	case 49:
        	Typeface serif = Typeface.create("serif", Typeface.NORMAL);
        	textView.setTypeface(serif);
        	break;
        	case 50:
        	Typeface googlesansclock = Typeface.create("googlesansclock", Typeface.NORMAL);
        	textView.setTypeface(googlesansclock);
        	break;
        	case 51:
        	Typeface apiceoutline = Typeface.create("apiceoutline", Typeface.NORMAL);
        	textView.setTypeface(apiceoutline);
        	break;
        	case 52:
        	Typeface audimat = Typeface.create("audimat", Typeface.NORMAL);
        	textView.setTypeface(audimat);
        	break;
        	case 53:
        	Typeface geometossoftextrabold = Typeface.create("geometossoftextrabold", Typeface.NORMAL);
        	textView.setTypeface(geometossoftextrabold);
        	break;
        	default:
        	break;
        }
    }

   private int updateTextSize() {
        final ContentResolver resolver = mContext.getContentResolver();
        int mAmbientTextSize = Settings.System.getIntForUser(resolver,
                Settings.System.AMBIENT_TEXT_SIZE, 30, UserHandle.USER_CURRENT);

        switch (mAmbientTextSize) {
            case 1:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_1);
            case 2:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_2);
            case 3:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_3);
            case 4:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_4);
            case 5:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_5);
            case 6:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_6);
            case 7:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_7);
            case 8:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_8);
            case 9:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_9);
            case 10:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_10);
            case 11:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_11);
            case 12:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_12);
            case 13:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_13);
            case 14:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_14);
            case 15:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_15);
            case 16:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_16);
            case 17:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_17);
            case 18:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_18);
            case 19:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_19);
            case 20:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_20);
            case 21:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_21);
            case 22:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_22);
            case 23:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_23);
            case 24:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_24);
            case 25:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_25);
            case 26:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_26);
            case 27:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_27);
            case 28:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_28);
            case 29:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_29);
            case 30:
            default:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_30);
            case 31:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_31);
            case 32:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_32);
            case 33:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_33);
            case 34:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_34);
            case 35:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_35);
            case 36:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_36);
            case 37:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_37);
            case 38:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_38);
            case 39:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_39);
            case 40:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_40);
            case 41:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_41);
            case 42:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_42);
            case 43:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_43);
            case 44:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_44);
            case 45:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_45);
            case 46:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_46);
            case 47:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_47);
            case 48:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_48);
            case 49:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_49);
            case 50:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_50);
            case 51:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_51);
            case 52:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_52);
            case 53:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_53);
            case 54:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_54);
            case 55:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_55);
            case 56:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_56);
            case 57:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_57);
            case 58:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_58);
            case 59:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_59);
            case 60:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_60);
            case 61:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_61);
            case 62:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_62);
            case 63:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_63);
            case 64:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_64);
            case 65:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_65);
            case 66:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_66);
            case 67:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_67);
            case 68:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_68);
            case 69:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_69);
            case 70:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_70);
            case 71:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_71);
            case 72:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_72);
            case 73:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_73);
            case 74:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_74);
            case 75:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_75);
            case 76:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_76);
            case 77:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_77);
            case 78:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_78);
            case 79:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_79);
            case 80:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_80);
            case 81:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_81);
            case 82:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_82);
            case 83:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_83);
            case 84:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_84);
            case 85:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_85);
            case 86:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_86);
            case 87:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_87);
            case 88:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_88);
            case 89:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_89);
            case 90:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_90);
            case 91:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_91);
            case 92:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_92);
            case 93:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_93);
            case 94:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_94);
            case 95:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_95);
            case 96:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_96);
            case 97:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_97);
            case 98:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_98);
            case 99:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_99);
            case 100:
                return (int) mContext.getResources().getDimension(R.dimen.amb_txt_font_size_100);
        }
    }

   public void animateText(boolean mEnable) {
       TextView text = (TextView) findViewById(R.id.ambient_text);
       mTextAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 2.0f});
       mTextAnimator.setDuration(5000);
       int textColorType = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.AMBIENT_TEXT_TYPE_COLOR, 0, UserHandle.USER_CURRENT);
       int color = Utils.getColorAccentDefaultColor(getContext());
       switch (textColorType) {
           case 1:
               try {
                   WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
                   WallpaperInfo wallpaperInfo = wallpaperManager.getWallpaperInfo();
                   if (wallpaperInfo == null) { // if not a live wallpaper
                       Drawable wallpaperDrawable = wallpaperManager.getDrawable();
                       Bitmap bitmap = ((BitmapDrawable)wallpaperDrawable).getBitmap();
                       if (bitmap != null) { // if wallpaper is not blank
                           Palette p = Palette.from(bitmap).generate();
                           int wallColor = p.getDominantColor(color);
                           if (color != wallColor)
                               color = wallColor;
                       }
                   }
               } catch (Exception e) {
                   // Nothing to do
               }
               break;
           case 2:
               color = Settings.System.getIntForUser(mContext.getContentResolver(),
                       Settings.System.AMBIENT_TEXT_COLOR, 0xFF3980FF,
                       UserHandle.USER_CURRENT);
               break;
       }

       if (mEnable) {
           mTextAnimator.setRepeatCount(ValueAnimator.INFINITE);
       }
       mTextAnimator.setRepeatMode(ValueAnimator.REVERSE);
       text.setTextColor(color);
       mTextAnimator.addUpdateListener(new AnimatorUpdateListener() {
           public void onAnimationUpdate(ValueAnimator animation) {
               if (DEBUG) Log.d(TAG, "onAnimationUpdate");
               float progress = ((Float) animation.getAnimatedValue()).floatValue();
               float alpha = 1.0f;
               if (mEnable) {
                 if (progress <= 0.3f) {
                     alpha = progress / 0.3f;
                 } else if (progress >= 1.0f) {
                     alpha = 2.0f - progress;
                 }
               }
               text.setAlpha(alpha);
           }
       });
       if (DEBUG) Log.d(TAG, "start");
       mTextAnimator.start();
   }

   public void animateEndText() {
       TextView text = (TextView) findViewById(R.id.ambient_text);
       mTextAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
       mTextAnimator.setDuration(5000);

       mTextAnimator.addUpdateListener(new AnimatorUpdateListener() {
           public void onAnimationUpdate(ValueAnimator animation) {
               if (DEBUG) Log.d(TAG, "onAnimationUpdate");
               float progress = ((Float) animation.getAnimatedValue()).floatValue();
               text.setAlpha(progress);
           }
       });
       if (DEBUG) Log.d(TAG, "start");
       mTextAnimator.reverse();
   }

}
