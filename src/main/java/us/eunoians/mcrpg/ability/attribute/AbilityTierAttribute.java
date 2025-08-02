package us.eunoians.mcrpg.ability.attribute;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys;
import us.eunoians.mcrpg.builder.item.ability.AbilityLoreAppender;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * This attribute stores the tier for an ability.
 */
public class AbilityTierAttribute extends OptionalSavingAbilityAttribute<Integer> implements DisplayableAttribute, GuiModifiableAttribute {

    AbilityTierAttribute() {
        super("tier", AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY);
    }

    public AbilityTierAttribute(@NotNull Integer content) {
        super("tier", AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY, content);
    }

    /**
     * Creates a new instance of this {@link AbilityTierAttribute} class, containing the provided {@link Integer} content as the value
     *
     * @param tier The {@link Integer} content to be used as the value in the returned {@link AbilityTierAttribute}
     * @return A new instance of this {@link AbilityTierAttribute} class, containing the provided {@link Integer} content as the value
     */
    @NotNull
    @Override
    public AbilityTierAttribute create(@NotNull Integer tier) {
        return new AbilityTierAttribute(tier);
    }

    /**
     * Converts the provided {@link String} content into content that matches the type of {@link Integer}.
     * <p>
     * This serves to allow abstraction to exist and all values to be stored as strings inside of {@link us.eunoians.mcrpg.database.table.SkillDAO}.
     *
     * @param stringContent The {@link String} content to be converted into type {@link Integer}
     * @return The {@link String} content that is now converted into {@link Integer} content
     */
    @NotNull
    @Override
    public Integer convertContent(@NotNull String stringContent) {
        return Integer.parseInt(stringContent);
    }

    /**
     * Gets the default content value for this attribute. This should be considered the "default state" for this attribute, such
     * as a tier defaulting to 0.
     * <p>
     * The largest use case for this is populating {@link AbilityAttributeRegistry} with initial instances of this class, which can then
     * be built on using {@link #create(Integer)}.
     *
     * @return {@code 0} as an {@link Integer}.
     */
    @NotNull
    @Override
    public Integer getDefaultContent() {
        return 1;
    }

    @Override
    public boolean shouldContentBeSaved() {
        return getContent() > 1;
    }

    @NotNull
    @Override
    public String getPlaceholderName() {
        return "tier";
    }

    @NotNull
    @Override
    public String getDisplayableContent() {
        return getContent().toString();
    }

    @NotNull
    @Override
    public McRPGSlot getSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Ability ability) {
        return new McRPGSlot() {
            @Override
            public boolean onClick(@NotNull McRPGPlayer player, @NotNull ClickType clickType) {
                if (ability instanceof TierableAbility tierableAbility && player.canPlayerStartUpgradeQuest(tierableAbility)) {
                    player.startUpgradeQuest(tierableAbility);
                }
                return true;
            }

            /**
             * {@inheritDoc}
             *
             * @throws IllegalArgumentException If the provided {@link Ability} is not a {@link TierableAbility}.
             */
            @NotNull
            @Override
            public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
                if (ability instanceof TierableAbility tierableAbility) {
                    MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
                    ItemBuilder itemBuilder = ItemBuilder.from(mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION).getLocalizedSection(LocalizationKey.TIER_ATTRIBUTE_DISPLAY_ITEM));
                    var extraLore = AbilityLoreAppender.getAppendLore(mcRPGPlayer, ability);
                    extraLore.getLeft().forEach(itemBuilder::addDisplayLore);
                    itemBuilder.setPlaceholders(extraLore.getRight());
                    itemBuilder.addPlaceholder(AbilityItemPlaceholderKeys.TIER.getKey(), Integer.toString(getContent()));
                    return itemBuilder;
                }
                throw new IllegalArgumentException(String.format("Expected ability %s to be a tierable ability but it was not.", ability.getName()));
            }
        };
    }
}
