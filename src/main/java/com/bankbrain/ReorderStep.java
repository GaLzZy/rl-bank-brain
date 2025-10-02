package com.bankbrain;

import lombok.Value;

@Value
public class ReorderStep
{
    int fromIndex;
    int toIndex;
    int itemId;
    String itemName;
    int quantity;
}
