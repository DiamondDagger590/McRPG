package us.eunoians.mcrpg.ability.impl.woodcutting;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponent;
import us.eunoians.mcrpg.ability.component.activatable.OnBlockBreakComponent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.skill.impl.woodcutting.WoodCutting;

import java.util.Set;

/**
 * Common activation components shared among {@link WoodCutting} abilities.
 */
public class WoodcuttingComponents {

    public static final HoldingAxeBreakBlockActivateComponent HOLDING_AXE_BREAK_BLOCK_ACTIVATE_COMPONENT = new HoldingAxeBreakBlockActivateComponent();
    public static final HoldingAxeInteractActivateComponent HOLDING_AXE_INTERACT_ACTIVATE_COMPONENT = new HoldingAxeInteractActivateComponent();

    private static final Set<Material> AXES = Set.of(Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.COPPER_AXE,
            Material.DIAMOND_AXE, Material.GOLDEN_AXE, Material.NETHERITE_AXE);

    private static class HoldingAxeBreakBlockActivateComponent implements OnBlockBreakComponent {

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
            return AXES.contains(entityEquipment.getItemInMainHand().getType());
        }
    }

    private static class HoldingAxeInteractActivateComponent implements EventActivatableComponent {

        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            PlayerInteractEvent playerInteractEvent = (PlayerInteractEvent) event;
            Player player = playerInteractEvent.getPlayer();
            EntityEquipment entityEquipment = player.getEquipment();
            return AXES.contains(entityEquipment.getItemInMainHand().getType()) && (playerInteractEvent.getAction() == Action.LEFT_CLICK_AIR || playerInteractEvent.getAction() == Action.LEFT_CLICK_BLOCK);
        }
    }
}
