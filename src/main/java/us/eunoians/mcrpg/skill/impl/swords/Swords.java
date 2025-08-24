package us.eunoians.mcrpg.skill.impl.swords;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
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
 * A {@link Skill} that focuses on the usage of Minecraft Swords.
 * <p>
 * Players will gain experience by attacking mobs with swords and unlock abilities focused
 * on the {@link us.eunoians.mcrpg.ability.impl.swords.Bleed} mechanic.
 */
public final class Swords extends McRPGSkill implements HeldItemBonusSkill, ConfigurableSkill {

    public static final NamespacedKey SWORDS_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "swords");
    private static final Map<Material, Route> MATERIAL_BONUS_ROUTE_MAP = new HashMap<>();

    private final McRPG mcRPG;
    public Swords(@NotNull McRPG mcRPG) {
        super(SWORDS_KEY);
        this.mcRPG = mcRPG;
        addLevelableComponent(SwordsSkillComponents.SWORDS_LEVEL_ON_ATTACK_COMPONENT, EntityDamageByEntityEvent.class, 0);
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.SWORDS_CONFIG);
    }

    @NotNull
    @Override
    public Route getDisplayItemRoute() {
        return LocalizationKey.SWORDS_DISPLAY_ITEM;
    }

    @NotNull
    @Override
    public Plugin getPlugin() {
        return mcRPG;
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "swords";
    }

    @Override
    public double getHeldItemBonus(@NotNull ItemStack... items) {
        double modifier = 0.0;
        for (ItemStack itemStack : items) {
            // TODO https://github.com/DiamondDagger590/McRPG/issues/117
            Material material = itemStack.getType();
            // Cache so we don't constantly rebuild routes (especially if players are spam clicking or smth)
            if (!MATERIAL_BONUS_ROUTE_MAP.containsKey(material)) {
                MATERIAL_BONUS_ROUTE_MAP.put(material, Route.fromString(toRoutePath(SwordsConfigFile.MATERIAL_MODIFIERS_HEADER, material.toString())));
            }
            YamlDocument swordsFile = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.SWORDS_CONFIG);
            modifier += (swordsFile.getDouble(MATERIAL_BONUS_ROUTE_MAP.get(material), 1.0d));
        }
        return modifier;
    }
}
