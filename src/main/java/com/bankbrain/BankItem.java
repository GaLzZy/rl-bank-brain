package com.bankbrain;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;

@Getter
public class BankItem
{
    private final int itemId;
    private final int quantity;
    private final int index;
    private final String name;
    private final boolean members;
    private final BankCategory category;
    private final SlotType slotType;
    private final Set<String> tags;
    private final long gePrice;
    private final int highAlchValue;
    private final double weight;
    private final boolean stackable;
    private final Instant lastWithdrawn;
    private final int tabIndex;

    public BankItem(
        int itemId,
        int quantity,
        int index,
        String name,
        boolean members,
        BankCategory category,
        SlotType slotType,
        Set<String> tags,
        long gePrice,
        int highAlchValue,
        double weight,
        boolean stackable,
        Instant lastWithdrawn,
        int tabIndex)
    {
        this.itemId = itemId;
        this.quantity = quantity;
        this.index = index;
        this.name = name;
        this.members = members;
        this.category = Objects.requireNonNullElse(category, BankCategory.UNASSIGNED);
        this.slotType = Objects.requireNonNullElse(slotType, SlotType.MISC);
        this.tags = tags == null ? new LinkedHashSet<>() : new LinkedHashSet<>(tags);
        this.gePrice = gePrice;
        this.highAlchValue = highAlchValue;
        this.weight = weight;
        this.stackable = stackable;
        this.lastWithdrawn = lastWithdrawn;
        this.tabIndex = tabIndex;
    }

    public Set<String> getTags()
    {
        return Collections.unmodifiableSet(tags);
    }
}
