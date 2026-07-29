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

    private final KillOnLogoutPunishment killOnLogout = new KillOnLogoutPunishment((NamespacedKey) null);
    private final DropItemsPunishment dropItems = new DropItemsPunishment((NamespacedKey) null);
    private final BroadcastMessagePunishment broadcastMessage = new BroadcastMessagePunishment((NamespacedKey) null);

    private CombatSession session() {
        return new CombatSession(UUID.randomUUID(), 16, 8000L);
    }

    @Test
    @DisplayName("built-in types have distinct NamespacedKeys under the mcrpg namespace")
    void builtInTypes_haveDistinctKeys() {
        assertEquals("mcrpg", killOnLogout.getKey().getNamespace());
        assertEquals("mcrpg", dropItems.getKey().getNamespace());
        assertEquals("mcrpg", broadcastMessage.getKey().getNamespace());

        assertNotEquals(killOnLogout.getKey(), dropItems.getKey());
        assertNotEquals(killOnLogout.getKey(), broadcastMessage.getKey());
        assertNotEquals(dropItems.getKey(), broadcastMessage.getKey());
    }

    @Test
    @DisplayName("each type returns its corresponding config key string")
    void types_returnConfigKeys() {
        assertEquals("kill-on-logout", killOnLogout.getConfigKey());
        assertEquals("drop-items", dropItems.getConfigKey());
        assertEquals("broadcast-message", broadcastMessage.getConfigKey());
    }

    @Test
    @DisplayName("KillOnLogoutPunishment excludes DropItemsPunishment")
    void killOnLogout_excludesDropItems() {
        assertTrue(killOnLogout.getExcludes().contains(DropItemsPunishment.KEY));
    }

    @Test
    @DisplayName("DropItemsPunishment has no exclusions")
    void dropItems_hasNoExclusions() {
        assertTrue(dropItems.getExcludes().isEmpty());
    }

    @Test
    @DisplayName("BroadcastMessagePunishment has no exclusions")
    void broadcastMessage_hasNoExclusions() {
        assertTrue(broadcastMessage.getExcludes().isEmpty());
    }

    @Test
    @DisplayName("KillOnLogoutPunishment.apply sets the player's health to zero")
    void killOnLogout_apply_setsHealthToZero() {
        PlayerMock player = server.addPlayer();
        player.setHealth(20.0);

        killOnLogout.apply(player, session(), mcRPG);

        assertEquals(0.0, player.getHealth());
    }

    @Test
    @DisplayName("DropItemsPunishment.apply drops inventory items at the player's location and clears inventory")
    void dropItems_apply_dropsAndClearsInventory() {
        PlayerMock player = server.addPlayer();
        player.getInventory().addItem(new ItemStack(Material.DIAMOND, 3));
        int initialItemsInWorld = player.getWorld().getEntitiesByClass(Item.class).size();

        dropItems.apply(player, session(), mcRPG);

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
    @DisplayName("BroadcastMessagePunishment.apply delegates to localizationManager.broadcastMessage with the player's location")
    void broadcastMessage_apply_delegatesToLocalizationManager() {
        PlayerMock player = server.addPlayer();
        var localizationManager = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);

        broadcastMessage.apply(player, session(), mcRPG);

        verify(localizationManager).broadcastMessage(
                eq(LocalizationKey.COMBAT_LOG_BROADCAST),
                argThat(map -> player.getName().equals(map.get("player"))));
    }

    @Test
    @DisplayName("equality is based on NamespacedKey")
    void equality_isBasedOnKey() {
        KillOnLogoutPunishment anotherKillOnLogout = new KillOnLogoutPunishment((NamespacedKey) null);
        assertEquals(killOnLogout, anotherKillOnLogout);
        assertNotEquals(killOnLogout, dropItems);
    }

    @Test
    @DisplayName("toString returns the NamespacedKey string representation")
    void toString_returnsKeyString() {
        NamespacedKey key = killOnLogout.getKey();
        assertEquals(key.toString(), killOnLogout.toString());
    }

    @Test
    @DisplayName("getExpansionKey returns empty for a type constructed with a null expansion key")
    void getExpansionKey_returnsEmpty_whenNull() {
        CombatLogPunishmentType custom = new CombatLogPunishmentType(
                new NamespacedKey("test", "custom"), "custom", null) {
            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public void apply(@org.jetbrains.annotations.NotNull Player player,
                              @org.jetbrains.annotations.NotNull CombatSession session,
                              @org.jetbrains.annotations.NotNull McRPG mcRPG) {
            }
        };

        assertTrue(custom.getExpansionKey().isEmpty());
    }

    @Test
    @DisplayName("isEnabled returns true by default when no config is initialized")
    void isEnabled_returnsTrue_whenNoConfig() {
        assertTrue(killOnLogout.isEnabled());
        assertTrue(dropItems.isEnabled());
        assertTrue(broadcastMessage.isEnabled());
    }
}
