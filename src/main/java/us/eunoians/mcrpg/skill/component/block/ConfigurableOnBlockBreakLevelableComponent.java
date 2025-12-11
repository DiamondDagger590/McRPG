package us.eunoians.mcrpg.skill.component.block;

import com.diamonddagger590.mccore.util.item.CustomBlockWrapper;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.experience.context.BlockBreakContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.diamonddagger590.mccore.util.Methods.toRoutePath;

/**
 * This component is an implementation of {@link OnBlockBreakLevelableComponent} that provides
 * pulling values out of a config file. Most skills are likely to implement this unless
 * hard coding is desired (such as testing or proof of concepts).
 */
public abstract class ConfigurableOnBlockBreakLevelableComponent implements OnBlockBreakLevelableComponent {

    private final Map<CustomBlockWrapper, Route> BLOCK_EXPERIENCE_ROUTE_MAP;

    public ConfigurableOnBlockBreakLevelableComponent() {
        BLOCK_EXPERIENCE_ROUTE_MAP = new HashMap<>();
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
     * Get the header for the {@link Route} pointing to the list of blocks
     * that can award experience. The route is constructed by combining this header
     * with the block's type or custom model.
     *
     * @return The header for the {@link Route} pointing to the list of blocks that
     * can award experience.
     */
    @NotNull
    public abstract String getBlockExperienceHeader();

    /**
     * Gets the {@link Skill} that this component belongs to.
     *
     * @return The {@link Skill} that this component belongs to.
     */
    @NotNull
    public abstract Skill getSkill();

    @Override
    public int calculateExperienceToGive(@NotNull SkillHolder skillHolder, @NotNull Event event) {
        BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event; //Safe cast since can only be called after checks are done
        Block block = blockBreakEvent.getBlock();
        int baseExperience = getBaseExperienceForBlock(skillHolder, block);
        BlockBreakContext blockBreakContext = new BlockBreakContext(skillHolder, getSkill(), baseExperience, blockBreakEvent);
        return (int) (baseExperience * McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.EXPERIENCE_MODIFIER)
                .calculateModifierForContext(blockBreakContext)) * getTotalAffectedBlocks(block);
    }

    @Override
    public boolean affectsBlock(@NotNull Block block) {
        CustomBlockWrapper customBlockWrapper = getCustomBlockWrapper(block);
        YamlDocument config = getSkillConfiguration();
        return config.contains(BLOCK_EXPERIENCE_ROUTE_MAP.get(customBlockWrapper));
    }

    @Override
    public boolean shouldGiveExperience(@NotNull SkillHolder skillHolder, @NotNull Event event) {
        if (OnBlockBreakLevelableComponent.super.shouldGiveExperience(skillHolder, event)) {
            BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event; //Safe cast due to super call
            Player player = blockBreakEvent.getPlayer();
            Block block = blockBreakEvent.getBlock();
            ItemStack heldItem = player.getEquipment().getItemInMainHand();
            CustomItemWrapper customItemWrapper = new CustomItemWrapper(heldItem);
            YamlDocument config = getSkillConfiguration();
            List<String> validItems = config.getStringList(getAllowedItemsForExperienceGainRoute());
            String itemValue = customItemWrapper.customItem().isPresent() ? customItemWrapper.customItem().get() : customItemWrapper.material().get().toString();
            return validItems == null || validItems.isEmpty() || validItems.contains(itemValue);
        }
        return false;
    }

    @Override
    public int getBaseExperienceForBlock(@NotNull SkillHolder skillHolder, @NotNull Block block) {
        CustomBlockWrapper customBlockWrapper = getCustomBlockWrapper(block);
        YamlDocument config = getSkillConfiguration();
        return config.getInt(BLOCK_EXPERIENCE_ROUTE_MAP.get(customBlockWrapper), 0);
    }

    /**
     * Gets a {@link CustomBlockWrapper} for the provided block and caches the {@link Route}
     * pointing to that block's experience configuration.
     *
     * @param block The block to get the wrapper for.
     * @return A {@link CustomBlockWrapper} for the provided block.
     */
    @NotNull
    private CustomBlockWrapper getCustomBlockWrapper(@NotNull Block block) {
        CustomBlockWrapper customBlockWrapper = new CustomBlockWrapper(block);
        String blockValue = customBlockWrapper.customBlock().isPresent() ? customBlockWrapper.customBlock().get() : customBlockWrapper.material().get().toString();
        if (!BLOCK_EXPERIENCE_ROUTE_MAP.containsKey(customBlockWrapper)) {
            BLOCK_EXPERIENCE_ROUTE_MAP.put(customBlockWrapper, Route.fromString(toRoutePath(getBlockExperienceHeader(), blockValue)));
        }
        return customBlockWrapper;
    }
}
