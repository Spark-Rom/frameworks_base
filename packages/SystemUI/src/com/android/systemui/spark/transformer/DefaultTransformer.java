package com.android.systemui.spark.transformer;

import android.view.View;

public class DefaultTransformer extends ABaseTransformer {
    protected void onTransform(View view, float position) {
    }

    public boolean isPagingEnabled() {
        return true;
    }
}
