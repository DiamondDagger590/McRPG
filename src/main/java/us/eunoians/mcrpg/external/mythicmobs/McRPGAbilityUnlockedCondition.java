package us.eunoians.mcrpg.external.mythicmobs;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityUnlockedAttribute;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;

/**
 * A custom MythicMobs condition that checks whether a player has unlocked
 * the specified McRPG ability.
 * <p>
 * Registered as {@code mcrpg_ability_unlocked} in MythicMobs. Used in
 * DropTable {@code TriggerConditions} to vary drop rates based on unlock state.
 * <p>
 * Configuration:
 * <pre>
 *   TriggerConditions:
 *   - mcrpg_ability_unlocked{ability=mcrpg:whirlpool} true
 * </pre>
 */
public class McRPGAbilityUnlockedCondition implements IEntityCondition {

    private final NamespacedKey abilityKey;

    /**
     * Creates a new condition from a MythicMobs line config.
     *
     * @param config The MythicMobs line config containing the {@code ability} parameter
     */
    public McRPGAbilityUnlockedCondition(@NotNull MythicLineConfig config) {
        String keyString = config.getString("ability", "");
        this.abilityKey = NamespacedKey.fromString(keyString);
    }

    @Override
    public boolean check(@NotNull AbstractEntity abstractEntity) {
        if (abilityKey == null) {
            return false;
        }
        if (!abstractEntity.isPlayer()) {
            return false;
        }
        Player player = (Player) abstractEntity.getBukkitEntity();
        Optional<McRPGPlayer> mcRPGPlayerOpt = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(player.getUniqueId());
        if (mcRPGPlayerOpt.isEmpty()) {
            return false;
        }
        McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();
        return mcRPGPlayer.asSkillHolder().getAbilityData(abilityKey)
                .flatMap(data -> data.getAbilityAttribute(
                        AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE))
                .filter(attr -> attr instanceof AbilityUnlockedAttribute)
                .map(attr -> ((AbilityUnlockedAttribute) attr).getContent())
                .orElse(false);
    }
}
