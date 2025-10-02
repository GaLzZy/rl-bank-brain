package com.bankbrain;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(BankBrainConfig.GROUP)
public interface BankBrainConfig extends Config
{
    String GROUP = "bankbrain";

    @ConfigItem(
        keyName = "autoPlan",
        name = "Auto rebuild plan",
        description = "Rebuild the suggested layout whenever the bank contents change."
    )
    default boolean autoPlan()
    {
        return true;
    }

    @ConfigItem(
        keyName = "defaultRules",
        name = "Default sort rules",
        description = "Comma separated sort rules used to generate the base layout (e.g. slot,ge-desc,alpha)."
    )
    default String defaultRules()
    {
        return "slot,ge-desc,alpha";
    }

    @ConfigItem(
        keyName = "ghostOverlay",
        name = "Show ghost overlay",
        description = "Display target slot overlays in the bank interface."
    )
    default boolean ghostOverlay()
    {
        return true;
    }
}
