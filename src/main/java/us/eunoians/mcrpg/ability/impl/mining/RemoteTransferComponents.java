package us.eunoians.mcrpg.ability.impl.mining;

import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockDropItemEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.attribute.AbilityLocationAttribute;
import us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponent;
import us.eunoians.mcrpg.event.event.fake.FakeChestOpenEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * All components that are used to check for activation logic for {@link RemoteTransfer}
 * are stored here.
 */
public class RemoteTransferComponents {

    public static final RemoteTransferActivateOnBlockDropComponent REMOTE_TRANSFER_ACTIVATE_ON_BLOCK_DROP_COMPONENT = new RemoteTransferActivateOnBlockDropComponent();

    private static class RemoteTransferActivateOnBlockDropComponent implements EventActivatableComponent {

        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            BlockDropItemEvent blockDropItemEvent = (BlockDropItemEvent) event;
            RemoteTransfer remoteTransfer = (RemoteTransfer) McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(RemoteTransfer.REMOTE_TRANSFER_KEY);
            if (!remoteTransfer.isAbilityEnabled() || !abilityHolder.getUUID().equals(blockDropItemEvent.getPlayer().getUniqueId())) {
                return false;
            }
            Player player  = blockDropItemEvent.getPlayer();
            var abilityDataOptional = abilityHolder.getAbilityData(remoteTransfer);
            if (abilityDataOptional.isPresent()) {
                AbilityData abilityData = abilityDataOptional.get();
                var locationAttributeOptional = abilityData.getAbilityAttribute(AbilityAttributeManager.ABILITY_LOCATION_ATTRIBUTE);
                // If the player has a location saved
                if (locationAttributeOptional.isPresent()) {
                    AbilityLocationAttribute attribute = (AbilityLocationAttribute) locationAttributeOptional.get();
                    Location location = attribute.getContent();
                    // If the content shouldn't be saved, the worlds aren't the same, or the chest is too far away, we skip
                    if (!attribute.shouldContentBeSaved() || !location.getWorld().equals(player.getWorld()) || location.distanceSquared(player.getLocation()) >= Math.pow(remoteTransfer.getRange(remoteTransfer.getCurrentAbilityTier(abilityHolder)), 2)) {
                        return false;
                    }
                    Chunk chunk = location.getChunk();

                    Chest chest;
                    Block block = location.getBlock();
                    if (block.getType() == Material.CHEST) {
                        chest = (Chest) block.getState();
                    }
                    // If it isn't a chest, alert player and remove the location attribute
                    else {
                        Audience audience = McRPG.getInstance().getAdventure().player(player);
                        audience.sendMessage(McRPG.getInstance().getMiniMessage().deserialize("<red>Your linked chest for remote transfer is missing... unlinking."));
                        abilityData.removeAttribute(attribute);
                        return false;
                    }
                    // Simulate opening the chest
                    FakeChestOpenEvent fakeChestOpenEvent = new FakeChestOpenEvent(player, block.getLocation());
                    Bukkit.getPluginManager().callEvent(fakeChestOpenEvent);
                    if (fakeChestOpenEvent.useInteractedBlock() == Event.Result.DENY) {
                        Audience audience = McRPG.getInstance().getAdventure().player(player);
                        audience.sendMessage(McRPG.getInstance().getMiniMessage().deserialize("<red>Your linked chest for remote transfer was blocked from your usage... unlinking."));
                        abilityData.removeAttribute(attribute);
                        return false;
                    }
                    // We validated and we can activate now
                    return true;
                }
            }
            return false;
        }
    }
}
