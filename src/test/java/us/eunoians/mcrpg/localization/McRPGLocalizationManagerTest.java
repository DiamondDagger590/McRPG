package us.eunoians.mcrpg.localization;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.util.LinkedNode;
import dev.dejvokep.boostedyaml.YamlDocument;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link McRPGLocalizationManager}.
 * <p>
 * The manager is normally registered as a full mock in the test registry (see {@code TestBootstrap}).
 * These tests use {@code CALLS_REAL_METHODS} to exercise concrete methods on the class without
 * starting the full plugin environment.
 */
class McRPGLocalizationManagerTest {

    /**
     * Representative subset of palette roles used to inject a known palette state into the
     * manager for {@code resolvePaletteColors} mechanism tests. Values may not match the
     * actual {@code config.yml} defaults — the tests exercise the replacement mechanism,
     * not specific configured colors.
     */
    private static final Map<String, String> DEFAULT_PALETTE;

    static {
        DEFAULT_PALETTE = new LinkedHashMap<>();
        addEntry(DEFAULT_PALETTE, "primary", "<color:#D4A76A>");
        addEntry(DEFAULT_PALETTE, "hint", "<color:#E8C97A>");
        addEntry(DEFAULT_PALETTE, "mana", "<color:#5EA8FF>");
        addEntry(DEFAULT_PALETTE, "ability-active", "<color:#FF7B5E>");
        addEntry(DEFAULT_PALETTE, "ability-passive", "<color:#7FB87F>");
        addEntry(DEFAULT_PALETTE, "ability-innate", "<color:#9E9E9E>");
        addEntry(DEFAULT_PALETTE, "body", "<gray>");
        addEntry(DEFAULT_PALETTE, "positive", "<green>");
        addEntry(DEFAULT_PALETTE, "negative", "<red>");
        addEntry(DEFAULT_PALETTE, "warning", "<yellow>");
    }

    /**
     * Mirrors the open+close tag logic in {@link McRPGLocalizationManager#addPaletteEntry}.
     */
    private static void addEntry(Map<String, String> map, String role, String openValue) {
        map.put("<" + role + ">", openValue);
        map.put("</" + role + ">", "</" + openValue.substring(1));
    }

    private McRPGLocalizationManager manager;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() throws Exception {
        manager = mock(McRPGLocalizationManager.class, CALLS_REAL_METHODS);
        ReloadableContent<Map<String, String>> mockReloadable = mock(ReloadableContent.class);
        when(mockReloadable.getContent()).thenReturn(DEFAULT_PALETTE);
        Field paletteField = McRPGLocalizationManager.class.getDeclaredField("paletteReplacements");
        paletteField.setAccessible(true);
        paletteField.set(manager, mockReloadable);
    }

    @DisplayName("Given a template with a single placeholder, when getLocalizedMessage is called, then the placeholder is replaced")
    @Test
    void getLocalizedMessage_singlePlaceholder_isReplaced() {
        String result = manager.getLocalizedMessage("You gained <amount> XP", Map.of("amount", "500"));

        assertEquals("You gained 500 XP", result);
    }

    @DisplayName("Given a template with multiple placeholders, when getLocalizedMessage is called, then all placeholders are replaced")
    @Test
    void getLocalizedMessage_multiplePlaceholders_allReplaced() {
        String result = manager.getLocalizedMessage("<skill> +<amount> XP", Map.of("skill", "Mining", "amount", "250"));

        assertEquals("Mining +250 XP", result);
    }

    @DisplayName("Given a template with a placeholder not in the map, when getLocalizedMessage is called, then the token is left unchanged")
    @Test
    void getLocalizedMessage_unknownPlaceholder_leftUnchanged() {
        String result = manager.getLocalizedMessage("You gained <amount> <skill> XP", Map.of("amount", "100"));

        assertEquals("You gained 100 <skill> XP", result);
    }

    @DisplayName("Given an empty placeholder map, when getLocalizedMessage is called, then the template is returned unchanged")
    @Test
    void getLocalizedMessage_emptyMap_returnsTemplateUnchanged() {
        String template = "Complete the quest to earn a reward";

        String result = manager.getLocalizedMessage(template, Map.of());

        assertEquals(template, result);
    }

