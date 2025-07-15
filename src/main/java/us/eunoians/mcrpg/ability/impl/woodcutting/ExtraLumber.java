package us.eunoians.mcrpg.ability.impl.woodcutting;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.configuration.collection.ReloadableSet;
import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableAbility;
import us.eunoians.mcrpg.ability.impl.type.DropMultiplierAbility;
import us.eunoians.mcrpg.ability.impl.type.PassiveAbility;
import us.eunoians.mcrpg.ability.impl.type.ReloadableContentAbility;
import us.eunoians.mcrpg.builder.item.AbilityItemPlaceholderKeys;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.configuration.file.skill.WoodcuttingConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.woodcutting.ExtraLumberActivateEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.woodcutting.Woodcutting;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ExtraLumber extends McRPGAbility implements PassiveAbility, ConfigurableAbility, ReloadableContentAbility, DropMultiplierAbility {

    public static final NamespacedKey EXTRA_LUMBER_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "extra_lumber");

    private final ReloadableSet<Material> VALID_BLOCK_TYPES;
    private final Map<Location, Integer> multiplierMap = new HashMap<>();

    public ExtraLumber(@NotNull McRPG mcRPG) {
        super(mcRPG, EXTRA_LUMBER_KEY);
        addActivatableComponent(WoodcuttingComponents.HOLDING_AXE_BREAK_BLOCK_ACTIVATE_COMPONENT, BlockBreakEvent.class, 0);
        addActivatableComponent(ExtraLumberComponents.EXTRA_LUMBER_ON_BREAK_COMPONENT, BlockBreakEvent.class, 1);
        VALID_BLOCK_TYPES = getValidBlockTypes();
    }

    /**
     * Get a {@link ReloadableSet} of {@link Material}s that can trigger this ability.
     *
     * @return A {@link ReloadableSet} of {@link Material}s that can trigger this ability.
     */
    private ReloadableSet<Material> getValidBlockTypes() {
        return new ReloadableSet<>(getYamlDocument(), WoodcuttingConfigFile.EXTRA_LUMBER_VALID_DROPS, strings -> strings.stream().map(Material::getMaterial).collect(Collectors.toSet()));
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.WOODCUTTING_CONFIG);
    }

    @NotNull
    @Override
    public Route getDisplayItemRoute() {
        return LocalizationKey.EXTRA_LUMBER_DISPLAY_ITEM_HEADER;
    }

    @Override
    public Map<Location, Integer> getMultiplierMap() {
        return multiplierMap;
    }

    @Override
    public Set<ReloadableContent<?>> getReloadableContent() {
        return Set.of(VALID_BLOCK_TYPES);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getSkill() {
        return Optional.of(Woodcutting.WOODCUTTING_KEY);
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "extra_lumber";
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event;
        ExtraLumberActivateEvent extraLumberActivateEvent = new ExtraLumberActivateEvent(abilityHolder, 2);
        Bukkit.getPluginManager().callEvent(extraLumberActivateEvent);
        if (!extraLumberActivateEvent.isCancelled()) {
            addMultiplier(blockBreakEvent.getBlock(), extraLumberActivateEvent.getDropMultiplier());
        }
    }

    @Override
    public boolean isAbilityEnabled() {
        return getYamlDocument().getBoolean(WoodcuttingConfigFile.EXTRA_LUMBER_ENABLED);
    }

    public double getActivationChance(@NotNull SkillHolder skillHolder) {
        var skillHolderDataOptional = skillHolder.getSkillHolderData(Woodcutting.WOODCUTTING_KEY);
        if (skillHolderDataOptional.isPresent()) {
            Parser parser = new Parser(getYamlDocument().getString(WoodcuttingConfigFile.EXTRA_LUMBER_ACTIVATION_EQUATION));
            parser.setVariable("woodcutting_level", skillHolderDataOptional.get().getCurrentLevel());
            return parser.getValue();
        }

        return 0.0;
    }

    /**
     * Checks to see if the provided {@link Block} can be used to activate this ability.
     *
     * @param block The {@link Block} to check.
     * @return {@code true} if the provided {@link Block} can be used to activate this ability.
     */
    public boolean isBlockValid(@NotNull Block block) {
        return VALID_BLOCK_TYPES.getContent().contains(block.getType());
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
