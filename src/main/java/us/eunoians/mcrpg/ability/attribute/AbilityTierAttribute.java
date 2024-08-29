package us.eunoians.mcrpg.ability.attribute;

import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.util.Methods;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.TierableAbility;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This attribute stores the tier for an ability.
 */
public class AbilityTierAttribute extends OptionalSavingAbilityAttribute<Integer> implements DisplayableAttribute, GuiModifiableAttribute {

    AbilityTierAttribute() {
        super("tier", AbilityAttributeManager.ABILITY_TIER_ATTRIBUTE_KEY);
    }

    public AbilityTierAttribute(@NotNull Integer content) {
        super("tier", AbilityAttributeManager.ABILITY_TIER_ATTRIBUTE_KEY, content);
    }

    /**
     * Creates a new instance of this {@link AbilityTierAttribute} class, containing the provided {@link Integer} content as the value
     *
     * @param tier The {@link Integer} content to be used as the value in the returned {@link AbilityTierAttribute}
     * @return A new instance of this {@link AbilityTierAttribute} class, containing the provided {@link Integer} content as the value
     */
    @NotNull
    @Override
    public AbilityTierAttribute create(@NotNull Integer tier) {
        return new AbilityTierAttribute(tier);
    }

    /**
     * Converts the provided {@link String} content into content that matches the type of {@link Integer}.
     * <p>
     * This serves to allow abstraction to exist and all values to be stored as strings inside of {@link us.eunoians.mcrpg.database.table.SkillDAO}.
     *
     * @param stringContent The {@link String} content to be converted into type {@link Integer}
     * @return The {@link String} content that is now converted into {@link Integer} content
     */
    @NotNull
    @Override
    public Integer convertContent(@NotNull String stringContent) {
        return Integer.parseInt(stringContent);
    }

    /**
     * Gets the default content value for this attribute. This should be considered the "default state" for this attribute, such
     * as a tier defaulting to 0.
     * <p>
     * The largest use case for this is populating {@link AbilityAttributeManager} with initial instances of this class, which can then
     * be built on using {@link #create(Integer)}.
     *
     * @return {@code 0} as an {@link Integer}.
     */
    @NotNull
    @Override
    public Integer getDefaultContent() {
        return 1;
    }

    @Override
    public boolean shouldContentBeSaved() {
        return getContent() > 1;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Tier";
    }

    @NotNull
    @Override
    public List<Component> getGuiLore(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Ability ability) {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        SkillRegistry skillRegistry = McRPG.getInstance().getSkillRegistry();
        List<Component> lore = new ArrayList<>();
        Optional<AbilityData> abilityDataOptional = skillHolder.getAbilityData(ability);
        Component blankLine = miniMessage.deserialize("");
        if (abilityDataOptional.isPresent()) {
            AbilityData abilityData = abilityDataOptional.get();
            // If it's a tierable ability
            if (ability instanceof TierableAbility tierableAbility) {
                lore.add(miniMessage.deserialize("<gray>Tier: <gold>" + getContent()));
                // Check if it's unlocked
                if (abilityData.getAbilityAttribute(AbilityAttributeManager.ABILITY_UNLOCKED_ATTRIBUTE)
                        .map(value -> value instanceof AbilityUnlockedAttribute attribute && attribute.getContent()).orElse(true)) {
                    var abilityQuestOptional = abilityData.getAbilityAttribute(AbilityAttributeManager.ABILITY_QUEST_ATTRIBUTE);
                    // If there is an active quest
                    if (abilityQuestOptional.isPresent() && abilityQuestOptional.get() instanceof AbilityUpgradeQuestAttribute questAttribute && questAttribute.shouldContentBeSaved()) {
                        QuestManager questManager = McRPG.getInstance().getQuestManager();
                        var questOptional = questManager.getActiveQuest(questAttribute.getContent());
                        if (questOptional.isPresent()) {
                            lore.add(blankLine);
                            lore.add(miniMessage.deserialize("<gray>Upgrade Quest Progress: ").append(Methods.getProgressBar(questOptional.get().getQuestProgress(), 20)));
                        } else {
                            throw new IllegalArgumentException("The ability quest for ability " + ability.getDisplayName() + " was not found.");
                        }
                    }
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
                                                    lore.add(blankLine);
                                                    lore.add(miniMessage.deserialize(String.format("<green>Click to spend <gold>%s upgrade points<green> to start upgrade quest.", upgradeCost)));
                                                }
                                                // If they don't have enough, tell them how many they need
                                                else {
                                                    lore.add(blankLine);
                                                    lore.add(miniMessage.deserialize(String.format("<gray>You need <gold>%s upgrade points<gray> to start the upgrade quest.", upgradeCost)));
                                                }
                                                lore.add(miniMessage.deserialize("<gray>You currently have <gold>" + skillHolder.getUpgradePoints() + "</gold> upgrade points."));
                                            }
                                            // Otherwise tell the player the level they need to reach
                                            else {
                                                lore.add(blankLine);
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
                                            lore.add(blankLine);
                                            lore.add(miniMessage.deserialize(String.format("<green>Click to spend <gold>%s upgrade points<green> to start upgrade quest.", upgradeCost)));
                                        }
                                        // If they don't have enough, tell them how many they need
                                        else {
                                            lore.add(blankLine);
                                            lore.add(miniMessage.deserialize(String.format("<gray>You need <gold>%s upgrade points<gray> to start the upgrade quest.", upgradeCost)));
                                        }
                                        lore.add(miniMessage.deserialize("<gray>You currently have <gold>" + skillHolder.getUpgradePoints() + "</gold> upgrade points."));
                                    }
                                }
                            }
                        });
                    }
                }
                else {
                    lore.add(blankLine);
                    lore.add(miniMessage.deserialize("<gray>Unlock this ability when your <gold>" +
                            skillRegistry.getRegisteredSkill(ability.getSkill().get()).getDisplayName() + " <gray>skill"));
                    lore.add(miniMessage.deserialize("<gray>reaches level <gold>" + tierableAbility.getUnlockLevel() + "<gray>."));
                }
            }
        }
        return lore;
    }

    @NotNull
    @Override
    public Slot getSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Ability ability) {
        return new Slot() {
            @Override
            public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
                if (corePlayer instanceof McRPGPlayer player &&
                        ability instanceof TierableAbility tierableAbility &&
                        player.canPlayerStartUpgradeQuest(tierableAbility)) {
                    player.startUpgradeQuest(tierableAbility);
                }
                return true;
            }

            @NotNull
            @Override
            public ItemStack getItem() {
                MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
                ItemStack itemStack = new ItemStack(ability instanceof TierableAbility ? Material.IRON_INGOT : Material.AIR);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.displayName(miniMessage.deserialize("<gold>Ability Tier Upgrade</gold>"));
                itemMeta.lore(getGuiLore(mcRPGPlayer, ability));
                itemStack.setItemMeta(itemMeta);
                return itemStack;
            }
        };
    }
}
