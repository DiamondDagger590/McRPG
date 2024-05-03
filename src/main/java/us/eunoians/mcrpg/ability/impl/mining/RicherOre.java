package us.eunoians.mcrpg.ability.impl.mining;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.TierableAbility;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.quest.Quest;
import us.eunoians.mcrpg.quest.objective.BlockBreakQuestObjective;
import us.eunoians.mcrpg.skill.impl.mining.Mining;

import java.util.Optional;

public class RicherOre extends TierableAbility {

    public static final NamespacedKey RICHER_ORE_KEY = new NamespacedKey(McRPG.getInstance(), "richer_ore");

    public RicherOre() {
        super(RICHER_ORE_KEY);
    }

    @Override
    public Optional<NamespacedKey> getSkill() {
        return Optional.of(Mining.MINING_KEY);
    }

    @Override
    public Optional<String> getDatabaseName() {
        return Optional.empty();
    }

    @Override
    public String getDisplayName() {
        return "Richer Ore";
    }

    @Override
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        return new ItemStack(Material.DIAMOND_ORE);
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {

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
        Quest quest = new Quest("configpath");
        BlockBreakQuestObjective objective = new BlockBreakQuestObjective(quest, 10 * tier);
        quest.addQuestObjective(objective);
        return quest;
    }

    @Override
    public int getUnlockLevel() {
        return 1;
    }
}
