package com.spark.android.systemui.dagger;

import com.android.systemui.dagger.DefaultComponentBinder;
import com.android.systemui.dagger.DependencyProvider;
import com.android.systemui.dagger.SysUISingleton;
import com.android.systemui.dagger.SystemUIBinder;
import com.android.systemui.dagger.SysUIComponent;
import com.android.systemui.dagger.SystemUIModule;

import com.spark.android.systemui.keyguard.KeyguardSliceProviderSpark;
import com.spark.android.systemui.smartspace.KeyguardSmartspaceController;
import com.spark.android.systemui.columbus.ColumbusModule;
import com.spark.android.systemui.elmyra.ElmyraModule;

import dagger.Subcomponent;

@SysUISingleton
@Subcomponent(modules = {
        ColumbusModule.class,
        DefaultComponentBinder.class,
        DependencyProvider.class,
        SystemUISparkBinder.class,
        ElmyraModule.class,
        SystemUIModule.class,
        SystemUISparkModule.class})
public interface SysUIComponentSpark extends SysUIComponent {
    @SysUISingleton
    @Subcomponent.Builder
    interface Builder extends SysUIComponent.Builder {
        SysUIComponentSpark build();
    }

    /**
     * Member injection into the supplied argument.
     */
    void inject(KeyguardSliceProviderSpark keyguardSliceProviderSpark);

    @SysUISingleton
    KeyguardSmartspaceController createKeyguardSmartspaceController();
}
