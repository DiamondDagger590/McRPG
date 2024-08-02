package us.eunoians.mcrpg.ability.impl.mining;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.audience.Audience;
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
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.attribute.AbilityLocationAttribute;
import us.eunoians.mcrpg.ability.impl.BaseAbility;
import us.eunoians.mcrpg.ability.impl.ConfigurableTierableAbility;
import us.eunoians.mcrpg.ability.impl.PassiveAbility;
import us.eunoians.mcrpg.api.event.fake.FakeChestOpenEvent;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.skill.impl.mining.Mining;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RemoteTransfer extends BaseAbility implements PassiveAbility, ConfigurableTierableAbility {

    public static final NamespacedKey REMOTE_TRANSFER_KEY = new NamespacedKey(McRPG.getInstance(), "remote_transfer");

    public RemoteTransfer() {
        super(REMOTE_TRANSFER_KEY);
    }

    @NotNull
    @Override
    public Route getAbilityTierConfigurationRoute() {
        return MiningConfigFile.REMOTE_TRANSFER_CONFIGURATION_HEADER;
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return McRPG.getInstance().getFileManager().getFile(FileType.MINING_CONFIG);
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
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        return new ItemStack(Material.CHEST);
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        BlockDropItemEvent blockDropItemEvent = (BlockDropItemEvent) event;
        if (!this.isAbilityEnabled() || !abilityHolder.getUUID().equals(blockDropItemEvent.getPlayer().getUniqueId())) {
            return;
        }
        Player player  = blockDropItemEvent.getPlayer();
        var abilityDataOptional = abilityHolder.getAbilityData(this);
        if (abilityDataOptional.isPresent()) {
            AbilityData abilityData = abilityDataOptional.get();
            var locationAttributeOptional = abilityData.getAbilityAttribute(AbilityAttributeManager.ABILITY_LOCATION_ATTRIBUTE);
            // If the player has a location saved
            if (locationAttributeOptional.isPresent()) {
                AbilityLocationAttribute attribute = (AbilityLocationAttribute) locationAttributeOptional.get();
                Location location = attribute.getContent();
                // If the content shouldn't be saved, the worlds aren't the same, or the chest is too far away, we skip
                if (!attribute.shouldContentBeSaved() || !location.getWorld().equals(player.getWorld()) || location.distanceSquared(player.getLocation()) >= Math.pow(100, 2)) {
                    return;
                }
                Chunk chunk = location.getChunk();
                // Force the chunk to load if it isn't already
                chunk.addPluginChunkTicket(McRPG.getInstance());

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
                    return;
                }
                // Simulate opening the chest
                FakeChestOpenEvent fakeChestOpenEvent = new FakeChestOpenEvent(player, block.getLocation());
                Bukkit.getPluginManager().callEvent(fakeChestOpenEvent);
                if (fakeChestOpenEvent.useInteractedBlock() == Event.Result.DENY) {
                    Audience audience = McRPG.getInstance().getAdventure().player(player);
                    audience.sendMessage(McRPG.getInstance().getMiniMessage().deserialize("<red>Your linked chest for remote transfer was blocked from your usage... unlinking."));
                    abilityData.removeAttribute(attribute);
                    return;
                }
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
                                    if(amount > 64){
                                        newStack.setAmount(64);
                                        amount -= 64;
                                        currentInventory.setItem(i, newStack);
                                        continue c;
                                    }
                                    // Otherwise just slap the item in there and break since we dont need to put it anywhere else
                                    else{
                                        newStack.setAmount(amount);
                                        currentInventory.setItem(i, newStack);
                                        amount = 0;
                                        item.getItemStack().setAmount(0);
                                        break b;
                                    }
                                }
                                else if(currentItem.getType() == material){
                                    if(currentItem.getAmount() == 64){
                                        continue c;
                                    }
                                    else{
                                        if(currentItem.getAmount() + amount > 64){
                                            amount -= 64 - currentItem.getAmount();
                                            currentItem.setAmount(64);
                                            continue c;
                                        }
                                        else{
                                            currentItem.setAmount(currentItem.getAmount() + amount);
                                            amount = 0;
                                            item.getItemStack().setAmount(0);
                                            break b;
                                        }
                                    }
                                }
                                else {
                                    continue c;
                                }
                            }
                            block.getState().update();
                            if (isDouble) {
                                for(int i = -1; i <= 1; i += 2){
                                    block.getWorld().getBlockAt(block.getLocation().add(i, 0, 0)).getState().update();
                                    block.getWorld().getBlockAt(block.getLocation().add(0, 0, i)).getState().update();
                                }
                            }
                            // Drop leftovers
                            if(amount > 0){
                                item.getItemStack().setAmount(amount);
                            }
                        }
                    }
                    else {
                        continue a;
                    }
                }
                // Allow the chunk to unload now that we are done
                // TODO set this to be on a runnable but reset it every time this is called so we aren't constantly loading/unloading
                chunk.removePluginChunkTicket(McRPG.getInstance());
            }
        }
    }

    @Override
    public boolean isAbilityEnabled() {
        return getYamlDocument().getBoolean(MiningConfigFile.ITS_A_TRIPLE_ENABLED);
    }

    public boolean isMaterialTransferable(@NotNull AbilityHolder abilityHolder, @NotNull Material material) {
        return true;
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        Set<NamespacedKey> applicableAttributes = new HashSet<>(ConfigurableTierableAbility.super.getApplicableAttributes());
        applicableAttributes.add(AbilityAttributeManager.ABILITY_LOCATION_ATTRIBUTE);
        applicableAttributes.add(AbilityAttributeManager.ABILITY_MATERIAL_SET_ATTRIBUTE);
        return applicableAttributes;
    }
}
