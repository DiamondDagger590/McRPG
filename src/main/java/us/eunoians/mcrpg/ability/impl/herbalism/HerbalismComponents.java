package us.eunoians.mcrpg.ability.impl.herbalism;

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
import us.eunoians.mcrpg.ability.ready.HerbalismReadyData;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.Set;

public class HerbalismComponents {

    public static final HerbalismReadyComponent HERBALISM_READY_COMPONENT = new HerbalismReadyComponent();
    public static final HerbalismActivateOnReadyComponent HERBALISM_ACTIVATE_ON_READY_COMPONENT = new HerbalismActivateOnReadyComponent();
    public static final HoldingHoeBreakBlockActivateComponent HOLDING_HOE_BREAK_BLOCK_ACTIVATE_COMPONENT = new HoldingHoeBreakBlockActivateComponent();
    public static final HoldingHoeInteractActivateComponent HOLDING_HOE_INTERACT_ACTIVATE_COMPONENT = new HoldingHoeInteractActivateComponent();

    protected static final Set<Material> HOES = Set.of(Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.COPPER_HOE,
            Material.DIAMOND_HOE, Material.GOLDEN_HOE, Material.NETHERITE_HOE);

    private static class HerbalismReadyComponent implements RightClickReadyComponent {
        @NotNull
        @Override
        public Set<Material> getValidMaterialsForActivation() {
            return HOES;
        }
    }

    private static class HoldingHoeBreakBlockActivateComponent implements OnBlockBreakComponent {

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
    }

    private static class HoldingHoeInteractActivateComponent implements EventActivatableComponent {

        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            PlayerInteractEvent playerInteractEvent = (PlayerInteractEvent) event;
            Player player = playerInteractEvent.getPlayer();
            EntityEquipment entityEquipment = player.getEquipment();
            return HOES.contains(entityEquipment.getItemInMainHand().getType()) && (playerInteractEvent.getAction() == Action.LEFT_CLICK_AIR || playerInteractEvent.getAction() == Action.LEFT_CLICK_BLOCK);
        }
    }

    private static class HerbalismActivateOnReadyComponent implements EventActivatableComponent {
        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            return abilityHolder.getReadiedAbility().isPresent() && abilityHolder.getReadiedAbility().get() instanceof HerbalismReadyData;
        }
    }
}
