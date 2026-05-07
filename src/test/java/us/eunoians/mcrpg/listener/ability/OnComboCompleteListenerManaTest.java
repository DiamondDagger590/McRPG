package us.eunoians.mcrpg.listener.ability;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.combo.ComboActivatable;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.event.ability.combo.ComboCompleteEvent;
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
 * Integration tests for the mana-consumption and refund logic in
 * {@link OnComboCompleteListener}.
 * <p>
 * Uses a {@link StubComboAbility} inner class to control whether
 * {@link ComboActivatable#comboActivate} succeeds or fails.
 */
class OnComboCompleteListenerManaTest extends McRPGBaseTest {

    private static final NamespacedKey MANA_KEY = McRPGPlayerStat.MANA.getKey();
    private static final int MANA_COST = 15;
    private static final double MANA_BASE = 100.0;

    private McRPGPlayer mcRPGPlayer;
    private PlayerMock playerMock;
    private StubComboAbility stubAbility;

    @BeforeEach
    void setUp() {
        org.bukkit.event.HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();

        // ReloadableContentManager must exist before WorldManager
        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);

        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);

        // Set up main config mock for WorldManager
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        YamlDocument mainConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);
        when(mainConfig.getStringList(MainConfigFile.DISABLED_WORLDS)).thenReturn(List.of());
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);

        WorldManager worldManager = spy(new WorldManager(mcRPG));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(worldManager);

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        stubAbility = new StubComboAbility(mcRPG, MANA_COST);
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

        // Add player to MockBukkit so Bukkit.getEntity(UUID) resolves for WorldManager
        playerMock = addPlayerToServer(mcRPGPlayer);

        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        entityManager.trackAbilityHolder(skillHolder);
        skillHolder.addAvailableAbility(stubAbility);
        Loadout loadout = new Loadout(mcRPGPlayer.getUUID(), 1, Set.of(stubAbility.getAbilityKey()));
        skillHolder.setLoadout(loadout);

        // Register the listener under test
        server.getPluginManager().registerEvents(new OnComboCompleteListener(), mcRPG);
    }

    @DisplayName("Sufficient mana + comboActivate returns true: mana consumed and not refunded")
    @Test
    void activationSucceeds_manaPermanentlyConsumed() {
        stubAbility.comboReturn = true;
        PlayerStatInstance mana = getMana();
        assertEquals(MANA_BASE, mana.getCurrent());

        fireComboSlot1();

        assertEquals(MANA_BASE - MANA_COST, mana.getCurrent());
    }

    @DisplayName("Sufficient mana + comboActivate returns false: mana refunded")
    @Test
    void activationReturnsFalse_manaRefunded() {
        stubAbility.comboReturn = false;
        PlayerStatInstance mana = getMana();
        double manaBefore = mana.getCurrent();

        fireComboSlot1();

        // Mana was consumed and then immediately restored
        assertEquals(manaBefore, mana.getCurrent(), 1e-9);
    }

    @DisplayName("PlayerStatConsumeEvent cancelled: early return, mana unchanged")
    @Test
    void consumeEventCancelled_manaUnchanged() {
        PlayerStatInstance mana = getMana();
        double manaBefore = mana.getCurrent();

        // Register a listener that cancels the consume event
        server.getPluginManager().registerEvents(new PlayerStatConsumeEventCanceller(), mcRPG);

        fireComboSlot1();

        assertEquals(manaBefore, mana.getCurrent(), 1e-9);
    }

    // Helpers

    @NotNull
    private PlayerStatInstance getMana() {
        return mcRPGPlayer.getPlayerStatData().getInstance(MANA_KEY).orElseThrow();
    }

    private void fireComboSlot1() {
        ComboCompleteEvent event = new ComboCompleteEvent(playerMock, 1);
        server.getPluginManager().callEvent(event);
    }

    /**
     * Minimal Bukkit listener that cancels every {@link PlayerStatConsumeEvent}.
     */
    private static class PlayerStatConsumeEventCanceller implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler
        public void onConsume(PlayerStatConsumeEvent event) {
            event.setCancelled(true);
        }
    }

    /**
     * Minimal ability that implements {@link ComboActivatable} with a controllable
     * {@link #comboReturn} to test the mana-refund path without full YAML setup.
     */
    private static class StubComboAbility extends McRPGAbility implements ComboActivatable {

        private static final NamespacedKey KEY =
                new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "stub_combo_ability");

        boolean comboReturn = true;
        private final int manaCost;

        StubComboAbility(@NotNull us.eunoians.mcrpg.McRPG plugin, int manaCost) {
            super(plugin, KEY);
            this.manaCost = manaCost;
        }

        @Override
        public boolean comboActivate(@NotNull AbilityHolder abilityHolder) {
            return comboReturn;
        }

        @Override
        public int getManaCost(@NotNull AbilityHolder abilityHolder) {
            return manaCost;
        }

        @Override
        public boolean activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            return true;
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
            return "stub_combo_ability";
        }

        @Override
        @NotNull
        public String getName(@NotNull McRPGPlayer player) {
            return "StubComboAbility";
        }

        @Override
        @NotNull
        public String getName() {
            return "StubComboAbility";
        }

        @Override
        @NotNull
        public net.kyori.adventure.text.Component getDisplayName(@NotNull McRPGPlayer player) {
            return net.kyori.adventure.text.Component.text("StubComboAbility");
        }

        @Override
        @NotNull
        public net.kyori.adventure.text.Component getDisplayName() {
            return net.kyori.adventure.text.Component.text("StubComboAbility");
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
