/*
 * Copyright (C) 2021 The Proton AOSP Project
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

package com.android.systemui.theme

import android.annotation.ColorInt
import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Context
import android.content.om.FabricatedOverlay
import android.content.res.Resources
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.UserHandle
import android.os.UserManager
import android.provider.Settings
import android.provider.Settings.Secure.MONET_ENGINE_COLOR_OVERRIDE
import android.provider.Settings.Secure.MONET_ENGINE_CHROMA_FACTOR
import android.provider.Settings.Secure.MONET_ENGINE_ACCURATE_SHADES
import android.provider.Settings.Secure.MONET_ENGINE_LINEAR_LIGHTNESS
import android.provider.Settings.Secure.MONET_ENGINE_WHITE_LUMINANCE
import android.provider.Settings.Secure.SYSTEM_CUSTOM_THEME
import android.util.Log
import android.util.TypedValue
import com.android.systemui.statusbar.policy.ConfigurationController
import com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
import com.android.systemui.broadcast.BroadcastDispatcher
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.dagger.qualifiers.Background
import com.android.systemui.dagger.qualifiers.Main
import com.android.systemui.dump.DumpManager
import com.android.systemui.flags.FeatureFlags
import com.android.systemui.keyguard.WakefulnessLifecycle
import com.android.systemui.monet.Style
import com.android.systemui.settings.UserTracker
import com.android.systemui.statusbar.policy.DeviceProvisionedController
import com.android.systemui.theme.ThemeOverlayApplier
import com.android.systemui.theme.ThemeOverlayController
import com.android.systemui.util.settings.SecureSettings

import dev.kdrag0n.colorkt.Color
import dev.kdrag0n.colorkt.cam.Zcam
import dev.kdrag0n.colorkt.conversion.ConversionGraph.convert
import dev.kdrag0n.colorkt.data.Illuminants
import dev.kdrag0n.colorkt.rgb.Srgb
import dev.kdrag0n.colorkt.tristimulus.CieXyzAbs.Companion.toAbs
import dev.kdrag0n.colorkt.ucs.lab.CieLab
import dev.kdrag0n.monet.theme.ColorSwatch
import dev.kdrag0n.monet.theme.DynamicColorScheme
import dev.kdrag0n.monet.theme.MaterialYouTargets

import java.util.concurrent.Executor

import javax.inject.Inject

import kotlin.math.log10
import kotlin.math.pow

@SysUISingleton
class SparkThemeOverlayController @Inject constructor(
    private val context: Context,
    broadcastDispatcher: BroadcastDispatcher,
    @Background bgHandler: Handler,
    @Main mainExecutor: Executor,
    @Background bgExecutor: Executor,
    themeOverlayApplier: ThemeOverlayApplier,
    private val secureSettings: SecureSettings,
    wallpaperManager: WallpaperManager,
    userManager: UserManager,
    deviceProvisionedController: DeviceProvisionedController,
    userTracker: UserTracker,
    dumpManager: DumpManager,
    featureFlags: FeatureFlags,
    @Main resources: Resources,
    wakefulnessLifecycle: WakefulnessLifecycle,
    configurationController: ConfigurationController,
) : ThemeOverlayController(
    context,
    broadcastDispatcher,
    bgHandler,
    mainExecutor,
    bgExecutor,
    themeOverlayApplier,
    secureSettings,
    wallpaperManager,
    userManager,
    deviceProvisionedController,
    userTracker,
    dumpManager,
    featureFlags,
    resources,
    wakefulnessLifecycle,
    configurationController,
) {

    private val settingsObserver = object : ContentObserver(bgHandler) {
        override fun onChange(selfChange: Boolean, uri: Uri) {
            when (uri.lastPathSegment) {
                MONET_ENGINE_ACCURATE_SHADES -> accurateShades = useAccurateShades()
                MONET_ENGINE_CHROMA_FACTOR -> chromaFactor = getChromaFactor()
                MONET_ENGINE_COLOR_OVERRIDE -> colorOverride = getColorOverride()
                MONET_ENGINE_LINEAR_LIGHTNESS -> {
                    linearLightness = useLinearLightness()
                    targets = getMaterialYouTargets()
                }
                MONET_ENGINE_WHITE_LUMINANCE -> {
                    whiteLuminance = parseWhiteLuminanceUser(secureSettings)
                    cond = getViewingConditions()
                    targets = getMaterialYouTargets()
                }
            }
            // Call super class method to reload system theme with updated
            // monet engine parameters
            reevaluateSystemTheme(true /* forceReload */)
        }
    }

    private var colorOverride: String?
    private var chromaFactor: Double
    private var accurateShades: Boolean
    private var whiteLuminance: Double
    private var linearLightness: Boolean
    private var cond: Zcam.ViewingConditions
    private var targets: MaterialYouTargets

    private var dynamicColorScheme: DynamicColorScheme? = null

    init {
        with(secureSettings) {
            registerContentObserverForUser(MONET_ENGINE_ACCURATE_SHADES,
                settingsObserver, UserHandle.USER_ALL)
            registerContentObserverForUser(MONET_ENGINE_CHROMA_FACTOR,
                settingsObserver, UserHandle.USER_ALL)
            registerContentObserverForUser(MONET_ENGINE_COLOR_OVERRIDE,
                settingsObserver, UserHandle.USER_ALL)
            registerContentObserverForUser(MONET_ENGINE_LINEAR_LIGHTNESS,
                settingsObserver, UserHandle.USER_ALL)
            registerContentObserverForUser(MONET_ENGINE_WHITE_LUMINANCE,
                settingsObserver, UserHandle.USER_ALL)
            registerContentObserverForUser(SYSTEM_CUSTOM_THEME,
                settingsObserver, UserHandle.USER_ALL)

        }

        colorOverride = getColorOverride()
        chromaFactor = getChromaFactor()
        accurateShades = useAccurateShades()
        whiteLuminance = parseWhiteLuminanceUser(secureSettings)
        linearLightness = useLinearLightness()
        cond = getViewingConditions()
        targets = getMaterialYouTargets()
    }

    private fun getViewingConditions() =
        Zcam.ViewingConditions(
            surroundFactor = Zcam.ViewingConditions.SURROUND_AVERAGE,
            // sRGB
            adaptingLuminance = 0.4 * whiteLuminance,
            // Gray world
            backgroundLuminance = CieLab(
                L = 50.0,
                a = 0.0,
                b = 0.0,
            ).toXyz().y * whiteLuminance,
            referenceWhite = Illuminants.D65.toAbs(whiteLuminance),
        )

    private fun getMaterialYouTargets() =
        MaterialYouTargets(
            chromaFactor = chromaFactor,
            useLinearLightness = linearLightness,
            cond = cond,
        )

    private fun useAccurateShades() =
        secureSettings.getIntForUser(MONET_ENGINE_ACCURATE_SHADES,
            1, UserHandle.USER_CURRENT) != 0

    private fun getChromaFactor() =
        secureSettings.getFloatForUser(MONET_ENGINE_CHROMA_FACTOR,
            1.0f, UserHandle.USER_CURRENT).toDouble()

    private fun getColorOverride(): String? =
        secureSettings.getStringForUser(MONET_ENGINE_COLOR_OVERRIDE,
            UserHandle.USER_CURRENT)

    private fun useLinearLightness() =
        secureSettings.getIntForUser(MONET_ENGINE_LINEAR_LIGHTNESS,
            0, UserHandle.USER_CURRENT) != 0

    // Seed colors
    override fun getNeutralColor(colors: WallpaperColors) = colors.primaryColor.toArgb()
    override fun getAccentColor(colors: WallpaperColors) = getNeutralColor(colors)

    override protected fun getOverlay(color: Int, type: Int, style: Style): FabricatedOverlay {
        // Generate color scheme
        val colorScheme = DynamicColorScheme(
            targets = targets,
            seedColor = colorOverride?.takeIf { it.isNotEmpty() }
                ?.let { Srgb(it) } ?: Srgb(color),
            chromaFactor = chromaFactor,
            cond = cond,
            accurateShades = accurateShades,
        )

        dynamicColorScheme = colorScheme

        val (groupKey, colorsList) = when (type) {
            ACCENT -> "accent" to colorScheme.accentColors
            NEUTRAL -> "neutral" to colorScheme.neutralColors
            else -> error("Unknown type $type")
        }

        return FabricatedOverlay.Builder(context.packageName, groupKey, "android").run {
            colorsList.forEachIndexed { index, swatch ->
                val group = "$groupKey${index + 1}"

                swatch.forEach { (shade, color) ->
                    setColor("system_${group}_$shade", color)
                }
            }

            // Override special modulated surface colors for performance and consistency
            if (type == NEUTRAL) {
                // surface light = neutral1 20 (L* 98)
                colorsList[0][20]?.let { setColor("surface_light", it) }

                // surface highlight dark = neutral1 650 (L* 35)
                colorsList[0][650]?.let { setColor("surface_highlight_dark", it) }

                // surface_header_dark_sysui = neutral1 950 (L* 5)
                colorsList[0][950]?.let { setColor("surface_header_dark_sysui", it) }
            }

            build()
        }
    }

    override protected fun getAccent1(): List<Int> {
        return getArgbColors(dynamicColorScheme?.accentColors?.get(0))
    }

    override protected fun getAccent2(): List<Int> {
        return getArgbColors(dynamicColorScheme?.accentColors?.get(1))
    }

    override protected fun getAccent3(): List<Int> {
        return getArgbColors(dynamicColorScheme?.accentColors?.get(2))
    }

    override protected fun getNeutral1(): List<Int> {
        return getArgbColors(dynamicColorScheme?.neutralColors?.get(0))
    }

    override protected fun getNeutral2(): List<Int> {
        return getArgbColors(dynamicColorScheme?.neutralColors?.get(1))
    }

    private fun getArgbColors(swatch: ColorSwatch?): List<Int> {
        return swatch?.values?.map { it.toArgb() } ?: emptyList()
    }

    companion object {

        private const val WHITE_LUMINANCE_MIN = 1.0
        private const val WHITE_LUMINANCE_MAX = 10000.0
        private const val WHITE_LUMINANCE_USER_MAX = 1000
        private const val WHITE_LUMINANCE_USER_DEFAULT = 425 // ~200.0 divisible by step (decoded = 199.526)

        private fun parseWhiteLuminanceUser(secureSettings: SecureSettings): Double {
            val userValue = secureSettings.getIntForUser(MONET_ENGINE_WHITE_LUMINANCE,
                WHITE_LUMINANCE_USER_DEFAULT, UserHandle.USER_CURRENT)
            val userSrc = userValue.toDouble() / WHITE_LUMINANCE_USER_MAX
            val userInv = 1.0 - userSrc
            return (10.0).pow(userInv * log10(WHITE_LUMINANCE_MAX))
                    .coerceAtLeast(WHITE_LUMINANCE_MIN)
        }

        private fun Color.toArgb(): Int {
            return convert<Srgb>().toRgb8() or (0xff shl 24)
        }

        private fun FabricatedOverlay.Builder.setColor(name: String, color: Color): FabricatedOverlay.Builder {
            return setResourceValue(
                "android:color/$name",
                TypedValue.TYPE_INT_COLOR_ARGB8,
                color.toArgb()
            )
        }
    }
}
