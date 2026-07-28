package us.eunoians.mcrpg.localization;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.util.LinkedNode;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.setting.impl.LocalePlayerSetting;
import us.eunoians.mcrpg.setting.impl.LocaleSetting;
import us.eunoians.mcrpg.setting.impl.SpecificLocaleSetting;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link McRPGLocalizationManager#getLocaleChain(McRPGPlayer)},
 * {@link McRPGLocalizationManager#formatDisplayDate(McRPGPlayer, Instant)},
 * {@link McRPGLocalizationManager#getRegisteredLocales()}, and
 * {@link McRPGLocalizationManager#addPaletteEntry(Map, String, String)}.
 */
@DisplayName("McRPGLocalizationManager locale chain and formatting")
class McRPGLocalizationManagerLocaleChainTest {

    private static final Locale SERVER_DEFAULT = Locale.of("de", "DE");
    private static final Locale CLIENT_LOCALE = Locale.of("fr", "FR");

    private McRPGLocalizationManager manager;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() throws Exception {
        manager = mock(McRPGLocalizationManager.class, CALLS_REAL_METHODS);

        LinkedNode<Locale> serverChain = new LinkedNode<>(SERVER_DEFAULT);
        serverChain.setNext(new LinkedNode<>(Locale.ENGLISH));
        ReloadableContent<LinkedNode<Locale>> localeChainContent = mock(ReloadableContent.class);
        when(localeChainContent.getContent()).thenReturn(serverChain);
        Field localeChainField = findField("localeChain");
        localeChainField.set(manager, localeChainContent);

        ReloadableContent<Map<String, String>> paletteContent = mock(ReloadableContent.class);
        when(paletteContent.getContent()).thenReturn(new LinkedHashMap<>());
        Field paletteField = McRPGLocalizationManager.class.getDeclaredField("paletteReplacements");
        paletteField.setAccessible(true);
        paletteField.set(manager, paletteContent);

        Map<Locale, Object> localizations = new LinkedHashMap<>();
        localizations.put(Locale.ENGLISH, new Object());
        localizations.put(SERVER_DEFAULT, new Object());
        localizations.put(CLIENT_LOCALE, new Object());
        Field localizationsField = findField("localizations");
        localizationsField.set(manager, localizations);
    }

    /**
     * Finds a field by name walking up the class hierarchy.
     */
    private Field findField(String name) throws NoSuchFieldException {
        Class<?> cls = McRPGLocalizationManager.class;
        while (cls != null) {
            try {
                Field field = cls.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                cls = cls.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private McRPGPlayer mockPlayerWithSetting(PlayerSetting setting) {
        McRPGPlayer player = mock(McRPGPlayer.class);
        doReturn(Optional.of(setting)).when(player).getPlayerSetting(eq(LocalePlayerSetting.SETTING_KEY));
        return player;
    }

    private McRPGPlayer mockPlayerWithBukkitLocale(Locale locale) {
        McRPGPlayer player = mock(McRPGPlayer.class);
        doReturn(Optional.empty()).when(player).getPlayerSetting(eq(LocalePlayerSetting.SETTING_KEY));
        Player bukkitPlayer = mock(Player.class);
        when(bukkitPlayer.locale()).thenReturn(locale);
        when(player.getAsBukkitPlayer()).thenReturn(Optional.of(bukkitPlayer));
        return player;
    }

    @Nested
    @DisplayName("getLocaleChain")
    class GetLocaleChain {

        @Test
        @DisplayName("Given CLIENT_LOCALE setting, when getLocaleChain is called, then chain starts with client locale followed by server default")
        void getLocaleChain_clientLocale_startsWithClientLocale() {
            McRPGPlayer player = mockPlayerWithSetting(LocaleSetting.CLIENT_LOCALE);
            Player bukkitPlayer = mock(Player.class);
            when(bukkitPlayer.locale()).thenReturn(CLIENT_LOCALE);
            when(player.getAsBukkitPlayer()).thenReturn(Optional.of(bukkitPlayer));

            LinkedNode<Locale> chain = manager.getLocaleChain(player);

            assertNotNull(chain);
            assertEquals(CLIENT_LOCALE, chain.getNodeValue());
            assertNotNull(chain.getNextNode());
            assertEquals(SERVER_DEFAULT, chain.getNextNode().getNodeValue());
        }

        @Test
        @DisplayName("Given SERVER_LOCALE setting with online player, when getLocaleChain is called, then chain is server default -> client -> english")
        void getLocaleChain_serverLocale_withOnlinePlayer() {
            McRPGPlayer player = mockPlayerWithSetting(LocaleSetting.SERVER_LOCALE);
            Player bukkitPlayer = mock(Player.class);
            when(bukkitPlayer.locale()).thenReturn(CLIENT_LOCALE);
            when(player.getAsBukkitPlayer()).thenReturn(Optional.of(bukkitPlayer));

            LinkedNode<Locale> chain = manager.getLocaleChain(player);

            assertEquals(SERVER_DEFAULT, chain.getNodeValue());
            assertNotNull(chain.getNextNode());
            assertEquals(CLIENT_LOCALE, chain.getNextNode().getNodeValue());
            assertNotNull(chain.getNextNode().getNextNode());
            assertEquals(Locale.ENGLISH, chain.getNextNode().getNextNode().getNodeValue());
        }

        @Test
        @DisplayName("Given SERVER_LOCALE setting with offline player, when getLocaleChain is called, then chain is server default -> english")
        void getLocaleChain_serverLocale_withOfflinePlayer() {
            McRPGPlayer player = mockPlayerWithSetting(LocaleSetting.SERVER_LOCALE);
            when(player.getAsBukkitPlayer()).thenReturn(Optional.empty());

            LinkedNode<Locale> chain = manager.getLocaleChain(player);

            assertEquals(SERVER_DEFAULT, chain.getNodeValue());
            assertNotNull(chain.getNextNode());
            assertEquals(Locale.ENGLISH, chain.getNextNode().getNodeValue());
        }

        @Test
        @DisplayName("Given SpecificLocaleSetting, when getLocaleChain is called, then specific locale is prepended to super chain")
        void getLocaleChain_specificLocale_prependsToSuperChain() {
            Locale specificLocale = Locale.of("lt");
            SpecificLocaleSetting specificSetting = mock(SpecificLocaleSetting.class);
            when(specificSetting.getLocale()).thenReturn(specificLocale);

            McRPGPlayer player = mockPlayerWithSetting(specificSetting);
            Player bukkitPlayer = mock(Player.class);
            when(bukkitPlayer.locale()).thenReturn(CLIENT_LOCALE);
            when(player.getAsBukkitPlayer()).thenReturn(Optional.of(bukkitPlayer));

            LinkedNode<Locale> chain = manager.getLocaleChain(player);

            assertEquals(specificLocale, chain.getNodeValue());
        }

        @Test
        @DisplayName("Given no locale setting on the player, when getLocaleChain is called, then falls back to client locale followed by server default")
        void getLocaleChain_noSetting_fallsBackToClientLocaleChain() {
            McRPGPlayer player = mock(McRPGPlayer.class);
            doReturn(Optional.empty()).when(player).getPlayerSetting(eq(LocalePlayerSetting.SETTING_KEY));
            Player bukkitPlayer = mock(Player.class);
            when(bukkitPlayer.locale()).thenReturn(CLIENT_LOCALE);
            when(player.getAsBukkitPlayer()).thenReturn(Optional.of(bukkitPlayer));

            LinkedNode<Locale> chain = manager.getLocaleChain(player);

            assertNotNull(chain);
            assertEquals(CLIENT_LOCALE, chain.getNodeValue());
            assertNotNull(chain.getNextNode());
            assertEquals(SERVER_DEFAULT, chain.getNextNode().getNodeValue());
        }
    }

    @Nested
    @DisplayName("formatDisplayDate")
    class FormatDisplayDate {

        @Test
        @DisplayName("Given a US-locale player, when formatDisplayDate is called, then returns english medium-style date")
        void formatDisplayDate_usLocale_returnsMediumDate() {
            McRPGPlayer player = mockPlayerWithSetting(LocaleSetting.SERVER_LOCALE);
            when(player.getAsBukkitPlayer()).thenReturn(Optional.empty());

            LinkedNode<Locale> deChain = new LinkedNode<>(Locale.US);
            deChain.setNext(new LinkedNode<>(Locale.ENGLISH));
            doReturn(deChain).when(manager).getLocaleChain(player);

            Instant instant = Instant.parse("2025-01-15T12:00:00Z");
            String formatted = manager.formatDisplayDate(player, instant);

            assertEquals("Jan 15, 2025", formatted);
        }

        @Test
        @DisplayName("Given a German-locale player, when formatDisplayDate is called, then returns German medium-style date")
        void formatDisplayDate_germanLocale_returnsGermanDate() {
            McRPGPlayer player = mockPlayerWithSetting(LocaleSetting.SERVER_LOCALE);
            when(player.getAsBukkitPlayer()).thenReturn(Optional.empty());

            LinkedNode<Locale> deChain = new LinkedNode<>(Locale.GERMANY);
            deChain.setNext(new LinkedNode<>(Locale.ENGLISH));
            doReturn(deChain).when(manager).getLocaleChain(player);

            Instant instant = Instant.parse("2025-01-15T12:00:00Z");
            String formatted = manager.formatDisplayDate(player, instant);

            assertTrue(formatted.contains("15") && formatted.contains("2025"),
                    "German formatted date should contain day 15 and year 2025, got: " + formatted);
        }
    }

    @Nested
    @DisplayName("getServerDefaultLocale")
    class GetServerDefaultLocale {

        @Test
        @DisplayName("Given a configured server default locale, when getServerDefaultLocale is called, then returns the chain head locale")
        void getServerDefaultLocale_returnsChainHead() {
            Locale result = manager.getServerDefaultLocale();

            assertEquals(SERVER_DEFAULT, result);
        }
    }

    @Nested
    @DisplayName("getRegisteredLocales")
    class GetRegisteredLocales {

        @Test
        @DisplayName("Given localizations are registered, when getRegisteredLocales is called, then returns all locale keys")
        void getRegisteredLocales_returnsAllKeys() {
            var locales = manager.getRegisteredLocales();

            assertEquals(3, locales.size());
            assertTrue(locales.contains(Locale.ENGLISH));
            assertTrue(locales.contains(SERVER_DEFAULT));
            assertTrue(locales.contains(CLIENT_LOCALE));
        }
    }

    @Nested
    @DisplayName("addPaletteEntry")
    class AddPaletteEntry {

        @Test
        @DisplayName("Given a MiniMessage color tag, when addPaletteEntry is called, then both open and close tags are added")
        void addPaletteEntry_colorTag_addsOpenAndClose() throws Exception {
            Map<String, String> map = new LinkedHashMap<>();
            var method = McRPGLocalizationManager.class.getDeclaredMethod(
                    "addPaletteEntry", Map.class, String.class, String.class);
            method.setAccessible(true);
            method.invoke(manager, map, "primary", "<color:#D4A76A>");

            assertEquals("<color:#D4A76A>", map.get("<primary>"));
            assertEquals("</color:#D4A76A>", map.get("</primary>"));
        }

        @Test
        @DisplayName("Given a named MiniMessage tag like <gray>, when addPaletteEntry is called, then close tag maps to </gray>")
        void addPaletteEntry_namedTag_addsMatchingCloseTag() throws Exception {
            Map<String, String> map = new LinkedHashMap<>();
            var method = McRPGLocalizationManager.class.getDeclaredMethod(
                    "addPaletteEntry", Map.class, String.class, String.class);
            method.setAccessible(true);
            method.invoke(manager, map, "body", "<gray>");

            assertEquals("<gray>", map.get("<body>"));
            assertEquals("</gray>", map.get("</body>"));
        }

        @Test
        @DisplayName("Given a value that does not start with <, when addPaletteEntry is called, then close tag uses the same value")
        void addPaletteEntry_nonAngleBracketValue_closeTagSameAsOpen() throws Exception {
            Map<String, String> map = new LinkedHashMap<>();
            var method = McRPGLocalizationManager.class.getDeclaredMethod(
                    "addPaletteEntry", Map.class, String.class, String.class);
            method.setAccessible(true);
            method.invoke(manager, map, "custom", "plaintext");

            assertEquals("plaintext", map.get("<custom>"));
            assertEquals("plaintext", map.get("</custom>"));
        }
    }
}
