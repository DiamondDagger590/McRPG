package us.eunoians.mcrpg.ability.impl.mining;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityLocationAttribute;
import us.eunoians.mcrpg.ability.attribute.RemoteTransferItemSetAttribute;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.mining.remotetransfer.ReloadableRemoteTransferMap;
import us.eunoians.mcrpg.ability.impl.mining.remotetransfer.RemoteTransferCategory;
import us.eunoians.mcrpg.ability.impl.type.PassiveAbility;
import us.eunoians.mcrpg.ability.impl.type.ReloadableContentAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableSkillAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableTierableAbility;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.mining.RemoteTransferActivateEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.mining.Mining;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.RANGE;

/**
 * This ability allows players to link to a chest and blocks they mine will automatically go into the chest if their allow list
 * has it enabled.
 */
public final class RemoteTransfer extends McRPGAbility implements PassiveAbility, ConfigurableTierableAbility,
        ReloadableContentAbility, ConfigurableSkillAbility {

    public static final NamespacedKey REMOTE_TRANSFER_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "remote_transfer");
    private static final ReloadableRemoteTransferMap REMOTE_TRANSFER_CATEGORIES = new ReloadableRemoteTransferMap();


    public RemoteTransfer(@NotNull McRPG plugin) {
        super(plugin, REMOTE_TRANSFER_KEY);
        addActivatableComponent(RemoteTransferComponents.REMOTE_TRANSFER_ACTIVATE_ON_BLOCK_DROP_COMPONENT, BlockDropItemEvent.class, 0);
    }

    @NotNull
    @Override
    public Route getAbilityTierConfigurationRoute() {
        return MiningConfigFile.REMOTE_TRANSFER_CONFIGURATION_HEADER;
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MINING_CONFIG);
    }

    @NotNull
    @Override
    public Route getDisplayItemRoute() {
        return LocalizationKey.REMOTE_TRANSFER_DISPLAY_ITEM_HEADER;
    }

    @Override
    public int getMaxTier() {
        return getYamlDocument().getInt(MiningConfigFile.REMOTE_TRANSFER_AMOUNT_OF_TIERS);
    }

    @NotNull
    @Override
    public NamespacedKey getSkillKey() {
        return Mining.MINING_KEY;
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "remote_transfer";
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        BlockDropItemEvent blockDropItemEvent = (BlockDropItemEvent) event;

        var abilityDataOptional = abilityHolder.getAbilityData(this);
        if (abilityDataOptional.isPresent()) {
            AbilityData abilityData = abilityDataOptional.get();
            var locationAttributeOptional = abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_LOCATION_ATTRIBUTE);
            // If the player has a location saved
            if (locationAttributeOptional.isPresent()) {
                AbilityLocationAttribute attribute = (AbilityLocationAttribute) locationAttributeOptional.get();
                Location location = attribute.getContent();

                // Skip default locations
                if (location.getWorld() == null) {
                    return;
                }

                Chunk chunk = location.getChunk();
                Block block = location.getBlock();
                Chest chest = (Chest) block.getState();

                RemoteTransferActivateEvent remoteTransferActivateEvent = new RemoteTransferActivateEvent(abilityHolder, location);
                Bukkit.getPluginManager().callEvent(remoteTransferActivateEvent);
                if (remoteTransferActivateEvent.isCancelled()) {
                    return;
                }

                // Force the chunk to load if it isn't already
                chunk.addPluginChunkTicket(getPlugin());
                List<Inventory> inventories = new ArrayList<>();
                boolean isDouble = false;
                if (chest.getInventory().getHolder() instanceof DoubleChest doubleChest) {
                    isDouble = true;
                    inventories.add(doubleChest.getRightSide().getInventory());
                    inventories.add(doubleChest.getLeftSide().getInventory());
                } else {
                    inventories.add(chest.getBlockInventory());
                }

                a:
                for (Item item : blockDropItemEvent.getItems()) {
                    Inventory currentInventory;
                    ItemStack itemStack = item.getItemStack();
                    CustomItemWrapper itemWrapper = new CustomItemWrapper(itemStack);
                    // Get the material of the item we are putting in the chest and the amount
                    // if the chest contents are full, check if there are any stacks we can increase before dropping
                    if (isItemTransferable(abilityHolder, itemStack)) {
                        int amount = itemStack.getAmount();
                        b:
                        for (Inventory inventory : inventories) {
                            currentInventory = inventory;
                            c:
                            for (int i = 0; i < currentInventory.getSize(); i++) {
                                // If the amount is no longer positive then we are done with this item
                                if (amount <= 0) {
                                    item.getItemStack().setAmount(0);
                                    break b;
                                }
                                // Get the current item per iteration
                                ItemStack currentItem = currentInventory.getItem(i);
                                // If the slot is empty
                                if (currentItem == null || currentItem.getType() == Material.AIR) {
                                    ItemStack newStack = itemStack.clone();
                                    // If the amount is greater than a stack
                                    if (amount > newStack.getMaxStackSize()) {
                                        newStack.setAmount(newStack.getMaxStackSize());
                                        amount -= newStack.getMaxStackSize();
                                        currentInventory.setItem(i, newStack);
                                        continue c;
                                    }
                                    // Otherwise just slap the item in there and break since we dont need to put it anywhere else
                                    else {
                                        newStack.setAmount(amount);
                                        currentInventory.setItem(i, newStack);
                                        amount = 0;
                                        item.getItemStack().setAmount(0);
                                        break b;
                                    }
                                } else if (new CustomItemWrapper(currentItem).equals(itemWrapper)) {
                                    if (currentItem.getAmount() == currentItem.getMaxStackSize()) {
                                        continue c;
                                    } else {
                                        if (currentItem.getAmount() + amount > currentItem.getMaxStackSize()) {
                                            amount -= currentItem.getMaxStackSize() - currentItem.getAmount();
                                            currentItem.setAmount(currentInventory.getMaxStackSize());
                                            continue c;
                                        } else {
                                            currentItem.setAmount(currentItem.getAmount() + amount);
                                            amount = 0;
                                            item.getItemStack().setAmount(0);
                                            break b;
                                        }
                                    }
                                } else {
                                    continue c;
                                }
                            }
                            block.getState().update();
                            if (isDouble) {
                                for (int i = -1; i <= 1; i += 2) {
                                    block.getWorld().getBlockAt(block.getLocation().add(i, 0, 0)).getState().update();
                                    block.getWorld().getBlockAt(block.getLocation().add(0, 0, i)).getState().update();
                                }
                            }
                            // Drop leftovers
                            if (amount > 0) {
                                item.getItemStack().setAmount(amount);
                            }
                        }
                    } else {
                        continue a;
                    }
                }
                // Allow the chunk to unload now that we are done
                // TODO set this to be on a runnable but reset it every time this is called so we aren't constantly loading/unloading
                chunk.removePluginChunkTicket(getPlugin());
            }
        }
    }

    @NotNull
    @Override
    public Route getAbilityEnabledRoute() {
        return MiningConfigFile.REMOTE_TRANSFER_ENABLED;
    }

    /**
     * Checks to see if the {@link ItemStack} is transferable to the holders's linked chest.
     *
     * @param abilityHolder The {@link AbilityHolder} to check for.
     * @param itemStack      The {@link ItemStack} to check.
     * @return {@code true} if the provided {@link ItemStack} is transferable to the holder's linked chest.
     */
    public boolean isItemTransferable(@NotNull AbilityHolder abilityHolder, @NotNull ItemStack itemStack) {
        boolean presentInConfig = false;
        CustomItemWrapper customItemWrapper = new CustomItemWrapper(itemStack);
        for (RemoteTransferCategory category : getRemoteTransferCategories()) {
            if (category.getCategoryItems().contains(customItemWrapper)) {
                presentInConfig = true;
            }
        }
        RemoteTransfer remoteTransfer = (RemoteTransfer) getPlugin().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(RemoteTransfer.REMOTE_TRANSFER_KEY);
        var abilityDataOptional = abilityHolder.getAbilityData(remoteTransfer);
        if (abilityDataOptional.isPresent() && abilityDataOptional.get().getAbilityAttribute(AbilityAttributeRegistry.REMOTE_TRANSFER_ITEM_SET_ATTRIBUTE).isPresent() &&
                abilityDataOptional.get().getAbilityAttribute(AbilityAttributeRegistry.REMOTE_TRANSFER_ITEM_SET_ATTRIBUTE).get() instanceof RemoteTransferItemSetAttribute remoteTransferItemSetAttribute) {

            return presentInConfig && !remoteTransferItemSetAttribute.isCustomItemWrapperStored(customItemWrapper);
        }
        return presentInConfig;
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        Set<NamespacedKey> applicableAttributes = new HashSet<>(ConfigurableTierableAbility.super.getApplicableAttributes());
        applicableAttributes.add(AbilityAttributeRegistry.ABILITY_LOCATION_ATTRIBUTE);
        applicableAttributes.add(AbilityAttributeRegistry.REMOTE_TRANSFER_ITEM_SET_ATTRIBUTE);
        return applicableAttributes;
    }

    @Override
    public Set<ReloadableContent<?>> getReloadableContent() {
        return Set.of(REMOTE_TRANSFER_CATEGORIES);
    }

    /**
     * Gets the range for this ability for the given tier.
     *
     * @param tier The tier to get the range for.
     * @return The range for this ability for the given tier.
     */
    public int getRange(int tier) {
        return getYamlDocument().getInt(Route.addTo(getRouteForTier(tier), "range"));
    }

    @NotNull
    public static Optional<RemoteTransferCategory> getRemoteTransferCategory(@NotNull String category) {
        return Optional.ofNullable(REMOTE_TRANSFER_CATEGORIES.getContent().get(category));
    }

    /**
     * Get all the {@link RemoteTransferCategory RemoteTransferCategories} currently loaded into memory.
     * <p>
     * The returned value should not be stored as a source of truth as it may be updated when the plugin reloads.
     * If the most up-to-date values are desired, ensure this method is called each time.
     *
     * @return An {@link ImmutableSet} of the {@link RemoteTransferCategory RemoteTransferCategories} currently
     * loaded into memory.
     */
    @NotNull
    public static Set<RemoteTransferCategory> getRemoteTransferCategories() {
        return Set.copyOf(REMOTE_TRANSFER_CATEGORIES.getContent().values());
    }

    @NotNull
    @Override
    public Map<String, String> getItemBuilderPlaceholders(@NotNull McRPGPlayer player) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(RANGE.getKey(), Integer.toString(getRange(getCurrentAbilityTier(player.asSkillHolder()))));
        return placeholders;
    }
}
