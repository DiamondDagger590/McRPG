package us.eunoians.mcrpg.gui.ability;

import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.exception.gui.InventoryAlreadyExistsForGuiException;
import com.diamonddagger590.mccore.gui.BaseGui;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;
import us.eunoians.mcrpg.ability.attribute.GuiModifiableAttribute;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.common.FillerItemGui;
import us.eunoians.mcrpg.gui.common.slot.McRPGPreviousGuiSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This gui is used for whenever an {@link Ability} is having its attributes modified.
 */
public class AbilityAttributeEditGui extends BaseGui<McRPGPlayer> implements FillerItemGui {

    private final Player player;
    private final Ability ability;

    public AbilityAttributeEditGui(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Ability ability) {
        super(mcRPGPlayer);
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
        this.ability = ability;
    }

    /**
     * Gets the {@link Ability} being modified.
     *
     * @return The {@link Ability} being modified.
     */
    @NotNull
    public Ability getAbility() {
        return ability;
    }

    @Override
    protected void buildInventory() {
        if (this.inventory != null) {
            throw new InventoryAlreadyExistsForGuiException(this);
        } else {
            int size = getModifiableAttributes().size();
            this.inventory = Bukkit.createInventory(player, Math.min(54, Math.max(9, Math.min(54, size % 9 != 0 ? (size / 9) * 9 + 9 : size) + 9)),
                    getCreatingPlayer().getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION)
                            .getLocalizedMessageAsComponent(getCreatingPlayer(), LocalizationKey.ABILITY_EDIT_GUI_TITLE, Map.of("ability", ability.getName(getCreatingPlayer()))));
            paintInventory();
        }
    }

    @Override
    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, McRPG.getInstance());
    }

    @Override
    public void unregisterListeners() {
        InventoryClickEvent.getHandlerList().unregister(this);
    }

    @Override
    public void paintInventory() {
        List<GuiModifiableAttribute> modifiableAttributes = getModifiableAttributes();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i < modifiableAttributes.size()) {
                setSlot(i, modifiableAttributes.get(i).getSlot(getCreatingPlayer(), ability));
            } else {
                removeSlot(i);
            }
        }
        setSlot(inventory.getSize() - 9, getPreviousGuiSlot());
        for (int i = inventory.getSize() - 8; i < inventory.getSize(); i++) {
            setSlot(i, getFillerItemSlot());
        }
    }

    @NotNull
    public McRPGPreviousGuiSlot getPreviousGuiSlot() {
        return new McRPGPreviousGuiSlot() {
            @Override
            public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
                if (mcRPGPlayer.getAsBukkitPlayer().isPresent()) {
                    Player player = mcRPGPlayer.getAsBukkitPlayer().get();
                    AbilityGui abilityGui = new AbilityGui(mcRPGPlayer);
                    player.openInventory(abilityGui.getInventory());
                    McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, abilityGui);
                }
                return true;
            }

            @NotNull
            @Override
            public Route getSpecificDisplayItemRoute() {
                return LocalizationKey.ABILITY_EDIT_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM;
            }
        };
    }

    /**
     * Get a {@link List} of all {@link GuiModifiableAttribute}s for the {@link Ability} being modified.
     *
     * @return A {@link List} of all {@link GuiModifiableAttribute}s for the {@link Ability} being modified.
     */
    @NotNull
    private List<GuiModifiableAttribute> getModifiableAttributes() {
        List<GuiModifiableAttribute> modifiableAttributes = new ArrayList<>();
        var abilityDataOptional = getCreatingPlayer().asSkillHolder().getAbilityData(ability);
        if (abilityDataOptional.isPresent()) {
            AbilityData abilityData = abilityDataOptional.get();
            for (AbilityAttribute<?> abilityAttribute : abilityData.getAllAttributes()) {
                if (abilityAttribute instanceof GuiModifiableAttribute guiModifiableAttribute) {
                    modifiableAttributes.add(guiModifiableAttribute);
                }
            }
        }
        return modifiableAttributes;
    }
}
