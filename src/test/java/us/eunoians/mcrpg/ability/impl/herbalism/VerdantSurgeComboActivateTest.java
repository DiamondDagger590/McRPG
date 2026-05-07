package us.eunoians.mcrpg.ability.impl.herbalism;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.skill.HerbalismConfigFile;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.event.ability.herbalism.VerdantSurgeActivateEvent;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.world.WorldManager;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasFiredEventInstance;

/**
 * Integration tests for {@link VerdantSurge#comboActivate(us.eunoians.mcrpg.entity.holder.AbilityHolder)}.
 * <p>
 * Verifies that the combo activation path fires {@link VerdantSurgeActivateEvent}, respects cancellation,
 * and does not apply cooldown itself (the combo listener handles cooldown).
 */
@ExtendWith(McRPGPlayerExtension.class)
class VerdantSurgeComboActivateTest extends McRPGBaseTest {

    private VerdantSurge verdantSurge;
    private YamlDocument herbalismConfig;

    @BeforeEach
    void setUp() {
        server.getPluginManager().clearEvents();

        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);

        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);

        herbalismConfig = mock(YamlDocument.class);
        YamlDocument mainConfig = mock(YamlDocument.class);

        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.HERBALISM_CONFIG)).thenReturn(herbalismConfig);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);

        when(herbalismConfig.getInt(HerbalismConfigFile.VERDANT_SURGE_AMOUNT_OF_TIERS)).thenReturn(5);
        when(herbalismConfig.contains(any(Route.class))).thenReturn(false);
        when(herbalismConfig.getString(any(Route.class))).thenReturn("3");
        when(herbalismConfig.getString(any(Route.class), any())).thenReturn("3");
        when(herbalismConfig.getStringList(any(Route.class))).thenReturn(List.of());
        when(mainConfig.getInt(MainConfigFile.MANA_MINIMUM_ABILITY_COST, 5)).thenReturn(1);
        when(mainConfig.getStringList(MainConfigFile.DISABLED_WORLDS)).thenReturn(List.of());
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        verdantSurge = new VerdantSurge(mcRPG);
        abilityRegistry.register(verdantSurge);

        EntityManager entityManager = new EntityManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(entityManager);

        WorldManager worldManager = spy(new WorldManager(mcRPG));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(worldManager);
    }

    @Test
    @DisplayName("Given a valid holder, when comboActivate() is called and the event is not cancelled, then it returns true")
    void comboActivate_returnsTrue_whenEventIsNotCancelled(@NotNull McRPGPlayer mcRPGPlayer) {
        setUpPlayer(mcRPGPlayer);

        boolean result = verdantSurge.comboActivate(mcRPGPlayer.asSkillHolder());

        assertTrue(result);
    }

    @Test
    @DisplayName("Given a cancelling listener, when comboActivate() is called, then it returns false")
    void comboActivate_returnsFalse_whenEventIsCancelled(@NotNull McRPGPlayer mcRPGPlayer) {
        setUpPlayer(mcRPGPlayer);
        server.getPluginManager().registerEvents(new VerdantSurgeCanceller(), mcRPG);

        boolean result = verdantSurge.comboActivate(mcRPGPlayer.asSkillHolder());

        assertFalse(result);
    }

    @Test
    @DisplayName("Given a valid holder, when comboActivate() is called, then VerdantSurgeActivateEvent is fired")
    void comboActivate_firesVerdantSurgeActivateEvent(@NotNull McRPGPlayer mcRPGPlayer) {
        setUpPlayer(mcRPGPlayer);

        verdantSurge.comboActivate(mcRPGPlayer.asSkillHolder());

        assertThat(server.getPluginManager(), hasFiredEventInstance(VerdantSurgeActivateEvent.class));
    }

    @Test
    @DisplayName("Given a valid holder after comboActivate() succeeds, when isAbilityOnCooldown() is checked, then it is false (listener handles cooldown)")
    void comboActivate_doesNotApplyCooldown(@NotNull McRPGPlayer mcRPGPlayer) {
        setUpPlayer(mcRPGPlayer);

        verdantSurge.comboActivate(mcRPGPlayer.asSkillHolder());

        assertFalse(verdantSurge.isAbilityOnCooldown(mcRPGPlayer.asSkillHolder()),
                "comboActivate() must not apply cooldown — that is OnComboCompleteListener's responsibility");
    }

    @Test
    @DisplayName("Given an AbilityHolder whose UUID has no McRPGPlayer, when comboActivate() is called, then it returns false")
    void comboActivate_returnsFalse_whenPlayerNotFound() {
        SkillHolder orphanHolder = mock(SkillHolder.class);
        when(orphanHolder.getUUID()).thenReturn(UUID.randomUUID());
        when(orphanHolder.getAbilityData(any(Ability.class))).thenReturn(Optional.empty());

        boolean result = verdantSurge.comboActivate(orphanHolder);

        assertFalse(result);
    }

    /**
     * Sets up the player in the entity manager, registers the ability on the holder,
     * and adds the player to MockBukkit so the pulse task can resolve the Bukkit Player.
     *
     * @param mcRPGPlayer The player to configure.
     */
    private void setUpPlayer(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        EntityManager entityManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.ENTITY);
        entityManager.trackAbilityHolder(mcRPGPlayer.asSkillHolder());
        mcRPGPlayer.asSkillHolder().addAvailableAbility(verdantSurge);
    }

    /** Cancels every {@link VerdantSurgeActivateEvent}. */
    private static class VerdantSurgeCanceller implements Listener {
        @org.bukkit.event.EventHandler
        public void onActivate(@NotNull VerdantSurgeActivateEvent event) {
            event.setCancelled(true);
        }
    }
}
