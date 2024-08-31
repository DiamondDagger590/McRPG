package us.eunoians.mcrpg.gui.slot;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.util.Methods;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.geysermc.api.Geyser;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityToggledOffAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityUpgradeQuestAttribute;
import us.eunoians.mcrpg.ability.attribute.DisplayableAttribute;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.TierableAbility;
import us.eunoians.mcrpg.ability.impl.UnlockableAbility;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.ability.AbilityEditGui;
import us.eunoians.mcrpg.gui.ability.AbilityGui;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This slot is used in {@link us.eunoians.mcrpg.gui.ability.AbilityGui}s to represent an {@link Ability}
 * while providing click actions for said ability.
 */
public class AbilitySlot extends Slot {

    private final McRPGPlayer mcRPGPlayer;
    private final Ability ability;

    public AbilitySlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Ability ability) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.ability = ability;
    }

    /**
     * Gets the {@link McRPGPlayer} creating this slot.
     *
     * @return The {@link McRPGPlayer} creating this slot.
     */
    @NotNull
    public McRPGPlayer getMcRPGPlayer() {
        return mcRPGPlayer;
    }

    /**
     * Gets the {@link Ability} represented by this slot.
     *
     * @return The {@link Ability} represented by this slot.
     */
    @NotNull
    public Ability getAbility() {
        return ability;
    }

    @Override
    public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
        var guiOptional = CorePlugin.getInstance().getGuiTracker().getOpenedGui(corePlayer);
        guiOptional.ifPresent(gui -> {
            var playerOptional = mcRPGPlayer.getAsBukkitPlayer();
            playerOptional.ifPresent(player -> {
                // If the player is using geyser, we have custom logic for them since they don't have right/left clicks. (Or if they just did a left click lol)
                if ((McRPG.getInstance().isGeyserEnabled() && Geyser.api().isBedrockPlayer(corePlayer.getUUID())) || clickType == ClickType.RIGHT) {
                    AbilityEditGui abilityEditGui = new AbilityEditGui(mcRPGPlayer, ability);
                    player.closeInventory();
                    McRPG.getInstance().getGuiTracker().trackPlayerGui(mcRPGPlayer.getUUID(), abilityEditGui);
                    player.openInventory(abilityEditGui.getInventory());
                }
                // If they're on java and right-clicked
                else if (clickType == ClickType.LEFT) {
                    SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
                    skillHolder.getAbilityData(ability).ifPresent(abilityData -> {
                        Optional<AbilityAttribute<?>> abilityAttributeOptional = abilityData.getAbilityAttribute(AbilityAttributeManager.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY);
                        if (abilityAttributeOptional.isPresent() && abilityAttributeOptional.get() instanceof AbilityToggledOffAttribute toggledOffAttribute) {
                            AbilityToggledOffAttribute abilityToggledOffAttribute = new AbilityToggledOffAttribute(!toggledOffAttribute.getContent());
                            abilityData.addAttribute(abilityToggledOffAttribute);
                            gui.refreshGUI();
                        }
                    });
                }
            });
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

            // If it's a tierable ability and also is unlocked if it's an unlocked ability
            if (ability instanceof TierableAbility tierableAbility && tierableAbility.isAbilityUnlocked(skillHolder)) {
                var abilityQuestOptional = abilityData.getAbilityAttribute(AbilityAttributeManager.ABILITY_QUEST_ATTRIBUTE);
                // If there is an active quest
                if (abilityQuestOptional.isPresent() && abilityQuestOptional.get() instanceof AbilityUpgradeQuestAttribute questAttribute && questAttribute.shouldContentBeSaved()) {
                    QuestManager questManager = McRPG.getInstance().getQuestManager();
                    var questOptional = questManager.getActiveQuest(questAttribute.getContent());
                    if (questOptional.isPresent()) {
                        lore.add(miniMessage.deserialize("<gray>Upgrade Quest Progress: ").append(Methods.getProgressBar(questOptional.get().getQuestProgress(), 20)));
                    } else {
                        throw new IllegalArgumentException("The ability quest for ability " + ability.getDisplayName() + " was not found.");
                    }
                }
                // If there isn't a quest, check to see if they can upgrade
                else {
                    abilityData.getAbilityAttribute(AbilityAttributeManager.ABILITY_TIER_ATTRIBUTE_KEY).ifPresent(abilityAttribute -> {
                        if (abilityAttribute instanceof AbilityTierAttribute abilityTierAttribute) {
                            int tier = abilityTierAttribute.getContent();
                            int nextTier = tier + 1;
                            int upgradeCost = tierableAbility.getUpgradeCostForTier(nextTier);
                            // If the ability isn't the max tier
                            if (tierableAbility.getMaxTier() > tier) {
                                // If the ability has a skill it belongs to
                                if (tierableAbility.getSkill().isPresent()) {
                                    var skillDataOptional = skillHolder.getSkillHolderData(tierableAbility.getSkill().get());
                                    if (skillDataOptional.isPresent()) {
                                        Skill skill = skillRegistry.getRegisteredSkill(ability.getSkill().get());
                                        int currentLevel = skillDataOptional.get().getCurrentLevel();
                                        // If the current skill level is above the unlock level
                                        if (currentLevel >= tierableAbility.getUnlockLevelForTier(nextTier)) {
                                            // If they have enough upgrade points, tell them they can click
                                            if (skillHolder.getUpgradePoints() >= upgradeCost) {
                                                lore.add(miniMessage.deserialize(String.format("<green>Click to spend <gold>%s upgrade points<green> to start upgrade quest.", upgradeCost)));
                                            }
                                            // If they don't have enough, tell them how many they need
                                            else {
                                                lore.add(miniMessage.deserialize(String.format("<gray>You need <gold>%s upgrade points<gray> to start the upgrade quest.", upgradeCost)));
                                            }
                                        }
                                        // Otherwise tell the player the level they need to reach
                                        else {
                                            lore.add(miniMessage.deserialize(
                                                    String.format("<gray>You can upgrade this ability once you reach <gold>Lv %d<gray> in <gold>%s<gray>.",
                                                            tierableAbility.getUnlockLevelForTier(nextTier), skill.getDisplayName())));
                                        }
                                    }
                                }
                                // If the ability doesn't have a skill, we only care about upgrade cost
                                else {
                                    // If they have enough upgrade points, tell them they can click
                                    if (skillHolder.getUpgradePoints() >= upgradeCost) {
                                        lore.add(miniMessage.deserialize(String.format("<green>Click to spend <gold>%s upgrade points<green> to start upgrade quest.", upgradeCost)));
                                    }
                                    // If they don't have enough, tell them how many they need
                                    else {
                                        lore.add(miniMessage.deserialize(String.format("<gray>You need <gold>%s upgrade points<gray> to start the upgrade quest.", upgradeCost)));
                                    }
                                }
                            }
                        }
                    });
                }
            }

            // Custom handling of toggled since we enchant toggled on items
            Optional<AbilityAttribute<?>> abilityAttributeOptional = abilityData.getAbilityAttribute(AbilityAttributeManager.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY);
            if (abilityAttributeOptional.isPresent() && abilityAttributeOptional.get() instanceof AbilityToggledOffAttribute toggledOffAttribute) {
                if (!toggledOffAttribute.getContent()) {
                    itemMeta.addEnchant(Enchantment.POWER, 1, true);
                    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                lore.add(blankLine);
                // If they are on geyser, they only get one option since they can't right/left click
                if (McRPG.getInstance().isGeyserEnabled() && Geyser.api().isBedrockPlayer(mcRPGPlayer.getUUID())) {
                    lore.add(miniMessage.deserialize("<gray>Click to edit ability attributes.</gray"));
                } else {
                    lore.add(miniMessage.deserialize("<gold>Left click</gold><gray> to toggle ability " + (toggledOffAttribute.getContent() ? "<green>on" : "<red>off")));
                    lore.add(miniMessage.deserialize("<gold>Right click</gold><gray> to edit ability attributes.</gray>"));
                }
            }
        }
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public Set<Class<? extends Gui>> getValidGuiTypes() {
        return Set.of(AbilityGui.class);
    }
}
