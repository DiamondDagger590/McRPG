package us.eunoians.mcrpg.ability.impl.mining;

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
import us.eunoians.mcrpg.ability.component.readyable.RightClickReadyComponent;
import us.eunoians.mcrpg.ability.ready.MiningReadyData;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.Set;

/**
 * Common components shared across all mining abilities.
 */
public class MiningComponents {

    public static final MiningReadyComponent MINING_READY_COMPONENT = new MiningReadyComponent();
    public static final MiningActivateOnReadyComponent MINING_ACTIVATE_ON_READY_COMPONENT = new MiningActivateOnReadyComponent();
    public static final HoldingPickaxeBreakBlockActivateComponent HOLDING_PICKAXE_BREAK_BLOCK_ACTIVATE_COMPONENT = new HoldingPickaxeBreakBlockActivateComponent();
    public static final HoldingPickaxeInteractActivateComponent HOLDING_PICKAXE_INTERACT_ACTIVATE_COMPONENT = new HoldingPickaxeInteractActivateComponent();

    private static final Set<Material> PICKAXES = Set.of(Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE,
            Material.DIAMOND_PICKAXE, Material.GOLDEN_PICKAXE, Material.NETHERITE_PICKAXE);

    private static class MiningReadyComponent implements RightClickReadyComponent {
        @NotNull
        @Override
        public Set<Material> getValidMaterialsForActivation() {
            return PICKAXES;
        }
    }

    private static class HoldingPickaxeBreakBlockActivateComponent implements OnBlockBreakComponent {

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
            return PICKAXES.contains(entityEquipment.getItemInMainHand().getType());
        }
    }

    private static class HoldingPickaxeInteractActivateComponent implements EventActivatableComponent {

        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            PlayerInteractEvent playerInteractEvent = (PlayerInteractEvent) event;
            Player player = playerInteractEvent.getPlayer();
            EntityEquipment entityEquipment = player.getEquipment();
            return PICKAXES.contains(entityEquipment.getItemInMainHand().getType()) && (playerInteractEvent.getAction() == Action.LEFT_CLICK_AIR || playerInteractEvent.getAction() == Action.LEFT_CLICK_BLOCK);
        }
    }

    private static class MiningActivateOnReadyComponent implements EventActivatableComponent {
        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            return abilityHolder.getReadiedAbility().isPresent() && abilityHolder.getReadiedAbility().get() instanceof MiningReadyData;
        }
    }
}
