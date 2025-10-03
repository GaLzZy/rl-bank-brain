package com.bankorganizer;

final class BankItemSuggestion
{
        private final int itemId;
        private final String itemName;
        private final int quantity;
        private final String categoryName;
        private final String subcategoryName;
        private final int slot;

        BankItemSuggestion(
                final int itemId,
                final String itemName,
                final int quantity,
                final String categoryName,
                final String subcategoryName,
                final int slot)
        {
                this.itemId = itemId;
                this.itemName = itemName;
                this.quantity = quantity;
                this.categoryName = categoryName;
                this.subcategoryName = subcategoryName;
                this.slot = slot;
        }

        int getItemId()
        {
                return itemId;
        }

        String getItemName()
        {
                return itemName;
        }

        int getQuantity()
        {
                return quantity;
        }

        String getCategoryName()
        {
                return categoryName;
        }

        String getSubcategoryName()
        {
                return subcategoryName;
        }

        int getSlot()
        {
                return slot;
        }
}
