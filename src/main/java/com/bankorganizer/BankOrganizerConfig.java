package com.bankorganizer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("bankorganizer")
public interface BankOrganizerConfig extends Config
{
        @ConfigItem(
                keyName = "loopHighlights",
                name = "Loop highlights",
                description = "Keep cycling through the item queue instead of stopping at the end."
        )
        default boolean loopHighlights()
        {
                return false;
        }
}
