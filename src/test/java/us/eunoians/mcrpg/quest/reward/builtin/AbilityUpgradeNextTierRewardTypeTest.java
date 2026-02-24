package us.eunoians.mcrpg.quest.reward.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.ArgumentCaptor;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityUpgradeQuestAttribute;
import us.eunoians.mcrpg.ability.impl.type.SkillAbility;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbilityUpgradeNextTierRewardTypeTest extends McRPGBaseTest {

    private AbilityRegistry abilityRegistry;
    private QuestManager mockQuestManager;
    private McRPGPlayerManager mockPlayerManager;

    @BeforeEach
    public void setup() {
        RegistryAccess registryAccess = RegistryAccess.registryAccess();

        abilityRegistry = registryAccess.registry(McRPGRegistryKey.ABILITY);
        if (abilityRegistry == null) {
            registryAccess.register(new AbilityRegistry(mcRPG));
            abilityRegistry = registryAccess.registry(McRPGRegistryKey.ABILITY);
        }

        // Retrieve the QuestManager mock already registered by TestBootstrap so verifications
        // target the same instance the production code looks up from the registry.
        mockQuestManager = registryAccess.registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);

        mockPlayerManager = mock(McRPGPlayerManager.class);
        try {
            registryAccess.registry(RegistryKey.MANAGER).register(mockPlayerManager);
        } catch (Exception ignored) {
            mockPlayerManager = registryAccess.registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
        }
    }

    @DisplayName("Given configured next-tier reward, when granted, then ability tier increments and sanity check is invoked")
    @Test
    public void grant_incrementsTier_andInvokesSanityCheck() {
        PlayerMock player = server.addPlayer();
        UUID playerUUID = player.getUniqueId();

        NamespacedKey abilityKey = NamespacedKey.fromString("mcrpg:test_next_tier");
        TestTierableAbility ability = new TestTierableAbility(mcRPG, abilityKey, 5, 2);
        abilityRegistry.register(ability);

        McRPGPlayer mcRPGPlayer = mock(McRPGPlayer.class);
        SkillHolder mockHolder = mock(SkillHolder.class);
        AbilityData abilityData = mock(AbilityData.class);

        when(mockPlayerManager.getPlayer(playerUUID)).thenReturn(Optional.of(mcRPGPlayer));
        when(mcRPGPlayer.asSkillHolder()).thenReturn(mockHolder);
        when(mockHolder.getAbilityData(ability)).thenReturn(Optional.of(abilityData));
        when(abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE))
                .thenReturn(Optional.of(new AbilityUpgradeQuestAttribute(UUID.randomUUID())));

        AbilityUpgradeNextTierRewardType configured = new AbilityUpgradeNextTierRewardType()
                .fromSerializedConfig(Map.of("ability", abilityKey.toString()));
        configured.grant(player);

        ArgumentCaptor<AbilityTierAttribute> tierAttrCaptor = ArgumentCaptor.forClass(AbilityTierAttribute.class);
        verify(abilityData).updateAttribute(tierAttrCaptor.capture(), any(Integer.class));
        assertEquals(3, tierAttrCaptor.getValue().getContent());

        verify(abilityData).addAttribute(any(AbilityUpgradeQuestAttribute.class));
        verify(mockQuestManager).sanityCheckUpgradeQuests(mcRPGPlayer);
    }

    @DisplayName("Given player at max tier, when granted, then it does nothing")
    @Test
    public void grant_atMaxTier_noop() {
        PlayerMock player = server.addPlayer();
        UUID playerUUID = player.getUniqueId();

        NamespacedKey abilityKey = NamespacedKey.fromString("mcrpg:test_next_tier_max");
        TestTierableAbility ability = new TestTierableAbility(mcRPG, abilityKey, 3, 3);
        abilityRegistry.register(ability);

        McRPGPlayer mcRPGPlayer = mock(McRPGPlayer.class);
        SkillHolder mockHolder = mock(SkillHolder.class);
        AbilityData abilityData = mock(AbilityData.class);

        when(mockPlayerManager.getPlayer(playerUUID)).thenReturn(Optional.of(mcRPGPlayer));
        when(mcRPGPlayer.asSkillHolder()).thenReturn(mockHolder);
        when(mockHolder.getAbilityData(ability)).thenReturn(Optional.of(abilityData));

        AbilityUpgradeNextTierRewardType configured = new AbilityUpgradeNextTierRewardType()
                .fromSerializedConfig(Map.of("ability", abilityKey.toString()));
        configured.grant(player);

        verify(abilityData, never()).updateAttribute(any(), any(Integer.class));
        verify(mockQuestManager, never()).sanityCheckUpgradeQuests(any());
    }

    @DisplayName("Given a SkillAbility below required level, when granted, then it does not upgrade")
    @Test
    public void grant_skillAbility_belowRequiredLevel_noop() {
        PlayerMock player = server.addPlayer();
        UUID playerUUID = player.getUniqueId();

        NamespacedKey skillKey = NamespacedKey.fromString("mcrpg:test_skill");
        NamespacedKey abilityKey = NamespacedKey.fromString("mcrpg:test_next_tier_skill_gated");
        TestSkillTierableAbility ability = new TestSkillTierableAbility(mcRPG, abilityKey, skillKey, 5, 1, 10);
        abilityRegistry.register(ability);

        McRPGPlayer mcRPGPlayer = mock(McRPGPlayer.class);
        SkillHolder mockHolder = mock(SkillHolder.class);
        SkillHolder.SkillHolderData skillData = mock(SkillHolder.SkillHolderData.class);
        AbilityData abilityData = mock(AbilityData.class);

        when(mockPlayerManager.getPlayer(playerUUID)).thenReturn(Optional.of(mcRPGPlayer));
        when(mcRPGPlayer.asSkillHolder()).thenReturn(mockHolder);
        when(mockHolder.getAbilityData(ability)).thenReturn(Optional.of(abilityData));
        when(mockHolder.getSkillHolderData(eq(skillKey))).thenReturn(Optional.of(skillData));
        when(skillData.getCurrentLevel()).thenReturn(1); // below required unlock level for tier 2

        AbilityUpgradeNextTierRewardType configured = new AbilityUpgradeNextTierRewardType()
                .fromSerializedConfig(Map.of("ability", abilityKey.toString()));
        configured.grant(player);

        verify(abilityData, never()).updateAttribute(any(), any(Integer.class));
        verify(mockQuestManager, never()).sanityCheckUpgradeQuests(any());
    }

    @DisplayName("Given the type, when calling getExpansionKey, then it returns McRPGExpansion key")
    @Test
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        AbilityUpgradeNextTierRewardType type = new AbilityUpgradeNextTierRewardType();
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }

    @DisplayName("Given serialized config, when round-tripping, then ability key is preserved")
    @Test
    public void serializeAndDeserialize_roundTripsCorrectly() {
        NamespacedKey abilityKey = NamespacedKey.fromString("mcrpg:round_trip_ability");
        AbilityUpgradeNextTierRewardType configured = new AbilityUpgradeNextTierRewardType()
                .fromSerializedConfig(Map.of("ability", abilityKey.toString()));
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals(abilityKey.toString(), serialized.get("ability"));
    }

    @DisplayName("Given mocked Section config, when parseConfig is called, then it parses ability key")
    @Test
    public void parseConfig_parsesAbilityKey() {
        Section section = mock(Section.class);
        when(section.getString("ability")).thenReturn("mcrpg:parse_config_ability");

        AbilityUpgradeNextTierRewardType parsed = new AbilityUpgradeNextTierRewardType().parseConfig(section);
        assertEquals("mcrpg:parse_config_ability", parsed.serializeConfig().get("ability"));
    }

    private static final class TestTierableAbility implements TierableAbility {
        private final McRPG plugin;
        private final NamespacedKey key;
        private final int maxTier;
        private final int currentTier;

        private TestTierableAbility(McRPG plugin, NamespacedKey key, int maxTier, int currentTier) {
            this.plugin = plugin;
            this.key = key;
            this.maxTier = maxTier;
            this.currentTier = currentTier;
        }

        @Override
        public int getMaxTier() {
            return maxTier;
        }

        @Override
        public int getUnlockLevelForTier(int tier) {
            return 1;
        }

        @Override
        public int getUpgradeCostForTier(int tier) {
            return 0;
        }

        @Override
        public int getCurrentAbilityTier(@NotNull AbilityHolder abilityHolder) {
            return currentTier;
        }

        @Override
        public @NotNull Plugin getPlugin() {
            return plugin;
        }

        @Override
        public @NotNull NamespacedKey getAbilityKey() {
            return key;
        }

        @Override
        public @NotNull Set<NamespacedKey> getApplicableAttributes() {
            return Set.of();
        }

        @Override
        public @NotNull String getDatabaseName() {
            return key.getKey();
        }

        @Override
        public @NotNull String getName(@NotNull McRPGPlayer player) {
            return "TestTierableAbility";
        }

        @Override
        public @NotNull String getName() {
            return "TestTierableAbility";
        }

        @Override
        public Component getDisplayName(@NotNull McRPGPlayer player) {
            return Component.text("TestTierableAbility");
        }

        @Override
        public Component getDisplayName() {
            return Component.text("TestTierableAbility");
        }

        @Override
        public AbilityItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        }

        @Override
        public boolean isAbilityEnabled() {
            return true;
        }

        @Override
        public boolean isPassive() {
            return true;
        }

        @Override
        public @NotNull Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }
    }

    private static final class TestSkillTierableAbility implements TierableAbility, SkillAbility {
        private final McRPG plugin;
        private final NamespacedKey key;
        private final NamespacedKey skillKey;
        private final int maxTier;
        private final int currentTier;
        private final int requiredLevelForTier2;

        private TestSkillTierableAbility(McRPG plugin,
                                         NamespacedKey key,
                                         NamespacedKey skillKey,
                                         int maxTier,
                                         int currentTier,
                                         int requiredLevelForTier2) {
            this.plugin = plugin;
            this.key = key;
            this.skillKey = skillKey;
            this.maxTier = maxTier;
            this.currentTier = currentTier;
            this.requiredLevelForTier2 = requiredLevelForTier2;
        }

        @Override
        public @NotNull NamespacedKey getSkillKey() {
            return skillKey;
        }

        @Override
        public int getMaxTier() {
            return maxTier;
        }

        @Override
        public int getUnlockLevelForTier(int tier) {
            if (tier == 2) {
                return requiredLevelForTier2;
            }
            return 1;
        }

        @Override
        public int getUpgradeCostForTier(int tier) {
            return 0;
        }

        @Override
        public int getCurrentAbilityTier(@NotNull AbilityHolder abilityHolder) {
            return currentTier;
        }

        @Override
        public @NotNull Plugin getPlugin() {
            return plugin;
        }

        @Override
        public @NotNull NamespacedKey getAbilityKey() {
            return key;
        }

        @Override
        public @NotNull Set<NamespacedKey> getApplicableAttributes() {
            return Set.of();
        }

        @Override
        public @NotNull String getDatabaseName() {
            return key.getKey();
        }

        @Override
        public @NotNull String getName(@NotNull McRPGPlayer player) {
            return "TestSkillTierableAbility";
        }

        @Override
        public @NotNull String getName() {
            return "TestSkillTierableAbility";
        }

        @Override
        public Component getDisplayName(@NotNull McRPGPlayer player) {
            return Component.text("TestSkillTierableAbility");
        }

        @Override
        public Component getDisplayName() {
            return Component.text("TestSkillTierableAbility");
        }

        @Override
        public AbilityItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        }

        @Override
        public boolean isAbilityEnabled() {
            return true;
        }

        @Override
        public boolean isPassive() {
            return true;
        }

        @Override
        public @NotNull Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }
    }
}

