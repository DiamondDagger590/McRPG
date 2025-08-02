package us.eunoians.mcrpg.external.papi.placeholder.ability;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.external.papi.placeholder.McRPGPlaceholder;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * This placeholder exists for all registered {@link Ability Abilities} and allows
 * PAPI to use the ability tier as a placeholder.
 */
public class AbilityTierPlaceholder extends McRPGPlaceholder {

    private static final String PLACEHOLDER = "%s_tier";
    private final NamespacedKey abilityKey;

    public AbilityTierPlaceholder(@NotNull NamespacedKey abilityKey) {
        super(String.format(PLACEHOLDER, abilityKey.getKey()));
        this.abilityKey = abilityKey;
    }

    @Nullable
    @Override
    public String parsePlaceholder(@NotNull OfflinePlayer offlinePlayer) {
        McRPG mcRPG = McRPG.getInstance();
        McRPGPlayerManager playerManager = mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
        var playerOptional = playerManager.getPlayer(offlinePlayer.getUniqueId());
        if (playerOptional.isPresent()) {
            McRPGPlayer mcRPGPlayer = playerOptional.get();
            SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
            var abilityDataOptional = skillHolder.getAbilityData(abilityKey);
            if (abilityDataOptional.isPresent()) {
                var attributeOptional = abilityDataOptional.get().getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY);
                if (attributeOptional.isPresent() && attributeOptional.get() instanceof AbilityTierAttribute attribute) {
                    return Integer.toString(attribute.getContent());
                }
            }
        }
        return null;
    }
}
