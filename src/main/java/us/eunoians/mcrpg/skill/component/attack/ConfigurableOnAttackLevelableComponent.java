package us.eunoians.mcrpg.skill.component.attack;

import com.diamonddagger590.mccore.external.common.NpcPluginHook;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.experience.context.EntityDamageContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.diamonddagger590.mccore.util.Methods.toRoutePath;

/**
 * This component is an implementation of {@link OnAttackLevelableComponent} that provides
 * pulling values out of a config file. Most skills are likely to implement this unless
 * hard coding is desired (such as testing or proof of concepts).
 */
public abstract class ConfigurableOnAttackLevelableComponent implements OnAttackLevelableComponent {

    private final Map<CustomEntityWrapper, Route> ENTITY_TYPE_EXPERIENCE_ROUTE_MAP;

    public ConfigurableOnAttackLevelableComponent() {
        ENTITY_TYPE_EXPERIENCE_ROUTE_MAP = new HashMap<>();
    }

    /**
     * Get the {@link YamlDocument} that the skill's configuration lives in.
     *
     * @return The {@link YamlDocument} that the skill's configuration lives in.
     */
    @NotNull
    public abstract YamlDocument getSkillConfiguration();

    /**
     * Gets the {@link Route} pointing to the list of allowed items in the
     * skill's config. An item being absent from the retrieved string list
     * will not be able to award experience.
     *
     * @return The {@link Route} pointing to the list of allowed items in the
     * skill's config.
     */
    @NotNull
    public abstract Route getAllowedItemsForExperienceGainRoute();

    /**
     * Get the header for the {@link Route} pointing to the list of entities
     * that can award experience. The route is constructed by combining this header
     * with the entity's type or custom model.
     *
     * @return The header for the {@link Route} pointing to the list of entities that
     * can award experience.
     */
    @NotNull
    public abstract String getEntityExperienceHeader();

    /**
     * Gets the {@link Skill} that this component belongs to.
     *
     * @return The {@link Skill} that this component belongs to.
     */
    @NotNull
    public abstract Skill getSkill();

    @Override
    public int calculateExperienceToGive(@NotNull SkillHolder skillHolder, @NotNull Event event) {
        EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event; //Safe cast since can only be called after checks are done
        Skill skill = getSkill();
        Entity damaged = entityDamageByEntityEvent.getEntity();
        int baseExperience = (int) (getDamageToAwardExperienceFor(entityDamageByEntityEvent) * getBaseExperienceForEntity(skillHolder, damaged));
        EntityDamageContext entityDamageContext = new EntityDamageContext(skillHolder, skill, baseExperience, entityDamageByEntityEvent);
        return (int) (baseExperience * McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.EXPERIENCE_MODIFIER)
                .calculateModifierForContext(entityDamageContext));
    }

    @Override
    public boolean shouldGiveExperience(@NotNull SkillHolder skillHolder, @NotNull Event event) {
        if (OnAttackLevelableComponent.super.shouldGiveExperience(skillHolder, event)) {
            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event; //Safe cast due to super call
            Entity damager = entityDamageByEntityEvent.getDamager();
            Entity damaged = entityDamageByEntityEvent.getEntity();
            if (damager instanceof LivingEntity livingDamager && livingDamager.getEquipment() != null && damaged instanceof LivingEntity) {
                ItemStack heldItem = livingDamager.getEquipment().getItemInMainHand();
                CustomItemWrapper customItemWrapper = new CustomItemWrapper(heldItem);
                YamlDocument config = getSkillConfiguration();
                List<String> validItems = config.getStringList(getAllowedItemsForExperienceGainRoute());
                String itemValue = customItemWrapper.customItem().isPresent() ? customItemWrapper.customItem().get() : customItemWrapper.material().get().toString();
                return validItems.contains(itemValue);
            }
        }
        return false;
    }

    @Override
    public int getBaseExperienceForEntity(@NotNull SkillHolder skillHolder, @NotNull Entity attackedEntity) {
        CustomEntityWrapper customEntityWrapper = getCustomEntityWrapper(attackedEntity);
        YamlDocument config = getSkillConfiguration();
        return config.getInt(ENTITY_TYPE_EXPERIENCE_ROUTE_MAP.get(customEntityWrapper), 0);
    }

    @Override
    public boolean affectsEntity(@NotNull Entity entity) {
        CustomEntityWrapper customEntityWrapper = getCustomEntityWrapper(entity);
        YamlDocument config = getSkillConfiguration();
        List<NpcPluginHook> npcPluginHooks = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHooks(NpcPluginHook.class);
        boolean isNpc = npcPluginHooks.stream().anyMatch(npcPluginHook -> npcPluginHook.isEntityNpc(entity));
        return config.contains(ENTITY_TYPE_EXPERIENCE_ROUTE_MAP.get(customEntityWrapper)) && !isNpc;
    }

    /**
     * Gets a {@link CustomEntityWrapper} for the provided entity and caches the {@link Route}
     * pointing to that entity's experience configuration.
     *
     * @param entity The entity to get the wrapper for.
     * @return A {@link CustomEntityWrapper} for the provided entity.
     */
    @NotNull
    private CustomEntityWrapper getCustomEntityWrapper(@NotNull Entity entity) {
        CustomEntityWrapper customEntityWrapper = new CustomEntityWrapper(entity);
        String entityTypeValue = customEntityWrapper.customEntity().isPresent() ? customEntityWrapper.customEntity().get() : customEntityWrapper.entityType().get().toString();
        if (!ENTITY_TYPE_EXPERIENCE_ROUTE_MAP.containsKey(customEntityWrapper)) {
            ENTITY_TYPE_EXPERIENCE_ROUTE_MAP.put(customEntityWrapper, Route.fromString(toRoutePath(getEntityExperienceHeader(), entityTypeValue)));
        }
        return customEntityWrapper;
    }
}
