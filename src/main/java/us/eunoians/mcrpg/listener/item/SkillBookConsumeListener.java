package us.eunoians.mcrpg.listener.item;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityUnlockedAttribute;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.AbilityUnlockEvent;
import us.eunoians.mcrpg.event.item.SkillBookConsumeEvent;
import us.eunoians.mcrpg.item.skillbook.SkillBookFactory;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;
import java.util.Optional;

/**
 * Listens for right-click interactions with skill book items and handles
 * the consumption flow: validation, event firing, ability unlock, and
 * item removal.
 * <p>
 * This listener is always registered — skill books can be consumed regardless
 * of whether MythicMobs is present (books may come from quest rewards or commands).
 */
public class SkillBookConsumeListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        // Only handle right-click actions
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Prevent double-fire from dual-hand events
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || !SkillBookFactory.isSkillBook(item)) {
            return;
        }

        // Cancel the interact event to prevent placing/using the book
        event.setCancelled(true);

        Player player = event.getPlayer();
        McRPG plugin = McRPG.getInstance();

        // Read the ability key from the item
        String abilityKeyString = SkillBookFactory.getAbilityKeyString(item);
        if (abilityKeyString == null) {
            return;
        }

        NamespacedKey abilityKey = NamespacedKey.fromString(abilityKeyString);
        if (abilityKey == null) {
            return;
        }

        McRPGLocalizationManager localizationManager = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);

        // Look up the McRPGPlayer
        Optional<McRPGPlayer> playerOptional = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(player.getUniqueId());

        if (playerOptional.isEmpty()) {
            return;
        }

        McRPGPlayer mcRPGPlayer = playerOptional.get();

        // Validate the ability exists in the registry
        AbilityRegistry abilityRegistry = plugin.registryAccess()
                .registry(McRPGRegistryKey.ABILITY);

        if (!abilityRegistry.registered(abilityKey)) {
            player.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                    mcRPGPlayer, LocalizationKey.SKILL_BOOK_UNKNOWN_ABILITY,
                    Map.of("ability", abilityKeyString)));
            return;
        }

        var ability = abilityRegistry.getRegisteredAbility(abilityKey);

        // The ability must be unlockable
        if (!(ability instanceof UnlockableAbility unlockableAbility)) {
            player.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                    mcRPGPlayer, LocalizationKey.SKILL_BOOK_UNKNOWN_ABILITY,
                    Map.of("ability", abilityKeyString)));
            return;
        }

        // Check if already unlocked
        boolean isUnlocked = mcRPGPlayer.getAbilityData(abilityKey)
                .flatMap(data -> data.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE))
                .filter(attr -> attr instanceof AbilityUnlockedAttribute)
                .map(attr -> ((AbilityUnlockedAttribute) attr).getContent())
                .orElse(false);

        if (isUnlocked) {
            String abilityName = plugin.getMiniMessage().serialize(unlockableAbility.getDisplayName(mcRPGPlayer));
            player.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                    mcRPGPlayer, LocalizationKey.SKILL_BOOK_ALREADY_UNLOCKED,
                    Map.of("ability", abilityName)));
            return;
        }

        // Fire SkillBookConsumeEvent (cancellable)
        SkillBookConsumeEvent consumeEvent = new SkillBookConsumeEvent(mcRPGPlayer, abilityKey, item);
        Bukkit.getPluginManager().callEvent(consumeEvent);

        if (consumeEvent.isCancelled()) {
            return;
        }

        // Unlock the ability — set attribute and fire AbilityUnlockEvent
        mcRPGPlayer.getAbilityData(abilityKey).ifPresent(data -> {
            data.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE)
                    .filter(attr -> attr instanceof AbilityUnlockedAttribute)
                    .ifPresent(attr -> ((AbilityUnlockedAttribute) attr).setContent(true));
        });

        AbilityUnlockEvent unlockEvent = new AbilityUnlockEvent(mcRPGPlayer, unlockableAbility);
        Bukkit.getPluginManager().callEvent(unlockEvent);

        // Remove one skill book from the player's hand
        item.setAmount(item.getAmount() - 1);

        // Send consumption message
        String abilityName = plugin.getMiniMessage().serialize(unlockableAbility.getDisplayName(mcRPGPlayer));
        player.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                mcRPGPlayer, LocalizationKey.SKILL_BOOK_CONSUMED,
                Map.of("ability", abilityName)));
    }
}
