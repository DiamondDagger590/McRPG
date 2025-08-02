package us.eunoians.mcrpg.ability.impl.mining.remotetransfer;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import com.google.common.collect.ImmutableSet;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashSet;
import java.util.Set;

public class RemoteTransferCategory {

    private final String categoryKey;
    private final Set<CustomItemWrapper> categoryItems;

    public RemoteTransferCategory(@NotNull String categoryKey){
        this.categoryKey = categoryKey;
        this.categoryItems = new HashSet<>();
    }

    public RemoteTransferCategory(@NotNull String categoryKey, @NotNull Set<CustomItemWrapper> categoryItems){
        this.categoryKey = categoryKey;
        this.categoryItems = categoryItems;
    }

    @NotNull
    public String getCategoryKey(){
        return categoryKey;
    }

    @NotNull
    public Set<CustomItemWrapper> getCategoryItems(){
        return ImmutableSet.copyOf(categoryItems);
    }

    public void setCategoryBlocks(@NotNull Set<CustomItemWrapper> categoryItems){
        this.categoryItems.clear();
        this.categoryItems.addAll(categoryItems);
    }

    public void addCategoryItem(@NotNull CustomItemWrapper customItemWrapper){
        categoryItems.add(customItemWrapper);
    }

    @NotNull
    public String getName(@NotNull McRPGPlayer player){
        return player.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION).getLocalizedMessage(player, Route.addTo(LocalizationKey.REMOTE_TRANSFER_GUI_CATEGORIES_HEADER, categoryKey + ".name"));
    }

    @NotNull
    public ItemBuilder getItemBuilder(@NotNull McRPGPlayer player){
        ItemBuilder itemBuilder = ItemBuilder.from(player.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION).getLocalizedSection(player, Route.addTo(LocalizationKey.REMOTE_TRANSFER_GUI_CATEGORIES_HEADER, categoryKey + ".display-item")));
        itemBuilder.addPlaceholder("remote-transfer-category", getName(player));
        return itemBuilder;
    }
}
