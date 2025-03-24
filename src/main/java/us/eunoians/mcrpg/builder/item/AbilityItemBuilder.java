package us.eunoians.mcrpg.builder.item;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;
import us.eunoians.mcrpg.ability.attribute.DisplayableAttribute;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.ActivationChanceAbility;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;

import java.text.NumberFormat;
import java.util.Optional;

public class AbilityItemBuilder extends ItemBuilder {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();
    static {
        NUMBER_FORMAT.setMaximumFractionDigits(2);
        NUMBER_FORMAT.setMinimumFractionDigits(1);
    }
    private final McRPGPlayer player;
    private final Ability ability;

    public AbilityItemBuilder(@NotNull final ItemStack itemStack, @NotNull McRPGPlayer player, @NotNull final Ability ability) {
        super(itemStack);
        this.player = player;
        this.ability = ability;
    }

    public AbilityItemBuilder(@NotNull final String value, @NotNull McRPGPlayer player, @NotNull final Ability ability) {
        super(value);
        this.player = player;
        this.ability = ability;
    }

    @NotNull
    public static AbilityItemBuilder from(@NotNull ItemBuilder itemBuilder, @NotNull McRPGPlayer mcRPGPlayer, @NotNull Ability ability) {
        return new AbilityItemBuilder(itemBuilder.asItemStack(), mcRPGPlayer, ability);
    }

    @NotNull
    public static AbilityItemBuilder from(@NotNull Section section, @NotNull McRPGPlayer mcRPGPlayer, @NotNull Ability ability) {
        return new AbilityItemBuilder(ItemBuilder.from(section).asItemStack(), mcRPGPlayer, ability);
    }

    private void addPlaceholders() {
        McRPG plugin = player.getMcRPGInstance();
        SkillRegistry skillRegistry = plugin.getSkillRegistry();
        SkillHolder skillHolder = player.asSkillHolder();
        if (ability.getSkill().isPresent() && skillRegistry.isSkillRegistered(ability.getSkill().get())) {
            Skill skill = skillRegistry.getRegisteredSkill(ability.getSkill().get());
            addPlaceholder("skill", skill.getDisplayName(player));
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
        if (ability instanceof ActivationChanceAbility activationChanceAbility) {
            addPlaceholder("activation-chance", NUMBER_FORMAT.format(activationChanceAbility.getActivationChance(skillHolder)));
        }
        addPlaceholder("upgrade-point-amount", Integer.toString(skillHolder.getUpgradePoints()));
    }
}
