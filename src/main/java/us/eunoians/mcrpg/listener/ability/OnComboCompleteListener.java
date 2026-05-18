package us.eunoians.mcrpg.listener.ability;

import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.display.hud.ActionBarHudDisplay;
import us.eunoians.mcrpg.display.hud.CenterContentPriority;
import us.eunoians.mcrpg.display.hud.content.CountdownCooldownCenterContent;
import us.eunoians.mcrpg.display.hud.content.TimedCenterContent;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.combo.ComboCompleteEvent;
import us.eunoians.mcrpg.event.stat.PlayerStatConsumeEvent;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.stat.McRPGPlayerStat;
import us.eunoians.mcrpg.stat.instance.PlayerStatData;
import us.eunoians.mcrpg.stat.instance.PlayerStatInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles {@link ComboCompleteEvent} by resolving which ability occupies the completed slot,
 * checking preconditions (mana, cooldown), and dispatching
 * {@link ComboActivatable#comboActivate(us.eunoians.mcrpg.entity.holder.AbilityHolder)}.
 * <p>
 * Slot assignment mirrors the player's loadout GUI order: combo slot 1 is the first
 * {@link ComboActivatable} in the loadout, slot 2 is the second, and so on. Abilities
 * are iterated via {@link us.eunoians.mcrpg.loadout.Loadout#getOrderedAbilities()} to
 * preserve insertion order. Only abilities the player explicitly placed in their loadout
 * are considered — default (non-unlockable) abilities are excluded.
 */
public class OnComboCompleteListener implements Listener {

    private static final long CENTER_CONTENT_DURATION_TICKS = 60L;

    // TODO(#217): Break this method up and make the feedback messages / sounds /
    // durations configurable. Right now preconditions (cooldown check, mana check,
    // ability resolution) are inlined into one ~90-line handler, and the feedback
    // strings / sounds / tick durations are hard-coded. Split into private helpers
    // (or a small collaborator) per precondition and move user-facing text to the
    // localization system plus the combo config YAML.

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

        if (!mcRPG.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.WORLD).isMcRPGEnabledForHolder(abilityHolder)) {
            return;
        }

        if (!(abilityHolder instanceof LoadoutHolder loadoutHolder)) {
            return;
        }

        var abilityRegistry = mcRPG.registryAccess().registry(McRPGRegistryKey.ABILITY);

        List<ComboActivatable> comboAbilities = new ArrayList<>();
        for (var key : loadoutHolder.getLoadout().getOrderedAbilities()) {
            Ability ability = abilityRegistry.getRegisteredAbility(key);
            if (ability instanceof ComboActivatable comboActivatable) {
                comboAbilities.add(comboActivatable);
            }
        }

        int slotIndex = event.getSlotIndex();
        if (slotIndex > comboAbilities.size()) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            return;
        }

        var mcRPGPlayerOpt = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId());
        if (mcRPGPlayerOpt.isEmpty()) {
            return;
        }
        McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();

        McRPGLocalizationManager localizationManager = mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        ComboActivatable comboAbility = comboAbilities.get(slotIndex - 1);
        Ability ability = (Ability) comboAbility;

        if (comboAbility instanceof CooldownableAbility cooldownableAbility && cooldownableAbility.isAbilityOnCooldown(abilityHolder)) {
            long expiryMillis = cooldownableAbility.getCooldownForHolder(abilityHolder);
            long remainingSeconds = Math.max(1, (expiryMillis - mcRPG.getTimeProvider().now().toEpochMilli()) / 1000);
            String remainingStr = String.valueOf(remainingSeconds);
            String abilityName = ability.getName(mcRPGPlayer);

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);

            long maxExpiryTick = Bukkit.getCurrentTick() + CENTER_CONTENT_DURATION_TICKS;
            ActionBarHudDisplay hud = getOrCreateHud(mcRPG, mcRPGPlayer);
            hud.setSlot(CenterContentPriority.ABILITY_FEEDBACK,
                    new CountdownCooldownCenterContent(abilityName, expiryMillis, maxExpiryTick, mcRPG.getTimeProvider()));

            Component chatText = localizationManager.getLocalizedMessageAsComponent(
                    mcRPGPlayer, LocalizationKey.COOLDOWN_ACTIVE,
                    Map.of("ability", abilityName, "remaining", remainingStr));
            player.sendMessage(chatText);
            return;
        }

        PlayerStatData statData = mcRPGPlayer.getPlayerStatData();
        PlayerStatInstance manaInstance = statData.getInstance(McRPGPlayerStat.MANA.getKey())
                .orElseThrow(() -> new IllegalStateException(
                        "Stat " + McRPGPlayerStat.MANA.getKey() + " not registered in PlayerStatData for UUID: "
                                + mcRPGPlayer.getUUID()));

        int manaCost = comboAbility.getManaCost(abilityHolder);

        PlayerStatConsumeEvent consumeEvent = new PlayerStatConsumeEvent(abilityHolder, McRPGPlayerStat.MANA.getKey(), manaCost);
        Bukkit.getPluginManager().callEvent(consumeEvent);

        if (consumeEvent.isCancelled()) {
            return;
        }

        double effectiveCost = consumeEvent.getEffectiveAmount();
        if (!manaInstance.consume(effectiveCost)) {
            int currentMana = (int) Math.round(manaInstance.getCurrent());
            String abilityName = ability.getName(mcRPGPlayer);

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);

            Component hudText = localizationManager.getLocalizedMessageAsComponent(
                    mcRPGPlayer, LocalizationKey.MANA_INSUFFICIENT_HUD,
                    Map.of("ability", abilityName));
            long expiryTick = Bukkit.getCurrentTick() + CENTER_CONTENT_DURATION_TICKS;
            ActionBarHudDisplay hud = getOrCreateHud(mcRPG, mcRPGPlayer);
            hud.setSlot(CenterContentPriority.ABILITY_FEEDBACK, new TimedCenterContent(hudText, expiryTick));

            Component chatText = localizationManager.getLocalizedMessageAsComponent(
                    mcRPGPlayer, LocalizationKey.MANA_INSUFFICIENT,
                    Map.of("ability", abilityName, "cost", String.valueOf(manaCost), "current", String.valueOf(currentMana)));
            player.sendMessage(chatText);
            return;
        }

        boolean activated = comboAbility.comboActivate(abilityHolder);
        if (!activated) {
            manaInstance.restore(effectiveCost);
            return;
        }

        if (comboAbility instanceof CooldownableAbility cooldownableAbility) {
            cooldownableAbility.putHolderOnCooldown(abilityHolder);
        }
    }

    @NotNull
    private ActionBarHudDisplay getOrCreateHud(@NotNull McRPG mcRPG, @NotNull McRPGPlayer mcRPGPlayer) {
        return mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DISPLAY)
                .getOrCreateActionBarHud(mcRPGPlayer);
    }
}
