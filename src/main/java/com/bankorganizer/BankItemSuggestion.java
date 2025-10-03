package com.bankorganizer;

final class BankItemSuggestion
{
        private final int itemId;
        private final String itemName;
        private final int quantity;
        private final String categoryName;
        private final String subcategoryName;

        BankItemSuggestion(final int itemId, final String itemName, final int quantity, final String categoryName, final String subcategoryName)
        {
                this.itemId = itemId;
                this.itemName = itemName;
                this.quantity = quantity;
                this.categoryName = categoryName;
                this.subcategoryName = subcategoryName;
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
}
