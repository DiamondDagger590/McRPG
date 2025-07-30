package us.eunoians.mcrpg.gui.slot.loadout;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;
import us.eunoians.mcrpg.ability.attribute.DisplayableAttribute;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.type.SkillAbility;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.loadout.LoadoutAbilitySelectGui;
import us.eunoians.mcrpg.gui.loadout.LoadoutGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
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
public class LoadoutAbilitySlot implements McRPGSlot {

    private final McRPGPlayer mcRPGPlayer;
    private final Loadout loadout;
    @Nullable
    private final Ability ability;

    public LoadoutAbilitySlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.loadout = loadout;
        this.ability = null;
    }

    public LoadoutAbilitySlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout, @NotNull Ability ability) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.loadout = loadout;
        this.ability = ability;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            if (ability != null) {
                LoadoutAbilitySelectGui loadoutAbilitySelectGui = new LoadoutAbilitySelectGui(mcRPGPlayer, loadout, ability.getAbilityKey());
                mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, loadoutAbilitySelectGui);
                player.openInventory(loadoutAbilitySelectGui.getInventory());
            } else {
                LoadoutAbilitySelectGui loadoutAbilitySelectGui = new LoadoutAbilitySelectGui(mcRPGPlayer, loadout);
                mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, loadoutAbilitySelectGui);
                player.openInventory(loadoutAbilitySelectGui.getInventory());
            }
        });

        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        ItemStack itemStack;
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        if (ability != null) {
            SkillRegistry skillRegistry = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.SKILL);
            SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
            itemStack = ability.getDisplayItemBuilder(mcRPGPlayer).asItemStack(McRPG.getInstance().getAdventure().player(mcRPGPlayer.getUUID()));
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(miniMessage.deserialize("<red>" + ability.getDisplayName(mcRPGPlayer)));

            List<Component> lore = new ArrayList<>();
            // Add skill information
            if (ability instanceof SkillAbility skillAbility) {
                Skill skill = skillRegistry.getRegisteredSkill(skillAbility.getSkillKey());
                lore.add(miniMessage.deserialize("<gray>Skill: <gold>" + skill.getDisplayName(mcRPGPlayer)));
            }
            // Add information about specific ability attributes
            Optional<AbilityData> abilityDataOptional = skillHolder.getAbilityData(ability);
            if (abilityDataOptional.isPresent()) {
                AbilityData abilityData = abilityDataOptional.get();
                for (AbilityAttribute<?> abilityAttribute : abilityData.getAllAttributes()) {
                    // If the attribute can be displayed
                    if (abilityAttribute instanceof DisplayableAttribute displayableAttribute) {
                        lore.add(miniMessage.deserialize("<gray>" + displayableAttribute.getPlaceholderName() + ": <gold>" + abilityAttribute.getContent()));
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
        return ItemBuilder.from(itemStack);
    }

    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(LoadoutGui.class);
    }
}
