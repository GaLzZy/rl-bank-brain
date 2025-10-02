package com.bankbrain;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Singleton;

@Singleton
public class LayoutEngine
{
    public LayoutResult buildLayout(List<BankItem> items, java.util.Comparator<BankItem> comparator, int[] slotTabs, int containerSize)
    {
        Map<Integer, List<BankItem>> itemsByTab = new LinkedHashMap<>();
        for (BankItem item : items)
        {
            itemsByTab.computeIfAbsent(item.getTabIndex(), key -> new ArrayList<>()).add(item);
        }

        Map<Integer, Deque<BankItem>> sortedQueues = new HashMap<>();
        for (Map.Entry<Integer, List<BankItem>> entry : itemsByTab.entrySet())
        {
            List<BankItem> copy = new ArrayList<>(entry.getValue());
            copy.sort(comparator);
            sortedQueues.put(entry.getKey(), new ArrayDeque<>(copy));
        }

        Map<Integer, Integer> targetIndex = new HashMap<>();
        List<BankItem> ordered = new ArrayList<>();

        for (int slot = 0; slot < containerSize; slot++)
        {
            int tab = slot < slotTabs.length ? slotTabs[slot] : 0;
            Deque<BankItem> queue = sortedQueues.get(tab);
            if (queue == null || queue.isEmpty())
            {
                continue;
            }

            BankItem next = queue.pollFirst();
            ordered.add(next);
            targetIndex.put(next.getIndex(), slot);
        }

        return new LayoutResult(ordered, targetIndex, Arrays.copyOf(slotTabs, slotTabs.length));
    }

    public static class LayoutResult
    {
        private final List<BankItem> orderedItems;
        private final Map<Integer, Integer> targetIndex;
        private final int[] slotTabs;

        LayoutResult(List<BankItem> orderedItems, Map<Integer, Integer> targetIndex, int[] slotTabs)
        {
            this.orderedItems = orderedItems;
            this.targetIndex = targetIndex;
            this.slotTabs = slotTabs;
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

        public int[] getSlotTabs()
        {
            return Arrays.copyOf(slotTabs, slotTabs.length);
        }

        public static LayoutResult empty()
        {
            return new LayoutResult(List.of(), Map.of(), new int[0]);
        }
    }
}
