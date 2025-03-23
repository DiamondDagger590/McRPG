package us.eunoians.mcrpg.builder.item;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.Ability;

public class AbilityItemBuilder extends ItemBuilder {

    private final Ability ability;

    public AbilityItemBuilder(@NotNull final ItemStack itemStack, @NotNull final Ability ability) {
        super(itemStack);
        this.ability = ability;
    }

    public AbilityItemBuilder(@NotNull final String value, @NotNull final Ability ability) {
        super(value);
        this.ability = ability;
    }

    @NotNull
    public static AbilityItemBuilder from(@NotNull ItemBuilder itemBuilder, @NotNull Ability ability) {
        return new AbilityItemBuilder(itemBuilder.asItemStack(), ability);
    }

    @NotNull
    public static AbilityItemBuilder from(@NotNull Section section, @NotNull Ability ability) {
        return new AbilityItemBuilder(ItemBuilder.from(section).asItemStack(), ability);
    }
}
