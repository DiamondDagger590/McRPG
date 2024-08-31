package us.eunoians.mcrpg.gui.slot.loadout;

import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.attribute.AbilityToggledOffAttribute;
import us.eunoians.mcrpg.ability.attribute.DisplayableAttribute;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.UnlockableAbility;
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
 * This slot is used to select an {@link Ability} to go into a player's {@link Loadout}.
 */
public class LoadoutSelectAbilitySlot extends Slot {

    private final McRPGPlayer mcRPGPlayer;
    private final Loadout loadout;
    private final Ability ability;
    private final Optional<NamespacedKey> oldAbilityKey;

    public LoadoutSelectAbilitySlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout, @NotNull Ability ability) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.loadout = loadout;
        this.ability = ability;
        this.oldAbilityKey = Optional.empty();
    }

    public LoadoutSelectAbilitySlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout, @NotNull Ability ability, @NotNull NamespacedKey oldAbilityKey) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.loadout = loadout;
        this.ability = ability;
        this.oldAbilityKey = Optional.of(oldAbilityKey);
    }

    @Override
    public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
        corePlayer.getAsBukkitPlayer().ifPresent(player -> {
            if (oldAbilityKey.isPresent()) {
                loadout.replaceAbility(oldAbilityKey.get(), ability.getAbilityKey());
            } else if (loadout.getRemainingLoadoutSize() > 0) {
                loadout.addAbility(ability.getAbilityKey());
            }
            LoadoutGui loadoutGui = new LoadoutGui(mcRPGPlayer, loadout);
            McRPG.getInstance().getGuiTracker().trackPlayerGui(mcRPGPlayer, loadoutGui);
            player.openInventory(loadoutGui.getInventory());
        });
        return true;
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        SkillRegistry skillRegistry = McRPG.getInstance().getSkillRegistry();
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        Component blankLine = miniMessage.deserialize("");

        ItemStack itemStack = ability.getGuiItem(mcRPGPlayer.asSkillHolder());
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<red>" + ability.getDisplayName()));

        List<Component> lore = new ArrayList<>();
        // Add skill information
        if (ability.getSkill().isPresent() && skillRegistry.isSkillRegistered(ability.getSkill().get())) {
            Skill skill = skillRegistry.getRegisteredSkill(ability.getSkill().get());
            lore.add(miniMessage.deserialize("<gray>Skill: <gold>" + skill.getDisplayName()));
        }
        // Add ability description
        for (String string : ability.getDescription(mcRPGPlayer)) {
            lore.add(miniMessage.deserialize(string));
        }
        lore.add(miniMessage.deserialize(""));
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

            // If it is an unlockable ability, display information about unlocking it.
            if (ability instanceof UnlockableAbility unlockableAbility) {
                if (unlockableAbility.isAbilityUnlocked(mcRPGPlayer.asSkillHolder())) {
                    lore.add(miniMessage.deserialize("<gray>You have unlocked this ability."));
                } else {
                    lore.add(miniMessage.deserialize("<gray>Unlock this ability when your <gold>" +
                            skillRegistry.getRegisteredSkill(ability.getSkill().get()).getDisplayName() + " <gray>skill"));
                    lore.add(miniMessage.deserialize("<gray>reaches level <gold>" + unlockableAbility.getUnlockLevel() + "<gray>."));
                }
            }

            // Custom handling of toggled since we enchant toggled on items
            Optional<AbilityAttribute<?>> abilityAttributeOptional = abilityData.getAbilityAttribute(AbilityAttributeManager.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY);
            if (abilityAttributeOptional.isPresent() && abilityAttributeOptional.get() instanceof AbilityToggledOffAttribute toggledOffAttribute) {
                if (!toggledOffAttribute.getContent()) {
                    itemMeta.addEnchant(Enchantment.POWER, 1, true);
                    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
            }
            lore.add(miniMessage.deserialize("<gray>Click to add this ability to your loadout.</gray>"));
        }
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public Set<Class<? extends Gui>> getValidGuiTypes() {
        return Set.of(LoadoutAbilitySelectGui.class);
    }
}
