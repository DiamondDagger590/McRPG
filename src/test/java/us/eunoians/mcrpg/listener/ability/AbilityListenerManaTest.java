package us.eunoians.mcrpg.listener.ability;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.type.ManaAbility;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.event.stat.PlayerStatConsumeEvent;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.stat.McRPGPlayerStat;
import us.eunoians.mcrpg.stat.PlayerStatRegistry;
import us.eunoians.mcrpg.stat.impl.ResourcePoolPlayerStat;
import us.eunoians.mcrpg.stat.instance.PlayerStatInstance;
import us.eunoians.mcrpg.util.McRPGMethods;
import us.eunoians.mcrpg.world.WorldManager;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Integration tests for the mana-gating logic in
 * {@link AbilityListener#activateAbilities(UUID, Event)}.
 * <p>
 * Sets up the minimum infrastructure required (EntityManager, WorldManager, AbilityRegistry,
 * PlayerManager) and uses a {@link StubManaAbility} inner class to isolate the mana path.
 */
class AbilityListenerManaTest extends McRPGBaseTest {

    private static final NamespacedKey MANA_KEY = McRPGPlayerStat.MANA.getKey();
    private static final int MANA_COST = 20;
    private static final double MANA_BASE = 100.0;

    private McRPGPlayer mcRPGPlayer;
    private PlayerMock playerMock;
    private StubManaAbility stubAbility;
    private AbilityListener listener;

    @BeforeEach
    void setUp() {
        org.bukkit.event.HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();

        // ReloadableContentManager must exist before WorldManager
        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);

        // Ability attribute registry is required by the ability/loadout infrastructure
        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);

        // Set up main config mock for WorldManager (empty disabled worlds list)
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        YamlDocument mainConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);
        when(mainConfig.getStringList(MainConfigFile.DISABLED_WORLDS)).thenReturn(List.of());
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_PASSIVE_LOADOUT_SIZE)).thenReturn(2);

        WorldManager worldManager = spy(new WorldManager(mcRPG));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(worldManager);

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        stubAbility = new StubManaAbility(mcRPG, MANA_COST);
        abilityRegistry.register(stubAbility);

        EntityManager entityManager = new EntityManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(entityManager);

        // Register mana BEFORE creating McRPGPlayer so PlayerStatData seeds it.
        // Guard against duplicate registration when another test class in the same JVM
        // has already registered the same key (registry state may survive across test classes).
        PlayerStatRegistry statRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.PLAYER_STAT);
        if (statRegistry.getStat(MANA_KEY).isEmpty()) {
            statRegistry.register(new ResourcePoolPlayerStat(MANA_KEY, "Mana", "\u2746", MANA_BASE, 0));
        }

        McRPGPlayerManager playerManager = new McRPGPlayerManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(playerManager);

        mcRPGPlayer = spy(new McRPGPlayer(UUID.randomUUID(), mcRPG));
        playerManager.addPlayer(mcRPGPlayer);

        // Add player to MockBukkit so Bukkit.getEntity(UUID) resolves correctly for WorldManager
        playerMock = addPlayerToServer(mcRPGPlayer);

        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        entityManager.trackAbilityHolder(skillHolder);
        skillHolder.addAvailableAbility(stubAbility);
        Loadout loadout = new Loadout(mcRPGPlayer.getUUID(), 1, Set.of(stubAbility.getAbilityKey()));
        skillHolder.setLoadout(loadout);

        listener = new AbilityListener() {};
    }

    @Test
    @DisplayName("Given sufficient mana, when activateAbilities is called, then the ability activates and mana is consumed")
    void activateAbilities_consumesMana_whenAbilityActivates() {
        stubAbility.activateReturn = true;
        PlayerStatInstance mana = getMana();
        assertEquals(MANA_BASE, mana.getCurrent());

        listener.activateAbilities(mcRPGPlayer.getUUID(), breakEvent());

        assertEquals(MANA_BASE - MANA_COST, mana.getCurrent());
    }

    @Test
    @DisplayName("Given insufficient mana, when activateAbilities is called, then the ability is skipped and mana is unchanged")
    void activateAbilities_skipsAbility_whenManaInsufficient() {
        PlayerStatInstance mana = getMana();
        mana.consume(MANA_BASE - (MANA_COST - 1)); // leave MANA_COST - 1 remaining
        double manaBefore = mana.getCurrent();

        listener.activateAbilities(mcRPGPlayer.getUUID(), breakEvent());

        assertEquals(manaBefore, mana.getCurrent(), 1e-9);
    }

    @Test
    @DisplayName("Given a listener that cancels the consume event, when activateAbilities is called, then activation is skipped and mana is unchanged")
    void activateAbilities_skipsActivation_whenConsumeEventIsCancelled() {
        PlayerStatInstance mana = getMana();
        double manaBefore = mana.getCurrent();

        // Register a listener that cancels the consume event
        server.getPluginManager().registerEvents(
                (org.bukkit.event.Listener) (Object) new PlayerStatConsumeEventCanceller(), mcRPG);

        listener.activateAbilities(mcRPGPlayer.getUUID(), breakEvent());

        assertEquals(manaBefore, mana.getCurrent(), 1e-9);
    }

    @Test
    @DisplayName("Given an ability that returns false from activateAbility, when activateAbilities is called, then mana is refunded")
    void activateAbilities_refundsMana_whenActivationReturnsFalse() {
        stubAbility.activateReturn = false;
        PlayerStatInstance mana = getMana();
        double manaBefore = mana.getCurrent();

        listener.activateAbilities(mcRPGPlayer.getUUID(), breakEvent());

        // Mana was consumed then immediately restored
        assertEquals(manaBefore, mana.getCurrent(), 1e-9);
    }

    // Helpers

    @NotNull
    private PlayerStatInstance getMana() {
        return mcRPGPlayer.getPlayerStatData().getInstance(MANA_KEY).orElseThrow();
    }

    @NotNull
    private BlockBreakEvent breakEvent() {
        Block block = server.getWorld("world").getBlockAt(0, 0, 0);
        return new BlockBreakEvent(block, playerMock);
    }

    /**
     * Minimal Bukkit listener that cancels every {@link PlayerStatConsumeEvent}.
     * Registered in the specific test that needs the cancel scenario.
     */
    private static class PlayerStatConsumeEventCanceller implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler
        public void onConsume(PlayerStatConsumeEvent event) {
            event.setCancelled(true);
        }
    }

    /**
     * Minimal ability that implements {@link ManaAbility} and always passes its
     * single activation component.  Used to isolate the mana-gating path in
     * {@link AbilityListener} without pulling in full config/YAML dependencies.
     */
    private static class StubManaAbility extends McRPGAbility implements ManaAbility {

        private static final NamespacedKey KEY =
                new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "stub_mana_ability");

        boolean activateReturn = true;
        private final int manaCost;

        StubManaAbility(@NotNull us.eunoians.mcrpg.McRPG plugin, int manaCost) {
            super(plugin, KEY);
            this.manaCost = manaCost;
            // Register an always-pass component for BlockBreakEvent
            addActivatableComponent((holder, event) -> true, BlockBreakEvent.class, 0);
        }

        @Override
        public boolean activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull org.bukkit.event.Event event) {
            return activateReturn;
        }

        @Override
        public int getManaCost(@NotNull AbilityHolder abilityHolder) {
            return manaCost;
        }

        @Override
        public boolean isAbilityEnabled() {
            return true;
        }

        @Override
        public boolean isPassive() {
            return false;
        }

        @Override
        @NotNull
        public String getDatabaseName() {
            return "stub_mana_ability";
        }

        @Override
        @NotNull
        public String getName(@NotNull McRPGPlayer player) {
            return "StubManaAbility";
        }

        @Override
        @NotNull
        public String getName() {
            return "StubManaAbility";
        }

        @Override
        @NotNull
        public net.kyori.adventure.text.Component getDisplayName(@NotNull McRPGPlayer player) {
            return net.kyori.adventure.text.Component.text("StubManaAbility");
        }

        @Override
        @NotNull
        public net.kyori.adventure.text.Component getDisplayName() {
            return net.kyori.adventure.text.Component.text("StubManaAbility");
        }

        @Override
        @NotNull
        public Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }

        @Override
        @NotNull
        public AbilityItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player) {
            throw new UnsupportedOperationException("Not used in tests");
        }
    }
}
