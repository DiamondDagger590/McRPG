package us.eunoians.mcrpg.papi.placeholder.ability;

import com.diamonddagger590.mccore.player.PlayerManager;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.papi.placeholder.McRPGPlaceholder;

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
        PlayerManager playerManager = mcRPG.getPlayerManager();
        var playerOptional = playerManager.getPlayer(offlinePlayer.getUniqueId());
        if (playerOptional.isPresent() && playerOptional.get() instanceof McRPGPlayer mcRPGPlayer) {
            SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
            var abilityDataOptional = skillHolder.getAbilityData(abilityKey);
            if (abilityDataOptional.isPresent()) {
                var attributeOptional = abilityDataOptional.get().getAbilityAttribute(AbilityAttributeManager.ABILITY_TIER_ATTRIBUTE_KEY);
                if (attributeOptional.isPresent() && attributeOptional.get() instanceof AbilityTierAttribute attribute) {
                    return Integer.toString(attribute.getContent());
                }
            }
        }
        return null;
    }
}
