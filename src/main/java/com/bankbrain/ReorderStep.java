package com.bankbrain;

import lombok.Value;

@Value
public class ReorderStep
{
    int fromIndex;
    int toIndex;
    int fromTab;
    int toTab;
    int fromTabSlot;
    int toTabSlot;
    int itemId;
    String itemName;
    int quantity;
}
