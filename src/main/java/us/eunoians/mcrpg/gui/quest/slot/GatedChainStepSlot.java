package us.eunoians.mcrpg.gui.quest.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.chain.QuestChainStep;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;
import java.util.Optional;

/**
 * Slot representing a gated (locked) chain step in a chain detail GUI.
 * Displays a preview item based on a three-level resolution order:
 * <ol>
 *   <li>Quest definition display item (if the quest is registered)</li>
 *   <li>Step-level preview item (from YAML {@code preview:} metadata)</li>
 *   <li>Generic locked barrier fallback</li>
 * </ol>
 * Clicking sends the player a localized deny message and plays a deny sound.
 */
public class GatedChainStepSlot implements McRPGSlot {

    private final QuestChainStep step;
    private final int stepNumber;

    /**
     * Constructs a gated chain step slot.
     *
     * @param step       the gated chain step
     * @param stepNumber the 1-based step number for display
     */
    public GatedChainStepSlot(@NotNull QuestChainStep step, int stepNumber) {
        this.step = step;
        this.stepNumber = stepNumber;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sends a localized deny message and plays a deny sound. Returns {@code true}
     * to prevent item movement.
     *
     * @param mcRPGPlayer the clicking player
     * @param clickType   the click type
     * @return always {@code true}
     */
    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            String message = localizationManager.getLocalizedMessage(
                    mcRPGPlayer, LocalizationKey.CHAIN_PREVIEW_LOCKED_TITLE,
                    Map.of("<step_number>", String.valueOf(stepNumber)));
            player.sendMessage(localizationManager.resolvePaletteColors(message));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        });
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Builds the display item using a three-level resolution order.
     *
     * @param mcRPGPlayer the viewing player
     * @return the item builder for this gated step
     */
    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        QuestDefinitionRegistry definitionRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_DEFINITION);

        Optional<QuestDefinition> definitionOpt = definitionRegistry.get(step.questKey());

        if (definitionOpt.isPresent()) {
            return buildDefinitionPreview(definitionOpt.get(), localizationManager, mcRPGPlayer);
        }

        if (step.previewItem() != null) {
            return ItemBuilder.from(step.previewItem().clone());
        }

        return buildLockedFallback(localizationManager, mcRPGPlayer);
    }

    /**
     * Builds a preview item from a registered quest definition, showing the quest name
     * but hiding detailed objectives and rewards.
     *
     * @param definition          the quest definition
     * @param localizationManager the localization manager
     * @param player              the viewing player
     * @return the preview item builder
     */
    @NotNull
    private ItemBuilder buildDefinitionPreview(@NotNull QuestDefinition definition,
                                                @NotNull McRPGLocalizationManager localizationManager,
                                                @NotNull McRPGPlayer player) {
        String questName = definition.getDisplayNameRoute() != null
                ? localizationManager.getLocalizedMessage(player, definition.getDisplayNameRoute())
                : step.questKey().toString();

        String title = localizationManager.getLocalizedMessage(
                player, LocalizationKey.CHAIN_PREVIEW_STEP_TITLE,
                Map.of("<step_number>", String.valueOf(stepNumber),
                        "<quest_name>", questName));
        String hiddenObjectives = localizationManager.getLocalizedMessage(
                player, LocalizationKey.CHAIN_PREVIEW_OBJECTIVES_HIDDEN);

        return ItemBuilder.from(new ItemStack(Material.PAPER))
                .setDisplayName(localizationManager.resolvePaletteColors(title))
                .addDisplayLore(localizationManager.resolvePaletteColors(hiddenObjectives));
    }

    /**
     * Builds the generic locked fallback item (barrier with locked message).
     *
     * @param localizationManager the localization manager
     * @param player              the viewing player
     * @return the locked fallback item builder
     */
    @NotNull
    private ItemBuilder buildLockedFallback(@NotNull McRPGLocalizationManager localizationManager,
                                             @NotNull McRPGPlayer player) {
        String title = localizationManager.getLocalizedMessage(
                player, LocalizationKey.CHAIN_PREVIEW_LOCKED_TITLE,
                Map.of("<step_number>", String.valueOf(stepNumber)));
        String description = localizationManager.getLocalizedMessage(
                player, LocalizationKey.CHAIN_PREVIEW_LOCKED_DESCRIPTION);

        return ItemBuilder.from(new ItemStack(Material.BARRIER))
                .setDisplayName(localizationManager.resolvePaletteColors(title))
                .addDisplayLore(localizationManager.resolvePaletteColors(description));
    }
}