    @DisplayName("Given a template containing MiniMessage color tags, when getLocalizedMessage is called, then the tags are preserved")
    @Test
    void getLocalizedMessage_miniMessageTagsPreserved() {
        String result = manager.getLocalizedMessage("<gold><amount> XP", Map.of("amount", "500"));

        assertEquals("<gold>500 XP", result);
    }

    @DisplayName("Given a string with <primary>, when resolvePaletteColors is called, then it is replaced with the configured value")
    @Test
    void resolvePaletteColors_primary_isReplaced() {
        assertEquals("<color:#D4A76A>Home GUI", manager.resolvePaletteColors("<primary>Home GUI"));
    }

    @DisplayName("Given a string with <hint>, when resolvePaletteColors is called, then it is replaced with the configured value")
    @Test
    void resolvePaletteColors_hint_isReplaced() {
        assertEquals("<color:#E8C97A>Click to open.", manager.resolvePaletteColors("<hint>Click to open."));
    }

    @DisplayName("Given a string with <body>, when resolvePaletteColors is called, then it is replaced with <gray>")
    @Test
    void resolvePaletteColors_body_isReplaced() {
        assertEquals("<gray>Descriptive text.", manager.resolvePaletteColors("<body>Descriptive text."));
    }

    @DisplayName("Given a string with <positive>, when resolvePaletteColors is called, then it is replaced with <green>")
    @Test
    void resolvePaletteColors_positive_isReplaced() {
        assertEquals("<green>Enabled", manager.resolvePaletteColors("<positive>Enabled"));
    }

    @DisplayName("Given a string with <negative>, when resolvePaletteColors is called, then it is replaced with <red>")
    @Test
    void resolvePaletteColors_negative_isReplaced() {
        assertEquals("<red>Disabled", manager.resolvePaletteColors("<negative>Disabled"));
    }

    @DisplayName("Given a string with <warning>, when resolvePaletteColors is called, then it is replaced with <yellow>")
    @Test
    void resolvePaletteColors_warning_isReplaced() {
        assertEquals("<yellow>Expires in 5m", manager.resolvePaletteColors("<warning>Expires in 5m"));
    }

    @DisplayName("Given a string with all 10 palette roles, when resolvePaletteColors is called, then all are replaced")
    @Test
    void resolvePaletteColors_allTenRoles_allReplaced() {
        String input = "<primary><hint><mana><ability-active><ability-passive><ability-innate><body><positive><negative><warning>";
        String expected = "<color:#D4A76A><color:#E8C97A><color:#5EA8FF><color:#FF7B5E><color:#7FB87F><color:#9E9E9E><gray><green><red><yellow>";
        assertEquals(expected, manager.resolvePaletteColors(input));
    }

    @DisplayName("Given a string with an unknown placeholder, when resolvePaletteColors is called, then it passes through unchanged")
    @Test
    void resolvePaletteColors_unknownPlaceholder_passesThrough() {
        String input = "<unknown>Some text";
        assertEquals(input, manager.resolvePaletteColors(input));
    }

    @DisplayName("Given a string with no palette placeholders, when resolvePaletteColors is called, then the string is returned unchanged")
    @Test
    void resolvePaletteColors_noPlaceholders_returnedUnchanged() {
        String input = "Hello, world!";
        assertEquals(input, manager.resolvePaletteColors(input));
    }

    @DisplayName("Given a string with nested palette tags, when resolvePaletteColors is called, then all are replaced in order")
    @Test
    void resolvePaletteColors_nestedPaletteTags_allReplaced() {
        assertEquals("<gray>Mana Cost: <color:#5EA8FF>30", manager.resolvePaletteColors("<body>Mana Cost: <mana>30"));
    }

    @DisplayName("Given an empty string, when resolvePaletteColors is called, then an empty string is returned")
    @Test
    void resolvePaletteColors_emptyString_returnsEmpty() {
        assertEquals("", manager.resolvePaletteColors(""));
    }

