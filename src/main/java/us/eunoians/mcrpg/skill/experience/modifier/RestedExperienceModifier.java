package us.eunoians.mcrpg.skill.experience.modifier;

import com.diamonddagger590.mccore.parser.EvaluationException;
import com.diamonddagger590.mccore.parser.ParseError;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.PlayerExperienceExtras;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.experience.context.SkillExperienceContext;

import java.util.UUID;

/**
 * This modifier will consume a player's rested experience to modify their experience gain.
 */
public class RestedExperienceModifier extends ExperienceModifier {

    private static final NamespacedKey MODIFIER_KEY = new NamespacedKey(McRPG.getInstance(), "rested-experience-modifier");

    private final McRPG mcRPG;

    public RestedExperienceModifier(@NotNull McRPG mcRPG) {
        this.mcRPG = mcRPG;
    }

    @Override
    public NamespacedKey getModifierKey() {
        return MODIFIER_KEY;
    }

    @Override
    public boolean canProcessContext(@NotNull SkillExperienceContext<? extends Event> skillExperienceContext) {
        var playerOptional = mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(skillExperienceContext.getSkillHolder().getUUID());
        if (playerOptional.isPresent()) {
            McRPGPlayer mcRPGPlayer = playerOptional.get();
            PlayerExperienceExtras playerExperienceExtras = mcRPGPlayer.getExperienceExtras();
            return playerExperienceExtras.getRestedExperience() > 0;
        }
        return false;
    }

    @Override
    public double getModifier(@NotNull SkillExperienceContext<? extends Event> skillExperienceContext, int experienceToCalculateOn) {
        UUID uuid = skillExperienceContext.getSkillHolder().getUUID();
        var playerOptional = mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if (playerOptional.isPresent()) {
            McRPGPlayer mcRPGPlayer = playerOptional.get();
            if (player != null) {
                var skillHolderDataOptional = mcRPGPlayer.asSkillHolder().getSkillHolderData(skillExperienceContext.getSkill());
                if (skillHolderDataOptional.isPresent()) {
                    SkillHolder.SkillHolderData skillHolderData = skillHolderDataOptional.get();
                    PlayerExperienceExtras playerExperienceExtras = mcRPGPlayer.getExperienceExtras();
                    int experienceForNextLevel = skillHolderData.getExperienceForNextLevel();
                    float playerRestedExperience = playerExperienceExtras.getRestedExperience();
                    try {
                        double boostToApply = mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG)
                                .getDouble(MainConfigFile.RESTED_EXPERIENCE_USAGE_RATE) - 1;
                        double boostedExperience = experienceToCalculateOn * boostToApply;
                        double consumedRestedExperience = boostedExperience / experienceForNextLevel;
                        // Since rested experience scales based on level, we want to make sure that if we have .5 levels of experience then we don't end up adding more than 50% of the current level's required experience
                        if (consumedRestedExperience > playerRestedExperience) {
                            boostToApply = ((experienceForNextLevel * playerRestedExperience) / experienceToCalculateOn);
                            consumedRestedExperience = playerRestedExperience;
                        }
                        playerExperienceExtras.modifyRestedExperience((float) (consumedRestedExperience * -1));
                        return boostToApply;
                    } catch (ParseError | EvaluationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return 0;
    }
}
