package com.bankbrain;

import java.util.Arrays;
import java.util.Locale;

public enum BankCategory
{
    COMBAT_GEAR("Combat Loadouts"),
    CONSUMABLES("Prayer/Slayer/Consumables"),
    RUNES_AND_TELEPORTS("Runes & Teleports"),
    GATHERING("Skilling – Gathering"),
    PRODUCTION("Skilling – Production"),
    CLUE("Clue & Treasure"),
    COSMETIC("Fashion & Cosmetics"),
    UTILITY("Tools & Utilities"),
    STAGING("Junk/Staging"),
    FARMING("Farming"),
    PETS("Pets"),
    QUEST("Quest Items"),
    UNASSIGNED("Unassigned");

    private final String displayName;

    BankCategory(String displayName)
    {
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public static BankCategory fromToken(String token)
    {
        final String normalised = token.toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        return Arrays.stream(values())
            .filter(cat -> cat.name().equals(normalised))
            .findFirst()
            .orElse(UNASSIGNED);
    }
}
