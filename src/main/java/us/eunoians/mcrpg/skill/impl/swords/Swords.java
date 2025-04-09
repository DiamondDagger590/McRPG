package us.eunoians.mcrpg.skill.impl.swords;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.skill.HeldItemBonusSkill;
import us.eunoians.mcrpg.skill.McRPGSkill;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link Skill} that focuses on the usage of Minecraft Swords.
 * <p>
 * Players will gain experience by attacking mobs with swords and unlock abilities focused
 * on the {@link us.eunoians.mcrpg.ability.impl.swords.Bleed} mechanic.
 */
public final class Swords extends McRPGSkill implements HeldItemBonusSkill {

    public static final NamespacedKey SWORDS_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "swords");
    private static final Map<Material, Route> MATERIAL_BONUS_ROUTE_MAP = new HashMap<>();

    public Swords() {
        super(SWORDS_KEY);
        addLevelableComponent(SwordsSkillComponents.SWORDS_LEVEL_ON_ATTACK_COMPONENT, EntityDamageByEntityEvent.class, 0);
    }

    @NotNull
    @Override
    public String getDisplayName(@Nullable McRPGPlayer player) {
        return "Swords";
    }

    @Override
    public int getMaxLevel() {
        return 1000;
    }

    @Override
    public double getHeldItemBonus(@NotNull ItemStack... items) {
        double modifier = 0.0;
        for (ItemStack itemStack : items) {
            Material material = itemStack.getType();
            // Cache so we don't constantly rebuild routes (especially if players are spam clicking or smth)
            if (!MATERIAL_BONUS_ROUTE_MAP.containsKey(material)) {
                MATERIAL_BONUS_ROUTE_MAP.put(material, Route.addTo(SwordsConfigFile.MATERIAL_MODIFIERS_HEADER, material.toString()));
            }
            YamlDocument swordsFile = McRPG.getInstance().getFileManager().getFile(FileType.SWORDS_CONFIG);
            modifier += (swordsFile.getDouble(MATERIAL_BONUS_ROUTE_MAP.get(material), 1.0d) - 1);
        }
        return modifier;
    }
}
