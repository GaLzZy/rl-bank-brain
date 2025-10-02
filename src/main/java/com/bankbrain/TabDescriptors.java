package com.bankbrain;

public final class TabDescriptors
{
    private static final String[] TAB_LABELS = {
        "All Items",
        "Weapons",
        "Armour",
        "Accessories",
        "Runes & Teleports",
        "Tools & Utilities",
        "Consumables",
        "Skilling Materials",
        "Pets",
        "Miscellaneous"
    };

    private TabDescriptors()
    {
    }

    public static String describe(int tab)
    {
        if (tab <= 0)
        {
            return TAB_LABELS[0];
        }
        if (tab < TAB_LABELS.length)
        {
            return "Tab " + tab + " — " + TAB_LABELS[tab];
        }
        return "Tab " + tab;
    }

    public static String typeName(int tab)
    {
        if (tab >= 0 && tab < TAB_LABELS.length)
        {
            return TAB_LABELS[tab];
        }
        return "Tab " + tab;
    }
}
