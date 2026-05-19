package us.eunoians.mcrpg.builder.item.ability;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;
import us.eunoians.mcrpg.ability.attribute.DisplayableAttribute;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.type.SkillAbility;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;

import java.util.Map;
import java.util.Optional;

public class AbilityItemBuilder extends ItemBuilder {

    private final McRPGPlayer player;
    private final Ability ability;

    public AbilityItemBuilder(@NotNull final ItemStack itemStack, @NotNull McRPGPlayer player, @NotNull final Ability ability) {
        super(itemStack);
        this.player = player;
        this.ability = ability;
        addPlaceholders();
    }

    public AbilityItemBuilder(@NotNull final String value, @NotNull McRPGPlayer player, @NotNull final Ability ability) {
        super(value);
        this.player = player;
        this.ability = ability;
        addPlaceholders();
    }

    /**
     * Constructs an {@link AbilityItemBuilder} by copying all builder state from an existing
     * {@link ItemBuilder} without baking to an {@link org.bukkit.inventory.ItemStack}. This
     * keeps lore and display name in string form so placeholders added here are resolved in
     * the same MiniMessage parse pass as palette tags.
     *
     * @param source  The builder to copy state from.
     * @param player  The player context for placeholder resolution.
     * @param ability The ability this builder is for.
     */
    public AbilityItemBuilder(@NotNull final ItemBuilder source, @NotNull McRPGPlayer player, @NotNull final Ability ability) {
        super(source);
        this.player = player;
        this.ability = ability;
        addPlaceholders();
    }

    @NotNull
    public static AbilityItemBuilder from(@NotNull ItemBuilder itemBuilder, @NotNull McRPGPlayer mcRPGPlayer, @NotNull Ability ability) {
        return new AbilityItemBuilder(itemBuilder, mcRPGPlayer, ability);
    }

    @NotNull
    public static AbilityItemBuilder from(@NotNull Section section, @NotNull McRPGPlayer mcRPGPlayer, @NotNull Ability ability) {
        return new AbilityItemBuilder(ItemBuilder.from(section), mcRPGPlayer, ability);
    }

    private void addPlaceholders() {
        McRPG plugin = player.getPlugin();
        SkillRegistry skillRegistry = plugin.registryAccess().registry(McRPGRegistryKey.SKILL);
        AbilityRegistry abilityRegistry = plugin.registryAccess().registry(McRPGRegistryKey.ABILITY);
        SkillHolder skillHolder = player.asSkillHolder();
        // Plain ability placeholder (used in item name: field where locale wraps it in a palette tag)
        addPlaceholder(AbilityItemPlaceholderKeys.ABILITY.getKey(), ability.getName(player));
        // Colored self-reference — safe for use in lore lines
        addPlaceholder(AbilityItemPlaceholderKeys.COLORED_ABILITY.getKey(), ability.getColoredName(player));
        // Skill placeholder for the ability's own skill
        if (ability instanceof SkillAbility skillAbility) {
            Skill skill = skillRegistry.getRegisteredSkill(skillAbility.getSkillKey());
            addPlaceholder(AbilityItemPlaceholderKeys.SKILL.getKey(), skill.getColoredName(player));
        }
        // Cross-reference placeholders: <colored-ability_ns_key> for any registered ability.
        // Colons are replaced with underscores because MiniMessage tag names only allow [a-z0-9_-]*.
        for (var abilityKey : abilityRegistry.getAllAbilities()) {
            Ability registeredAbility = abilityRegistry.getRegisteredAbility(abilityKey);
            addPlaceholder("colored-ability_" + abilityKey.namespace() + "_" + abilityKey.getKey(), registeredAbility.getColoredName(player));
        }
        // Cross-reference placeholders: <colored-skill_ns_key> for any registered skill.
        for (Skill registeredSkill : skillRegistry.getRegisteredSkills()) {
            var skillKey = registeredSkill.getSkillKey();
            addPlaceholder("colored-skill_" + skillKey.namespace() + "_" + skillKey.getKey(), registeredSkill.getColoredName(player));
        }
        // Add information about specific ability attributes
        Optional<AbilityData> abilityDataOptional = skillHolder.getAbilityData(ability);
        if (abilityDataOptional.isPresent()) {
            AbilityData abilityData = abilityDataOptional.get();
            for (AbilityAttribute<?> abilityAttribute : abilityData.getAllAttributes()) {
                // If the attribute can be displayed
                if (abilityAttribute instanceof DisplayableAttribute displayableAttribute) {
                    addPlaceholder(displayableAttribute.getPlaceholderName(), displayableAttribute.getDisplayableContent());
                }
            }
        }
        var abilityExpansionOptional = ability.getExpansionKey();
        abilityExpansionOptional.flatMap(namespacedKey -> plugin.registryAccess().registry(McRPGRegistryKey.MANAGER)
                .manager(McRPGManagerKey.CONTENT_EXPANSION)
                .getContentExpansion(namespacedKey)).ifPresent(expansion -> {
            addPlaceholder(AbilityItemPlaceholderKeys.EXPANSION_PACK.getKey(), expansion.getExpansionName(player));
        });

        for (Map.Entry<String, String> entry : ability.getItemBuilderPlaceholders(player).entrySet()) {
            addPlaceholder(entry.getKey(), entry.getValue());
        }
    }
}
