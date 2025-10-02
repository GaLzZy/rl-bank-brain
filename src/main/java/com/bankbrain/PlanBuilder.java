package com.bankbrain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;

@Singleton
public class PlanBuilder
{
    public ReorderPlan buildPlan(List<BankItem> items, Map<Integer, Integer> targetIndex, int containerSize, int[] slotTabs, int[] tabSlotPositions)
    {
        int size = Math.max(containerSize, 0);
        int[] permutation = new int[size];
        Arrays.fill(permutation, -1);
        for (Map.Entry<Integer, Integer> entry : targetIndex.entrySet())
        {
            int key = entry.getKey();
            if (key >= 0 && key < size)
            {
                permutation[key] = entry.getValue();
            }
        }
        for (int i = 0; i < size; i++)
        {
            if (permutation[i] == -1)
            {
                permutation[i] = i;
            }
        }

        boolean[] seen = new boolean[size];
        List<ReorderStep> steps = new ArrayList<>();
        int cycles = 0;

        Map<Integer, BankItem> itemsByIndex = new HashMap<>();
        for (BankItem item : items)
        {
            itemsByIndex.put(item.getIndex(), item);
        }

        for (int start = 0; start < size; start++)
        {
            if (seen[start] || permutation[start] == start)
            {
                continue;
            }

            List<Integer> cycle = new ArrayList<>();
            int current = start;
            while (!seen[current])
            {
                seen[current] = true;
                cycle.add(current);
                current = permutation[current];
            }

            if (cycle.size() > 1)
            {
                cycles++;
                steps.addAll(resolveCycle(cycle, permutation, itemsByIndex, slotTabs, tabSlotPositions));
            }
        }

        return new ReorderPlan(steps, cycles);
    }

    private List<ReorderStep> resolveCycle(List<Integer> cycle, int[] permutation, Map<Integer, BankItem> itemsByIndex, int[] slotTabs, int[] tabSlotPositions)
    {
        List<ReorderStep> steps = new ArrayList<>();
        for (int i = cycle.size() - 1; i >= 0; i--)
        {
            int fromIndex = cycle.get(i);
            int toIndex = permutation[fromIndex];
            BankItem item = itemsByIndex.get(fromIndex);
            if (item == null)
            {
                continue;
            }

            steps.add(new ReorderStep(
                fromIndex,
                toIndex,
                tabFor(slotTabs, fromIndex),
                tabFor(slotTabs, toIndex),
                tabSlotFor(tabSlotPositions, fromIndex),
                tabSlotFor(tabSlotPositions, toIndex),
                item.getItemId(),
                item.getName(),
                item.getQuantity(),
                item.getSlotType(),
                item.getCategory()
            ));
        }
        return steps;
    }

    private int tabFor(int[] slotTabs, int index)
    {
        if (index < 0 || index >= slotTabs.length)
        {
            return 0;
        }
        return slotTabs[index];
    }

    private int tabSlotFor(int[] tabSlotPositions, int index)
    {
        if (index < 0 || index >= tabSlotPositions.length)
        {
            return 0;
        }
        return tabSlotPositions[index];
    }
}
