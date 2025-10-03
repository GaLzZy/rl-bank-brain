package com.bankorganizer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("bankorganizer")
public interface BankOrganizerConfig extends Config
{
        @ConfigItem(
                keyName = "showEmptyCategories",
                name = "Show empty categories",
                description = "Display all categories even when they have no matching bank items."
        )
        default boolean showEmptyCategories()
        {
                return false;
        }
}
