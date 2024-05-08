package us.eunoians.mcrpg.ability.impl.swords;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.TierableAbility;
import us.eunoians.mcrpg.api.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.api.event.ability.swords.EnhancedBleedActivateEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.quest.Quest;
import us.eunoians.mcrpg.quest.UpgradeQuestReward;
import us.eunoians.mcrpg.quest.objective.EntitySlayQuestObjective;
import us.eunoians.mcrpg.skill.impl.swords.Swords;

import java.util.Optional;

/**
 * This ability is an unlockable ability for {@link Swords} that
 * can increase the damage per tick for the {@link Bleed} ability
 */
public class EnhancedBleed extends TierableAbility {

    public static final NamespacedKey ENHANCED_BLEED_KEY = new NamespacedKey(McRPG.getInstance(), "enhanced_bleed");

    public EnhancedBleed() {
        super(ENHANCED_BLEED_KEY);
        addActivatableComponent(EnhancedBleedComponents.ENHANCED_BLEED_ACTIVATE_COMPONENT, BleedActivateEvent.class, 0);
    }

    @Override
    public Optional<NamespacedKey> getSkill() {
        return Optional.of(Swords.SWORDS_KEY);
    }

    @Override
    public Optional<String> getLegacyName() {
        return Optional.of("Bleed+");
    }

    @Override
    public Optional<String> getDatabaseName() {
        return Optional.empty();
    }

    @Override
    public String getDisplayName() {
        return "Poisoned Bleed";
    }

    @Override
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        return new ItemStack(Material.SPIDER_EYE);
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        BleedActivateEvent bleedActivateEvent = (BleedActivateEvent) event;

        EnhancedBleedActivateEvent enhancedBleedActivateEvent = new EnhancedBleedActivateEvent(abilityHolder, bleedActivateEvent.getBleedingEntity(), 2);
        Bukkit.getPluginManager().callEvent(enhancedBleedActivateEvent);

        if(!enhancedBleedActivateEvent.isCancelled()) {
            bleedActivateEvent.setBleedDamage(bleedActivateEvent.getBleedDamage() + enhancedBleedActivateEvent.getAdditionalBleedDamage());
        }
    }

    @Override
    public int getMaxTier() {
        return 5;
    }

    @Override
    public int getUnlockLevelForTier(int tier) {
        return 10;
    }

    @Override
    public int getUpgradeCostForTier(int tier) {
        return 1;
    }

    @Override
    public Quest getUpgradeQuestForTier(int tier) {
        Quest quest = new Quest(getAbilityKey().getKey());
        quest.addQuestReward(new UpgradeQuestReward());
        EntitySlayQuestObjective objective = new EntitySlayQuestObjective(quest, 10 * tier);
        quest.addQuestObjective(objective);
        return quest;
    }

    @Override
    public int getUnlockLevel() {
        return 2;
    }
}
