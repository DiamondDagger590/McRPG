package us.eunoians.mcrpg.listener.ability;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.Component;
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
import us.eunoians.mcrpg.ability.impl.type.CooldownableAbility;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.combo.ComboCompleteEvent;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.stat.McRPGPlayerStat;
import us.eunoians.mcrpg.stat.PlayerStatRegistry;
import us.eunoians.mcrpg.stat.impl.ResourcePoolPlayerStat;
import us.eunoians.mcrpg.stat.instance.PlayerStatData;
import us.eunoians.mcrpg.stat.instance.PlayerStatInstance;
import us.eunoians.mcrpg.util.McRPGMethods;
import us.eunoians.mcrpg.world.WorldManager;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests for the cooldown-on-cancel bug fix in {@link OnComboCompleteListener}.
 * <p>
 * Verifies that cooldown is only applied when {@link ComboActivatable#comboActivate} returns
 * {@code true}, and that mana is refunded when it returns {@code false}.
 */
class OnComboCompleteListenerCooldownTest extends McRPGBaseTest {

    private static final NamespacedKey MANA_KEY = McRPGPlayerStat.MANA.getKey();
    private static final int MANA_COST = 15;
    private static final double MANA_BASE = 100.0;

    private McRPGPlayer mcRPGPlayer;
    private PlayerMock playerMock;
    private CooldownableStubComboAbility stubAbility;

    @BeforeEach
    void setUp() {
        org.bukkit.event.HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();

        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);

        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);

        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        YamlDocument mainConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);
        when(mainConfig.getStringList(MainConfigFile.DISABLED_WORLDS)).thenReturn(List.of());
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);

        WorldManager worldManager = spy(new WorldManager(mcRPG));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(worldManager);

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        stubAbility = new CooldownableStubComboAbility(mcRPG, MANA_COST);
        abilityRegistry.register(stubAbility);

        EntityManager entityManager = new EntityManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(entityManager);

        PlayerStatRegistry statRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.PLAYER_STAT);
        if (statRegistry.getStat(MANA_KEY).isEmpty()) {
            statRegistry.register(new ResourcePoolPlayerStat(MANA_KEY, "Mana", "\u2746", MANA_BASE, 0));
        }

        McRPGPlayerManager playerManager = new McRPGPlayerManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(playerManager);

        mcRPGPlayer = spy(new McRPGPlayer(UUID.randomUUID(), mcRPG));
        playerManager.addPlayer(mcRPGPlayer);
        playerMock = addPlayerToServer(mcRPGPlayer);

        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        entityManager.trackAbilityHolder(skillHolder);
        skillHolder.addAvailableAbility(stubAbility);
        Loadout loadout = new Loadout(mcRPGPlayer.getUUID(), 1, Set.of(stubAbility.getAbilityKey()));
        skillHolder.setLoadout(loadout);

        server.getPluginManager().registerEvents(new OnComboCompleteListener(), mcRPG);
    }

    @Test
    @DisplayName("Given a successful comboActivate, when the combo completes, then cooldown is applied to the holder")
    void onComboComplete_appliesCooldown_whenComboActivateReturnsTrue() {
        stubAbility.comboReturn = true;

        assertFalse(stubAbility.isAbilityOnCooldown(mcRPGPlayer.asSkillHolder()),
                "Precondition: ability should not be on cooldown before activation");

        fireComboSlot1();

        assertTrue(stubAbility.isAbilityOnCooldown(mcRPGPlayer.asSkillHolder()),
                "Cooldown should be applied after successful activation");
    }

    @Test
    @DisplayName("Given comboActivate returning false, when the combo completes, then cooldown is NOT applied")
    void onComboComplete_doesNotApplyCooldown_whenComboActivateReturnsFalse() {
        stubAbility.comboReturn = false;

        assertFalse(stubAbility.isAbilityOnCooldown(mcRPGPlayer.asSkillHolder()),
                "Precondition: ability should not be on cooldown before activation");

        fireComboSlot1();

        assertFalse(stubAbility.isAbilityOnCooldown(mcRPGPlayer.asSkillHolder()),
                "Cooldown should NOT be applied when comboActivate returns false");
    }

    @Test
    @DisplayName("Given comboActivate returning false, when the combo completes, then mana is refunded")
    void onComboComplete_refundsMana_whenComboActivateReturnsFalse() {
        stubAbility.comboReturn = false;
        PlayerStatInstance mana = getMana();
        double manaBefore = mana.getCurrent();

        fireComboSlot1();

        assertEquals(manaBefore, mana.getCurrent(), 1e-9,
                "Mana should be fully refunded after a cancelled activation");
    }

    @NotNull
    private PlayerStatInstance getMana() {
        return mcRPGPlayer.getPlayerStatData().getInstance(MANA_KEY).orElseThrow();
    }

    private void fireComboSlot1() {
        ComboCompleteEvent event = new ComboCompleteEvent(playerMock, 1);
        server.getPluginManager().callEvent(event);
    }

    /**
     * Stub combo ability that also implements {@link CooldownableAbility} with a
     * fixed cooldown. The {@link #comboReturn} field controls activation success.
     */
    private static class CooldownableStubComboAbility extends McRPGAbility
            implements ComboActivatable, CooldownableAbility {

        private static final NamespacedKey KEY =
                new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "stub_cooldownable_combo");

        boolean comboReturn = true;
        private final int manaCost;

        CooldownableStubComboAbility(@NotNull us.eunoians.mcrpg.McRPG plugin, int manaCost) {
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
        public long getCooldown(@NotNull AbilityHolder abilityHolder) {
            return 10L;
        }

        @NotNull
        @Override
        public Set<NamespacedKey> getApplicableAttributes() {
            return Set.of(AbilityAttributeRegistry.ABILITY_COOLDOWN_ATTRIBUTE_KEY);
        }

        @Override
        public boolean activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            return comboReturn;
        }

        @Override
        public boolean isAbilityEnabled() {
            return true;
        }

        @Override
        public boolean isPassive() {
            return false;
        }

        @NotNull
        @Override
        public String getDatabaseName() {
            return "stub_cooldownable_combo";
        }

        @NotNull
        @Override
        public String getName(@NotNull McRPGPlayer player) {
            return "StubCooldownableCombo";
        }

        @NotNull
        @Override
        public String getName() {
            return "StubCooldownableCombo";
        }

        @NotNull
        @Override
        public Component getDisplayName(@NotNull McRPGPlayer player) {
            return Component.text("StubCooldownableCombo");
        }

        @NotNull
        @Override
        public Component getDisplayName() {
            return Component.text("StubCooldownableCombo");
        }

        @NotNull
        @Override
        public Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }

        @NotNull
        @Override
        public AbilityItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player) {
            throw new UnsupportedOperationException("Not used in tests");
        }
    }
}
