package com.bankbrain;

import java.util.Collections;
import java.util.List;
import lombok.Getter;

@Getter
public class ReorderPlan
{
    private final List<ReorderStep> steps;
    private final int cycleCount;

    public ReorderPlan(List<ReorderStep> steps, int cycleCount)
    {
        this.steps = List.copyOf(steps);
        this.cycleCount = cycleCount;
    }

    public List<ReorderStep> getSteps()
    {
        return Collections.unmodifiableList(steps);
    }

    public boolean isEmpty()
    {
        return steps.isEmpty();
    }
}
