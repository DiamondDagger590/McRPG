package us.eunoians.mcrpg.ability.impl.woodcutting;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.configuration.ReloadableSet;
import com.diamonddagger590.mccore.parser.Parser;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.ConfigurableAbility;
import us.eunoians.mcrpg.ability.impl.DropMultiplierAbility;
import us.eunoians.mcrpg.ability.impl.PassiveAbility;
import us.eunoians.mcrpg.ability.impl.ReloadableContentAbility;
import us.eunoians.mcrpg.event.event.ability.woodcutting.ExtraLumberActivateEvent;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.WoodcuttingConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.skill.impl.woodcutting.Woodcutting;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.List;
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
        return getPlugin().getFileManager().getFile(FileType.WOODCUTTING_CONFIG);
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
    public Optional<String> getDatabaseName() {
        return Optional.of("extra_lumber");
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Extra Lumber";
    }

    @NotNull
    @Override
    public List<String> getDescription(@NotNull McRPGPlayer mcRPGPlayer) {
        return List.of("<gray>Will occasionally double drops while cutting wood.",
                "<gray>Activation Chance: <gold>" + FORMAT.format(getActivationChance(mcRPGPlayer.asSkillHolder())) + "%");
    }

    @NotNull
    @Override
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        return new ItemStack(Material.OAK_LOG, 2);
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
}
