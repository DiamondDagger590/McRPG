package us.eunoians.mcrpg.skill.impl.herbalism;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.HerbalismConfigFile;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.component.block.ConfigurableOnBlockBreakLevelableComponent;

/**
 * This component handles calculating how much experience can be given when
 * a skill holder breaks a crop.
 */
public class HerbalismLevelOnBlockBreakComponent extends ConfigurableOnBlockBreakLevelableComponent {

    @NotNull
    @Override
    public YamlDocument getSkillConfiguration() {
        return RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.HERBALISM_CONFIG);
    }

    @NotNull
    @Override
    public Route getAllowedItemsForExperienceGainRoute() {
        return HerbalismConfigFile.ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN;
    }

    @NotNull
    @Override
    public String getBlockExperienceHeader() {
        return HerbalismConfigFile.BLOCK_EXPERIENCE_HEADER;
    }

    @NotNull
    @Override
    public Herbalism getSkill() {
        return (Herbalism) RegistryAccess.registryAccess().registry(McRPGRegistryKey.SKILL).getRegisteredSkill(Herbalism.HERBALISM_KEY);
    }

    @Override
    public boolean shouldGiveExperience(@NotNull SkillHolder skillHolder, @NotNull Event event) {
        return super.shouldGiveExperience(skillHolder, event) && isCropFullyGrown(((BlockBreakEvent) event).getBlock());
    }

    /**
     * Checks to see if the provided {@link Block} is fully grown.
     *
     * @param block The {@link Block} to check.
     * @return {@code true} if the provided {@link Block} is fully grown
     */
    private boolean isCropFullyGrown(Block block) {
        if (!(block.getBlockData() instanceof Ageable ageable)) {
            return true;
        }
        return ageable.getAge() >= ageable.getMaximumAge();
    }
}