    @DisplayName("Given a string that is only a palette placeholder, when resolvePaletteColors is called, then just the replacement value is returned")
    @Test
    void resolvePaletteColors_onlyPlaceholder_returnsReplacementValue() {
        assertEquals("<color:#D4A76A>", manager.resolvePaletteColors("<primary>"));
    }

    @DisplayName("Given a string with a close tag like </primary>, when resolvePaletteColors is called, then it is replaced with the corresponding MiniMessage close tag")
    @Test
    void resolvePaletteColors_closeTag_isReplaced() {
        assertEquals("<color:#D4A76A>50%</color:#D4A76A> towards next level",
                manager.resolvePaletteColors("<primary>50%</primary> towards next level"));
    }

    @DisplayName("Given a string with </body> close tag, when resolvePaletteColors is called, then it resolves to </gray>")
    @Test
    void resolvePaletteColors_bodyCloseTag_isReplaced() {
        assertEquals("<green>Enabled</green> state",
                manager.resolvePaletteColors("<positive>Enabled</positive> state"));
    }

    @Nested
    @DisplayName("getLocaleChain")
    class GetLocaleChainTests {

        private McRPGPlayer mockPlayer;
        private LinkedNode<Locale> serverDefaultChain;

        @BeforeEach
        @SuppressWarnings("unchecked")
        void setupLocaleChain() throws Exception {
            mockPlayer = mock(McRPGPlayer.class);

            serverDefaultChain = new LinkedNode<>(Locale.GERMAN);
            serverDefaultChain.setNext(new LinkedNode<>(Locale.ENGLISH));

            ReloadableContent<LinkedNode<Locale>> mockLocaleChain = mock(ReloadableContent.class);
            when(mockLocaleChain.getContent()).thenReturn(serverDefaultChain);

            Field localeChainField = manager.getClass().getSuperclass().getDeclaredField("localeChain");
            localeChainField.setAccessible(true);
            localeChainField.set(manager, mockLocaleChain);
        }

        @DisplayName("Given no locale setting, when getLocaleChain is called, then the default chain (client -> server default -> english) is returned")
        @Test
        void getLocaleChain_noSetting_returnsDefaultChain() {
            doReturn(Optional.empty()).when(mockPlayer).getPlayerSetting(LocalePlayerSetting.SETTING_KEY);
            when(mockPlayer.getAsBukkitPlayer()).thenReturn(Optional.empty());

            LinkedNode<Locale> chain = manager.getLocaleChain(mockPlayer);

            assertEquals(Locale.GERMAN, chain.getNodeValue());
            assertTrue(chain.hasNext());
            assertEquals(Locale.ENGLISH, chain.getNextNode().getNodeValue());
        }

        @DisplayName("Given CLIENT_LOCALE setting, when getLocaleChain is called, then the default chain is returned")
        @Test
        void getLocaleChain_clientLocale_returnsDefaultChain() {
            doReturn(Optional.of(LocaleSetting.CLIENT_LOCALE)).when(mockPlayer).getPlayerSetting(LocalePlayerSetting.SETTING_KEY);
            when(mockPlayer.getAsBukkitPlayer()).thenReturn(Optional.empty());

            LinkedNode<Locale> chain = manager.getLocaleChain(mockPlayer);

            assertEquals(Locale.GERMAN, chain.getNodeValue());
            assertTrue(chain.hasNext());
            assertEquals(Locale.ENGLISH, chain.getNextNode().getNodeValue());
        }

