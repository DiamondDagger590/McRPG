package us.eunoians.mcrpg.skill.impl.mining;

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
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.impl.McRPGSkill;
import us.eunoians.mcrpg.skill.impl.type.ConfigurableSkill;
import us.eunoians.mcrpg.util.McRPGMethods;

/**
 * A {@link Skill} that focuses on the usage of pickaces to mine.
 * <p>
 * Players will gain experience by mining ores and stone and unlock abilities focused
 * on increasing the yield/ease of mining.
 */
public final class Mining extends McRPGSkill implements ConfigurableSkill {

    public static final NamespacedKey MINING_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "mining");

    private final McRPG mcRPG;

    public Mining(@NotNull McRPG mcRPG) {
        super(MINING_KEY);
        this.mcRPG = mcRPG;
        addLevelableComponent(new MiningLevelOnBlockBreakComponent(), BlockBreakBlockEvent.class, 0);
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MINING_CONFIG);
    }

    @NotNull
    @Override
    public Route getDisplayItemRoute() {
        return LocalizationKey.MINING_DISPLAY_ITEM;
    }

    @NotNull
    @Override
    public Plugin getPlugin() {
        return mcRPG;
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "mining";
    }
}
