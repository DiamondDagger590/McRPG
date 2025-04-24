package us.eunoians.mcrpg.skill.impl.swords;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.component.OnAttackLevelableComponent;
import us.eunoians.mcrpg.skill.experience.context.EntityDamageContext;

import java.util.HashMap;
import java.util.Map;

/**
 * A collection of all {@link us.eunoians.mcrpg.skill.component.EventLevelableComponent}s used for the
 * {@link Swords} skill.
 */
public class SwordsSkillComponents {

    private static final Map<EntityType, Route> ENTITY_TYPE_EXPERIENCE_ROUTE_MAP = new HashMap<>();

    public static final SwordsLevelOnAttackComponent SWORDS_LEVEL_ON_ATTACK_COMPONENT = new SwordsLevelOnAttackComponent();

    private static class SwordsLevelOnAttackComponent implements OnAttackLevelableComponent {

        @Override
        public int calculateExperienceToGive(@NotNull SkillHolder skillHolder, @NotNull Event event) {
            McRPG mcRPG = McRPG.getInstance();
            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event; //Safe cast since can only be called after checks are done
            Swords swords = (Swords) McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.SKILL).getRegisteredSkill(Swords.SWORDS_KEY);
            Entity damager = entityDamageByEntityEvent.getDamager();
            Entity damaged = entityDamageByEntityEvent.getEntity();
            double damage = entityDamageByEntityEvent.getFinalDamage();
            int baseExperience = (int) (getDamageToAwardExperienceFor(entityDamageByEntityEvent) * getBaseExperienceForEntity(skillHolder, damaged));
            EntityDamageContext entityDamageContext = new EntityDamageContext(skillHolder, swords, baseExperience, entityDamageByEntityEvent);

            if (damager instanceof LivingEntity livingDamager && livingDamager.getEquipment() != null && damaged instanceof LivingEntity livingDamaged) {
                return (int) (baseExperience * McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.EXPERIENCE_MODIFIER).calculateModifierForContext(entityDamageContext));
            }

            return 0;
        }

        @Override
        public boolean affectsEntity(@NotNull Entity entity) {
            return entity instanceof LivingEntity;
        }

        @Override
        public boolean shouldGiveExperience(@NotNull SkillHolder skillHolder, @NotNull Event event) {

            if (OnAttackLevelableComponent.super.shouldGiveExperience(skillHolder, event)) {
                EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event; //Safe cast due to super call
                Entity damager = entityDamageByEntityEvent.getDamager();
                Entity damaged = entityDamageByEntityEvent.getEntity();

                if (damager instanceof LivingEntity livingDamager && livingDamager.getEquipment() != null && damaged instanceof LivingEntity livingDamaged) {
                    ItemStack heldItem = livingDamager.getEquipment().getItemInMainHand();
                    if (heldItem.getType().name().contains("_SWORD")) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public int getBaseExperienceForEntity(@NotNull SkillHolder skillHolder, @NotNull Entity attackedEntity) {
            EntityType entityType = attackedEntity.getType();
            if (!ENTITY_TYPE_EXPERIENCE_ROUTE_MAP.containsKey(entityType)) {
                ENTITY_TYPE_EXPERIENCE_ROUTE_MAP.put(entityType, Route.addTo(SwordsConfigFile.ENTITY_EXPERIENCE_HEADER, entityType.toString()));
            }
            YamlDocument swordsFile = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.SWORDS_CONFIG);
            return swordsFile.getInt(ENTITY_TYPE_EXPERIENCE_ROUTE_MAP.get(entityType), 0);
        }
    }
}
