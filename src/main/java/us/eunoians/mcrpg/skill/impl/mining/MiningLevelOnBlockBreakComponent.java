package us.eunoians.mcrpg.skill.impl.mining;

import com.diamonddagger590.mccore.util.item.CustomBlockWrapper;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.component.OnBlockBreakLevelableComponent;

import java.util.HashMap;
import java.util.Map;

import static com.diamonddagger590.mccore.util.Methods.toRoutePath;

public final class MiningLevelOnBlockBreakComponent implements OnBlockBreakLevelableComponent {

    private final Map<CustomBlockWrapper, Route> BLOCK_EXPERIENCE_ROUTE_MAP = new HashMap<>();

    @Override
    public int calculateExperienceToGive(@NotNull SkillHolder skillHolder, @NotNull Event event) {
        BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event; //Safe cast since can only be called after checks are done
        Player player = blockBreakEvent.getPlayer();
        Block block = blockBreakEvent.getBlock();

        player.getEquipment();
        ItemStack heldItem = player.getEquipment().getItemInMainHand();
        CustomBlockWrapper customBlockWrapper = new CustomBlockWrapper(block);


        //TODO https://github.com/DiamondDagger590/McRPG/issues/117
        double expToAward = 1000;

        return (int) expToAward;

    }

    @Override
    public boolean affectsBlock(@NotNull Block block) {
        // TODO check
        return true;
    }

    @Override
    public boolean shouldGiveExperience(@NotNull SkillHolder skillHolder, @NotNull Event event) {

        if (OnBlockBreakLevelableComponent.super.shouldGiveExperience(skillHolder, event)) {
            BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event; //Safe cast due to super call
            Player player = blockBreakEvent.getPlayer();
            Block block = blockBreakEvent.getBlock();

            player.getEquipment();
            ItemStack heldItem = player.getEquipment().getItemInMainHand();
            // TODO https://github.com/DiamondDagger590/McRPG/issues/117
            return heldItem.getType().name().contains("_PICKAXE");
        }
        return false;
    }

    @Override
    public int getBaseExperienceForBlock(@NotNull SkillHolder skillHolder, @NotNull Block block) {
        CustomBlockWrapper customBlockWrapper = new CustomBlockWrapper(block);
        if (!BLOCK_EXPERIENCE_ROUTE_MAP.containsKey(customBlockWrapper)) {
            String blockIdentifier = customBlockWrapper.customBlock().isPresent() ? customBlockWrapper.customBlock().get() : customBlockWrapper.material().get().toString();
            BLOCK_EXPERIENCE_ROUTE_MAP.put(customBlockWrapper, Route.fromString(toRoutePath(MiningConfigFile.BLOCK_EXPERIENCE_HEADER, blockIdentifier)));
        }
        return 0;
    }
}

