package us.eunoians.mcrpg.external.papi.placeholder.experience;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.PlayerExperienceExtras;
import us.eunoians.mcrpg.external.papi.placeholder.McRPGPlaceholder;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * This placeholder allows for PAPI integration to support a placeholder
 * about a player's current rested experience amount.
 */
public class RestedExperiencePlaceholder extends McRPGPlaceholder {

    private static final String PLACEHOLDER = "rested_experience";

    public RestedExperiencePlaceholder() {
        super(PLACEHOLDER);
    }

    @Nullable
    @Override
    public String parsePlaceholder(@NotNull OfflinePlayer offlinePlayer) {
        McRPG mcRPG = McRPG.getInstance();
        McRPGPlayerManager playerManager = mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
        var playerOptional = playerManager.getPlayer(offlinePlayer.getUniqueId());
        if (playerOptional.isPresent()) {
            McRPGPlayer mcRPGPlayer = playerOptional.get();
            PlayerExperienceExtras experienceExtras = mcRPGPlayer.getExperienceExtras();
            return Float.toString(experienceExtras.getRestedExperience());
        }
        return null;
    }
}
