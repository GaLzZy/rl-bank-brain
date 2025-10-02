package com.bankbrain;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ItemComposition;

@Singleton
public class BankClassifier
{
    private static final Pattern POTION_PATTERN = Pattern.compile("\\(\\d\\)| potion", Pattern.CASE_INSENSITIVE);
    private static final Pattern TELEPORT_PATTERN = Pattern.compile("teleport|tablet|xeric|digsite|necklace", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOOL_PATTERN = Pattern.compile("axe|pickaxe|hammer|harpoon|net|machete|pouch|needle|saw", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLUE_PATTERN = Pattern.compile("clue|casket|scroll|god book|ornament|trims?", Pattern.CASE_INSENSITIVE);
    private static final Pattern GATHERING_PATTERN = Pattern.compile("ore|log|fish|raw|grimy|herb|bar|plank|leather|bolt|feather|fossil", Pattern.CASE_INSENSITIVE);
    private static final Pattern FARM_PATTERN = Pattern.compile("seed|sapling|compost|farm|herb patch", Pattern.CASE_INSENSITIVE);
    private static final Pattern FOOD_PATTERN = Pattern.compile("cake|pie|brew|shark|lobster|karambwan|pizza|wine", Pattern.CASE_INSENSITIVE);
    private static final Pattern CONSUMABLE_PATTERN = Pattern.compile("potion|brew|restore|prayer|overload|food|fish|cooked", Pattern.CASE_INSENSITIVE);

    @Inject
    public BankClassifier()
    {
    }

    public BankCategory categoryFor(ItemComposition composition)
    {
        String name = composition.getName().toLowerCase(Locale.ROOT);
        if (name.contains("bond"))
        {
            return BankCategory.UTILITY;
        }
        if (TELEPORT_PATTERN.matcher(name).find())
        {
            return BankCategory.RUNES_AND_TELEPORTS;
        }
        if (name.contains("rune") || name.contains("staff of") || name.contains("graceful"))
        {
            return BankCategory.RUNES_AND_TELEPORTS;
        }
        if (POTION_PATTERN.matcher(name).find() || FOOD_PATTERN.matcher(name).find() || CONSUMABLE_PATTERN.matcher(name).find())
        {
            return BankCategory.CONSUMABLES;
        }
        if (TOOL_PATTERN.matcher(name).find())
        {
            return BankCategory.UTILITY;
        }
        if (CLUE_PATTERN.matcher(name).find())
        {
            return BankCategory.CLUE;
        }
        if (GATHERING_PATTERN.matcher(name).find())
        {
            return BankCategory.GATHERING;
        }
        if (FARM_PATTERN.matcher(name).find())
        {
            return BankCategory.FARMING;
        }
        if (composition.getName().toLowerCase(Locale.ROOT).contains("seed"))
        {
            return BankCategory.FARMING;
        }
        if (looksLikeCombatGear(name))
        {
            return BankCategory.COMBAT_GEAR;
        }
        if (name.contains("pet"))
        {
            return BankCategory.PETS;
        }
        return BankCategory.UNASSIGNED;
    }

    public SlotType slotTypeFor(ItemComposition composition)
    {
        String name = composition.getName().toLowerCase(Locale.ROOT);
        if (name.contains("helm") || name.contains("hat") || name.contains("mask"))
        {
            return SlotType.HELM;
        }
        if (name.contains("cape"))
        {
            return SlotType.CAPE;
        }
        if (name.contains("amulet") || name.contains("necklace") || name.contains("pendant"))
        {
            return SlotType.AMULET;
        }
        if (name.contains("bow") || name.contains("sword") || name.contains("staff") || name.contains("wand") || name.contains("axe") || name.contains("maul"))
        {
            return SlotType.WEAPON;
        }
        if (name.contains("body") || name.contains("plate") || name.contains("chest") || name.contains("robe top"))
        {
            return SlotType.BODY;
        }
        if (name.contains("shield") || name.contains("defender") || name.contains("book"))
        {
            return SlotType.SHIELD;
        }
        if (name.contains("legs") || name.contains("skirt") || name.contains("chaps") || name.contains("robe bottom"))
        {
            return SlotType.LEGS;
        }
        if (name.contains("glove") || name.contains("gauntlet"))
        {
            return SlotType.GLOVES;
        }
        if (name.contains("boot") || name.contains("shoe"))
        {
            return SlotType.BOOTS;
        }
        if (name.contains("ring"))
        {
            return SlotType.RING;
        }
        if (name.contains("arrow") || name.contains("bolt") || name.contains("dart") || name.contains("knife") || name.contains("ammo"))
        {
            return SlotType.AMMO;
        }
        if (name.contains("pouch") || name.contains("spade") || name.contains("pickaxe") || name.contains("harpoon") || name.contains("needle"))
        {
            return SlotType.TOOL;
        }
        if (POTION_PATTERN.matcher(name).find() || FOOD_PATTERN.matcher(name).find())
        {
            return SlotType.CONSUMABLE;
        }
        if (GATHERING_PATTERN.matcher(name).find() || FARM_PATTERN.matcher(name).find())
        {
            return SlotType.MATERIAL;
        }
        return SlotType.MISC;
    }

    public Set<String> tagsFor(ItemComposition composition, BankCategory category)
    {
        Set<String> tags = new LinkedHashSet<>();
        tags.add(category.name());
        String name = composition.getName().toLowerCase(Locale.ROOT);
        if (name.contains("bird house"))
        {
            tags.add("BIRDHOUSE");
        }
        if (name.contains("vorkath"))
        {
            tags.add("PVM:VORKATH");
        }
        if (name.contains("barrows"))
        {
            tags.add("PVM:BARROWS");
        }
        if (name.contains("clue"))
        {
            tags.add("CLUE");
        }
        if (name.contains("seed"))
        {
            tags.add("FARM");
        }
        if (name.contains("potion"))
        {
            tags.add("POTIONS");
        }
        return tags;
    }

    private boolean looksLikeCombatGear(String name)
    {
        return name.contains("helm") ||
            name.contains("helmet") ||
            name.contains("plate") ||
            name.contains("legs") ||
            name.contains("skirt") ||
            name.contains("sword") ||
            name.contains("staff") ||
            name.contains("wand") ||
            name.contains("bow") ||
            name.contains("shield") ||
            name.contains("gauntlet") ||
            name.contains("glove") ||
            name.contains("boots") ||
            name.contains("body") ||
            name.contains("robe") ||
            name.contains("amulet") ||
            name.contains("necklace") ||
            name.contains("cape") ||
            name.contains("ring");
    }
}
