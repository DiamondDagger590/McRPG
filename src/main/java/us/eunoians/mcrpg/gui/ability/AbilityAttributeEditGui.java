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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import com.diamonddagger590.mccore.gui.KeyedGui;
import org.bukkit.NamespacedKey;
import us.eunoians.mcrpg.util.McRPGMethods;

/**
 * This gui is used for whenever an {@link Ability} is having its attributes modified.
 */
public class AbilityAttributeEditGui extends BaseGui<McRPGPlayer> implements FillerItemGui, KeyedGui {

    public static final NamespacedKey GUI_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "ability_edit");

    private final Player player;
    private final Ability ability;
    private final Supplier<BaseGui> backGuiSupplier;

    /**
     * Creates an {@link AbilityAttributeEditGui} that returns to a fresh {@link AbilityGui} when the back button is clicked.
     *
     * @param mcRPGPlayer The player opening this GUI.
     * @param ability     The ability whose attributes are being edited.
     */
    public AbilityAttributeEditGui(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Ability ability) {
        this(mcRPGPlayer, ability, () -> new AbilityGui(mcRPGPlayer));
    }

    /**
     * Creates an {@link AbilityAttributeEditGui} that returns to the GUI provided by {@code backGuiSupplier}
     * when the back button is clicked. Use this overload when the edit GUI is opened from a context other
     * than the standard {@link AbilityGui} (e.g., {@link InnateAbilityGui}).
     *
     * @param mcRPGPlayer    The player opening this GUI.
     * @param ability        The ability whose attributes are being edited.
     * @param backGuiSupplier A factory that produces the GUI to return to when back is clicked.
     */
    public AbilityAttributeEditGui(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Ability ability, @NotNull Supplier<BaseGui> backGuiSupplier) {
        super(mcRPGPlayer);
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
        this.ability = ability;
        this.backGuiSupplier = backGuiSupplier;
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
            this.inventory = Bukkit.createInventory(player,
                    Math.min(54, Math.max(9, Math.min(54, size % 9 != 0 ? (size / 9) * 9 + 9 : size) + 9)),
                    getCreatingPlayer().getPlugin().registryAccess().registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.LOCALIZATION)
                            .getLocalizedMessageAsComponent(getCreatingPlayer(),
                                    LocalizationKey.ABILITY_EDIT_GUI_TITLE,
                                    Map.of("ability", ability.getColoredName(getCreatingPlayer()))));
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
                    BaseGui backGui = backGuiSupplier.get();
                    player.openInventory(backGui.getInventory());
                    McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, backGui);
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
     * Get a {@link List} of all {@link GuiModifiableAttribute}s for the {@link Ability} being modified,
     * sorted ascending by {@link GuiModifiableAttribute#getDisplayPriority()} so slots always appear in a
     * stable, predictable position regardless of map iteration order.
     *
     * @return A sorted {@link List} of all {@link GuiModifiableAttribute}s for the {@link Ability} being modified.
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
        modifiableAttributes.sort(Comparator.comparingInt(GuiModifiableAttribute::getDisplayPriority));
        return modifiableAttributes;
    }

    @Override
    @NotNull
    public Optional<NamespacedKey> getGuiKey() {
        return Optional.of(GUI_KEY);
    }
}
