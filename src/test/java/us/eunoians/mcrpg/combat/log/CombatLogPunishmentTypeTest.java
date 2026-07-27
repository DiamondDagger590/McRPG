package us.eunoians.mcrpg.combat.log;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@DisplayName("CombatLogPunishmentType")
class CombatLogPunishmentTypeTest extends McRPGBaseTest {

    private CombatSession session() {
        return new CombatSession(UUID.randomUUID(), 16, 8000L);
    }

    @Test
    @DisplayName("built-in constants have distinct NamespacedKeys under the mcrpg namespace")
    void builtInConstants_haveDistinctKeys() {
        assertEquals("mcrpg", CombatLogPunishmentType.KILL_ON_LOGOUT.getKey().getNamespace());
        assertEquals("mcrpg", CombatLogPunishmentType.DROP_ITEMS.getKey().getNamespace());
        assertEquals("mcrpg", CombatLogPunishmentType.BROADCAST_MESSAGE.getKey().getNamespace());

        assertNotEquals(CombatLogPunishmentType.KILL_ON_LOGOUT.getKey(), CombatLogPunishmentType.DROP_ITEMS.getKey());
        assertNotEquals(CombatLogPunishmentType.KILL_ON_LOGOUT.getKey(), CombatLogPunishmentType.BROADCAST_MESSAGE.getKey());
        assertNotEquals(CombatLogPunishmentType.DROP_ITEMS.getKey(), CombatLogPunishmentType.BROADCAST_MESSAGE.getKey());
    }

    @Test
    @DisplayName("each constant returns its corresponding config key string")
    void constants_returnConfigKeys() {
        assertEquals("kill-on-logout", CombatLogPunishmentType.KILL_ON_LOGOUT.getConfigKey());
        assertEquals("drop-items", CombatLogPunishmentType.DROP_ITEMS.getConfigKey());
        assertEquals("broadcast-message", CombatLogPunishmentType.BROADCAST_MESSAGE.getConfigKey());
    }

    @Test
    @DisplayName("KILL_ON_LOGOUT excludes DROP_ITEMS")
    void killOnLogout_excludesDropItems() {
        assertTrue(CombatLogPunishmentType.KILL_ON_LOGOUT.getExcludes().contains(CombatLogPunishmentType.DROP_ITEMS.getKey()));
    }

    @Test
    @DisplayName("DROP_ITEMS has no exclusions")
    void dropItems_hasNoExclusions() {
        assertTrue(CombatLogPunishmentType.DROP_ITEMS.getExcludes().isEmpty());
    }

    @Test
    @DisplayName("BROADCAST_MESSAGE has no exclusions")
    void broadcastMessage_hasNoExclusions() {
        assertTrue(CombatLogPunishmentType.BROADCAST_MESSAGE.getExcludes().isEmpty());
    }

    @Test
    @DisplayName("KILL_ON_LOGOUT.apply sets the player's health to zero")
    void killOnLogout_apply_setsHealthToZero() {
        PlayerMock player = server.addPlayer();
        player.setHealth(20.0);

        CombatLogPunishmentType.KILL_ON_LOGOUT.apply(player, session(), mcRPG);

        assertEquals(0.0, player.getHealth());
    }

    @Test
    @DisplayName("DROP_ITEMS.apply drops inventory items at the player's location and clears inventory")
    void dropItems_apply_dropsAndClearsInventory() {
        PlayerMock player = server.addPlayer();
        player.getInventory().addItem(new ItemStack(Material.DIAMOND, 3));
        int initialItemsInWorld = player.getWorld().getEntitiesByClass(Item.class).size();

        CombatLogPunishmentType.DROP_ITEMS.apply(player, session(), mcRPG);

        boolean allAir = true;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                allAir = false;
            }
        }
        assertTrue(allAir, "Inventory should be cleared after DROP_ITEMS is applied");
        int itemsInWorldAfter = player.getWorld().getEntitiesByClass(Item.class).size();
        assertTrue(itemsInWorldAfter > initialItemsInWorld, "Dropped items should appear in the world");
    }

    @Test
    @DisplayName("BROADCAST_MESSAGE.apply delegates to localizationManager.broadcastMessage with the player's location")
    void broadcastMessage_apply_delegatesToLocalizationManager() {
        PlayerMock player = server.addPlayer();
        var localizationManager = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);

        CombatLogPunishmentType.BROADCAST_MESSAGE.apply(player, session(), mcRPG);

        verify(localizationManager).broadcastMessage(
                eq(LocalizationKey.COMBAT_LOG_BROADCAST),
                argThat(map -> player.getName().equals(map.get("player"))));
    }

    @Test
    @DisplayName("equality is based on NamespacedKey")
    void equality_isBasedOnKey() {
        CombatLogPunishmentType anotherKillOnLogoutReference = CombatLogPunishmentType.KILL_ON_LOGOUT;
        assertEquals(CombatLogPunishmentType.KILL_ON_LOGOUT, anotherKillOnLogoutReference);
        assertNotEquals(CombatLogPunishmentType.KILL_ON_LOGOUT, CombatLogPunishmentType.DROP_ITEMS);
    }

    @Test
    @DisplayName("toString returns the NamespacedKey string representation")
    void toString_returnsKeyString() {
        NamespacedKey key = CombatLogPunishmentType.KILL_ON_LOGOUT.getKey();
        assertEquals(key.toString(), CombatLogPunishmentType.KILL_ON_LOGOUT.toString());
    }

    @Test
    @DisplayName("getExpansionKey returns empty for a type constructed with a null expansion key")
    void getExpansionKey_returnsEmpty_whenNull() {
        CombatLogPunishmentType custom = new CombatLogPunishmentType(
                new NamespacedKey("test", "custom"), "custom", null) {
            @Override
            public void apply(@org.jetbrains.annotations.NotNull Player player,
                              @org.jetbrains.annotations.NotNull CombatSession session,
                              @org.jetbrains.annotations.NotNull McRPG mcRPG) {
                // no-op for this test
            }
        };

        assertTrue(custom.getExpansionKey().isEmpty());
    }
}
