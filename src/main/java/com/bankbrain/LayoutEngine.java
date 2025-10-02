package com.bankbrain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Singleton;

@Singleton
public class LayoutEngine
{
    public LayoutResult buildLayout(List<BankItem> items, java.util.Comparator<BankItem> comparator)
    {
        List<BankItem> ordered = new ArrayList<>(items);
        ordered.sort(comparator);

        Map<Integer, Integer> targetIndex = new HashMap<>();
        for (int target = 0; target < ordered.size(); target++)
        {
            BankItem item = ordered.get(target);
            targetIndex.put(item.getIndex(), target);
        }

        return new LayoutResult(ordered, targetIndex);
    }

    public static class LayoutResult
    {
        private final List<BankItem> orderedItems;
        private final Map<Integer, Integer> targetIndex;

        LayoutResult(List<BankItem> orderedItems, Map<Integer, Integer> targetIndex)
        {
            this.orderedItems = orderedItems;
            this.targetIndex = targetIndex;
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

        public static LayoutResult empty()
        {
            return new LayoutResult(List.of(), Map.of());
        }
    }
}
