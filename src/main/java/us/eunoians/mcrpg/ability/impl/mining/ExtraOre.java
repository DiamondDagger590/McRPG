package us.eunoians.mcrpg.ability.impl.mining;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.configuration.collection.ReloadableSet;
import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.item.CustomBlockWrapper;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.type.DropMultiplierAbility;
import us.eunoians.mcrpg.ability.impl.type.PassiveAbility;
import us.eunoians.mcrpg.ability.impl.type.ReloadableContentAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableSkillAbility;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.mining.ExtraOreActivateEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.mining.Mining;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is a default ability that has a chance to double the amount of drops from a
 * mined block.
 */
public final class ExtraOre extends McRPGAbility implements PassiveAbility,
        ReloadableContentAbility, DropMultiplierAbility, ConfigurableSkillAbility {

    public static final NamespacedKey EXTRA_ORE_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "extra_ore");

    private final ReloadableSet<CustomBlockWrapper> VALID_BLOCK_TYPES;
    private final Map<Location, Integer> multiplierMap = new HashMap<>();

    public ExtraOre(@NotNull McRPG mcRPG) {
        super(mcRPG, EXTRA_ORE_KEY);
        addActivatableComponent(MiningComponents.HOLDING_PICKAXE_BREAK_BLOCK_ACTIVATE_COMPONENT, BlockBreakEvent.class, 0);
        addActivatableComponent(ExtraOreComponents.EXTRA_ORE_ON_BREAK_COMPONENT, BlockBreakEvent.class, 1);
        VALID_BLOCK_TYPES = new ReloadableSet<>(getYamlDocument(), MiningConfigFile.EXTRA_ORE_VALID_DROPS,
                strings -> strings.stream().map(CustomBlockWrapper::new).collect(Collectors.toSet()));
    }

    /**
     * Get a {@link ReloadableSet} of {@link CustomBlockWrapper}s that can trigger this ability.
     *
     * @return A {@link ReloadableSet} of {@link CustomBlockWrapper}s that can trigger this ability.
     */
    public ReloadableSet<CustomBlockWrapper> getValidBlockTypes() {
        return VALID_BLOCK_TYPES;
    }

    @NotNull
    @Override
    public NamespacedKey getSkillKey() {
        return Mining.MINING_KEY;
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "extra_ore";
    }

    /**
     * Gets the chance of this ability activating for the provided {@link SkillHolder}.
     *
     * @param skillHolder The {@link SkillHolder} to get the activation chance for.
     * @return The activation chance of this ability for the provided {@link SkillHolder}l
     */
    public double getActivationChance(@NotNull SkillHolder skillHolder) {
        var skillHolderDataOptional = skillHolder.getSkillHolderData(Mining.MINING_KEY);
        if (skillHolderDataOptional.isPresent()) {
            Parser parser = new Parser(getYamlDocument().getString(MiningConfigFile.EXTRA_ORE_ACTIVATION_EQUATION));
            parser.setVariable("mining_level", skillHolderDataOptional.get().getCurrentLevel());
            return parser.getValue();
        }
        return 0.0;
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event;
        ExtraOreActivateEvent extraOreActivateEvent = new ExtraOreActivateEvent(abilityHolder, 2);
        Bukkit.getPluginManager().callEvent(extraOreActivateEvent);
        if (!extraOreActivateEvent.isCancelled()) {
            addMultiplier(blockBreakEvent.getBlock(), extraOreActivateEvent.getDropMultiplier());
        }
    }

    @NotNull
    @Override
    public Route getAbilityEnabledRoute() {
        return MiningConfigFile.EXTRA_ORE_ENABLED;
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MINING_CONFIG);
    }

    @NotNull
    @Override
    public Route getDisplayItemRoute() {
        return LocalizationKey.EXTRA_ORE_DISPLAY_ITEM_HEADER;
    }

    @Override
    public Set<ReloadableContent<?>> getReloadableContent() {
        return Set.of(VALID_BLOCK_TYPES);
    }

    /**
     * Checks to see if the provided {@link Block} can be used to activate this ability.
     *
     * @param block The {@link Block} to check.
     * @return {@code true} if the provided {@link Block} can be used to activate this ability.
     */
    public boolean isBlockValid(@NotNull Block block) {
        return VALID_BLOCK_TYPES.getContent().contains(new CustomBlockWrapper(block));
    }

    @NotNull
    @Override
    public Map<Location, Integer> getMultiplierMap() {
        return multiplierMap;
    }

    @NotNull
    @Override
    public Map<String, String> getItemBuilderPlaceholders(@NotNull McRPGPlayer player) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(AbilityItemPlaceholderKeys.ACTIVATION_CHANCE.getKey(),
                McRPGMethods.getChanceNumberFormat().format(getActivationChance(player.asSkillHolder())));
        return placeholders;
    }
}
