package com.bankbrain;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum SlotType
{
    HELM,
    CAPE,
    AMULET,
    WEAPON,
    BODY,
    SHIELD,
    LEGS,
    GLOVES,
    BOOTS,
    RING,
    AMMO,
    QUIVER,
    TOOL,
    JEWELLERY,
    CONSUMABLE,
    MATERIAL,
    PET,
    MISC;

    private static final List<SlotType> DEFAULT_ORDER = Collections.unmodifiableList(Arrays.asList(
        HELM,
        CAPE,
        AMULET,
        WEAPON,
        BODY,
        SHIELD,
        LEGS,
        GLOVES,
        BOOTS,
        RING,
        AMMO,
        QUIVER,
        TOOL,
        JEWELLERY,
        CONSUMABLE,
        MATERIAL,
        PET,
        MISC
    ));

    public static List<SlotType> defaultOrder()
    {
        return DEFAULT_ORDER;
    }

    public int sortIndex()
    {
        final int idx = DEFAULT_ORDER.indexOf(this);
        return idx >= 0 ? idx : DEFAULT_ORDER.size();
    }
}
