package us.eunoians.mcrpg.util.filter.ability;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityUnlockedAttribute;
import us.eunoians.mcrpg.ability.impl.type.PassiveAbility;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.ability.stub.StubAbilityBase;
import us.eunoians.mcrpg.ability.stub.StubActiveUnlockableAbility;
import us.eunoians.mcrpg.ability.stub.StubInnateAbility;
import us.eunoians.mcrpg.ability.stub.StubPassiveUnlockableAbility;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.skill.HerbalismConfigFile;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.impl.herbalism.Herbalism;
import us.eunoians.mcrpg.world.WorldManager;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
class AbilityFilterTest extends McRPGBaseTest {

    private YamlDocument mainConfig;

    @BeforeEach
    void setup() {
        server.getPluginManager().clearEvents();

        SkillRegistry skillRegistry = new SkillRegistry();
        RegistryAccess.registryAccess().register(skillRegistry);

        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);

        YamlDocument herbalismConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.HERBALISM_CONFIG)).thenReturn(herbalismConfig);
        when(herbalismConfig.getString(HerbalismConfigFile.LEVEL_UP_EQUATION)).thenReturn("5");

        mainConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);

        Herbalism herbalism = new Herbalism(mcRPG);
        skillRegistry.register(herbalism);

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);

        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);

        EntityManager entityManager = new EntityManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(entityManager);

        when(mainConfig.getStringList(MainConfigFile.DISABLED_WORLDS)).thenReturn(List.of(""));
        WorldManager worldManager = spy(new WorldManager(mcRPG));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(worldManager);

        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_PASSIVE_LOADOUT_SIZE)).thenReturn(2);
    }

    @Nested
    @DisplayName("ActiveAbilityFilter")
    class ActiveAbilityFilterTests {

        private ActiveAbilityFilter filter;

        @BeforeEach
        void setUp() {
            filter = new ActiveAbilityFilter();
        }

        @DisplayName("retains unlockable non-passive abilities")
        @Test
        void filter_retainsActiveAbilities(@NotNull McRPGPlayer mcRPGPlayer) {
            Ability active = createStubActiveUnlockable("active_one");
            Ability passive = createStubPassiveUnlockable("passive_one");
            Ability innate = createStubInnate("innate_one");

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(active, passive, innate));

            assertEquals(1, result.size());
            assertTrue(result.contains(active));
        }

        @DisplayName("returns empty when no active abilities present")
        @Test
        void filter_returnsEmpty_whenNoActiveAbilities(@NotNull McRPGPlayer mcRPGPlayer) {
            Ability passive = createStubPassiveUnlockable("passive_one");

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(passive));

            assertTrue(result.isEmpty());
        }

        @DisplayName("returns empty for empty input")
        @Test
        void filter_returnsEmpty_whenInputEmpty(@NotNull McRPGPlayer mcRPGPlayer) {
            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("PassiveAbilityFilter")
    class PassiveAbilityFilterTests {

        private PassiveAbilityFilter filter;

        @BeforeEach
        void setUp() {
            filter = new PassiveAbilityFilter();
        }

        @DisplayName("retains unlockable passive abilities")
        @Test
        void filter_retainsPassiveAbilities(@NotNull McRPGPlayer mcRPGPlayer) {
            Ability active = createStubActiveUnlockable("active_one");
            Ability passive = createStubPassiveUnlockable("passive_one");
            Ability innate = createStubInnate("innate_one");

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(active, passive, innate));

            assertEquals(1, result.size());
            assertTrue(result.contains(passive));
        }

        @DisplayName("excludes non-unlockable passive abilities")
        @Test
        void filter_excludesInnatePassive(@NotNull McRPGPlayer mcRPGPlayer) {
            Ability innate = createStubInnate("innate_one");

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(innate));

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("UnlockableAbilityFilter")
    class UnlockableAbilityFilterTests {

        private UnlockableAbilityFilter filter;

        @BeforeEach
        void setUp() {
            filter = new UnlockableAbilityFilter();
        }

        @DisplayName("retains all unlockable abilities regardless of active/passive")
        @Test
        void filter_retainsAllUnlockable(@NotNull McRPGPlayer mcRPGPlayer) {
            Ability active = createStubActiveUnlockable("active_one");
            Ability passive = createStubPassiveUnlockable("passive_one");
            Ability innate = createStubInnate("innate_one");

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(active, passive, innate));

            assertEquals(2, result.size());
            assertTrue(result.contains(active));
            assertTrue(result.contains(passive));
            assertFalse(result.contains(innate));
        }

        @DisplayName("returns empty when no unlockable abilities present")
        @Test
        void filter_returnsEmpty_whenNoUnlockableAbilities(@NotNull McRPGPlayer mcRPGPlayer) {
            Ability innate = createStubInnate("innate_one");

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(innate));

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("InnateAbilityFilter")
    class InnateAbilityFilterTests {

        private InnateAbilityFilter filter;

        @BeforeEach
        void setUp() {
            filter = new InnateAbilityFilter();
        }

        @DisplayName("retains abilities without unlock attribute")
        @Test
        void filter_retainsInnateAbilities(@NotNull McRPGPlayer mcRPGPlayer) {
            Ability innate = createStubInnate("innate_one");
            SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
            skillHolder.addAvailableAbility(innate.getAbilityKey());
            skillHolder.addAbilityData(new AbilityData(innate.getAbilityKey()));

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(innate));

            assertEquals(1, result.size());
            assertTrue(result.contains(innate));
        }

        @DisplayName("excludes abilities with unlock attribute")
        @Test
        void filter_excludesUnlockableAbilities(@NotNull McRPGPlayer mcRPGPlayer) {
            Ability unlockable = createStubPassiveUnlockable("passive_one");
            SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
            skillHolder.addAvailableAbility(unlockable.getAbilityKey());
            AbilityData data = new AbilityData(
                    unlockable.getAbilityKey(),
                    new AbilityUnlockedAttribute(false)
            );
            skillHolder.addAbilityData(data);

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(unlockable));

            assertTrue(result.isEmpty());
        }

        @DisplayName("retains innate ability even when no explicit data was added")
        @Test
        void filter_retainsInnateAbility_whenNoExplicitDataAdded(@NotNull McRPGPlayer mcRPGPlayer) {
            Ability innate = createStubInnate("innate_auto_data");

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(innate));

            assertEquals(1, result.size());
            assertTrue(result.contains(innate));
        }

        @DisplayName("excludes abilities not registered in the ability registry")
        @Test
        void filter_excludesUnregisteredAbilities(@NotNull McRPGPlayer mcRPGPlayer) {
            StubInnateAbility unregistered = new StubInnateAbility(mcRPG, "unregistered_innate");

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(unregistered));

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("AbilityUpgradeFilter")
    class AbilityUpgradeFilterTests {

        private AbilityUpgradeFilter filter;

        @BeforeEach
        void setUp() {
            filter = new AbilityUpgradeFilter();
        }

        @DisplayName("excludes non-tierable abilities")
        @Test
        void filter_excludesNonTierable(@NotNull McRPGPlayer mcRPGPlayer) {
            Ability nonTierable = createStubActiveUnlockable("non_tierable");

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(nonTierable));

            assertTrue(result.isEmpty());
        }

        @DisplayName("excludes abilities without ability data")
        @Test
        void filter_excludesAbilitiesWithoutData(@NotNull McRPGPlayer mcRPGPlayer) {
            Ability tierable = createStubTierableUnlockable("tierable_no_data", 5);
            AbilityRegistry abilityRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY);
            abilityRegistry.register(tierable);

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(tierable));

            assertTrue(result.isEmpty());
        }

        @DisplayName("excludes abilities at max tier")
        @Test
        void filter_excludesAbilitiesAtMaxTier(@NotNull McRPGPlayer mcRPGPlayer) {
            StubTierableUnlockableAbility tierable = createStubTierableUnlockable("maxed_out", 3);
            AbilityRegistry abilityRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY);
            abilityRegistry.register(tierable);

            SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
            skillHolder.addAvailableAbility(tierable.getAbilityKey());
            AbilityData data = new AbilityData(
                    tierable.getAbilityKey(),
                    new AbilityTierAttribute(3),
                    new AbilityUnlockedAttribute(true)
            );
            skillHolder.addAbilityData(data);

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(tierable));

            assertTrue(result.isEmpty());
        }

        @DisplayName("retains abilities below max tier that are unlocked")
        @Test
        void filter_retainsUpgradeableAbilities(@NotNull McRPGPlayer mcRPGPlayer) {
            StubTierableUnlockableAbility tierable = createStubTierableUnlockable("upgradeable", 5);
            AbilityRegistry abilityRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY);
            abilityRegistry.register(tierable);

            SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
            skillHolder.addAvailableAbility(tierable.getAbilityKey());
            AbilityData data = new AbilityData(
                    tierable.getAbilityKey(),
                    new AbilityTierAttribute(2),
                    new AbilityUnlockedAttribute(true)
            );
            skillHolder.addAbilityData(data);

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(tierable));

            assertEquals(1, result.size());
            assertTrue(result.contains(tierable));
        }

        @DisplayName("excludes abilities that are not unlocked")
        @Test
        void filter_excludesLockedAbilities(@NotNull McRPGPlayer mcRPGPlayer) {
            StubTierableUnlockableAbility tierable = createStubTierableUnlockable("locked_tierable", 5);
            AbilityRegistry abilityRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY);
            abilityRegistry.register(tierable);

            SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
            skillHolder.addAvailableAbility(tierable.getAbilityKey());
            AbilityData data = new AbilityData(
                    tierable.getAbilityKey(),
                    new AbilityTierAttribute(1),
                    new AbilityUnlockedAttribute(false)
            );
            skillHolder.addAbilityData(data);

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(tierable));

            assertTrue(result.isEmpty());
        }
    }

    private StubActiveUnlockableAbility createStubActiveUnlockable(@NotNull String name) {
        StubActiveUnlockableAbility ability = new StubActiveUnlockableAbility(mcRPG, name);
        AbilityRegistry abilityRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY);
        abilityRegistry.register(ability);
        return ability;
    }

    private StubPassiveUnlockableAbility createStubPassiveUnlockable(@NotNull String name) {
        StubPassiveUnlockableAbility ability = new StubPassiveUnlockableAbility(mcRPG, name);
        AbilityRegistry abilityRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY);
        abilityRegistry.register(ability);
        return ability;
    }

    private StubInnateAbility createStubInnate(@NotNull String name) {
        StubInnateAbility ability = new StubInnateAbility(mcRPG, name);
        AbilityRegistry abilityRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY);
        abilityRegistry.register(ability);
        return ability;
    }

    private StubTierableUnlockableAbility createStubTierableUnlockable(@NotNull String name, int maxTier) {
        return new StubTierableUnlockableAbility(mcRPG, name, maxTier);
    }

    static final class StubTierableUnlockableAbility extends StubAbilityBase implements TierableAbility, UnlockableAbility, PassiveAbility {

        private final int maxTier;

        StubTierableUnlockableAbility(@NotNull McRPG plugin, @NotNull String name, int maxTier) {
            super(plugin, name);
            this.maxTier = maxTier;
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
        public int getCurrentAbilityTier(@NotNull AbilityHolder abilityHolder) {
            return 1;
        }

        @NotNull
        @Override
        public Optional<NamespacedKey> getUpgradeQuestKey(int tier) {
            return Optional.empty();
        }

        @Override
        public int getUnlockLevel() {
            return 1;
        }

        @NotNull
        @Override
        public Set<NamespacedKey> getApplicableAttributes() {
            return Set.of(
                    AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY,
                    AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE,
                    AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY
            );
        }
    }
}
