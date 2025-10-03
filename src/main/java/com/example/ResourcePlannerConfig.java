package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(ResourcePlannerConfig.GROUP)
public interface ResourcePlannerConfig extends Config
{
    String GROUP = "resourceplanner";

    @ConfigItem(
            keyName = "resourceFilters",
            name = "Resource filters",
            description = "Comma separated list of itemId:target:threshold entries used to track supplies."
    )
    default String resourceFilters()
    {
        return "314:500:100, 7944:200:50";
    }

    @ConfigItem(
            keyName = "preferredActivities",
            name = "Preferred activities",
            description = "Comma separated list of activity categories you would like prioritised."
    )
    default String preferredActivities()
    {
        return "Skilling, Combat, Questing";
    }

    @ConfigItem(
            keyName = "monitoredSkills",
            name = "Skill goals",
            description = "Comma separated list of SKILL:targetLevel entries for progress tracking."
    )
    default String monitoredSkills()
    {
        return "FISHING:70, COOKING:70";
    }

    @ConfigItem(
            keyName = "notifyInChat",
            name = "Chat notifications",
            description = "If enabled the plugin will send recommendations to the chat box."
    )
    default boolean notifyInChat()
    {
        return true;
    }
}
