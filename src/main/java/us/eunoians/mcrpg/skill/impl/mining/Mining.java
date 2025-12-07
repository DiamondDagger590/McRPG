package us.eunoians.mcrpg.skill.impl.mining;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.impl.McRPGSkill;
import us.eunoians.mcrpg.skill.impl.type.ConfigurableSkill;
import us.eunoians.mcrpg.skill.impl.type.HeldItemBonusSkill;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;

import static com.diamonddagger590.mccore.util.Methods.toRoutePath;

/**
 * A {@link Skill} that focuses on the usage of pickaces to mine.
 * <p>
 * Players will gain experience by mining ores and stone and unlock abilities focused
 * on increasing the yield/ease of mining.
 */
public final class Mining extends McRPGSkill implements ConfigurableSkill, HeldItemBonusSkill {

    public static final NamespacedKey MINING_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "mining");

    private final Map<CustomItemWrapper, Route> MATERIAL_BONUS_ROUTE_MAP = new HashMap<>();
    private final McRPG mcRPG;

    public Mining(@NotNull McRPG mcRPG) {
        super(MINING_KEY);
        this.mcRPG = mcRPG;
        addLevelableComponent(new MiningLevelOnBlockBreakComponent(), BlockBreakEvent.class, 0);
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

    @Override
    public double getHeldItemBonus(@NotNull ItemStack... items) {
        double modifier = 0.0;
        // We only care about the main hand item which would be the first item in the array.
        ItemStack itemStack = items[0];
        CustomItemWrapper customItemWrapper = new CustomItemWrapper(itemStack);
        // Cache so we don't constantly rebuild routes (especially if players are spam clicking or smth)
        if (!MATERIAL_BONUS_ROUTE_MAP.containsKey(customItemWrapper)) {
            String materialValue = customItemWrapper.customItem().isPresent() ? customItemWrapper.customItem().get() : customItemWrapper.material().get().toString();
            MATERIAL_BONUS_ROUTE_MAP.put(customItemWrapper, Route.fromString(toRoutePath(MiningConfigFile.MATERIAL_MODIFIERS_HEADER, materialValue)));
        }
        YamlDocument miningConfig = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MINING_CONFIG);
        modifier += (miningConfig.getDouble(MATERIAL_BONUS_ROUTE_MAP.get(customItemWrapper), 1.0d));
        return modifier;
    }
}
