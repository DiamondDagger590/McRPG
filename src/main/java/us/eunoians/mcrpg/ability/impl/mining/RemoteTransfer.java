package us.eunoians.mcrpg.ability.impl.mining;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.google.common.collect.ImmutableMap;
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
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.McRPGAbility;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.attribute.AbilityLocationAttribute;
import us.eunoians.mcrpg.ability.attribute.RemoteTransferMaterialSetAttribute;
import us.eunoians.mcrpg.ability.impl.ConfigurableTierableAbility;
import us.eunoians.mcrpg.ability.impl.PassiveAbility;
import us.eunoians.mcrpg.ability.impl.ReloadableContentAbility;
import us.eunoians.mcrpg.ability.impl.mining.remotetransfer.RemoteTransferCategory;
import us.eunoians.mcrpg.ability.impl.mining.remotetransfer.RemoteTransferCategoryType;
import us.eunoians.mcrpg.event.event.ability.mining.RemoteTransferActivateEvent;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.skill.impl.mining.Mining;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This ability allows players to link to a chest and blocks they mine will automatically go into the chest if their allow list
 * has it enabled.
 */
public final class RemoteTransfer extends McRPGAbility implements PassiveAbility, ConfigurableTierableAbility, ReloadableContentAbility {

    public static final NamespacedKey REMOTE_TRANSFER_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "remote_transfer");
    private static final Map<RemoteTransferCategoryType, RemoteTransferCategory> REMOTE_TRANSFER_CATEGORIES = new HashMap<>();

    static {
        for (RemoteTransferCategoryType type : RemoteTransferCategoryType.values()) {
            REMOTE_TRANSFER_CATEGORIES.put(type, new RemoteTransferCategory(type));
        }
    }

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
        return getPlugin().getFileManager().getFile(FileType.MINING_CONFIG);
    }

    @Override
    public int getMaxTier() {
        return getYamlDocument().getInt(MiningConfigFile.REMOTE_TRANSFER_AMOUNT_OF_TIERS);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getSkill() {
        return Optional.of(Mining.MINING_KEY);
    }

    @NotNull
    @Override
    public Optional<String> getDatabaseName() {
        return Optional.of("remote_transfer");
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Remote Transfer";
    }

    @NotNull
    @Override
    public List<String> getDescription(@NotNull McRPGPlayer mcRPGPlayer) {
        int currentTier = getCurrentAbilityTier(mcRPGPlayer.asSkillHolder());
        return List.of("<gray>Allows for linking of a chest to teleport mined blocks into.",
                "<gray>Use <gold>/mcrpg link</gold> to link a chest.",
                "<gray>Use <gold>/mcrpg unlink</gold> to unlink a chest.",
                "<gray>Remote Transfer Range: <gold>" + getRange(currentTier));
    }

    @NotNull
    @Override
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        return new ItemStack(Material.CHEST);
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        BlockDropItemEvent blockDropItemEvent = (BlockDropItemEvent) event;
        Player player = blockDropItemEvent.getPlayer();

        var abilityDataOptional = abilityHolder.getAbilityData(this);
        if (abilityDataOptional.isPresent()) {
            AbilityData abilityData = abilityDataOptional.get();
            var locationAttributeOptional = abilityData.getAbilityAttribute(AbilityAttributeManager.ABILITY_LOCATION_ATTRIBUTE);
            // If the player has a location saved
            if (locationAttributeOptional.isPresent()) {
                AbilityLocationAttribute attribute = (AbilityLocationAttribute) locationAttributeOptional.get();
                Location location = attribute.getContent();

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
                    // TODO revist to support dropping custom items (cant be entirely material based in 2024 stoopid
                    ItemStack itemStack = item.getItemStack();
                    // Get the material of the item we are putting in the chest and the amount
                    // if the chest contents are full, check if there are any stacks we can increase before dropping
                    Material material = itemStack.getType();
                    if (isMaterialTransferable(abilityHolder, material)) {
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
                                    ItemStack newStack = new ItemStack(material);
                                    // If the amount is greater than a stack
                                    if (amount > material.getMaxStackSize()) {
                                        newStack.setAmount(material.getMaxStackSize());
                                        amount -= material.getMaxStackSize();
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
                                } else if (currentItem.getType() == material) {
                                    if (currentItem.getAmount() == material.getMaxStackSize()) {
                                        continue c;
                                    } else {
                                        if (currentItem.getAmount() + amount > material.getMaxStackSize()) {
                                            amount -= material.getMaxStackSize() - currentItem.getAmount();
                                            currentItem.setAmount(material.getMaxStackSize());
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

    @Override
    public boolean isAbilityEnabled() {
        return getYamlDocument().getBoolean(MiningConfigFile.REMOTE_TRANSFER_ENABLED);
    }

    /**
     * Checks to see if the {@link Material} is transferable to the holders's linked chest.
     *
     * @param abilityHolder The {@link AbilityHolder} to check for.
     * @param material      The {@link Material} to check.
     * @return {@code true} if the provided {@link Material} is transferable to the holder's linked chest.
     */
    public boolean isMaterialTransferable(@NotNull AbilityHolder abilityHolder, @NotNull Material material) {
        boolean presentInConfig = false;
        for (RemoteTransferCategory category : getRemoteTransferCategories().values()) {
            if (category.getContent().contains(material)) {
                presentInConfig = true;
            }
        }
        RemoteTransfer remoteTransfer = (RemoteTransfer) getPlugin().getAbilityRegistry().getRegisteredAbility(RemoteTransfer.REMOTE_TRANSFER_KEY);
        var abilityDataOptional = abilityHolder.getAbilityData(remoteTransfer);
        if (abilityDataOptional.isPresent() && abilityDataOptional.get().getAbilityAttribute(AbilityAttributeManager.REMOTE_TRANSFER_MATERIAL_SET_ATTRIBUTE).isPresent() &&
                abilityDataOptional.get().getAbilityAttribute(AbilityAttributeManager.REMOTE_TRANSFER_MATERIAL_SET_ATTRIBUTE).get() instanceof RemoteTransferMaterialSetAttribute remoteTransferMaterialSetAttribute) {

            return presentInConfig && !remoteTransferMaterialSetAttribute.isMaterialStored(material);
        }
        return presentInConfig;
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        Set<NamespacedKey> applicableAttributes = new HashSet<>(ConfigurableTierableAbility.super.getApplicableAttributes());
        applicableAttributes.add(AbilityAttributeManager.ABILITY_LOCATION_ATTRIBUTE);
        applicableAttributes.add(AbilityAttributeManager.REMOTE_TRANSFER_MATERIAL_SET_ATTRIBUTE);
        return applicableAttributes;
    }

    @Override
    public Set<ReloadableContent<?>> getReloadableContent() {
        return Set.copyOf(REMOTE_TRANSFER_CATEGORIES.values());
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

    /**
     * Gets the {@link RemoteTransferCategory} belonging to the provided {@link RemoteTransferCategoryType}.
     *
     * @param categoryType The {@link RemoteTransferCategoryType} to get the {@link RemoteTransferCategory} for.
     * @return The {@link RemoteTransferCategory} belonging to the provided {@link RemoteTransferCategoryType}.
     */
    @NotNull
    public static RemoteTransferCategory getRemoteTransferCategory(@NotNull RemoteTransferCategoryType categoryType) {
        return REMOTE_TRANSFER_CATEGORIES.get(categoryType);
    }

    /**
     * Gets an {@link ImmutableMap} of all the {@link RemoteTransferCategoryType}s mapped to their respective {@link RemoteTransferCategory}.
     *
     * @return An {@link ImmutableMap} of all the {@link RemoteTransferCategoryType}s mapped to their respective {@link RemoteTransferCategory}.
     */
    @NotNull
    public static Map<RemoteTransferCategoryType, RemoteTransferCategory> getRemoteTransferCategories() {
        return ImmutableMap.copyOf(REMOTE_TRANSFER_CATEGORIES);
    }
}
