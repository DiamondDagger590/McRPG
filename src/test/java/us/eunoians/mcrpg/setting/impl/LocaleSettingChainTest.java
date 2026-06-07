package us.eunoians.mcrpg.setting.impl;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.util.LinkedNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class LocaleSettingChainTest extends McRPGBaseTest {

    private McRPGLocalizationManager localizationManager;

    @BeforeEach
    void setUp() {
        localizationManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
    }

    @Nested
    @DisplayName("Chain with no specific locales")
    class NoSpecificLocales {

        @BeforeEach
        void setUpEmptyLocales() {
            when(localizationManager.getRegisteredLocales()).thenReturn(Set.of());
        }

        @DisplayName("getNodeForSetting returns node for CLIENT_LOCALE")
        @Test
        void getNodeForSetting_returnsNode_forClientLocale() {
            LinkedNode<? extends PlayerSetting> node = LocaleSettingChain.getNodeForSetting(LocaleSetting.CLIENT_LOCALE);

            assertNotNull(node);
            assertEquals(LocaleSetting.CLIENT_LOCALE, node.getNodeValue());
        }

        @DisplayName("getNodeForSetting returns node for SERVER_LOCALE")
        @Test
        void getNodeForSetting_returnsNode_forServerLocale() {
            LinkedNode<? extends PlayerSetting> node = LocaleSettingChain.getNodeForSetting(LocaleSetting.SERVER_LOCALE);

            assertEquals(LocaleSetting.SERVER_LOCALE, node.getNodeValue());
        }

        @DisplayName("CLIENT_LOCALE next is SERVER_LOCALE")
        @Test
        void clientLocaleNext_isServerLocale() {
            LinkedNode<? extends PlayerSetting> node = LocaleSettingChain.getNodeForSetting(LocaleSetting.CLIENT_LOCALE);

            assertEquals(LocaleSetting.SERVER_LOCALE, node.getNextNode().getNodeValue());
        }

        @DisplayName("SERVER_LOCALE next wraps to CLIENT_LOCALE")
        @Test
        void serverLocaleNext_wrapsToClientLocale() {
            LinkedNode<? extends PlayerSetting> node = LocaleSettingChain.getNodeForSetting(LocaleSetting.SERVER_LOCALE);

            assertEquals(LocaleSetting.CLIENT_LOCALE, node.getNextNode().getNodeValue());
        }

        @DisplayName("chain is circular with two nodes")
        @Test
        void chain_isCircular_withTwoNodes() {
            LinkedNode<? extends PlayerSetting> client = LocaleSettingChain.getNodeForSetting(LocaleSetting.CLIENT_LOCALE);
            LinkedNode<? extends PlayerSetting> server = client.getNextNode();
            LinkedNode<? extends PlayerSetting> backToClient = server.getNextNode();

            assertEquals(LocaleSetting.CLIENT_LOCALE, backToClient.getNodeValue());
        }

        @DisplayName("getNextSettingNode returns SERVER_LOCALE for CLIENT_LOCALE")
        @Test
        void getNextSettingNode_returnsServerLocale_forClientLocale() {
            LinkedNode<? extends PlayerSetting> next = LocaleSettingChain.getNextSettingNode(LocaleSetting.CLIENT_LOCALE);

            assertEquals(LocaleSetting.SERVER_LOCALE, next.getNodeValue());
        }

        @DisplayName("getNextSettingNode returns CLIENT_LOCALE for SERVER_LOCALE")
        @Test
        void getNextSettingNode_returnsClientLocale_forServerLocale() {
            LinkedNode<? extends PlayerSetting> next = LocaleSettingChain.getNextSettingNode(LocaleSetting.SERVER_LOCALE);

            assertEquals(LocaleSetting.CLIENT_LOCALE, next.getNodeValue());
        }
    }

    @Nested
    @DisplayName("Chain with specific locales")
    class WithSpecificLocales {

        @BeforeEach
        void setUpLocales() {
            when(localizationManager.getRegisteredLocales()).thenReturn(
                    Set.of(Locale.of("fr"), Locale.of("en"))
            );
        }

        @DisplayName("chain order is CLIENT_LOCALE -> SERVER_LOCALE -> en -> fr -> CLIENT_LOCALE")
        @Test
        void chain_ordersLocalesAlphabetically() {
            LinkedNode<? extends PlayerSetting> client = LocaleSettingChain.getNodeForSetting(LocaleSetting.CLIENT_LOCALE);

            LinkedNode<? extends PlayerSetting> server = client.getNextNode();
            assertEquals(LocaleSetting.SERVER_LOCALE, server.getNodeValue());

            LinkedNode<? extends PlayerSetting> enNode = server.getNextNode();
            assertTrue(enNode.getNodeValue() instanceof SpecificLocaleSetting);
            assertEquals("en", ((SpecificLocaleSetting) enNode.getNodeValue()).getLocaleCode());

            LinkedNode<? extends PlayerSetting> frNode = enNode.getNextNode();
            assertTrue(frNode.getNodeValue() instanceof SpecificLocaleSetting);
            assertEquals("fr", ((SpecificLocaleSetting) frNode.getNodeValue()).getLocaleCode());

            LinkedNode<? extends PlayerSetting> backToClient = frNode.getNextNode();
            assertEquals(LocaleSetting.CLIENT_LOCALE, backToClient.getNodeValue());
        }

        @DisplayName("getNodeForSetting returns matching SpecificLocaleSetting node")
        @Test
        void getNodeForSetting_returnsMatchingSpecificNode() {
            LinkedNode<? extends PlayerSetting> node = LocaleSettingChain.getNodeForSetting(new SpecificLocaleSetting("fr"));

            assertTrue(node.getNodeValue() instanceof SpecificLocaleSetting);
            assertEquals("fr", ((SpecificLocaleSetting) node.getNodeValue()).getLocaleCode());
        }

        @DisplayName("getNextSettingNode from fr wraps to CLIENT_LOCALE")
        @Test
        void getNextSettingNode_fromLastLocale_wrapsToClientLocale() {
            LinkedNode<? extends PlayerSetting> next = LocaleSettingChain.getNextSettingNode(new SpecificLocaleSetting("fr"));

            assertEquals(LocaleSetting.CLIENT_LOCALE, next.getNodeValue());
        }

        @DisplayName("getNextSettingNode from en advances to fr")
        @Test
        void getNextSettingNode_fromEn_advancesToFr() {
            LinkedNode<? extends PlayerSetting> next = LocaleSettingChain.getNextSettingNode(new SpecificLocaleSetting("en"));

            assertTrue(next.getNodeValue() instanceof SpecificLocaleSetting);
            assertEquals("fr", ((SpecificLocaleSetting) next.getNodeValue()).getLocaleCode());
        }

        @DisplayName("getNodeForSetting for unknown locale returns head")
        @Test
        void getNodeForSetting_unknownLocale_returnsHead() {
            LinkedNode<? extends PlayerSetting> node = LocaleSettingChain.getNodeForSetting(new SpecificLocaleSetting("xyz"));

            assertEquals(LocaleSetting.CLIENT_LOCALE, node.getNodeValue());
        }
    }

    @Nested
    @DisplayName("Chain with single specific locale")
    class SingleSpecificLocale {

        @BeforeEach
        void setUpSingleLocale() {
            when(localizationManager.getRegisteredLocales()).thenReturn(Set.of(Locale.of("de")));
        }

        @DisplayName("chain has three nodes")
        @Test
        void chain_hasThreeNodes() {
            LinkedNode<? extends PlayerSetting> client = LocaleSettingChain.getNodeForSetting(LocaleSetting.CLIENT_LOCALE);
            LinkedNode<? extends PlayerSetting> server = client.getNextNode();
            LinkedNode<? extends PlayerSetting> de = server.getNextNode();
            LinkedNode<? extends PlayerSetting> backToClient = de.getNextNode();

            assertEquals(LocaleSetting.CLIENT_LOCALE, client.getNodeValue());
            assertEquals(LocaleSetting.SERVER_LOCALE, server.getNodeValue());
            assertTrue(de.getNodeValue() instanceof SpecificLocaleSetting);
            assertEquals("de", ((SpecificLocaleSetting) de.getNodeValue()).getLocaleCode());
            assertEquals(LocaleSetting.CLIENT_LOCALE, backToClient.getNodeValue());
        }
    }

    @Nested
    @DisplayName("Blank locale filtering")
    class BlankLocaleFiltering {

        @BeforeEach
        void setUpWithBlankLocale() {
            when(localizationManager.getRegisteredLocales()).thenReturn(
                    Set.of(Locale.of("en"), Locale.of(""))
            );
        }

        @DisplayName("blank locale codes are filtered out")
        @Test
        void blankLocale_isFilteredOut() {
            LinkedNode<? extends PlayerSetting> client = LocaleSettingChain.getNodeForSetting(LocaleSetting.CLIENT_LOCALE);
            LinkedNode<? extends PlayerSetting> server = client.getNextNode();
            LinkedNode<? extends PlayerSetting> en = server.getNextNode();
            LinkedNode<? extends PlayerSetting> backToClient = en.getNextNode();

            assertTrue(en.getNodeValue() instanceof SpecificLocaleSetting);
            assertEquals("en", ((SpecificLocaleSetting) en.getNodeValue()).getLocaleCode());
            assertEquals(LocaleSetting.CLIENT_LOCALE, backToClient.getNodeValue());
        }
    }
}
