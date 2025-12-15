package us.eunoians.mcrpg.ability.impl.herbalism;

import com.diamonddagger590.mccore.util.item.CustomBlockWrapper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.component.activatable.OnBlockBreakComponent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.Set;

public class InstantIrrigationComponents extends HerbalismComponents {

    private static final Set<CustomBlockWrapper> IRRIGATION_BLOCKS = Set.of(new CustomBlockWrapper(Material.GRASS_BLOCK),
            new CustomBlockWrapper(Material.DIRT), new CustomBlockWrapper(Material.COARSE_DIRT), new CustomBlockWrapper(Material.FARMLAND));

    public static final InstantIrrigationBlockBreakComponent INSTANT_IRRIGATION_BLOCK_BREAK = new InstantIrrigationBlockBreakComponent();
    public static final InstantIrrigationHoldingHoeBreakBlockActivateComponent HOLDING_HOE_BREAK_BLOCK_ACTIVATE_COMPONENT = new InstantIrrigationHoldingHoeBreakBlockActivateComponent();

    private static final class InstantIrrigationBlockBreakComponent implements OnBlockBreakComponent {

        @Override
        public boolean affectsBlock(@NotNull Block block) {
            return IRRIGATION_BLOCKS.contains(new CustomBlockWrapper(block));
        }
    }

    private static class InstantIrrigationHoldingHoeBreakBlockActivateComponent implements OnBlockBreakComponent {

        @Override
        public boolean affectsBlock(@NotNull Block block) {
            return block.getType() != Material.AIR && !block.isLiquid();
        }

        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            if (!OnBlockBreakComponent.super.shouldActivate(abilityHolder, event)) {
                return false;
            }

            BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event; //safe to assume after super call
            Player player = blockBreakEvent.getPlayer();
            EntityEquipment entityEquipment = player.getEquipment();
            return HOES.contains(entityEquipment.getItemInMainHand().getType());
        }

        @Override
        public boolean affectsUnnaturalBlocks() {
            return true;
        }
    }}
