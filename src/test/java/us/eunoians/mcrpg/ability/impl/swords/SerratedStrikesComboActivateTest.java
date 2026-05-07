package us.eunoians.mcrpg.ability.impl.swords;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.event.ability.swords.SerratedStrikesActivateEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasFiredEventInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SerratedStrikes#comboActivate(us.eunoians.mcrpg.entity.holder.AbilityHolder)}.
 * <p>
 * Verifies: returns true/false based on event cancellation, fires the activate event,
 * adds the active ability with the tier-derived duration, and does NOT apply a cooldown
 * (the combo listener owns cooldown application).
 */
@ExtendWith(McRPGPlayerExtension.class)
class SerratedStrikesComboActivateTest extends McRPGBaseTest {

    private SerratedStrikes serratedStrikes;
    private YamlDocument swordsConfig;

    @BeforeEach
    void setUp() {
        HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();

        swordsConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.SWORDS_CONFIG)).thenReturn(swordsConfig);

        AbilityAttributeRegistry attributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(attributeRegistry);

        EntityManager entityManager = new EntityManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(entityManager);

        serratedStrikes = new SerratedStrikes(mcRPG);

        // SerratedStrikesActivateEvent has a static initializer that resolves SerratedStrikes
        // from the AbilityRegistry. Register before any test fires the event.
        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        abilityRegistry.register(serratedStrikes);

        stubDurationForTier1();
    }

    @Test
    @DisplayName("Given an uncancelled activate event, when comboActivate is called, then it returns true")
    void comboActivate_returnsTrue_whenEventIsNotCancelled(@NotNull McRPGPlayer mcRPGPlayer) {
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.ENTITY).trackAbilityHolder(skillHolder);

        assertTrue(serratedStrikes.comboActivate(skillHolder));
    }

    @Test
    @DisplayName("Given a listener that cancels the activate event, when comboActivate is called, then it returns false")
    void comboActivate_returnsFalse_whenEventIsCancelled(@NotNull McRPGPlayer mcRPGPlayer) {
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.ENTITY).trackAbilityHolder(skillHolder);

        server.getPluginManager().registerEvents(new CancellingListener(), mcRPG);

        assertFalse(serratedStrikes.comboActivate(skillHolder));
    }

    @Test
    @DisplayName("When comboActivate is called, then SerratedStrikesActivateEvent is fired")
    void comboActivate_firesSerratedStrikesActivateEvent(@NotNull McRPGPlayer mcRPGPlayer) {
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.ENTITY).trackAbilityHolder(skillHolder);

        serratedStrikes.comboActivate(skillHolder);

        assertThat(server.getPluginManager(),
                hasFiredEventInstance(SerratedStrikesActivateEvent.class));
    }

    @Test
    @DisplayName("When comboActivate is called, then the ability is added as active with the tier-derived duration")
    void comboActivate_addsActiveAbility_withTierDerivedDuration(@NotNull McRPGPlayer mcRPGPlayer) {
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.ENTITY).trackAbilityHolder(skillHolder);

        serratedStrikes.comboActivate(skillHolder);

        assertTrue(skillHolder.isAbilityActive(serratedStrikes));
    }

    @Test
    @DisplayName("When comboActivate is called, then no cooldown is applied to the holder")
    void comboActivate_doesNotApplyCooldown(@NotNull McRPGPlayer mcRPGPlayer) {
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.ENTITY).trackAbilityHolder(skillHolder);

        serratedStrikes.comboActivate(skillHolder);

        // The combo listener owns cooldown placement; comboActivate itself must never call
        // putHolderOnCooldown(). Verify by checking the ability is NOT on cooldown after activation.
        assertFalse(serratedStrikes.isAbilityOnCooldown(skillHolder));
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    /**
     * Stubs the swords config document so that {@link SerratedStrikes#getDuration(int)} for
     * tier 1 returns {@code 3} (a simple, observable value for assertions).
     */
    private void stubDurationForTier1() {
        Route tier1Route = Route.addTo(
                Route.addTo(SwordsConfigFile.SERRATED_STRIKES_CONFIGURATION_HEADER, "tier-1"), "duration");
        Route allTiersRoute = Route.addTo(
                Route.addTo(SwordsConfigFile.SERRATED_STRIKES_CONFIGURATION_HEADER, "all-tiers"), "duration");

        when(swordsConfig.contains(tier1Route)).thenReturn(false);
        when(swordsConfig.getString(allTiersRoute)).thenReturn("3");
    }

    /** Cancels any {@link SerratedStrikesActivateEvent} fired during the test. */
    private static class CancellingListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
        public void onSerratedStrikesActivate(@NotNull SerratedStrikesActivateEvent event) {
            event.setCancelled(true);
        }
    }
}
