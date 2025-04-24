package us.eunoians.mcrpg.gui.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.Methods;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityToggledOffAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityUpgradeQuestAttribute;
import us.eunoians.mcrpg.ability.attribute.DisplayableAttribute;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.TierableAbility;
import us.eunoians.mcrpg.ability.impl.UnlockableAbility;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.McRPGGuiManager;
import us.eunoians.mcrpg.gui.ability.AbilityEditGui;
import us.eunoians.mcrpg.gui.ability.AbilityGui;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.registry.plugin.McRPGPluginHookKey;
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
public class AbilitySlot extends McRPGSlot {

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
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        McRPGGuiManager guiManager = mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI);
        var guiOptional = guiManager.getOpenedGui(mcRPGPlayer);
        guiOptional.ifPresent(gui -> {
            var playerOptional = mcRPGPlayer.getAsBukkitPlayer();
            playerOptional.ifPresent(player -> {
                // If the player is using geyser, we have custom logic for them since they don't have right/left clicks. (Or if they just did a left click lol)
                var geyserOptional = mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHook(McRPGPluginHookKey.GEYSER);
                if ((geyserOptional.isPresent() && geyserOptional.get().isBedrockPlayer(mcRPGPlayer.getUUID())) || clickType == ClickType.RIGHT) {
                    AbilityEditGui abilityEditGui = new AbilityEditGui(mcRPGPlayer, ability);
                    player.closeInventory();
                    guiManager.trackPlayerGui(mcRPGPlayer.getUUID(), abilityEditGui);
                    player.openInventory(abilityEditGui.getInventory());
                }
                // If they're on java and right-clicked
                else if (clickType == ClickType.LEFT) {
                    SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
                    skillHolder.getAbilityData(ability).ifPresent(abilityData -> {
                        Optional<AbilityAttribute<?>> abilityAttributeOptional = abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY);
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
    public ItemBuilder getItem(@Nullable McRPGPlayer mcRPGPlayer) {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        SkillRegistry skillRegistry = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.SKILL);
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        Component blankLine = miniMessage.deserialize("");

        ItemStack itemStack = ability.getDisplayItemBuilder(mcRPGPlayer).asItemStack();
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<red>" + ability.getDisplayName(mcRPGPlayer)));

        List<Component> lore = new ArrayList<>();
        // Add skill information
        if (ability.getSkill().isPresent() && skillRegistry.registered(ability.getSkill().get())) {
            Skill skill = skillRegistry.getRegisteredSkill(ability.getSkill().get());
            lore.add(miniMessage.deserialize("<gray>Skill: <gold>" + skill.getDisplayName(mcRPGPlayer)));
        }
        // Add ability description
//        for (String string : ability.getDescription(mcRPGPlayer)) {
//            lore.add(miniMessage.deserialize(string));
//        }
        lore.add(miniMessage.deserialize(""));

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

            // If it is an unlockable ability, display information about unlocking it.
            if (ability instanceof UnlockableAbility unlockableAbility) {
                if (unlockableAbility.isAbilityUnlocked(mcRPGPlayer.asSkillHolder())) {
                    lore.add(miniMessage.deserialize("<gray>You have unlocked this ability."));
                } else {
                    lore.add(miniMessage.deserialize("<gray>Unlock this ability when your <gold>" +
                            skillRegistry.getRegisteredSkill(ability.getSkill().get()).getDisplayName(mcRPGPlayer) + " <gray>skill"));
                    lore.add(miniMessage.deserialize("<gray>reaches level <gold>" + unlockableAbility.getUnlockLevel() + "<gray>."));
                }
            }

            // If it's a tierable ability and also is unlocked if it's an unlocked ability
            if (ability instanceof TierableAbility tierableAbility && tierableAbility.isAbilityUnlocked(skillHolder)) {
                var abilityQuestOptional = abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE);
                // If there is an active quest
                if (abilityQuestOptional.isPresent() && abilityQuestOptional.get() instanceof AbilityUpgradeQuestAttribute questAttribute && questAttribute.shouldContentBeSaved()) {
                    QuestManager questManager = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
                    var questOptional = questManager.getActiveQuest(questAttribute.getContent());
                    if (questOptional.isPresent()) {
                        lore.add(miniMessage.deserialize("<gray>Upgrade Quest Progress: ").append(Methods.getProgressBar(questOptional.get().getQuestProgress(), 20)));
                    } else {
                        throw new IllegalArgumentException("The ability quest for ability " + ability.getDisplayName(mcRPGPlayer) + " was not found.");
                    }
                }
                // If there isn't a quest, check to see if they can upgrade
                else {
                    abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY).ifPresent(abilityAttribute -> {
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
                                                            tierableAbility.getUnlockLevelForTier(nextTier), skill.getDisplayName(mcRPGPlayer))));
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
            Optional<AbilityAttribute<?>> abilityAttributeOptional = abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY);
            if (abilityAttributeOptional.isPresent() && abilityAttributeOptional.get() instanceof AbilityToggledOffAttribute toggledOffAttribute) {
                if (!toggledOffAttribute.getContent()) {
                    itemMeta.addEnchant(Enchantment.POWER, 1, true);
                    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                lore.add(blankLine);
                // If they are on geyser, they only get one option since they can't right/left click
                var geyserHookOptional = mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHook(McRPGPluginHookKey.GEYSER);
                if (geyserHookOptional.isPresent() && geyserHookOptional.get().isBedrockPlayer(mcRPGPlayer.getUUID())) {
                    lore.add(miniMessage.deserialize("<gray>Click to edit ability attributes.</gray"));
                } else {
                    lore.add(miniMessage.deserialize("<gold>Left click</gold><gray> to toggle ability " + (toggledOffAttribute.getContent() ? "<green>on" : "<red>off")));
                    lore.add(miniMessage.deserialize("<gold>Right click</gold><gray> to edit ability attributes.</gray>"));
                }
            }
        }
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return ItemBuilder.from(itemStack);
    }

    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(AbilityGui.class);
    }
}
