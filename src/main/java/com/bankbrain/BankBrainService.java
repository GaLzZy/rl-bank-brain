package com.bankbrain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemComposition;
import net.runelite.api.Varbits;
import net.runelite.client.game.ItemManager;
import net.runelite.http.api.item.ItemStats;

@Singleton
public class BankBrainService
{
    private static final int[] TAB_COUNT_VARBITS = {
        Varbits.BANK_TAB_ONE_COUNT,
        Varbits.BANK_TAB_TWO_COUNT,
        Varbits.BANK_TAB_THREE_COUNT,
        Varbits.BANK_TAB_FOUR_COUNT,
        Varbits.BANK_TAB_FIVE_COUNT,
        Varbits.BANK_TAB_SIX_COUNT,
        Varbits.BANK_TAB_SEVEN_COUNT,
        Varbits.BANK_TAB_EIGHT_COUNT,
        Varbits.BANK_TAB_NINE_COUNT
    };

    private static final Map<SlotType, Integer> SLOT_TYPE_TAB_MAP = Map.ofEntries(
        Map.entry(SlotType.WEAPON, 1),
        Map.entry(SlotType.HELM, 2),
        Map.entry(SlotType.BODY, 2),
        Map.entry(SlotType.LEGS, 2),
        Map.entry(SlotType.SHIELD, 2),
        Map.entry(SlotType.GLOVES, 2),
        Map.entry(SlotType.BOOTS, 2),
        Map.entry(SlotType.CAPE, 3),
        Map.entry(SlotType.AMULET, 3),
        Map.entry(SlotType.RING, 3),
        Map.entry(SlotType.JEWELLERY, 3),
        Map.entry(SlotType.AMMO, 4),
        Map.entry(SlotType.QUIVER, 4),
        Map.entry(SlotType.TOOL, 5),
        Map.entry(SlotType.CONSUMABLE, 6),
        Map.entry(SlotType.MATERIAL, 7),
        Map.entry(SlotType.PET, 8)
    );

    private final Client client;
    private final ItemManager itemManager;
    private final BankClassifier classifier;
    private final BankSorter sorter;
    private final LayoutEngine layoutEngine;
    private final PlanBuilder planBuilder;

    private final Map<Integer, Instant> usageHistory = new HashMap<>();

    private List<BankItem> snapshot = List.of();
    private ReorderPlan reorderPlan = new ReorderPlan(List.of(), 0);
    private LayoutEngine.LayoutResult layoutResult = LayoutEngine.LayoutResult.empty();
    private Instant lastRebuild = Instant.EPOCH;
    private int[] slotTabs = new int[0];
    private int[] tabSlotPositions = new int[0];
    private int containerSize = 0;
    private int activeStep = -1;

    @Inject
    public BankBrainService(
        Client client,
        ItemManager itemManager,
        BankClassifier classifier,
        BankSorter sorter,
        LayoutEngine layoutEngine,
        PlanBuilder planBuilder)
    {
        this.client = client;
        this.itemManager = itemManager;
        this.classifier = classifier;
        this.sorter = sorter;
        this.layoutEngine = layoutEngine;
        this.planBuilder = planBuilder;
    }

