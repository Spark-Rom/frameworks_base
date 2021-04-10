package com.android.systemui.statusbar;

import android.content.Context;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.text.BidiFormatter;
import android.text.format.Formatter;
import android.text.format.Formatter.BytesResult;
import android.util.AttributeSet;
import android.widget.TextView;

import com.android.settingslib.net.DataUsageController;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.NetworkController;

import android.provider.Settings;

public class DataUsageView extends TextView {

    private static boolean shouldUpdateData;
    private static boolean shouldUpdateDataTextView;
    private NetworkController mNetworkController;
    private Context mContext;
    private String formatedinfo;

    public DataUsageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mNetworkController = Dependency.get(NetworkController.class);
    }

    public static void updateUsage() {
        shouldUpdateData = true;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (shouldUpdateData) {
            shouldUpdateData = false;
            AsyncTask.execute(this::updateUsageData);
        }
        if (shouldUpdateDataTextView) {
            shouldUpdateDataTextView = false;
            setText(formatedinfo);
        }
    }

    private void updateUsageData() {
        boolean showDailyDataUsage = Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.DATA_USAGE_PERIOD, 1) == 0;
        DataUsageController mobileDataController = new DataUsageController(mContext);
        mobileDataController.setSubscriptionId(
                SubscriptionManager.getDefaultDataSubscriptionId());
        final DataUsageController.DataUsageInfo info = showDailyDataUsage ? mobileDataController.getDailyDataUsageInfo()
                : mobileDataController.getDataUsageInfo();
        formatedinfo = formatDataUsage(info.usageLevel) + " " + mContext.getResources().getString(R.string.usage_data);
        shouldUpdateDataTextView = true;
    }

    private CharSequence formatDataUsage(long byteValue) {
        final BytesResult res = Formatter.formatBytes(mContext.getResources(), byteValue,
                Formatter.FLAG_IEC_UNITS);
        return BidiFormatter.getInstance().unicodeWrap(mContext.getString(
                com.android.internal.R.string.fileSizeSuffix, res.value, res.units));
    }
}
