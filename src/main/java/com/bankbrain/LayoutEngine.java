package com.bankbrain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Singleton;

@Singleton
public class LayoutEngine
{
    private static final int MAX_TABS = 9;

    public LayoutResult buildLayout(
        List<BankItem> items,
        java.util.Comparator<BankItem> comparator,
        int[] currentSlotTabs,
        int containerSize,
        Map<Integer, Integer> desiredTabs)
    {
        Map<Integer, List<BankItem>> itemsByTab = new LinkedHashMap<>();
        for (BankItem item : items)
        {
            int tab = desiredTabs.getOrDefault(item.getIndex(), item.getTabIndex());
            itemsByTab.computeIfAbsent(tab, key -> new ArrayList<>()).add(item);
        }

        Map<Integer, Integer> targetIndex = new HashMap<>();
        List<BankItem> ordered = new ArrayList<>();
        int[] targetSlotTabs = new int[containerSize];
        int[] targetTabSlotPositions = new int[containerSize];

        int slotPointer = 0;
        for (int tab = 0; tab <= MAX_TABS; tab++)
        {
            List<BankItem> tabItems = itemsByTab.get(tab);
            if (tabItems == null || tabItems.isEmpty())
            {
                continue;
            }

            List<BankItem> copy = new ArrayList<>(tabItems);
            copy.sort((left, right) -> {
                int result = comparator.compare(left, right);
                return result != 0 ? result : Integer.compare(left.getIndex(), right.getIndex());
            });

            int tabSlotCounter = 0;
            for (BankItem item : copy)
            {
                if (slotPointer >= containerSize)
                {
                    break;
                }

                ordered.add(item);
                targetIndex.put(item.getIndex(), slotPointer);
                targetSlotTabs[slotPointer] = tab;
                targetTabSlotPositions[slotPointer] = ++tabSlotCounter;
                slotPointer++;
            }
        }

        if (slotPointer < items.size())
        {
            // Handle any items whose target tabs were outside the expected range
            for (Map.Entry<Integer, List<BankItem>> entry : itemsByTab.entrySet())
            {
                int tab = entry.getKey();
                if (tab >= 0 && tab <= MAX_TABS)
                {
                    continue;
                }

                List<BankItem> copy = new ArrayList<>(entry.getValue());
                copy.sort((left, right) -> {
                    int result = comparator.compare(left, right);
                    return result != 0 ? result : Integer.compare(left.getIndex(), right.getIndex());
                });

                int tabSlotCounter = 0;
                for (BankItem item : copy)
                {
                    if (slotPointer >= containerSize)
                    {
                        break;
                    }

                    ordered.add(item);
                    targetIndex.put(item.getIndex(), slotPointer);
                    targetSlotTabs[slotPointer] = tab;
                    targetTabSlotPositions[slotPointer] = ++tabSlotCounter;
                    slotPointer++;
                }
            }
        }

        return new LayoutResult(
            ordered,
            targetIndex,
            Arrays.copyOf(currentSlotTabs, currentSlotTabs.length),
            targetSlotTabs,
            targetTabSlotPositions);
    }

    public static class LayoutResult
    {
        private final List<BankItem> orderedItems;
        private final Map<Integer, Integer> targetIndex;
        private final int[] sourceSlotTabs;
        private final int[] targetSlotTabs;
        private final int[] targetTabSlotPositions;

        LayoutResult(
            List<BankItem> orderedItems,
            Map<Integer, Integer> targetIndex,
            int[] sourceSlotTabs,
            int[] targetSlotTabs,
            int[] targetTabSlotPositions)
        {
            this.orderedItems = orderedItems;
            this.targetIndex = targetIndex;
            this.sourceSlotTabs = sourceSlotTabs;
            this.targetSlotTabs = targetSlotTabs;
            this.targetTabSlotPositions = targetTabSlotPositions;
        }

        public List<BankItem> getOrderedItems()
        {
            return orderedItems;
        }

        public Optional<Integer> getTargetForCurrentIndex(int currentIndex)
        {
            return Optional.ofNullable(targetIndex.get(currentIndex));
        }

        public Map<Integer, Integer> getTargetIndex()
        {
            return targetIndex;
        }

        public int[] getSourceSlotTabs()
        {
            return Arrays.copyOf(sourceSlotTabs, sourceSlotTabs.length);
        }

        public int[] getTargetSlotTabs()
        {
            return Arrays.copyOf(targetSlotTabs, targetSlotTabs.length);
        }

        public int[] getTargetTabSlotPositions()
        {
            return Arrays.copyOf(targetTabSlotPositions, targetTabSlotPositions.length);
        }

        public static LayoutResult empty()
        {
            return new LayoutResult(List.of(), Map.of(), new int[0], new int[0], new int[0]);
        }
    }
}
