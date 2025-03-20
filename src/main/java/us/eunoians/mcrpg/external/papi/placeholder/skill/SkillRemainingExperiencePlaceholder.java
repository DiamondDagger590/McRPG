package us.eunoians.mcrpg.external.papi.placeholder.skill;

import com.diamonddagger590.mccore.player.PlayerManager;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.external.papi.placeholder.McRPGPlaceholder;

/**
 * This placeholder exists for all registered {@link us.eunoians.mcrpg.skill.Skill}s and allows
 * PAPI to use the skill's remaining experience as a placeholder.
 */
public class SkillRemainingExperiencePlaceholder extends McRPGPlaceholder {

    private static final String PLACEHOLDER = "%s_remaining_experience_needed";
    private final NamespacedKey skillKey;

    public SkillRemainingExperiencePlaceholder(@NotNull NamespacedKey skillKey) {
        super(String.format(PLACEHOLDER, skillKey.getKey()));
        this.skillKey = skillKey;
    }

    @Nullable
    @Override
    public String parsePlaceholder(@NotNull OfflinePlayer offlinePlayer) {
        McRPG mcRPG = McRPG.getInstance();
        PlayerManager playerManager = mcRPG.getPlayerManager();
        var playerOptional = playerManager.getPlayer(offlinePlayer.getUniqueId());
        if (playerOptional.isPresent() && playerOptional.get() instanceof McRPGPlayer mcRPGPlayer) {
            SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
            var skillDataOptional = skillHolder.getSkillHolderData(skillKey);
            if (skillDataOptional.isPresent()) {
                var skillData = skillDataOptional.get();
                return Integer.toString(skillData.getExperienceForNextLevel());
            }
        }
        return null;
    }
}
