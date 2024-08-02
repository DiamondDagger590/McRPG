package us.eunoians.mcrpg.ability.attribute;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class AbilityMaterialSetAttribute extends OptionalSavingAbilityAttribute<Set<Material>> {

    AbilityMaterialSetAttribute() {
        super("material_set", AbilityAttributeManager.ABILITY_MATERIAL_SET_ATTRIBUTE);
    }

    public AbilityMaterialSetAttribute(@NotNull Set<Material> set) {
        super("material_set", AbilityAttributeManager.ABILITY_MATERIAL_SET_ATTRIBUTE, set);
    }

    @Override
    public boolean shouldContentBeSaved() {
        return !getContent().isEmpty();
    }

    @NotNull
    @Override
    public AbilityAttribute<Set<Material>> create(@NotNull Set<Material> content) {
        return new AbilityMaterialSetAttribute(content);
    }

    @NotNull
    @Override
    public Set<Material> convertContent(@NotNull String stringContent) {
        Set<Material> set = new HashSet<>();
        for (String type : stringContent.split(",")) {
            Material material = Material.getMaterial(type);
            if (material != null) {
                set.add(material);
            }
        }
        return set;
    }

    @NotNull
    @Override
    public Set<Material> getDefaultContent() {
        return Set.of();
    }

    @NotNull
    @Override
    public String serializeContent() {
        return getContent().stream().map(Material::name).reduce((s, s2) -> s + "," + s2).orElse("");
    }
}
