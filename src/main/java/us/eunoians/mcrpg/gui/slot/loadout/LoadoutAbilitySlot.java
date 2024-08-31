package us.eunoians.mcrpg.gui.slot.loadout;

import com.diamonddagger590.mccore.gui.Gui;
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
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;
import us.eunoians.mcrpg.ability.attribute.DisplayableAttribute;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.loadout.LoadoutAbilitySelectGui;
import us.eunoians.mcrpg.gui.loadout.LoadoutGui;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This slot is used to represent a spot in a player's {@link Loadout}.
 * <p>
 * The slot can be empty or represent an {@link Ability} in the loadout.
 * When clicked, it will open a {@link LoadoutAbilitySelectGui} for a player
 * to select a new ability to go into this slot.
 */
public class LoadoutAbilitySlot extends Slot {

    private final McRPGPlayer mcRPGPlayer;
    private final Loadout loadout;
    private final Optional<Ability> abilityOptional;

    public LoadoutAbilitySlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.loadout = loadout;
        this.abilityOptional = Optional.empty();
    }

    public LoadoutAbilitySlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout, @NotNull Ability ability) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.loadout = loadout;
        this.abilityOptional = Optional.of(ability);
    }

    @Override
    public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
        corePlayer.getAsBukkitPlayer().ifPresent(player -> {
            if (abilityOptional.isPresent()) {
                Ability ability = abilityOptional.get();
                LoadoutAbilitySelectGui loadoutAbilitySelectGui = new LoadoutAbilitySelectGui(mcRPGPlayer, loadout, ability.getAbilityKey());
                McRPG.getInstance().getGuiTracker().trackPlayerGui(mcRPGPlayer, loadoutAbilitySelectGui);
                player.openInventory(loadoutAbilitySelectGui.getInventory());
            } else {
                LoadoutAbilitySelectGui loadoutAbilitySelectGui = new LoadoutAbilitySelectGui(mcRPGPlayer, loadout);
                McRPG.getInstance().getGuiTracker().trackPlayerGui(mcRPGPlayer, loadoutAbilitySelectGui);
                player.openInventory(loadoutAbilitySelectGui.getInventory());
            }
        });

        return true;
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        ItemStack itemStack;
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        if (abilityOptional.isPresent()) {
            Ability ability = abilityOptional.get();
            SkillRegistry skillRegistry = McRPG.getInstance().getSkillRegistry();
            SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
            itemStack = ability.getGuiItem(skillHolder);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(miniMessage.deserialize("<red>" + ability.getDisplayName()));

            List<Component> lore = new ArrayList<>();
            // Add skill information
            if (ability.getSkill().isPresent() && skillRegistry.isSkillRegistered(ability.getSkill().get())) {
                Skill skill = skillRegistry.getRegisteredSkill(ability.getSkill().get());
                lore.add(miniMessage.deserialize("<gray>Skill: <gold>" + skill.getDisplayName()));
            }
            // Add information about specific ability attributes
            Optional<AbilityData> abilityDataOptional = skillHolder.getAbilityData(ability);
            if (abilityDataOptional.isPresent()) {
                AbilityData abilityData = abilityDataOptional.get();
                for (AbilityAttribute<?> abilityAttribute : abilityData.getAllAttributes()) {
                    // If the attribute can be displayed
                    if (abilityAttribute instanceof DisplayableAttribute displayableAttribute) {
                        lore.add(miniMessage.deserialize("<gray>" + displayableAttribute.getDisplayName() + ": <gold>" + abilityAttribute.getContent()));
                    }
                }
            }
            lore.add(miniMessage.deserialize(""));
            lore.add(miniMessage.deserialize("<gray>Click to change what ability is in this slot."));
            itemMeta.lore(lore);
            itemStack.setItemMeta(itemMeta);
        } else {
            itemStack = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(miniMessage.deserialize("<red>Empty Loadout Slot"));
            itemMeta.lore(List.of(miniMessage.deserialize("<gray>Click to change what ability is in this slot.")));
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    @Override
    public Set<Class<? extends Gui>> getValidGuiTypes() {
        return Set.of(LoadoutGui.class);
    }
}
