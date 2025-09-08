package us.eunoians.mcrpg.quest;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.transaction.FailSafeTransaction;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.audience.Audience;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;
import us.eunoians.mcrpg.database.table.SkillDAO;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

/**
 * A placeholder quest reward that is ran whenever a player finishes their upgrade quest for a given ability
 */
public class UpgradeQuestReward implements QuestReward {

    @Override
    public void giveReward(@NotNull UUID uuid, @NotNull Quest quest) {
        var playerOptional = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(uuid);
        if (playerOptional.isPresent()) {
            McRPGPlayer mcRPGPlayer = playerOptional.get();
            NamespacedKey namespacedKey = new NamespacedKey(McRPG.getInstance(), quest.getConfigKey());
            AbilityHolder abilityHolder = mcRPGPlayer.asSkillHolder();
            abilityHolder.getAbilityData(namespacedKey).ifPresent(abilityData -> {
                abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY).ifPresent(abilityAttribute -> {
                    Ability ability = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(namespacedKey);
                    if (ability instanceof TierableAbility tierableAbility) {
                        SkillHolder skillHolder = (SkillHolder) abilityHolder;
                        int newTier = Math.min(tierableAbility.getMaxTier(), (int) abilityAttribute.getContent() + 1);
                        abilityData.addAttribute(new AbilityTierAttribute(newTier));
                        abilityData.removeAttribute(AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE);
                        Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DATABASE).getDatabase();
                        database.getDatabaseExecutorService().submit(() -> {
                            try (Connection connection = database.getConnection()) {
                                new FailSafeTransaction(connection, SkillDAO.savePlayerAbilityAttributes(connection, skillHolder)).executeTransaction();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        });
                        Audience audience = mcRPGPlayer.getAsBukkitPlayer().get();
                        audience.sendMessage(McRPG.getInstance().getMiniMessage().deserialize(String.format("<green>You have completed the upgrade quest for your <gold>%s ability<green>! It is now tier <gold>%d<green>.", ability.getDisplayName(mcRPGPlayer), newTier)));
                    }
                });
            });
        }
    }
}