        @DisplayName("Given CLIENT_LOCALE setting with client locale available, when getLocaleChain is called, then client locale leads the chain")
        @Test
        void getLocaleChain_clientLocaleWithBukkitPlayer_clientLeadsChain() {
            doReturn(Optional.of(LocaleSetting.CLIENT_LOCALE)).when(mockPlayer).getPlayerSetting(LocalePlayerSetting.SETTING_KEY);
            Player playerMock = mock(Player.class);
            when(playerMock.locale()).thenReturn(Locale.FRENCH);
            when(mockPlayer.getAsBukkitPlayer()).thenReturn(Optional.of(playerMock));

            LinkedNode<Locale> chain = manager.getLocaleChain(mockPlayer);

            assertEquals(Locale.FRENCH, chain.getNodeValue());
            assertTrue(chain.hasNext());
            assertEquals(Locale.GERMAN, chain.getNextNode().getNodeValue());
            assertTrue(chain.getNextNode().hasNext());
            assertEquals(Locale.ENGLISH, chain.getNextNode().getNextNode().getNodeValue());
        }

        @DisplayName("Given SERVER_LOCALE setting with client locale available, when getLocaleChain is called, then server default leads the chain")
        @Test
        void getLocaleChain_serverLocaleWithClient_serverDefaultLeadsChain() {
            doReturn(Optional.of(LocaleSetting.SERVER_LOCALE)).when(mockPlayer).getPlayerSetting(LocalePlayerSetting.SETTING_KEY);
            Player playerMock = mock(Player.class);
            when(playerMock.locale()).thenReturn(Locale.FRENCH);
            when(mockPlayer.getAsBukkitPlayer()).thenReturn(Optional.of(playerMock));

            LinkedNode<Locale> chain = manager.getLocaleChain(mockPlayer);

            assertEquals(Locale.GERMAN, chain.getNodeValue());
            assertTrue(chain.hasNext());
            assertEquals(Locale.FRENCH, chain.getNextNode().getNodeValue());
            assertTrue(chain.getNextNode().hasNext());
            assertEquals(Locale.ENGLISH, chain.getNextNode().getNextNode().getNodeValue());
        }

        @DisplayName("Given SERVER_LOCALE setting without client locale, when getLocaleChain is called, then server default followed by english")
        @Test
        void getLocaleChain_serverLocaleWithoutClient_serverDefaultThenEnglish() {
            doReturn(Optional.of(LocaleSetting.SERVER_LOCALE)).when(mockPlayer).getPlayerSetting(LocalePlayerSetting.SETTING_KEY);
            when(mockPlayer.getAsBukkitPlayer()).thenReturn(Optional.empty());

            LinkedNode<Locale> chain = manager.getLocaleChain(mockPlayer);

            assertEquals(Locale.GERMAN, chain.getNodeValue());
            assertTrue(chain.hasNext());
            assertEquals(Locale.ENGLISH, chain.getNextNode().getNodeValue());
            assertFalse(chain.getNextNode().hasNext());
        }

        @DisplayName("Given a SpecificLocaleSetting, when getLocaleChain is called, then the specific locale leads the chain")
        @Test
        void getLocaleChain_specificLocale_specificLeadsChain() {
            SpecificLocaleSetting specificSetting = mock(SpecificLocaleSetting.class);
            when(specificSetting.getLocale()).thenReturn(Locale.JAPANESE);
            doReturn(Optional.of(specificSetting)).when(mockPlayer).getPlayerSetting(LocalePlayerSetting.SETTING_KEY);
            when(mockPlayer.getAsBukkitPlayer()).thenReturn(Optional.empty());

            LinkedNode<Locale> chain = manager.getLocaleChain(mockPlayer);

            assertEquals(Locale.JAPANESE, chain.getNodeValue());
            assertTrue(chain.hasNext());
            assertEquals(Locale.GERMAN, chain.getNextNode().getNodeValue());
            assertTrue(chain.getNextNode().hasNext());
            assertEquals(Locale.ENGLISH, chain.getNextNode().getNextNode().getNodeValue());
        }

