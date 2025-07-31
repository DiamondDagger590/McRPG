package us.eunoians.mcrpg.gui.skill;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.gui.PaginatedGui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.LinkedNode;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.util.comparator.McRPGPlayerContextComparator;
import us.eunoians.mcrpg.util.filter.core.McRPGPlayerContextFilter;
import us.eunoians.mcrpg.util.filter.skill.SkillHolderDataPresentFilter;

import java.util.Comparator;
import java.util.List;

/**
 * This enum contains all the different ways {@link Skill}s can be sorted, allowing an easily accessible way
 * to add a sorting {@link Slot} to a given {@link com.diamonddagger590.mccore.gui.Gui} using {@link #getSlot()}.
 * <p>
 * This enum also provides a {@link LinkedNode}, where each node is linked to each other in a closed loop. The first node in the list can be
 * {@link #getFirstSortType()}.
 */
public enum SkillSortType {

    ALPHABETICAL(LocalizationKey.SKILL_SORT_ALPHABETICAL_DISPLAY_ITEM, null, mcrpgPlayer -> Comparator.comparing((Skill skill) -> skill.getName(mcrpgPlayer))),
    SKILL_LEVEL(LocalizationKey.SKILL_SORT_SKILL_LEVEL_DISPLAY_ITEM, new SkillHolderDataPresentFilter(),
            mcRPGPlayer -> Comparator.comparing((Skill skill) ->  mcRPGPlayer.asSkillHolder().getSkillHolderData(skill).get().getCurrentLevel())),
    SKILL_EXPERIENCE_TO_LEVEL(LocalizationKey.SKILL_SORT_EXPERIENCE_TO_LEVEL_DISPLAY_ITEM, new SkillHolderDataPresentFilter(),
            mcRPGPlayer -> Comparator.comparing((Skill skill) ->  mcRPGPlayer.asSkillHolder().getSkillHolderData(skill).get().getRemainingExperienceForNextLevel())),
    ;

    private final static LinkedNode<SkillSortType> FIRST_SORT_TYPE = new LinkedNode<>(SkillSortType.ALPHABETICAL);
    static {
        LinkedNode<SkillSortType> prev = FIRST_SORT_TYPE;
        // Using definition order as the link order
        for (SkillSortType type : values()) {
            // Skip our first node type
            if (type != FIRST_SORT_TYPE.getNodeValue()) {
                LinkedNode<SkillSortType> next = new LinkedNode<>(type);
                prev.setNext(next);
                prev = next;
            }
        }
        // Set the tail of these linked nodes to start back at the head
        prev.setNext(FIRST_SORT_TYPE);
    }

    private final Route displayItemRoute;
    private final McRPGPlayerContextFilter<Skill> filter;
    private final McRPGPlayerContextComparator<Skill> skillComparator;

    SkillSortType(@NotNull Route displayItemRoute, @Nullable McRPGPlayerContextFilter<Skill> filter, @NotNull McRPGPlayerContextComparator<Skill> skillComparator) {
        this.displayItemRoute = displayItemRoute;
        this.filter = filter;
        this.skillComparator = skillComparator;
    }

    /**
     * Gets the {@link Comparator} used to sort {@link Skill}s.
     *
     * @return The {@link Comparator} used to sort {@link Skill}s.
     */
    @NotNull
    public Comparator<Skill> getSkillComparator(@NotNull McRPGPlayer mcRPGPlayer) {
        return this.skillComparator.getComparator(mcRPGPlayer);
    }

    /**
     * Gets the filtered collection to display based on the filter for this sort type, or
     * just the original list if there is no filter present.
     *
     * @param skills The list of {@link Skill}s to filter.
     * @return A filtered {@link List} of {@link Skill}s
     */
    @NotNull
    public List<Skill> filter(@NotNull McRPGPlayer mcRPGPlayer, @NotNull List<Skill> skills) {
        return filter == null ? skills : List.copyOf(filter.filter(mcRPGPlayer, skills));
    }

    /**
     * Gets a {@link Slot} that will progress to the next sort type and refresh the gui it is hosted in.
     * <p>
     * This slot can only be added to {@link SortableSkillGui}'s, and if it is added to a {@link PaginatedGui} as well
     * (see {@link PaginatedSortedSkillGui}), then it will set the page to 1 before refreshing the gui.
     *
     * @return A {@link Slot} that when clicked will progress to the next sort type and refresh the gui it is
     * hosted in.
     */
    @NotNull
    public McRPGSlot getSlot() {
        return new McRPGSlot() {
            @Override
            public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
                var guiOptional = mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).getOpenedGui(mcRPGPlayer);
                guiOptional.ifPresent(gui -> {
                    // If it's a sortable gui, progress node
                    if (gui instanceof SortableSkillGui sortableSkillGui) {
                        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
                            sortableSkillGui.progressToNextSortNode();
                            // If it's a paginated gui, reset page
                            if (gui instanceof PaginatedGui<?> paginatedGui) {
                                paginatedGui.setPage(1);
                            }
                            // Refresh gui
                            gui.refreshGUI();
                        });
                    }
                });
                return true;
            }

            @NotNull
            @Override
            public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
                return ItemBuilder.from(mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION).getLocalizedSection(mcRPGPlayer, displayItemRoute));
            }
        };
    }

    /**
     * Gets the first {@link LinkedNode} in the chain of sort types.
     *
     * @return The first {@link LinkedNode} in the chain of sort types.
     */
    @NotNull
    public static LinkedNode<SkillSortType> getFirstSortType() {
        return FIRST_SORT_TYPE;
    }
}
