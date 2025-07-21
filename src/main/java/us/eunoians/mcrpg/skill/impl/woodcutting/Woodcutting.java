package us.eunoians.mcrpg.skill.impl.woodcutting;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.configuration.file.skill.WoodcuttingConfigFile;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.impl.McRPGSkill;
import us.eunoians.mcrpg.skill.impl.type.ConfigurableSkill;

/**
 * A {@link Skill} that focuses on the usage of breaking wood with an axe.
 * <p>
 * Players will gain experience by breaking wood with an axe and unlock abilities focused
 * on increasing the yield/ease of woodcutting.
 */
public class Woodcutting extends McRPGSkill implements ConfigurableSkill {

    public static final NamespacedKey WOODCUTTING_KEY = new NamespacedKey(McRPG.getInstance(), "woodcutting");

    private final McRPG mcRPG;

    public Woodcutting(@NotNull McRPG mcRPG) {
        super(WOODCUTTING_KEY);
        this.mcRPG = mcRPG;
        addLevelableComponent(WoodcuttingSkillComponents.WOODCUTTING_LEVEL_ON_BLOCK_BREAK_COMPONENT, BlockBreakBlockEvent.class, 0);
    }

    @NotNull
    @Override
    public Plugin getPlugin() {
        return mcRPG;
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "woodcutting";
    }

    @Override
    public int getMaxLevel() {
        return 1000;
    }

    @Override
    public boolean isSkillEnabled() {
        return getYamlDocument().getBoolean(WoodcuttingConfigFile.SKILL_ENABLED);
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.WOODCUTTING_CONFIG);
    }

    @NotNull
    @Override
    public Route getDisplayItemRoute() {
        return LocalizationKey.WOODCUTTING_DISPLAY_ITEM;
    }
}
