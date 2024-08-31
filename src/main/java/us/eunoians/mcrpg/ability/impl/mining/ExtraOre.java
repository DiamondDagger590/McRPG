package us.eunoians.mcrpg.ability.impl.mining;

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
import us.eunoians.mcrpg.ability.impl.BaseAbility;
import us.eunoians.mcrpg.ability.impl.ConfigurableAbility;
import us.eunoians.mcrpg.ability.impl.DropMultiplierAbility;
import us.eunoians.mcrpg.ability.impl.PassiveAbility;
import us.eunoians.mcrpg.ability.impl.ReloadableContentAbility;
import us.eunoians.mcrpg.api.event.ability.mining.ExtraOreActivateEvent;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.skill.impl.mining.Mining;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class ExtraOre extends BaseAbility implements PassiveAbility, ConfigurableAbility, ReloadableContentAbility, DropMultiplierAbility {

    public static final NamespacedKey EXTRA_ORE_KEY = new NamespacedKey(McRPG.getInstance(), "extra_ore");

    private final ReloadableSet<Material> VALID_BLOCK_TYPES = new ReloadableSet<>(getYamlDocument(), MiningConfigFile.EXTRA_ORE_VALID_DROPS, strings -> strings.stream().map(Material::getMaterial).collect(Collectors.toSet()));
    private final Map<Location, Integer> multiplierMap = new HashMap<>();

    public ExtraOre() {
        super(EXTRA_ORE_KEY);
        addActivatableComponent(MiningComponents.HOLDING_PICKAXE_ACTIVATE_COMPONENT, BlockBreakEvent.class, 0);
        addActivatableComponent(ExtraOreComponents.EXTRA_ORE_ON_BREAK_COMPONENT, BlockBreakEvent.class, 1);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getSkill() {
        return Optional.of(Mining.MINING_KEY);
    }

    @NotNull
    @Override
    public Optional<String> getDatabaseName() {
        return Optional.of("extra_ore");
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Extra Ore";
    }

    @NotNull
    @Override
    public List<String> getDescription(@NotNull McRPGPlayer mcRPGPlayer) {
        return List.of("<gray>Will occasionally double drops while mining.",
                "<gray>Activation Chance: <gold>" + getActivationChance(mcRPGPlayer.asSkillHolder()));
    }

    public double getActivationChance(@NotNull SkillHolder skillHolder) {
        var skillHolderDataOptional = skillHolder.getSkillHolderData(Mining.MINING_KEY);
        if (skillHolderDataOptional.isPresent()) {
            Parser parser = new Parser(McRPG.getInstance().getFileManager().getFile(FileType.MINING_CONFIG).getString(MiningConfigFile.EXTRA_ORE_ACTIVATION_EQUATION));
            parser.setVariable("mining_level", skillHolderDataOptional.get().getCurrentLevel());
            return parser.getValue();
        }

        return 0.0;
    }

    @NotNull
    @Override
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        return new ItemStack(Material.DIAMOND, 2);
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

    @Override
    public boolean isAbilityEnabled() {
        return getYamlDocument().getBoolean(MiningConfigFile.EXTRA_ORE_ENABLED);
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return McRPG.getInstance().getFileManager().getFile(FileType.MINING_CONFIG);
    }

    @Override
    public Set<ReloadableContent<?>> getReloadableContent() {
        return Set.of(VALID_BLOCK_TYPES);
    }

    public boolean isBlockValid(@NotNull Block block) {
        return VALID_BLOCK_TYPES.getContent().contains(block.getType());
    }

    @Override
    public Map<Location, Integer> getMultiplierMap() {
        return multiplierMap;
    }
}
