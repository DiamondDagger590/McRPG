package us.eunoians.mcrpg.gui.ability;

import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.exception.gui.InventoryAlreadyExistsForGuiException;
import com.diamonddagger590.mccore.gui.ClosableGui;
import com.diamonddagger590.mccore.gui.Guiv2;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;
import us.eunoians.mcrpg.ability.attribute.GuiModifiableAttribute;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This gui is used for whenever an {@link Ability} is having its attributes modified.
 */
public class AbilityEditGui extends Guiv2 implements ClosableGui {

    private final McRPGPlayer mcRPGPlayer;
    private final Player player;
    private final Ability ability;
    private boolean ignoreClose = false;

    public AbilityEditGui(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Ability ability) {
        this.mcRPGPlayer = mcRPGPlayer;
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
        this.ability = ability;
    }

    /**
     * Gets the {@link McRPGPlayer} modifying the ability.
     *
     * @return The {@link McRPGPlayer} modifying the ability.
     */
    @NotNull
    public McRPGPlayer getMcRPGPlayer() {
        return mcRPGPlayer;
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
            this.inventory = Bukkit.createInventory(player, Math.max(9, Math.min(54, size % 9 != 0 ? (size / 9) * 9 + 9 : size)), McRPG.getInstance().getMiniMessage().deserialize("<gold>Editing " + ability.getDisplayName()));
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
        List<GuiModifiableAttribute> modifyableAttributes = getModifiableAttributes();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i < modifyableAttributes.size()) {
                setSlot(i, modifyableAttributes.get(i).getSlot(mcRPGPlayer, ability));
            } else {
                removeSlot(i);
            }
        }
    }

    @Override
    public void onClose(InventoryCloseEvent inventoryCloseEvent) {
        if (!ignoreClose) {
            Player bukkitPlayer = (Player) inventoryCloseEvent.getPlayer();
            var corePlayerOptional = McRPG.getInstance().getPlayerManager().getPlayer(bukkitPlayer.getUniqueId());
            if (corePlayerOptional.isPresent() && corePlayerOptional.get() instanceof McRPGPlayer mcRPGPlayer) {
                AbilityGui abilityGui = new AbilityGui(mcRPGPlayer);
                Bukkit.getScheduler().scheduleSyncDelayedTask(McRPG.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        McRPG.getInstance().getGuiTrackerv2().trackPlayerGui(mcRPGPlayer, abilityGui);
                        bukkitPlayer.openInventory(abilityGui.getInventory());
                    }
                }, 1L);
            }
        }
    }

    /**
     * Set if this gui should not open a new {@link AbilityGui} on close.
     *
     * @param ignoreClose If this gui should not open a new {@link AbilityGui} on close.
     */
    public void setIgnoreClose(boolean ignoreClose) {
        this.ignoreClose = ignoreClose;
    }

    /**
     * Get a {@link List} of all {@link GuiModifiableAttribute}s for the {@link Ability} being modified.
     *
     * @return A {@link List} of all {@link GuiModifiableAttribute}s for the {@link Ability} being modified.
     */
    @NotNull
    private List<GuiModifiableAttribute> getModifiableAttributes() {
        List<GuiModifiableAttribute> modifiableAttributes = new ArrayList<>();
        var abilityDataOptional = mcRPGPlayer.asSkillHolder().getAbilityData(ability);
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
