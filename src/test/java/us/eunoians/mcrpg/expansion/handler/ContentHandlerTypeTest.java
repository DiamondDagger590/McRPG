package us.eunoians.mcrpg.expansion.handler;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.statistic.SimpleStatistic;
import com.diamonddagger590.mccore.statistic.StatisticType;
import com.diamonddagger590.mccore.util.LinkedNode;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.expansion.content.AbilityContentPack;
import us.eunoians.mcrpg.expansion.content.LocalizationContentPack;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.expansion.content.McRPGContentPack;
import us.eunoians.mcrpg.expansion.content.PlayerSettingContentPack;
import us.eunoians.mcrpg.expansion.content.PlayerStatContentPack;
import us.eunoians.mcrpg.expansion.content.QuestContentPack;
import us.eunoians.mcrpg.expansion.content.QuestObjectiveTypeContentPack;
import us.eunoians.mcrpg.expansion.content.QuestRarityContentPack;
import us.eunoians.mcrpg.expansion.content.QuestRewardTypeContentPack;
import us.eunoians.mcrpg.expansion.content.QuestScopeProviderContentPack;
import us.eunoians.mcrpg.expansion.content.QuestSourceContentPack;
import us.eunoians.mcrpg.expansion.content.QuestTemplateContentPack;
import us.eunoians.mcrpg.expansion.content.RewardDistributionTypeContentPack;
import us.eunoians.mcrpg.expansion.content.ScopedBoardAdapterContentPack;
import us.eunoians.mcrpg.expansion.content.SkillContentPack;
import us.eunoians.mcrpg.expansion.content.StatisticContent;
import us.eunoians.mcrpg.expansion.content.StatisticContentPack;
import us.eunoians.mcrpg.expansion.content.TemplateConditionContentPack;
import us.eunoians.mcrpg.gui.setting.slot.McRPGSettingSlot;
import us.eunoians.mcrpg.localization.McRPGLocalization;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.objective.type.MockQuestObjectiveType;
import us.eunoians.mcrpg.quest.reward.MockQuestRewardType;
import us.eunoians.mcrpg.setting.McRPGSetting;
import us.eunoians.mcrpg.stat.impl.ResourcePoolPlayerStat;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContentHandlerTypeTest extends McRPGBaseTest {

    private static final ContentExpansion STUB_EXPANSION = new ContentExpansion(new NamespacedKey("test", "handler_test")) {
        @NotNull
        @Override
        public Set<McRPGContentPack<? extends McRPGContent>> getExpansionContent() {
            return Set.of();
        }

        @NotNull
        @Override
        public String getExpansionName(@NotNull McRPGPlayer player) {
            return "HandlerTest";
        }
    };

    @DisplayName("getContentHandler returns non-null for all enum values")
    @ParameterizedTest
    @EnumSource(ContentHandlerType.class)
    void getContentHandler_returnsNonNull(ContentHandlerType type) {
        assertNotNull(type.getContentHandler());
    }

    @Nested
    @DisplayName("SKILL handler")
    class SkillHandler {

        @DisplayName("returns false for non-matching content pack")
        @Test
        void processContentPack_nonMatchingPack_returnsFalse() {
            StatisticContentPack wrongPack = new StatisticContentPack(STUB_EXPANSION);
            assertFalse(ContentHandlerType.SKILL.getContentHandler().processContentPack(mcRPG, wrongPack));
        }

        @DisplayName("returns true for matching SkillContentPack")
        @Test
        void processContentPack_matchingPack_returnsTrue() {
            SkillContentPack pack = new SkillContentPack(STUB_EXPANSION);
            assertTrue(ContentHandlerType.SKILL.getContentHandler().processContentPack(mcRPG, pack));
        }
    }

    @Nested
    @DisplayName("ABILITY handler")
    class AbilityHandler {

        @DisplayName("returns false for non-matching content pack")
        @Test
        void processContentPack_nonMatchingPack_returnsFalse() {
            StatisticContentPack wrongPack = new StatisticContentPack(STUB_EXPANSION);
            assertFalse(ContentHandlerType.ABILITY.getContentHandler().processContentPack(mcRPG, wrongPack));
        }

        @DisplayName("returns true for matching AbilityContentPack")
        @Test
        void processContentPack_matchingPack_returnsTrue() {
            AbilityContentPack pack = new AbilityContentPack(STUB_EXPANSION);
            assertTrue(ContentHandlerType.ABILITY.getContentHandler().processContentPack(mcRPG, pack));
        }
    }

    @Nested
    @DisplayName("SETTING handler")
    class SettingHandler {

        @DisplayName("returns false for non-matching content pack")
        @Test
        void processContentPack_nonMatchingPack_returnsFalse() {
            StatisticContentPack wrongPack = new StatisticContentPack(STUB_EXPANSION);
            assertFalse(ContentHandlerType.SETTING.getContentHandler().processContentPack(mcRPG, wrongPack));
        }

        @DisplayName("returns true for matching PlayerSettingContentPack")
        @Test
        void processContentPack_matchingPack_returnsTrue() {
            PlayerSettingContentPack pack = new PlayerSettingContentPack(STUB_EXPANSION);
            McRPGSetting stubSetting = new McRPGSetting() {

                @NotNull
                @Override
                public NamespacedKey getSettingKey() {
                    return new NamespacedKey("test", "handler_test_setting");
                }

                @NotNull
                @Override
                public LinkedNode<? extends PlayerSetting> getFirstSetting() {
                    return new LinkedNode<>(this);
                }

                @NotNull
                @Override
                public LinkedNode<? extends PlayerSetting> getNextSetting() {
                    return new LinkedNode<>(this);
                }

                @Override
                public void onSettingChange(@NotNull CorePlayer player, @NotNull Optional<PlayerSetting> oldSetting) {
                }

                @NotNull
                @Override
                public Optional<? extends PlayerSetting> fromString(@NotNull String setting) {
                    return Optional.empty();
                }

                @NotNull
                @Override
                public String name() {
                    return "STUB";
                }

                @NotNull
                @Override
                public McRPGSettingSlot<? extends McRPGSetting> getSettingSlot(@NotNull McRPGPlayer player) {
                    return null;
                }
            };
            pack.addContent(stubSetting);
            assertTrue(ContentHandlerType.SETTING.getContentHandler().processContentPack(mcRPG, pack));
        }
    }

    @Nested
    @DisplayName("LOCALIZATION handler")
    class LocalizationHandler {

        @DisplayName("returns false for non-matching content pack")
        @Test
        void processContentPack_nonMatchingPack_returnsFalse() {
            StatisticContentPack wrongPack = new StatisticContentPack(STUB_EXPANSION);
            assertFalse(ContentHandlerType.LOCALIZATION.getContentHandler().processContentPack(mcRPG, wrongPack));
        }

        @DisplayName("returns true for matching LocalizationContentPack")
        @Test
        void processContentPack_matchingPack_returnsTrue() {
            LocalizationContentPack pack = new LocalizationContentPack(STUB_EXPANSION);
            McRPGLocalization stubLocale = new McRPGLocalization() {
                @NotNull
                @Override
                public Optional<NamespacedKey> getExpansionKey() {
                    return Optional.of(STUB_EXPANSION.getExpansionKey());
                }

                @NotNull
                @Override
                public Locale getLocale() {
                    return Locale.FRENCH;
                }

                @NotNull
                @Override
                public YamlDocument getConfigurationFile() {
                    return null;
                }
            };
            pack.addContent(stubLocale);
            assertTrue(ContentHandlerType.LOCALIZATION.getContentHandler().processContentPack(mcRPG, pack));
        }
    }

    @Nested
    @DisplayName("QUEST handler")
    class QuestHandler {

        @DisplayName("returns false for non-matching content pack")
        @Test
        void processContentPack_nonMatchingPack_returnsFalse() {
            StatisticContentPack wrongPack = new StatisticContentPack(STUB_EXPANSION);
            assertFalse(ContentHandlerType.QUEST.getContentHandler().processContentPack(mcRPG, wrongPack));
        }

        @DisplayName("returns true for matching QuestContentPack")
        @Test
        void processContentPack_matchingPack_returnsTrue() {
            QuestContentPack pack = new QuestContentPack(STUB_EXPANSION);
            assertTrue(ContentHandlerType.QUEST.getContentHandler().processContentPack(mcRPG, pack));
        }
    }

    @Nested
    @DisplayName("QUEST_OBJECTIVE_TYPE handler")
    class QuestObjectiveTypeHandler {

        @DisplayName("returns false for non-matching content pack")
        @Test
        void processContentPack_nonMatchingPack_returnsFalse() {
            StatisticContentPack wrongPack = new StatisticContentPack(STUB_EXPANSION);
            assertFalse(ContentHandlerType.QUEST_OBJECTIVE_TYPE.getContentHandler().processContentPack(mcRPG, wrongPack));
        }

        @DisplayName("returns true for matching QuestObjectiveTypeContentPack")
        @Test
        void processContentPack_matchingPack_returnsTrue() {
            QuestObjectiveTypeContentPack pack = new QuestObjectiveTypeContentPack(STUB_EXPANSION);
            NamespacedKey testKey = new NamespacedKey("test", "handler_test_objective");
            pack.addContent(new MockQuestObjectiveType(testKey, STUB_EXPANSION.getExpansionKey()));
            assertTrue(ContentHandlerType.QUEST_OBJECTIVE_TYPE.getContentHandler().processContentPack(mcRPG, pack));
        }
    }

    @Nested
    @DisplayName("QUEST_REWARD_TYPE handler")
    class QuestRewardTypeHandler {

        @DisplayName("returns false for non-matching content pack")
        @Test
        void processContentPack_nonMatchingPack_returnsFalse() {
            StatisticContentPack wrongPack = new StatisticContentPack(STUB_EXPANSION);
            assertFalse(ContentHandlerType.QUEST_REWARD_TYPE.getContentHandler().processContentPack(mcRPG, wrongPack));
        }

        @DisplayName("returns true for matching QuestRewardTypeContentPack")
        @Test
        void processContentPack_matchingPack_returnsTrue() {
            QuestRewardTypeContentPack pack = new QuestRewardTypeContentPack(STUB_EXPANSION);
            NamespacedKey testKey = new NamespacedKey("test", "handler_test_reward");
            pack.addContent(new MockQuestRewardType(testKey, STUB_EXPANSION.getExpansionKey()));
            assertTrue(ContentHandlerType.QUEST_REWARD_TYPE.getContentHandler().processContentPack(mcRPG, pack));
        }
    }

    @Nested
    @DisplayName("QUEST_SOURCE handler")
    class QuestSourceHandler {

        @DisplayName("returns false for non-matching content pack")
        @Test
        void processContentPack_nonMatchingPack_returnsFalse() {
            StatisticContentPack wrongPack = new StatisticContentPack(STUB_EXPANSION);
            assertFalse(ContentHandlerType.QUEST_SOURCE.getContentHandler().processContentPack(mcRPG, wrongPack));
        }

        @DisplayName("returns true for matching empty QuestSourceContentPack")
        @Test
        void processContentPack_matchingEmptyPack_returnsTrue() {
            QuestSourceContentPack pack = new QuestSourceContentPack(STUB_EXPANSION);
            assertTrue(ContentHandlerType.QUEST_SOURCE.getContentHandler().processContentPack(mcRPG, pack));
        }
    }

    @Nested
    @DisplayName("QUEST_RARITY handler")
    class QuestRarityHandler {

        @DisplayName("returns false for non-matching content pack")
        @Test
        void processContentPack_nonMatchingPack_returnsFalse() {
            StatisticContentPack wrongPack = new StatisticContentPack(STUB_EXPANSION);
            assertFalse(ContentHandlerType.QUEST_RARITY.getContentHandler().processContentPack(mcRPG, wrongPack));
        }

        @DisplayName("returns true for matching QuestRarityContentPack")
        @Test
        void processContentPack_matchingPack_returnsTrue() {
            QuestRarityContentPack pack = new QuestRarityContentPack(STUB_EXPANSION);
            NamespacedKey testKey = new NamespacedKey("test", "handler_test_rarity");
            QuestRarity rarity = new QuestRarity(testKey, 50, 1.0, 1.0, STUB_EXPANSION.getExpansionKey());
            pack.addContent(rarity);
            assertTrue(ContentHandlerType.QUEST_RARITY.getContentHandler().processContentPack(mcRPG, pack));
        }
    }

    @Nested
    @DisplayName("QUEST_SCOPE_PROVIDER handler")
    class QuestScopeProviderHandler {

        @DisplayName("returns false for non-matching content pack")
        @Test
        void processContentPack_nonMatchingPack_returnsFalse() {
            StatisticContentPack wrongPack = new StatisticContentPack(STUB_EXPANSION);
            assertFalse(ContentHandlerType.QUEST_SCOPE_PROVIDER.getContentHandler().processContentPack(mcRPG, wrongPack));
        }

        @DisplayName("returns true for matching empty QuestScopeProviderContentPack")
        @Test
        void processContentPack_matchingEmptyPack_returnsTrue() {
            QuestScopeProviderContentPack pack = new QuestScopeProviderContentPack(STUB_EXPANSION);
            assertTrue(ContentHandlerType.QUEST_SCOPE_PROVIDER.getContentHandler().processContentPack(mcRPG, pack));
        }
    }

    @Nested
    @DisplayName("QUEST_TEMPLATE handler")
    class QuestTemplateHandler {

        @DisplayName("returns false for non-matching content pack")
        @Test
        void processContentPack_nonMatchingPack_returnsFalse() {
            StatisticContentPack wrongPack = new StatisticContentPack(STUB_EXPANSION);
            assertFalse(ContentHandlerType.QUEST_TEMPLATE.getContentHandler().processContentPack(mcRPG, wrongPack));
        }

        @DisplayName("returns true for matching QuestTemplateContentPack")
        @Test
        void processContentPack_matchingPack_returnsTrue() {
            QuestTemplateContentPack pack = new QuestTemplateContentPack(STUB_EXPANSION);
            assertTrue(ContentHandlerType.QUEST_TEMPLATE.getContentHandler().processContentPack(mcRPG, pack));
        }
    }

    @Nested
    @DisplayName("REWARD_DISTRIBUTION_TYPE handler")
    class RewardDistributionTypeHandler {

        @DisplayName("returns false for non-matching content pack")
        @Test
        void processContentPack_nonMatchingPack_returnsFalse() {
            StatisticContentPack wrongPack = new StatisticContentPack(STUB_EXPANSION);
            assertFalse(ContentHandlerType.REWARD_DISTRIBUTION_TYPE.getContentHandler().processContentPack(mcRPG, wrongPack));
        }

        @DisplayName("returns true for matching empty RewardDistributionTypeContentPack")
        @Test
        void processContentPack_matchingEmptyPack_returnsTrue() {
            RewardDistributionTypeContentPack pack = new RewardDistributionTypeContentPack(STUB_EXPANSION);
            assertTrue(ContentHandlerType.REWARD_DISTRIBUTION_TYPE.getContentHandler().processContentPack(mcRPG, pack));
        }
    }

    @Nested
    @DisplayName("SCOPED_BOARD_ADAPTER handler")
    class ScopedBoardAdapterHandler {

        @DisplayName("returns false for non-matching content pack")
        @Test
        void processContentPack_nonMatchingPack_returnsFalse() {
            StatisticContentPack wrongPack = new StatisticContentPack(STUB_EXPANSION);
            assertFalse(ContentHandlerType.SCOPED_BOARD_ADAPTER.getContentHandler().processContentPack(mcRPG, wrongPack));
        }

        @DisplayName("returns true for matching empty ScopedBoardAdapterContentPack")
        @Test
        void processContentPack_matchingEmptyPack_returnsTrue() {
            ScopedBoardAdapterContentPack pack = new ScopedBoardAdapterContentPack(STUB_EXPANSION);
            assertTrue(ContentHandlerType.SCOPED_BOARD_ADAPTER.getContentHandler().processContentPack(mcRPG, pack));
        }
    }

    @Nested
    @DisplayName("TEMPLATE_CONDITION handler")
    class TemplateConditionHandler {

        @DisplayName("returns false for non-matching content pack")
        @Test
        void processContentPack_nonMatchingPack_returnsFalse() {
            StatisticContentPack wrongPack = new StatisticContentPack(STUB_EXPANSION);
            assertFalse(ContentHandlerType.TEMPLATE_CONDITION.getContentHandler().processContentPack(mcRPG, wrongPack));
        }

        @DisplayName("returns true for matching empty TemplateConditionContentPack")
        @Test
        void processContentPack_matchingEmptyPack_returnsTrue() {
            TemplateConditionContentPack pack = new TemplateConditionContentPack(STUB_EXPANSION);
            assertTrue(ContentHandlerType.TEMPLATE_CONDITION.getContentHandler().processContentPack(mcRPG, pack));
        }
    }

    @Nested
    @DisplayName("STATISTIC handler")
    class StatisticHandler {

        @DisplayName("returns false for non-matching content pack")
        @Test
        void processContentPack_nonMatchingPack_returnsFalse() {
            SkillContentPack wrongPack = new SkillContentPack(STUB_EXPANSION);
            assertFalse(ContentHandlerType.STATISTIC.getContentHandler().processContentPack(mcRPG, wrongPack));
        }

        @DisplayName("returns true for matching StatisticContentPack")
        @Test
        void processContentPack_matchingPack_returnsTrue() {
            StatisticContentPack pack = new StatisticContentPack(STUB_EXPANSION);
            NamespacedKey testKey = new NamespacedKey("test", "handler_test_statistic");
            SimpleStatistic stat = new SimpleStatistic(testKey, StatisticType.INT, 0, "Test Stat", "A test statistic");
            pack.addContent(new StatisticContent(stat, STUB_EXPANSION.getExpansionKey()));
            assertTrue(ContentHandlerType.STATISTIC.getContentHandler().processContentPack(mcRPG, pack));
        }

        @DisplayName("registers statistic into StatisticRegistry")
        @Test
        void processContentPack_registersStatistic() {
            StatisticContentPack pack = new StatisticContentPack(STUB_EXPANSION);
            NamespacedKey testKey = new NamespacedKey("test", "handler_verify_stat");
            SimpleStatistic stat = new SimpleStatistic(testKey, StatisticType.LONG, 0L, "Verify Stat", "Verify statistic");
            pack.addContent(new StatisticContent(stat, STUB_EXPANSION.getExpansionKey()));

            ContentHandlerType.STATISTIC.getContentHandler().processContentPack(mcRPG, pack);

            assertTrue(mcRPG.registryAccess().registry(RegistryKey.STATISTIC).registered(stat));
        }
    }

    @Nested
    @DisplayName("PLAYER_STAT handler")
    class PlayerStatHandler {

        @DisplayName("returns false for non-matching content pack")
        @Test
        void processContentPack_nonMatchingPack_returnsFalse() {
            StatisticContentPack wrongPack = new StatisticContentPack(STUB_EXPANSION);
            assertFalse(ContentHandlerType.PLAYER_STAT.getContentHandler().processContentPack(mcRPG, wrongPack));
        }

        @DisplayName("returns true for matching PlayerStatContentPack")
        @Test
        void processContentPack_matchingPack_returnsTrue() {
            PlayerStatContentPack pack = new PlayerStatContentPack(STUB_EXPANSION);
            NamespacedKey testKey = new NamespacedKey("test", "handler_test_player_stat");
            ResourcePoolPlayerStat stat = new ResourcePoolPlayerStat(testKey, "Test Stat", "T", 100.0, 1.0);
            pack.addContent(stat);
            assertTrue(ContentHandlerType.PLAYER_STAT.getContentHandler().processContentPack(mcRPG, pack));
        }

        @DisplayName("registers stat into PlayerStatRegistry")
        @Test
        void processContentPack_registersPlayerStat() {
            PlayerStatContentPack pack = new PlayerStatContentPack(STUB_EXPANSION);
            NamespacedKey testKey = new NamespacedKey("test", "handler_verify_player_stat");
            ResourcePoolPlayerStat stat = new ResourcePoolPlayerStat(testKey, "Verify Stat", "V", 50.0, 0.5);
            pack.addContent(stat);

            ContentHandlerType.PLAYER_STAT.getContentHandler().processContentPack(mcRPG, pack);

            assertTrue(mcRPG.registryAccess().registry(us.eunoians.mcrpg.registry.McRPGRegistryKey.PLAYER_STAT).registered(stat));
        }
    }

    @Nested
    @DisplayName("Cross-handler type safety")
    class CrossHandlerTypeSafety {

        @DisplayName("every handler returns false for a StatisticContentPack except STATISTIC")
        @ParameterizedTest
        @EnumSource(value = ContentHandlerType.class, mode = EnumSource.Mode.EXCLUDE, names = "STATISTIC")
        void handler_returnsFalse_forStatisticContentPack(ContentHandlerType type) {
            StatisticContentPack wrongPack = new StatisticContentPack(STUB_EXPANSION);
            assertFalse(type.getContentHandler().processContentPack(mcRPG, wrongPack));
        }

        @DisplayName("every handler returns false for a SkillContentPack except SKILL")
        @ParameterizedTest
        @EnumSource(value = ContentHandlerType.class, mode = EnumSource.Mode.EXCLUDE, names = "SKILL")
        void handler_returnsFalse_forSkillContentPack(ContentHandlerType type) {
            SkillContentPack wrongPack = new SkillContentPack(STUB_EXPANSION);
            assertFalse(type.getContentHandler().processContentPack(mcRPG, wrongPack));
        }
    }
}
