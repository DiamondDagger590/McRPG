package us.eunoians.mcrpg.listener.ability;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.transaction.FailSafeTransaction;
import com.diamonddagger590.mccore.event.player.PlayerLoadEvent;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityUnlockedAttribute;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.database.table.SkillDAO;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.AbilityUnlockEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Listens for {@link PlayerLoadEvent} and runs the unlock-condition sweep for every
 * registered {@link UnlockableAbility} the player has data for and has not unlocked. For each
 * ability where {@link UnlockableAbility#isAnyConditionMet(us.eunoians.mcrpg.entity.holder.AbilityHolder)}
 * is satisfied, the listener flips {@code AbilityUnlockedAttribute} and fires
 * {@link AbilityUnlockEvent} — the same flow as {@code OnSkillLevelUpListener}, just on a
 * different trigger. Resolves issue #220.
 */
public class OnPlayerLoadUnlockSweepListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLoad(PlayerLoadEvent event) {
        if (!(event.getCorePlayer() instanceof McRPGPlayer mcRPGPlayer)) {
            return;
        }
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        AbilityRegistry abilityRegistry = McRPG.getInstance().registryAccess()
                .registry(McRPGRegistryKey.ABILITY);
        for (NamespacedKey abilityKey : abilityRegistry.getAllAbilities()) {
            Ability ability = abilityRegistry.getRegisteredAbility(abilityKey);
            if (!(ability instanceof UnlockableAbility unlockable)) {
                continue;
            }
            if (unlockable.isAbilityUnlocked(skillHolder) || !unlockable.isAnyConditionMet(skillHolder)) {
                continue;
            }
            flipAttributeAndFire(skillHolder, unlockable);
        }
    }

    private void flipAttributeAndFire(@NotNull SkillHolder skillHolder, @NotNull UnlockableAbility unlockable) {
        var abilityDataOptional = skillHolder.getAbilityData(unlockable);
        if (abilityDataOptional.isEmpty()) {
            return;
        }
        AbilityData abilityData = abilityDataOptional.get();
        var attributeOptional = abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE);
        if (attributeOptional.isEmpty() || !(attributeOptional.get() instanceof AbilityUnlockedAttribute attribute)) {
            return;
        }
        if (attribute.getContent()) {
            return;
        }
        AbilityUnlockEvent abilityUnlockEvent = new AbilityUnlockEvent(skillHolder, unlockable);
        Bukkit.getPluginManager().callEvent(abilityUnlockEvent);
        abilityData.updateAttribute(attribute, true);

        Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                new FailSafeTransaction(connection,
                        SkillDAO.savePlayerSkillData(connection, skillHolder)).executeTransaction();
            } catch (SQLException e) {
                McRPG.getInstance().getLogger().log(Level.SEVERE,
                        "Failed to save unlocked ability " + unlockable.getAbilityKey()
                                + " for player " + skillHolder.getUUID(), e);
            }
        });
    }
}
