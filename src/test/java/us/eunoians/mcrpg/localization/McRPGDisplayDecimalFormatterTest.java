package us.eunoians.mcrpg.localization;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.LinkedNode;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class McRPGDisplayDecimalFormatterTest {

    private final McRPGDisplayDecimalFormatter formatter = new McRPGDisplayDecimalFormatter();

    @Nested
    @DisplayName("Input validation")
    class InputValidation {

        @DisplayName("negative min fraction digits throws IllegalArgumentException")
        @Test
        void formatDisplayDecimal_negativeMinFractionDigits_throws() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> formatter.formatDisplayDecimal(Locale.ENGLISH, 1.0, -1, 2));
        }

        @DisplayName("negative max fraction digits throws IllegalArgumentException")
        @Test
        void formatDisplayDecimal_negativeMaxFractionDigits_throws() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> formatter.formatDisplayDecimal(Locale.ENGLISH, 1.0, 0, -1));
        }

        @DisplayName("min exceeds max throws IllegalArgumentException")
        @Test
        void formatDisplayDecimal_minExceedsMax_throws() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> formatter.formatDisplayDecimal(Locale.ENGLISH, 1.0, 3, 2));
        }

        @DisplayName("min equals max is valid")
        @Test
        void formatDisplayDecimal_minEqualsMax_doesNotThrow() {
            assertDoesNotThrow(() -> formatter.formatDisplayDecimal(Locale.US, 1.0, 2, 2));
        }

        @DisplayName("zero min and zero max is valid")
        @Test
        void formatDisplayDecimal_zeroMinZeroMax_doesNotThrow() {
            String result = formatter.formatDisplayDecimal(Locale.US, 3.14, 0, 0);
            assertEquals("3", result);
        }
    }

    @Nested
    @DisplayName("US locale formatting")
    class UsLocale {

        @DisplayName("default bounds format Pi with decimal point")
        @Test
        void formatDisplayDecimal_pi_usesDecimalPoint() {
            String result = formatter.formatDisplayDecimal(Locale.US, 3.14159);

            assertTrue(result.contains("."));
        }

        @DisplayName("default bounds format 1.5 as '1.5'")
        @Test
        void formatDisplayDecimal_onePointFive_formats() {
            String result = formatter.formatDisplayDecimal(Locale.US, 1.5);

            assertEquals("1.5", result);
        }

        @DisplayName("default bounds format 0.0 as '0.0'")
        @Test
        void formatDisplayDecimal_zero_showsMinDigit() {
            String result = formatter.formatDisplayDecimal(Locale.US, 0.0);

            assertEquals("0.0", result);
        }

        @DisplayName("default bounds format integer value shows at least 1 decimal place")
        @Test
        void formatDisplayDecimal_wholeNumber_showsMinDecimal() {
            String result = formatter.formatDisplayDecimal(Locale.US, 42.0);

            assertEquals("42.0", result);
        }

        @DisplayName("default bounds truncate beyond 2 decimal places")
        @Test
        void formatDisplayDecimal_manyDecimals_truncatesAtTwo() {
            String result = formatter.formatDisplayDecimal(Locale.US, 3.14159);

            assertEquals("3.14", result);
        }

        @DisplayName("custom bounds with 4 max fraction digits")
        @Test
        void formatDisplayDecimal_customBounds_fourDecimals() {
            String result = formatter.formatDisplayDecimal(Locale.US, 3.14159, 1, 4);

            assertEquals("3.1416", result);
        }

        @DisplayName("large number uses grouping separator")
        @Test
        void formatDisplayDecimal_largeNumber_usesGrouping() {
            String result = formatter.formatDisplayDecimal(Locale.US, 1234567.89);

            assertTrue(result.contains(","));
        }

        @DisplayName("negative value formats correctly")
        @Test
        void formatDisplayDecimal_negativeValue_formatsCorrectly() {
            String result = formatter.formatDisplayDecimal(Locale.US, -42.5);

            assertEquals("-42.5", result);
        }
    }

    @Nested
    @DisplayName("German locale formatting")
    class GermanLocale {

        @DisplayName("German locale uses comma as decimal separator")
        @Test
        void formatDisplayDecimal_germanLocale_usesComma() {
            String result = formatter.formatDisplayDecimal(Locale.GERMANY, 3.14);

            assertTrue(result.contains(","), "Expected comma as decimal separator in: " + result);
            assertNotNull(result);
        }

        @DisplayName("German locale uses period as grouping separator")
        @Test
        void formatDisplayDecimal_germanLocale_usesGroupingDot() {
            String result = formatter.formatDisplayDecimal(Locale.GERMANY, 1234567.89);

            assertTrue(result.contains("."), "Expected period as grouping separator in: " + result);
        }
    }

    @Nested
    @DisplayName("French locale formatting")
    class FrenchLocale {

        @DisplayName("French locale uses comma as decimal separator")
        @Test
        void formatDisplayDecimal_frenchLocale_usesComma() {
            String result = formatter.formatDisplayDecimal(Locale.FRANCE, 3.14);

            assertTrue(result.contains(","), "Expected comma as decimal separator in: " + result);
        }
    }

    @Nested
    @DisplayName("Float overloads")
    class FloatOverloads {

        @DisplayName("float overload with default bounds formats correctly")
        @Test
        void formatDisplayDecimal_float_defaultBounds() {
            String result = formatter.formatDisplayDecimal(Locale.US, 2.5f);

            assertEquals("2.5", result);
        }

        @DisplayName("float overload with custom bounds formats correctly")
        @Test
        void formatDisplayDecimal_float_customBounds() {
            String result = formatter.formatDisplayDecimal(Locale.US, 2.567f, 1, 2);

            assertEquals("2.57", result);
        }

        @DisplayName("float overload validation throws for negative min")
        @Test
        void formatDisplayDecimal_float_negativeMin_throws() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> formatter.formatDisplayDecimal(Locale.US, 1.0f, -1, 2));
        }
    }

    @Nested
    @DisplayName("Cache behavior")
    class CacheBehavior {

        @DisplayName("same locale reuses cached NumberFormat instance")
        @Test
        void formatDisplayDecimal_sameLocale_reusesCache() {
            String first = formatter.formatDisplayDecimal(Locale.US, 1.23);
            String second = formatter.formatDisplayDecimal(Locale.US, 1.23);

            assertEquals(first, second);
        }

        @DisplayName("different locales produce different formatting")
        @Test
        void formatDisplayDecimal_differentLocales_differentFormatting() {
            String usResult = formatter.formatDisplayDecimal(Locale.US, 1234.56);
            String germanResult = formatter.formatDisplayDecimal(Locale.GERMANY, 1234.56);

            // US uses period for decimal, Germany uses comma
            assertTrue(usResult.contains("."));
            assertTrue(germanResult.contains(","));
        }

        @DisplayName("different digit bounds on same locale do not corrupt results")
        @Test
        void formatDisplayDecimal_varyingDigitBounds_correctResults() {
            String result0 = formatter.formatDisplayDecimal(Locale.US, 3.14159, 0, 0);
            String result2 = formatter.formatDisplayDecimal(Locale.US, 3.14159, 2, 2);
            String result4 = formatter.formatDisplayDecimal(Locale.US, 3.14159, 4, 4);

            assertEquals("3", result0);
            assertEquals("3.14", result2);
            assertEquals("3.1416", result4);
        }
    }

    @Nested
    @DisplayName("Concurrency")
    class Concurrency {

        @DisplayName("concurrent formatting from multiple threads does not corrupt results")
        @Test
        void formatDisplayDecimal_concurrentAccess_noCorruption() throws InterruptedException {
            int threadCount = 8;
            int iterationsPerThread = 100;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicBoolean failure = new AtomicBoolean(false);

            for (int t = 0; t < threadCount; t++) {
                final int threadId = t;
                executor.submit(() -> {
                    try {
                        for (int i = 0; i < iterationsPerThread; i++) {
                            Locale locale = (threadId % 2 == 0) ? Locale.US : Locale.GERMANY;
                            double value = 1234.56;
                            String result = formatter.formatDisplayDecimal(locale, value);

                            if (locale == Locale.US && !result.contains(".")) {
                                failure.set(true);
                            }
                            if (locale == Locale.GERMANY && !result.contains(",")) {
                                failure.set(true);
                            }
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            assertFalse(failure.get(), "Concurrent formatting produced corrupted results");
        }
    }

    @Nested
    @DisplayName("Player/Audience overloads without manager")
    class NoManagerOverloads {

        @DisplayName("no-arg constructor formatter throws NullPointerException on McRPGPlayer overload")
        @Test
        void formatDisplayDecimal_playerOverload_throwsWithoutManager() {
            assertThrows(
                    NullPointerException.class,
                    () -> formatter.formatDisplayDecimal((McRPGPlayer) null, 1.0));
        }

        @DisplayName("no-arg constructor formatter throws NullPointerException on Audience double overload")
        @Test
        void formatDisplayDecimal_audienceDoubleOverload_throwsWithoutManager() {
            Audience audience = mock(Audience.class);
            assertThrows(
                    NullPointerException.class,
                    () -> formatter.formatDisplayDecimal(audience, 1.0));
        }

        @DisplayName("no-arg constructor formatter throws NullPointerException on Audience float overload")
        @Test
        void formatDisplayDecimal_audienceFloatOverload_throwsWithoutManager() {
            Audience audience = mock(Audience.class);
            assertThrows(
                    NullPointerException.class,
                    () -> formatter.formatDisplayDecimal(audience, 1.0f));
        }

        @DisplayName("no-arg constructor formatter throws NullPointerException on Audience custom digits overload")
        @Test
        void formatDisplayDecimal_audienceCustomDigitsOverload_throwsWithoutManager() {
            Audience audience = mock(Audience.class);
            assertThrows(
                    NullPointerException.class,
                    () -> formatter.formatDisplayDecimal(audience, 1.0, 1, 2));
        }

        @DisplayName("no-arg constructor formatter throws NullPointerException on Audience float custom digits overload")
        @Test
        void formatDisplayDecimal_audienceFloatCustomDigitsOverload_throwsWithoutManager() {
            Audience audience = mock(Audience.class);
            assertThrows(
                    NullPointerException.class,
                    () -> formatter.formatDisplayDecimal(audience, 1.0f, 1, 2));
        }

        @DisplayName("no-arg constructor formatter throws NullPointerException on McRPGPlayer custom digits overload")
        @Test
        void formatDisplayDecimal_playerCustomDigitsOverload_throwsWithoutManager() {
            McRPGPlayer player = mock(McRPGPlayer.class);
            assertThrows(
                    NullPointerException.class,
                    () -> formatter.formatDisplayDecimal(player, 1.0, 1, 2));
        }

        @DisplayName("no-arg constructor formatter throws NullPointerException on McRPGPlayer float overload")
        @Test
        void formatDisplayDecimal_playerFloatOverload_throwsWithoutManager() {
            McRPGPlayer player = mock(McRPGPlayer.class);
            assertThrows(
                    NullPointerException.class,
                    () -> formatter.formatDisplayDecimal(player, 1.0f));
        }

        @DisplayName("no-arg constructor formatter throws NullPointerException on McRPGPlayer float custom digits overload")
        @Test
        void formatDisplayDecimal_playerFloatCustomDigitsOverload_throwsWithoutManager() {
            McRPGPlayer player = mock(McRPGPlayer.class);
            assertThrows(
                    NullPointerException.class,
                    () -> formatter.formatDisplayDecimal(player, 1.0f, 1, 2));
        }
    }

    @Nested
    @DisplayName("McRPGPlayer overloads with manager")
    class PlayerOverloadsWithManager {

        private McRPGLocalizationManager mockManager;
        private McRPGDisplayDecimalFormatter managedFormatter;
        private McRPGPlayer mockPlayer;

        @Test
        @DisplayName("player double overload uses locale chain head")
        void formatDisplayDecimal_playerDouble_usesLocaleChainHead() {
            setupManagedFormatter(Locale.GERMANY);

            String result = managedFormatter.formatDisplayDecimal(mockPlayer, 1234.56);

            assertTrue(result.contains(","), "Expected German comma as decimal separator in: " + result);
        }

        @Test
        @DisplayName("player double overload with custom digit bounds")
        void formatDisplayDecimal_playerDouble_customDigitBounds() {
            setupManagedFormatter(Locale.US);

            String result = managedFormatter.formatDisplayDecimal(mockPlayer, 3.14159, 0, 4);

            assertEquals("3.1416", result);
        }

        @Test
        @DisplayName("player float overload uses locale chain head")
        void formatDisplayDecimal_playerFloat_usesLocaleChainHead() {
            setupManagedFormatter(Locale.US);

            String result = managedFormatter.formatDisplayDecimal(mockPlayer, 2.5f);

            assertEquals("2.5", result);
        }

        @Test
        @DisplayName("player float overload with custom digit bounds")
        void formatDisplayDecimal_playerFloat_customDigitBounds() {
            setupManagedFormatter(Locale.US);

            String result = managedFormatter.formatDisplayDecimal(mockPlayer, 2.567f, 1, 2);

            assertEquals("2.57", result);
        }

        @Test
        @DisplayName("player double default bounds formats whole number")
        void formatDisplayDecimal_playerDouble_wholeNumber() {
            setupManagedFormatter(Locale.US);

            String result = managedFormatter.formatDisplayDecimal(mockPlayer, 42.0);

            assertEquals("42.0", result);
        }

        @SuppressWarnings("unchecked")
        private void setupManagedFormatter(Locale playerLocale) {
            mockManager = mock(McRPGLocalizationManager.class);
            managedFormatter = new McRPGDisplayDecimalFormatter(mockManager);
            mockPlayer = mock(McRPGPlayer.class);
            LinkedNode<Locale> chain = new LinkedNode<>(playerLocale);
            when(mockManager.getLocaleChain(mockPlayer)).thenReturn(chain);
        }
    }

    @Nested
    @DisplayName("Audience overloads with manager")
    class AudienceOverloadsWithManager {

        @Test
        @DisplayName("non-Player audience uses server default locale")
        void formatDisplayDecimal_nonPlayerAudience_usesServerDefault() {
            McRPGLocalizationManager mockManager = mock(McRPGLocalizationManager.class);
            when(mockManager.getServerDefaultLocale()).thenReturn(Locale.US);
            McRPGDisplayDecimalFormatter managedFormatter = new McRPGDisplayDecimalFormatter(mockManager);

            Audience audience = mock(Audience.class);
            String result = managedFormatter.formatDisplayDecimal(audience, 1234.56);

            assertTrue(result.contains(","), "Expected US comma as grouping separator in: " + result);
            assertTrue(result.contains("."), "Expected US period as decimal separator in: " + result);
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Player audience with loaded McRPGPlayer uses player locale chain")
        void formatDisplayDecimal_playerAudience_loaded_usesPlayerLocale() {
            McRPGLocalizationManager mockManager = mock(McRPGLocalizationManager.class);
            McRPGDisplayDecimalFormatter managedFormatter = new McRPGDisplayDecimalFormatter(mockManager);

            Player mockBukkitPlayer = mock(Player.class);
            UUID playerUuid = UUID.randomUUID();
            when(mockBukkitPlayer.getUniqueId()).thenReturn(playerUuid);

            McRPGPlayer mockMcRPGPlayer = mock(McRPGPlayer.class);
            LinkedNode<Locale> chain = new LinkedNode<>(Locale.GERMANY);
            when(mockManager.getLocaleChain(mockMcRPGPlayer)).thenReturn(chain);
            when(mockManager.getServerDefaultLocale()).thenReturn(Locale.US);

            McRPGPlayerManager mockPlayerManager = mock(McRPGPlayerManager.class);
            when(mockPlayerManager.getPlayer(playerUuid)).thenReturn(Optional.of(mockMcRPGPlayer));

            RegistryAccess mockRegistryAccess = mock(RegistryAccess.class);
            com.diamonddagger590.mccore.registry.manager.ManagerRegistry mockManagerRegistry = mock(com.diamonddagger590.mccore.registry.manager.ManagerRegistry.class);
            when(mockRegistryAccess.registry(RegistryKey.MANAGER)).thenReturn(mockManagerRegistry);
            when(mockManagerRegistry.<McRPGPlayerManager>manager(McRPGManagerKey.PLAYER)).thenReturn(mockPlayerManager);
            when(mockManager.plugin()).thenReturn(mock(us.eunoians.mcrpg.McRPG.class));
            when(mockManager.plugin().registryAccess()).thenReturn(mockRegistryAccess);

            String result = managedFormatter.formatDisplayDecimal((Audience) mockBukkitPlayer, 3.14);

            assertTrue(result.contains(","), "Expected German comma as decimal separator in: " + result);
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Player audience with unloaded McRPGPlayer uses server default")
        void formatDisplayDecimal_playerAudience_unloaded_usesServerDefault() {
            McRPGLocalizationManager mockManager = mock(McRPGLocalizationManager.class);
            McRPGDisplayDecimalFormatter managedFormatter = new McRPGDisplayDecimalFormatter(mockManager);

            Player mockBukkitPlayer = mock(Player.class);
            UUID playerUuid = UUID.randomUUID();
            when(mockBukkitPlayer.getUniqueId()).thenReturn(playerUuid);

            when(mockManager.getServerDefaultLocale()).thenReturn(Locale.US);

            McRPGPlayerManager mockPlayerManager = mock(McRPGPlayerManager.class);
            when(mockPlayerManager.getPlayer(playerUuid)).thenReturn(Optional.empty());

            RegistryAccess mockRegistryAccess = mock(RegistryAccess.class);
            com.diamonddagger590.mccore.registry.manager.ManagerRegistry mockManagerRegistry = mock(com.diamonddagger590.mccore.registry.manager.ManagerRegistry.class);
            when(mockRegistryAccess.registry(RegistryKey.MANAGER)).thenReturn(mockManagerRegistry);
            when(mockManagerRegistry.<McRPGPlayerManager>manager(McRPGManagerKey.PLAYER)).thenReturn(mockPlayerManager);
            when(mockManager.plugin()).thenReturn(mock(us.eunoians.mcrpg.McRPG.class));
            when(mockManager.plugin().registryAccess()).thenReturn(mockRegistryAccess);

            String result = managedFormatter.formatDisplayDecimal((Audience) mockBukkitPlayer, 3.14);

            assertTrue(result.contains("."), "Expected US period as decimal separator in: " + result);
        }

        @Test
        @DisplayName("non-Player Audience float overload uses server default")
        void formatDisplayDecimal_nonPlayerAudienceFloat_usesServerDefault() {
            McRPGLocalizationManager mockManager = mock(McRPGLocalizationManager.class);
            when(mockManager.getServerDefaultLocale()).thenReturn(Locale.US);
            McRPGDisplayDecimalFormatter managedFormatter = new McRPGDisplayDecimalFormatter(mockManager);

            Audience audience = mock(Audience.class);
            String result = managedFormatter.formatDisplayDecimal(audience, 2.5f);

            assertEquals("2.5", result);
        }

        @Test
        @DisplayName("non-Player Audience with custom digit bounds uses server default")
        void formatDisplayDecimal_nonPlayerAudienceCustomDigits_usesServerDefault() {
            McRPGLocalizationManager mockManager = mock(McRPGLocalizationManager.class);
            when(mockManager.getServerDefaultLocale()).thenReturn(Locale.US);
            McRPGDisplayDecimalFormatter managedFormatter = new McRPGDisplayDecimalFormatter(mockManager);

            Audience audience = mock(Audience.class);
            String result = managedFormatter.formatDisplayDecimal(audience, 3.14159, 0, 4);

            assertEquals("3.1416", result);
        }

        @Test
        @DisplayName("non-Player Audience float with custom digit bounds uses server default")
        void formatDisplayDecimal_nonPlayerAudienceFloatCustomDigits_usesServerDefault() {
            McRPGLocalizationManager mockManager = mock(McRPGLocalizationManager.class);
            when(mockManager.getServerDefaultLocale()).thenReturn(Locale.US);
            McRPGDisplayDecimalFormatter managedFormatter = new McRPGDisplayDecimalFormatter(mockManager);

            Audience audience = mock(Audience.class);
            String result = managedFormatter.formatDisplayDecimal(audience, 2.567f, 1, 2);

            assertEquals("2.57", result);
        }
    }
}
