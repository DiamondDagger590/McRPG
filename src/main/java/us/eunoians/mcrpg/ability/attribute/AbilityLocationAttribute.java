package us.eunoians.mcrpg.ability.attribute;

import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.util.Methods;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This attribute stores a {@link Location} value for a given ability
 */
public class AbilityLocationAttribute extends OptionalSavingAbilityAttribute<Location> implements GuiModifiableAttribute {

    private static final Location DEFAULT_LOCATION = new Location(null, 0, 0 ,0);

    AbilityLocationAttribute() {
        super("location", AbilityAttributeManager.ABILITY_LOCATION_ATTRIBUTE);
    }

    public AbilityLocationAttribute(@NotNull Location content) {
        super("location", AbilityAttributeManager.ABILITY_LOCATION_ATTRIBUTE, content);
    }

    @Override
    public boolean shouldContentBeSaved() {
        return getContent().getWorld() != null;
    }

    @NotNull
    @Override
    public AbilityAttribute<Location> create(@NotNull Location content) {
        return new AbilityLocationAttribute(content);
    }

    @NotNull
    @Override
    public Location convertContent(@NotNull String stringContent) {
        Optional<Location> optionalLocation = Methods.deserializeLocation(stringContent);
        if (optionalLocation.isEmpty()) {
            throw new RuntimeException("Expected a location to be able to be deserialized, but it wasn't. Serialized content was :" + stringContent);
        }
        return optionalLocation.get();
    }

    @NotNull
    @Override
    public Location getDefaultContent() {
        return DEFAULT_LOCATION;
    }

    @NotNull
    @Override
    public String serializeContent() {
        return Methods.serializeLocation(getContent());
    }

    @NotNull
    @Override
    public Slot getSlot(@NotNull McRPGPlayer player, @NotNull Ability ability) {
        return new Slot() {
            @Override
            public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
                return false;
            }

            @NotNull
            @Override
            public ItemStack getItem() {
                ItemStack itemStack = new ItemStack(Material.CHERRY_SIGN);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.displayName(McRPG.getInstance().getMiniMessage().deserialize("<gold>Bound Location"));
                itemMeta.lore(getGuiLore(player, ability));
                itemStack.setItemMeta(itemMeta);
                return itemStack;
            }
        };
    }

    @NotNull
    @Override
    public List<Component> getGuiLore(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Ability ability) {
        List<Component> lore = new ArrayList<>();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        if (shouldContentBeSaved()) {
            lore.add(miniMessage.deserialize("<gray>This ability's bound location is:"));
            lore.add(miniMessage.deserialize("<gray>X: <gold>" + getContent().getX()));
            lore.add(miniMessage.deserialize("<gray>Y: <gold>" + getContent().getY()));
            lore.add(miniMessage.deserialize("<gray>Z: <gold>" + getContent().getZ()));
            lore.add(miniMessage.deserialize("<gray>World: <gold>" + getContent().getWorld()));
        }
        else {
            lore.add(miniMessage.deserialize("<gray>You currently don't have a bound location for this ability."));
        }
        return lore;
    }
}