        @DisplayName("Given a SpecificLocaleSetting with client locale available, when getLocaleChain is called, then specific -> client -> server default -> english")
        @Test
        void getLocaleChain_specificLocaleWithClient_fullChain() {
            SpecificLocaleSetting specificSetting = mock(SpecificLocaleSetting.class);
            when(specificSetting.getLocale()).thenReturn(Locale.JAPANESE);
            doReturn(Optional.of(specificSetting)).when(mockPlayer).getPlayerSetting(LocalePlayerSetting.SETTING_KEY);
            Player playerMock = mock(Player.class);
            when(playerMock.locale()).thenReturn(Locale.FRENCH);
            when(mockPlayer.getAsBukkitPlayer()).thenReturn(Optional.of(playerMock));

            LinkedNode<Locale> chain = manager.getLocaleChain(mockPlayer);

            assertEquals(Locale.JAPANESE, chain.getNodeValue());
            assertTrue(chain.hasNext());
            assertEquals(Locale.FRENCH, chain.getNextNode().getNodeValue());
            assertTrue(chain.getNextNode().hasNext());
            assertEquals(Locale.GERMAN, chain.getNextNode().getNextNode().getNodeValue());
            assertTrue(chain.getNextNode().getNextNode().hasNext());
            assertEquals(Locale.ENGLISH, chain.getNextNode().getNextNode().getNextNode().getNodeValue());
        }
    }

    @Nested
    @DisplayName("getServerDefaultLocale")
    class GetServerDefaultLocaleTests {

        @DisplayName("Given a configured server default locale, when getServerDefaultLocale is called, then the locale chain head is returned")
        @Test
        @SuppressWarnings("unchecked")
        void getServerDefaultLocale_returnsLocaleChainHead() throws Exception {
            LinkedNode<Locale> serverDefaultChain = new LinkedNode<>(Locale.ITALIAN);
            serverDefaultChain.setNext(new LinkedNode<>(Locale.ENGLISH));

            ReloadableContent<LinkedNode<Locale>> mockLocaleChain = mock(ReloadableContent.class);
            when(mockLocaleChain.getContent()).thenReturn(serverDefaultChain);

            Field localeChainField = manager.getClass().getSuperclass().getDeclaredField("localeChain");
            localeChainField.setAccessible(true);
            localeChainField.set(manager, mockLocaleChain);

            assertEquals(Locale.ITALIAN, manager.getServerDefaultLocale());
        }
    }

    @Nested
    @DisplayName("getRegisteredLocales")
    class GetRegisteredLocalesTests {

        @DisplayName("Given no registered locales, when getRegisteredLocales is called, then an empty set is returned")
        @Test
        void getRegisteredLocales_noLocales_returnsEmptySet() throws Exception {
            Map<Locale, List<YamlDocument>> localizationsMap = new HashMap<>();

            Field localizationsField = manager.getClass().getSuperclass().getDeclaredField("localizations");
            localizationsField.setAccessible(true);
            localizationsField.set(manager, localizationsMap);

            Set<Locale> result = manager.getRegisteredLocales();
            assertTrue(result.isEmpty());
        }

        @DisplayName("Given multiple registered locales, when getRegisteredLocales is called, then all locale keys are returned")
        @Test
        void getRegisteredLocales_multipleLocales_returnsAllKeys() throws Exception {
            Map<Locale, List<YamlDocument>> localizationsMap = new HashMap<>();
            localizationsMap.put(Locale.ENGLISH, new ArrayList<>());
            localizationsMap.put(Locale.FRENCH, new ArrayList<>());
            localizationsMap.put(Locale.GERMAN, new ArrayList<>());

            Field localizationsField = manager.getClass().getSuperclass().getDeclaredField("localizations");
            localizationsField.setAccessible(true);
            localizationsField.set(manager, localizationsMap);

            Set<Locale> result = manager.getRegisteredLocales();
            assertEquals(3, result.size());
            assertTrue(result.contains(Locale.ENGLISH));
            assertTrue(result.contains(Locale.FRENCH));
            assertTrue(result.contains(Locale.GERMAN));
        }

        @DisplayName("Given registered locales, when getRegisteredLocales is called, then the returned set is a defensive copy")
        @Test
        void getRegisteredLocales_returnedSetIsDefensiveCopy() throws Exception {
            Map<Locale, List<YamlDocument>> localizationsMap = new HashMap<>();
            localizationsMap.put(Locale.ENGLISH, new ArrayList<>());

            Field localizationsField = manager.getClass().getSuperclass().getDeclaredField("localizations");
            localizationsField.setAccessible(true);
            localizationsField.set(manager, localizationsMap);

            Set<Locale> result = manager.getRegisteredLocales();
            assertNotSame(localizationsMap.keySet(), result);
        }
    }

