package us.eunoians.mcrpg.listener.ability;

import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.combo.ComboActivatable;
import us.eunoians.mcrpg.ability.impl.type.CooldownableAbility;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.event.ability.combo.ComboCompleteEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles {@link ComboCompleteEvent} by resolving which ability occupies the completed slot,
 * checking preconditions (hunger, cooldown), and dispatching
 * {@link ComboActivatable#comboActivate(us.eunoians.mcrpg.entity.holder.AbilityHolder)}.
 * <p>
 * Slot assignment is purely positional: all {@link ComboActivatable} abilities in the player's
 * available ability set are sorted alphabetically by their {@link org.bukkit.NamespacedKey#toString()}
 * and mapped to slots 1–3 by index. This makes slot assignment deterministic without requiring
 * any UI or database changes for the PoC.
 */
public class OnComboCompleteListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onComboComplete(@NotNull ComboCompleteEvent event) {
        Player player = event.getPlayer();
        McRPG mcRPG = McRPG.getInstance();
        EntityManager entityManager = mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.ENTITY);

        var abilityHolderOptional = entityManager.getAbilityHolder(player.getUniqueId());
        if (abilityHolderOptional.isEmpty()) {
            return;
        }
        var abilityHolder = abilityHolderOptional.get();

        // Validate McRPG is enabled for this holder
        if (!mcRPG.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.WORLD).isMcRPGEnabledForHolder(abilityHolder)) {
            return;
        }

        // Only LoadoutHolders can have combo abilities
        if (!(abilityHolder instanceof LoadoutHolder loadoutHolder)) {
            return;
        }

        var abilityRegistry = mcRPG.registryAccess().registry(McRPGRegistryKey.ABILITY);

        // Build a map of NamespacedKey -> ComboActivatable so we can sort by key deterministically
        Map<String, ComboActivatable> comboAbilityMap = new HashMap<>();
        for (var key : loadoutHolder.getAvailableAbilitiesToUse()) {
            Ability ability = abilityRegistry.getRegisteredAbility(key);
            if (ability instanceof ComboActivatable comboActivatable) {
                comboAbilityMap.put(key.toString(), comboActivatable);
            }
        }

        // Sort alphabetically by key string so slot assignment is deterministic across restarts
        List<ComboActivatable> comboAbilities = comboAbilityMap.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(Map.Entry::getValue)
                .toList();

        int slotIndex = event.getSlotIndex();
        if (slotIndex > comboAbilities.size()) {
            // No ability assigned to this slot — play a soft "empty" click
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            return;
        }

        ComboActivatable comboAbility = comboAbilities.get(slotIndex - 1);
        Ability ability = (Ability) comboAbility;

        // Hunger check
        int hungerCost = comboAbility.getHungerCost(abilityHolder);
        if (player.getFoodLevel() < hungerCost) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            player.sendActionBar(Component.text("Not enough hunger to use " + ability.getName() + "!", NamedTextColor.RED));
            return;
        }

        // Cooldown check
        if (comboAbility instanceof CooldownableAbility cooldownableAbility && cooldownableAbility.isAbilityOnCooldown(abilityHolder)) {
            player.sendActionBar(Component.text(ability.getName() + " is on cooldown!", NamedTextColor.RED));
            return;
        }

        // Deduct hunger before activation
        player.setFoodLevel(player.getFoodLevel() - hungerCost);

        // Dispatch the combo activation
        comboAbility.comboActivate(abilityHolder);

        // Put on cooldown after activation if applicable
        if (comboAbility instanceof CooldownableAbility cooldownableAbility) {
            cooldownableAbility.putHolderOnCooldown(abilityHolder);
        }
    }
}
