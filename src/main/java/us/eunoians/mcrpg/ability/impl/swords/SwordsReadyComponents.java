package us.eunoians.mcrpg.ability.impl.swords;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.component.readyable.RightClickReadyComponent;

import java.util.Set;

public class SwordsReadyComponents {

    public static final SwordsReadyComponent SWORDS_READY_COMPONENT = new SwordsReadyComponent();

    private static class SwordsReadyComponent implements RightClickReadyComponent {
        @NotNull
        @Override
        public Set<Material> getValidMaterialsForActivation() {
            return Set.of(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
                    Material.DIAMOND_SWORD, Material.GOLDEN_SWORD, Material.NETHERITE_SWORD);
        }
    }
}
