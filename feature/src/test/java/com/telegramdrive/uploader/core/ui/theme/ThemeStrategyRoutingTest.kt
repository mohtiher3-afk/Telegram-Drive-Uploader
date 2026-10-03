package com.telegramdrive.uploader.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Guards the contract that makes the Glow Color settings feature work.
 *
 * The user picks one of seven presets (or a custom hex) in SettingsScreen and the
 * app is expected to visibly change colour. Only DynamicColorStrategy.StaticBrand
 * routes that choice to `primary`; BrandAccented deliberately leaves `primary` to
 * the wallpaper-derived scheme and only touches secondary/tertiary/surface roles.
 * A regression that moved the default back to BrandAccented would make the whole
 * settings UI silently inert, which is invisible in a build but obvious to a user.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33])
class ThemeStrategyRoutingTest {

    @get:Rule
    val composeRule = createComposeRule()

    private companion object {
        const val CUSTOM_HEX = "1A73E8"
    }

    /**
     * Renders the theme and captures `MaterialTheme.colorScheme.primary` from *inside*
     * the theme's content lambda. Reading it as a sibling of the TelegramDriveTheme call
     * would fall back to the ambient default scheme and assert nothing about the theme.
     */
    private fun primaryUnder(
        strategy: DynamicColorStrategy,
        preset: GlowColorPreset = GlowColorPreset.COBALT,
        darkTheme: Boolean = true
    ): Color {
        var observed = Color.Unspecified
        composeRule.setContent {
            TelegramDriveTheme(
                darkTheme = darkTheme,
                dynamicColorStrategy = strategy,
                glowColorPreset = preset,
                customGlowHex = CUSTOM_HEX
            ) {
                observed = MaterialTheme.colorScheme.primary
            }
        }
        composeRule.waitForIdle()
        return observed
    }

    /**
     * Same, but omits the strategy argument to exercise the declared default.
     * darkTheme is pinned so the assertion compares like with like: swatchColor()
     * returns the dark variant, and Robolectric's default is light mode.
     *
     * `darkTheme` no longer selects a scheme — the app is dark-only — so the
     * argument is accepted and ignored.
     */
    private fun primaryWithDefaultStrategy(
        preset: GlowColorPreset = GlowColorPreset.COBALT
    ): Color {
        var observed = Color.Unspecified
        composeRule.setContent {
            TelegramDriveTheme(
                darkTheme = true,
                glowColorPreset = preset,
                customGlowHex = CUSTOM_HEX
            ) {
                observed = MaterialTheme.colorScheme.primary
            }
        }
        composeRule.waitForIdle()
        return observed
    }

    @Test
    fun `static brand routes the selected preset to primary`() {
        assertEquals(GlowColorPreset.COBALT.swatchColor(), primaryUnder(DynamicColorStrategy.StaticBrand))
    }

    @Test
    fun `static brand honours the custom hex through primary`() {
        // The theme is dark-only, so the expected value comes from the dark path and
        // the darkTheme argument below is deliberately false: it is accepted and
        // ignored, and the routing contract is what is under test here, not the
        // literal hex. A preset may darken a colour to reach readable contrast.
        val expected = GlowColorPreset.CUSTOM
            .applyTo(DarkColorScheme, darkTheme = true, customHex = CUSTOM_HEX)
            .primary
        assertEquals(
            expected,
            primaryUnder(DynamicColorStrategy.StaticBrand, preset = GlowColorPreset.CUSTOM, darkTheme = false)
        )
    }

    @Test
    fun `the darkTheme argument no longer selects a scheme`() {
        // Regression guard for the dark-only decision. Before it, darkTheme = false
        // produced the light scheme; now both arguments resolve to the same primary.
        //
        // Both themes are rendered inside one composition: createComposeRule permits a
        // single setContent per test, so calling primaryUnder twice would not produce
        // two observations to compare.
        var fromDarkArgument = Color.Unspecified
        var fromLightArgument = Color.Unspecified
        composeRule.setContent {
            TelegramDriveTheme(
                darkTheme = true,
                dynamicColorStrategy = DynamicColorStrategy.StaticBrand,
                glowColorPreset = GlowColorPreset.COBALT,
                customGlowHex = CUSTOM_HEX
            ) {
                fromDarkArgument = MaterialTheme.colorScheme.primary
            }
            TelegramDriveTheme(
                darkTheme = false,
                dynamicColorStrategy = DynamicColorStrategy.StaticBrand,
                glowColorPreset = GlowColorPreset.COBALT,
                customGlowHex = CUSTOM_HEX
            ) {
                fromLightArgument = MaterialTheme.colorScheme.primary
            }
        }
        composeRule.waitForIdle()
        assertEquals(
            "darkTheme = true and darkTheme = false must resolve to the same primary",
            fromDarkArgument,
            fromLightArgument
        )
    }

    @Test
    fun `brand accented leaves primary to the base scheme so the setting is inert`() {
        // Documents *why* StaticBrand is the default: under BrandAccented the
        // chosen preset does not reach primary, so selecting a colour changes nothing.
        assertNotEquals(
            GlowColorPreset.COBALT.swatchColor(),
            primaryUnder(DynamicColorStrategy.BrandAccented)
        )
    }

    @Test
    fun `omitting the strategy argument still honours the glow setting`() {
        // A caller that forgets the argument must not silently disable the feature.
        assertEquals(GlowColorPreset.COBALT.swatchColor(), primaryWithDefaultStrategy())
    }
}