    public void rebuildPlan(BankBrainConfig config)
    {
        ItemContainer bank = client.getItemContainer(InventoryID.BANK);
        if (bank == null)
        {
            snapshot = List.of();
            reorderPlan = new ReorderPlan(List.of(), 0);
            layoutResult = LayoutEngine.LayoutResult.empty();
            slotTabs = new int[0];
            tabSlotPositions = new int[0];
            containerSize = 0;
            activeStep = -1;
            return;
        }

        Item[] items = bank.getItems();
        containerSize = items.length;
        slotTabs = computeSlotTabs(containerSize);
        tabSlotPositions = computeTabSlotPositions(slotTabs);
        List<BankItem> collected = new ArrayList<>();
        for (int index = 0; index < items.length; index++)
        {
            Item item = items[index];
            if (item == null || item.getId() <= 0)
            {
                continue;
            }

            ItemComposition composition = itemManager.getItemComposition(item.getId());
            BankCategory category = classifier.categoryFor(composition);
            SlotType slotType = classifier.slotTypeFor(composition);
            Set<String> tags = classifier.tagsFor(composition, category);
            long gePrice = itemManager.getItemPrice(item.getId());
            int ha = composition.getHaPrice();
            ItemStats stats = itemManager.getItemStats(item.getId(), false);
            double weight = stats != null ? stats.getWeight() : 0.0;
            Instant lastWithdrawn = usageHistory.get(item.getId());

            BankItem bankItem = new BankItem(
                item.getId(),
                item.getQuantity(),
                index,
                composition.getName(),
                composition.isMembers(),
                category,
                slotType,
                tags,
                gePrice,
                ha,
                weight,
                composition.isStackable(),
                lastWithdrawn,
                index < slotTabs.length ? slotTabs[index] : 0
            );
            collected.add(bankItem);
        }

        snapshot = Collections.unmodifiableList(collected);
        List<BankSortRule> rules = sorter.parseRules(config.defaultRules());
        java.util.Comparator<BankItem> comparator = sorter.buildComparator(rules);
        Map<Integer, Integer> desiredTabs = snapshot.stream()
            .collect(Collectors.toMap(BankItem::getIndex, this::desiredTabFor));

        layoutResult = layoutEngine.buildLayout(snapshot, comparator, slotTabs, containerSize, desiredTabs);
        reorderPlan = planBuilder.buildPlan(
            snapshot,
            layoutResult.getTargetIndex(),
            containerSize,
            slotTabs,
            tabSlotPositions,
            layoutResult.getTargetSlotTabs(),
            layoutResult.getTargetTabSlotPositions());
        lastRebuild = Instant.now();
        activeStep = reorderPlan.isEmpty() ? -1 : 0;
    }

    public void recordWithdrawal(int itemId)
    {
        usageHistory.put(itemId, Instant.now());
    }

    public List<BankItem> getSnapshot()
    {
        return snapshot;
    }

    public ReorderPlan getReorderPlan()
    {
        return reorderPlan;
    }

    public Optional<LayoutEngine.LayoutResult> getLayoutResult()
    {
        return Optional.ofNullable(layoutResult);
    }

    public Instant getLastRebuild()
    {
        return lastRebuild;
    }

    public int[] getSlotTabs()
    {
        return Arrays.copyOf(slotTabs, slotTabs.length);
    }

    public int[] getTabSlotPositions()
    {
        return Arrays.copyOf(tabSlotPositions, tabSlotPositions.length);
    }

    public int getContainerSize()
    {
        return containerSize;
    }

    public int getActiveStep()
    {
        return activeStep;
    }

    public void setActiveStep(int step)
    {
        if (reorderPlan.isEmpty())
        {
            activeStep = -1;
            return;
        }

        int clamped = Math.max(-1, Math.min(step, reorderPlan.getSteps().size() - 1));
        activeStep = clamped;
    }

    private int[] computeSlotTabs(int slotCount)
    {
        if (slotCount <= 0)
        {
            return new int[0];
        }

        int[] tabs = new int[slotCount];
        int cursor = 0;
        for (int tab = 0; tab < TAB_COUNT_VARBITS.length && cursor < slotCount; tab++)
        {
            int count = Math.max(0, client.getVarbitValue(TAB_COUNT_VARBITS[tab]));
            for (int i = 0; i < count && cursor < slotCount; i++)
            {
                tabs[cursor++] = tab + 1;
            }
        }

        while (cursor < slotCount)
        {
            tabs[cursor++] = 0;
        }

        return tabs;
    }

    private int[] computeTabSlotPositions(int[] tabs)
    {
        int[] positions = new int[tabs.length];
        Map<Integer, Integer> counters = new HashMap<>();
        for (int i = 0; i < tabs.length; i++)
        {
            int tab = tabs[i];
            int next = counters.merge(tab, 1, Integer::sum);
            positions[i] = next;
        }
        return positions;
    }

    private int desiredTabFor(BankItem item)
    {
        Integer mappedTab = SLOT_TYPE_TAB_MAP.get(item.getSlotType());
        if (mappedTab != null)
        {
            return mappedTab;
        }

        switch (item.getCategory())
        {
            case RUNES_AND_TELEPORTS:
                return 4;
            case UTILITY:
                return 5;
            case CONSUMABLES:
                return 6;
            case GATHERING:
            case PRODUCTION:
            case FARMING:
                return 7;
            case PETS:
                return 8;
            case CLUE:
            case COSMETIC:
            case QUEST:
            case STAGING:
                return 9;
            case COMBAT_GEAR:
            case UNASSIGNED:
            default:
                return 9;
        }
    }
}
