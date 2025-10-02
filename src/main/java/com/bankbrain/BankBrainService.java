package com.bankbrain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;
import net.runelite.http.api.item.ItemStats;

@Singleton
public class BankBrainService
{
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
            return;
        }

        Item[] items = bank.getItems();
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
                lastWithdrawn
            );
            collected.add(bankItem);
        }

        snapshot = Collections.unmodifiableList(collected);
        List<BankSortRule> rules = sorter.parseRules(config.defaultRules());
        java.util.Comparator<BankItem> comparator = sorter.buildComparator(rules);
        layoutResult = layoutEngine.buildLayout(snapshot, comparator);
        reorderPlan = planBuilder.buildPlan(snapshot, layoutResult.getTargetIndex());
        lastRebuild = Instant.now();
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
}
