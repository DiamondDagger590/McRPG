package us.eunoians.mcrpg.ability.impl.woodcutting;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.registry.McRPGAbilityKey;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.util.Random;

public class DryadsGiftComponents {

    private static final Random RANDOM = new Random();
    public static final DryadsGiftActivateComponent DRYADS_GIFT_ACTIVATE_COMPONENT = new DryadsGiftActivateComponent();

    private static class DryadsGiftActivateComponent implements EventActivatableComponent {

        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            if (event instanceof BlockBreakEvent blockBreakEvent) {
                DryadsGift dryadsGift = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).ability(McRPGAbilityKey.DRYADS_GIFT);
                return blockBreakEvent.getPlayer().getUniqueId().equals(abilityHolder.getUUID()) && dryadsGift.isBlockValid(blockBreakEvent.getBlock())
                        && dryadsGift.getActivationChance(dryadsGift.getCurrentAbilityTier(abilityHolder)) * 1000 > RANDOM.nextInt(100000);
            }
            return false;
        }
    }
}
