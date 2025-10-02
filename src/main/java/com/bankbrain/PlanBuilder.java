package com.bankbrain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;

@Singleton
public class PlanBuilder
{
    public ReorderPlan buildPlan(List<BankItem> items, Map<Integer, Integer> targetIndex)
    {
        int size = items.size();
        int[] permutation = new int[size];
        Arrays.fill(permutation, -1);
        for (Map.Entry<Integer, Integer> entry : targetIndex.entrySet())
        {
            if (entry.getKey() < size)
            {
                permutation[entry.getKey()] = entry.getValue();
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
                steps.addAll(resolveCycle(cycle, permutation, items));
            }
        }

        return new ReorderPlan(steps, cycles);
    }

    private List<ReorderStep> resolveCycle(List<Integer> cycle, int[] permutation, List<BankItem> items)
    {
        List<ReorderStep> steps = new ArrayList<>();
        for (int i = cycle.size() - 1; i >= 0; i--)
        {
            int fromIndex = cycle.get(i);
            int toIndex = permutation[fromIndex];
            BankItem item = items.get(fromIndex);
            steps.add(new ReorderStep(fromIndex, toIndex, item.getItemId(), item.getName(), item.getQuantity()));
        }
        return steps;
    }
}
