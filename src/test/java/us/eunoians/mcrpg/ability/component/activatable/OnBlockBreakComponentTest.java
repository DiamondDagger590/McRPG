package us.eunoians.mcrpg.ability.component.activatable;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.world.WorldManager;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@DisplayName("OnBlockBreakComponent")
public class OnBlockBreakComponentTest extends McRPGBaseTest {

    private static final UUID HOLDER_UUID = UUID.randomUUID();
    private AbilityHolder holder;

    @BeforeEach
    public void setup() {
        holder = mock(AbilityHolder.class);
        when(holder.getUUID()).thenReturn(HOLDER_UUID);

        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);

        YamlDocument mainConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);
        when(mainConfig.getStringList(MainConfigFile.DISABLED_WORLDS)).thenReturn(List.of(""));

        WorldManager worldManager = spy(new WorldManager(mcRPG));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(worldManager);
    }

    @Nested
    @DisplayName("shouldActivate")
    class ShouldActivate {

        @DisplayName("returns true when player matches holder, affectsBlock is true, and block is natural")
        @Test
        public void shouldActivate_returnsTrue_whenAllConditionsMet() {
            Player player = mock(Player.class);
            when(player.getUniqueId()).thenReturn(HOLDER_UUID);

            Block block = mock(Block.class);

            BlockBreakEvent event = mock(BlockBreakEvent.class);
            when(event.getPlayer()).thenReturn(player);
            when(event.getBlock()).thenReturn(block);

            WorldManager worldManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.WORLD);
            doReturn(true).when(worldManager).isBlockNatural(block);

            OnBlockBreakComponent component = new OnBlockBreakComponent() {
                @Override
                public boolean affectsBlock(@NotNull Block b) {
                    return true;
                }
            };

            assertTrue(component.shouldActivate(holder, event));
        }

        @DisplayName("returns false when player UUID does not match holder UUID")
        @Test
        public void shouldActivate_returnsFalse_whenPlayerDoesNotMatchHolder() {
            Player player = mock(Player.class);
            when(player.getUniqueId()).thenReturn(UUID.randomUUID());

            Block block = mock(Block.class);

            BlockBreakEvent event = mock(BlockBreakEvent.class);
            when(event.getPlayer()).thenReturn(player);
            when(event.getBlock()).thenReturn(block);

            OnBlockBreakComponent component = new OnBlockBreakComponent() {
                @Override
                public boolean affectsBlock(@NotNull Block b) {
                    return true;
                }
            };

            assertFalse(component.shouldActivate(holder, event));
        }

        @DisplayName("returns false when affectsBlock returns false")
        @Test
        public void shouldActivate_returnsFalse_whenAffectsBlockReturnsFalse() {
            Player player = mock(Player.class);
            when(player.getUniqueId()).thenReturn(HOLDER_UUID);

            Block block = mock(Block.class);

            BlockBreakEvent event = mock(BlockBreakEvent.class);
            when(event.getPlayer()).thenReturn(player);
            when(event.getBlock()).thenReturn(block);

            OnBlockBreakComponent component = new OnBlockBreakComponent() {
                @Override
                public boolean affectsBlock(@NotNull Block b) {
                    return false;
                }
            };

            assertFalse(component.shouldActivate(holder, event));
        }

        @DisplayName("returns false for non-BlockBreakEvent")
        @Test
        public void shouldActivate_returnsFalse_whenEventIsNotBlockBreakEvent() {
            EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);

            OnBlockBreakComponent component = new OnBlockBreakComponent() {
                @Override
                public boolean affectsBlock(@NotNull Block b) {
                    return true;
                }
            };

            assertFalse(component.shouldActivate(holder, event));
        }

        @DisplayName("returns false when block is unnatural and affectsUnnaturalBlocks is false")
        @Test
        public void shouldActivate_returnsFalse_whenBlockUnnaturalAndDoesNotAffectUnnatural() {
            Player player = mock(Player.class);
            when(player.getUniqueId()).thenReturn(HOLDER_UUID);

            Block block = mock(Block.class);

            BlockBreakEvent event = mock(BlockBreakEvent.class);
            when(event.getPlayer()).thenReturn(player);
            when(event.getBlock()).thenReturn(block);

            WorldManager worldManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.WORLD);
            doReturn(false).when(worldManager).isBlockNatural(block);

            OnBlockBreakComponent component = new OnBlockBreakComponent() {
                @Override
                public boolean affectsBlock(@NotNull Block b) {
                    return true;
                }
            };

            assertFalse(component.shouldActivate(holder, event));
        }

        @DisplayName("returns true when block is unnatural and affectsUnnaturalBlocks is true")
        @Test
        public void shouldActivate_returnsTrue_whenBlockUnnaturalAndAffectsUnnatural() {
            Player player = mock(Player.class);
            when(player.getUniqueId()).thenReturn(HOLDER_UUID);

            Block block = mock(Block.class);

            BlockBreakEvent event = mock(BlockBreakEvent.class);
            when(event.getPlayer()).thenReturn(player);
            when(event.getBlock()).thenReturn(block);

            WorldManager worldManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.WORLD);
            doReturn(false).when(worldManager).isBlockNatural(block);

            OnBlockBreakComponent component = new OnBlockBreakComponent() {
                @Override
                public boolean affectsBlock(@NotNull Block b) {
                    return true;
                }

                @Override
                public boolean affectsUnnaturalBlocks() {
                    return true;
                }
            };

            assertTrue(component.shouldActivate(holder, event));
        }
    }

    @Nested
    @DisplayName("affectsUnnaturalBlocks")
    class AffectsUnnaturalBlocks {

        @DisplayName("default returns false")
        @Test
        public void affectsUnnaturalBlocks_defaultReturnsFalse() {
            OnBlockBreakComponent component = new OnBlockBreakComponent() {
                @Override
                public boolean affectsBlock(@NotNull Block b) {
                    return true;
                }
            };

            assertFalse(component.affectsUnnaturalBlocks());
        }
    }
}
