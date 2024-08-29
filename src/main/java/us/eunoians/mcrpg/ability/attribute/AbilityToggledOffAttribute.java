package us.eunoians.mcrpg.ability.attribute;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.gui.Guiv2;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.List;
import java.util.Optional;

/**
 * This attribute stores if an ability is toggled off or not. So a value of
 * {@code true} would mean the ability is toggled off.
 */
public class AbilityToggledOffAttribute extends OptionalSavingAbilityAttribute<Boolean> implements GuiModifiableAttribute {

    AbilityToggledOffAttribute() {
        super("toggled", AbilityAttributeManager.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY);
    }

    public AbilityToggledOffAttribute(@NotNull Boolean content) {
        super("toggled", AbilityAttributeManager.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY, content);
    }

    @NotNull
    @Override
    public AbilityAttribute<Boolean> create(@NotNull Boolean content) {
        return new AbilityToggledOffAttribute(content);
    }

    @NotNull
    @Override
    public Boolean convertContent(@NotNull String stringContent) {
        return Boolean.parseBoolean(stringContent);
    }

    @NotNull
    @Override
    public Boolean getDefaultContent() {
        return false;
    }

    @Override
    public boolean shouldContentBeSaved() {
        return getContent();
    }

    @NotNull
    @Override
    public List<Component> getGuiLore(@NotNull McRPGPlayer player, @NotNull Ability ability) {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        return List.of(miniMessage.deserialize(String.format("<gray>Ability is currently %s.</gray>", getContent() ? "<red>DISABLED</red>" : "<green>ENABLED</green>")),
                miniMessage.deserialize(String.format("<gray>Click to %s this ability.", getContent() ? "<green>enable</green>" : "<red>disable</red>")));
    }

    @Override
    @NotNull
    public Slot getSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Ability ability) {
        return new Slot() {
            @Override
            public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
                SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
                skillHolder.getAbilityData(ability).ifPresent(abilityData -> {
                    Optional<AbilityAttribute<?>> abilityAttributeOptional = abilityData.getAbilityAttribute(AbilityAttributeManager.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY);
                    if (abilityAttributeOptional.isPresent() && abilityAttributeOptional.get() instanceof AbilityToggledOffAttribute toggledOffAttribute) {
                        AbilityToggledOffAttribute abilityToggledOffAttribute = new AbilityToggledOffAttribute(!toggledOffAttribute.getContent());
                        abilityData.addAttribute(abilityToggledOffAttribute);
                    }
                });
                CorePlugin.getInstance().getGuiTrackerv2().getOpenedGui(corePlayer).ifPresent(Guiv2::refreshGUI);
                return true;
            }

            @NotNull
            @Override
            public ItemStack getItem() {
                MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
                ItemStack itemStack = new ItemStack(getContent() ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.displayName(miniMessage.deserialize("<gold>Ability Toggle Status</gold>"));
                itemMeta.lore(getGuiLore(mcRPGPlayer, ability));
                itemStack.setItemMeta(itemMeta);
                return itemStack;
            }
        };
    }
}
