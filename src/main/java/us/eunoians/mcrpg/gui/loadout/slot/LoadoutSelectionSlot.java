package us.eunoians.mcrpg.gui.loadout.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.combo.ComboActivatable;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.loadout.LoadoutGui;
import us.eunoians.mcrpg.gui.loadout.LoadoutSelectionGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.registry.plugin.McRPGPluginHookKey;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.diamonddagger590.mccore.registry.RegistryAccess.registryAccess;

/**
 * This slot is used to select a specific {@link Loadout} to edit.
 * <p>
 * In addition to the loadout display item and name, the slot appends a preview line
 * summarising the active (combo) abilities currently in the loadout so players can
 * identify their combat loadout at a glance from the selection screen.
 */
public class LoadoutSelectionSlot implements McRPGSlot {

    private final McRPGPlayer mcRPGPlayer;
    private final Player player;
    private final Loadout loadout;

    public LoadoutSelectionSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout) {
        this.mcRPGPlayer = mcRPGPlayer;
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
        this.loadout = loadout;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        var guiOptional = mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).getOpenedGui(mcRPGPlayer);
        guiOptional.ifPresent(gui -> {
            if (isPlayerOnGeyser() || clickType != ClickType.RIGHT) {
                LoadoutGui loadoutGui = new LoadoutGui(mcRPGPlayer, loadout);
                mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, loadoutGui);
                player.openInventory(loadoutGui.getInventory());
            } else {
                player.performCommand("mcrpg loadout set " + loadout.getLoadoutSlot());
                gui.refreshGUI();
            }
        });
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        var localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        ItemBuilder itemBuilder;
        if (isPlayerOnGeyser()) {
            itemBuilder = ItemBuilder.from(localizationManager.getLocalizedSection(mcRPGPlayer,
                            isLoadoutActive() ? LocalizationKey.LOADOUT_SELECTION_GUI_ACTIVE_LOADOUT_SELECTION_SLOT_GEYSER_DISPLAY_ITEM :
                                    LocalizationKey.LOADOUT_SELECTION_GUI_INACTIVE_LOADOUT_SELECTION_SLOT_GEYSER_DISPLAY_ITEM), loadout.getDisplay().getDisplayItem().itemBuilder());
        } else {
            itemBuilder = ItemBuilder.from(localizationManager.getLocalizedSection(mcRPGPlayer,
                            isLoadoutActive() ? LocalizationKey.LOADOUT_SELECTION_GUI_ACTIVE_LOADOUT_SELECTION_SLOT_DISPLAY_ITEM :
                                    LocalizationKey.LOADOUT_SELECTION_GUI_INACTIVE_LOADOUT_SELECTION_SLOT_DISPLAY_ITEM), loadout.getDisplay().getDisplayItem().itemBuilder());
        }
        itemBuilder.applyTagReplacements(localizationManager.getPaletteReplacements());
        itemBuilder.addPlaceholder("name", loadout.getDisplay().getDisplayName().orElse(Integer.toString(loadout.getLoadoutSlot())));
        appendActiveAbilitiesPreview(mcRPGPlayer, itemBuilder);
        return itemBuilder;
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(LoadoutSelectionGui.class);
    }

    /**
     * Appends a summary line listing the active (combo) abilities in the loadout.
     * <p>
     * The line is only added when at least one active ability is present. Each ability name
     * is derived from its registry key and formatted as title-case words
     * (e.g., {@code "serrated_strikes"} → {@code "Serrated Strikes"}).
     *
     * @param mcRPGPlayer The player viewing the slot.
     * @param itemBuilder The item builder to append the line to.
     */
    private void appendActiveAbilitiesPreview(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ItemBuilder itemBuilder) {
        List<NamespacedKey> activeKeys = loadout.getOrderedActiveAbilities();
        if (activeKeys.isEmpty()) {
            return;
        }
        var abilityRegistry = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY);
        String abilitiesDisplay = activeKeys.stream()
                .filter(key -> abilityRegistry.registered(key) && abilityRegistry.getRegisteredAbility(key) instanceof ComboActivatable)
                .map(key -> "<gold>" + formatAbilityName(key))
                .collect(Collectors.joining("<gray>, "));
        if (abilitiesDisplay.isBlank()) {
            return;
        }
        String previewLine = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION)
                .getLocalizedMessage(mcRPGPlayer, LocalizationKey.LOADOUT_SELECTION_GUI_ACTIVE_ABILITIES_PREVIEW);
        previewLine = previewLine.replace("<active-abilities>", abilitiesDisplay);
        itemBuilder.addDisplayLore(List.of("", previewLine));
    }

    /**
     * Formats a {@link NamespacedKey} key string into a title-case display name.
     * For example, {@code "serrated_strikes"} becomes {@code "Serrated Strikes"}.
     *
     * @param key The namespaced key whose {@link NamespacedKey#getKey()} is formatted.
     * @return The title-case formatted name.
     */
    @NotNull
    private String formatAbilityName(@NotNull NamespacedKey key) {
        return Arrays.stream(key.getKey().split("_"))
                .map(word -> word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    private boolean isLoadoutActive() {
        return loadout.getLoadoutSlot() == mcRPGPlayer.asSkillHolder().getCurrentLoadoutSlot();
    }

    private boolean isPlayerOnGeyser() {
        return registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHook(McRPGPluginHookKey.GEYSER).map(geyserHook -> geyserHook.isBedrockPlayer(mcRPGPlayer.getUUID())).orElse(false);
    }
}