    @Nested
    @DisplayName("formatDisplayDate")
    class FormatDisplayDateTests {

        @DisplayName("Given a player with English locale, when formatDisplayDate is called, then the date is formatted in English MEDIUM style")
        @Test
        @SuppressWarnings("unchecked")
        void formatDisplayDate_englishLocale_formatsInEnglish() throws Exception {
            McRPGPlayer mockPlayer = mock(McRPGPlayer.class);
            doReturn(Optional.of(LocaleSetting.CLIENT_LOCALE)).when(mockPlayer).getPlayerSetting(LocalePlayerSetting.SETTING_KEY);
            when(mockPlayer.getAsBukkitPlayer()).thenReturn(Optional.empty());

            LinkedNode<Locale> serverDefaultChain = new LinkedNode<>(Locale.US);
            serverDefaultChain.setNext(new LinkedNode<>(Locale.ENGLISH));

            ReloadableContent<LinkedNode<Locale>> mockLocaleChain = mock(ReloadableContent.class);
            when(mockLocaleChain.getContent()).thenReturn(serverDefaultChain);

            Field localeChainField = manager.getClass().getSuperclass().getDeclaredField("localeChain");
            localeChainField.setAccessible(true);
            localeChainField.set(manager, mockLocaleChain);

            Instant testInstant = Instant.parse("2025-01-15T12:00:00Z");
            String result = manager.formatDisplayDate(mockPlayer, testInstant);

            assertEquals("Jan 15, 2025", result);
        }

        @DisplayName("Given a player with German locale, when formatDisplayDate is called, then the date is formatted in German MEDIUM style")
        @Test
        @SuppressWarnings("unchecked")
        void formatDisplayDate_germanLocale_formatsInGerman() throws Exception {
            McRPGPlayer mockPlayer = mock(McRPGPlayer.class);
            Player playerMock = mock(Player.class);
            when(playerMock.locale()).thenReturn(Locale.GERMANY);
            when(mockPlayer.getAsBukkitPlayer()).thenReturn(Optional.of(playerMock));
            doReturn(Optional.of(LocaleSetting.CLIENT_LOCALE)).when(mockPlayer).getPlayerSetting(LocalePlayerSetting.SETTING_KEY);

            LinkedNode<Locale> serverDefaultChain = new LinkedNode<>(Locale.US);
            serverDefaultChain.setNext(new LinkedNode<>(Locale.ENGLISH));

            ReloadableContent<LinkedNode<Locale>> mockLocaleChain = mock(ReloadableContent.class);
            when(mockLocaleChain.getContent()).thenReturn(serverDefaultChain);

            Field localeChainField = manager.getClass().getSuperclass().getDeclaredField("localeChain");
            localeChainField.setAccessible(true);
            localeChainField.set(manager, mockLocaleChain);

            Instant testInstant = Instant.parse("2025-01-15T12:00:00Z");
            String result = manager.formatDisplayDate(mockPlayer, testInstant);

            assertEquals("15.01.2025", result);
        }
    }

    @Nested
    @DisplayName("getPaletteReplacements")
    class GetPaletteReplacementsTests {

        @DisplayName("Given a configured palette, when getPaletteReplacements is called, then the replacement map is returned")
        @Test
        void getPaletteReplacements_returnsConfiguredMap() {
            Map<String, String> replacements = manager.getPaletteReplacements();
            assertEquals("<color:#D4A76A>", replacements.get("<primary>"));
            assertEquals("</color:#D4A76A>", replacements.get("</primary>"));
        }

        @DisplayName("Given a configured palette, when getPaletteReplacements is called, then both open and close tags exist for each role")
        @Test
        void getPaletteReplacements_hasOpenAndCloseTags() {
            Map<String, String> replacements = manager.getPaletteReplacements();
            assertEquals(20, replacements.size());
        }
    }
}
