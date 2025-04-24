package us.eunoians.mcrpg.ability.impl.mining.remotetransfer;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.configuration.ReloadableSet;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKeys;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.stream.Collectors;

public class RemoteTransferCategory {

    private final String categoryKey;
    private final ReloadableSet<Material> allowedMaterials;

    public RemoteTransferCategory(@NotNull String categoryKey){
        this.categoryKey = categoryKey;
        allowedMaterials = new ReloadableSet<>(McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MINING_CONFIG), categoryType.getConfigurationRoute(), strings -> strings.stream().map(Material::getMaterial).collect(Collectors.toSet()));
    }

    @NotNull
    public String getCategoryKey(){
        return categoryKey;
    }

    @NotNull
    public String getName(@NotNull McRPGPlayer player){
        return player.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION).getLocalizedMessage(player, Route.addTo(LocalizationKeys.REMOTE_TRANSFER_GUI_CATEGORIES_HEADER, categoryKey + ".name"));
    }

    @NotNull
    public ItemBuilder getItemBuilder(@NotNull McRPGPlayer player){
        ItemBuilder itemBuilder = ItemBuilder.from(player.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION).getLocalizedSection(player, Route.addTo(LocalizationKeys.REMOTE_TRANSFER_GUI_CATEGORIES_HEADER, categoryKey + ".display-item")));
        itemBuilder.addPlaceholder("remote-transfer-category", getName(player));
        return itemBuilder;
    }
}
