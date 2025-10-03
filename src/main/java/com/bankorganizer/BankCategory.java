package com.bankorganizer;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import net.runelite.api.EquipmentInventorySlot;

final class BankCategory
{
        private final String name;
        private final boolean requireEquipSlot;
        private final Set<EquipmentInventorySlot> slotWhitelist;
        private final List<Pattern> includePatterns;
        private final List<Pattern> excludePatterns;
        private final Set<Integer> idWhitelist;
        private final Set<Integer> idBlacklist;
        private final List<BankSubcategory> subcategories;

        BankCategory(
                final String name,
                final boolean requireEquipSlot,
                final Set<EquipmentInventorySlot> slotWhitelist,
                final List<Pattern> includePatterns,
                final List<Pattern> excludePatterns,
                final Set<Integer> idWhitelist,
                final Set<Integer> idBlacklist,
                final List<BankSubcategory> subcategories)
        {
                this.name = name;
                this.requireEquipSlot = requireEquipSlot;
                this.slotWhitelist = slotWhitelist;
                this.includePatterns = includePatterns;
                this.excludePatterns = excludePatterns;
                this.idWhitelist = idWhitelist;
                this.idBlacklist = idBlacklist;
                this.subcategories = subcategories;
        }

        String getName()
        {
                return name;
        }

        String getDisplayName()
        {
                return name.replace('_', ' ');
        }

        List<BankSubcategory> getSubcategories()
        {
                return subcategories;
        }

        boolean matches(final int itemId, final String itemName, final EquipmentInventorySlot slot)
        {
                if (idBlacklist.contains(itemId))
                {
                        return false;
                }

                if (requireEquipSlot && slot == null)
                {
                        return false;
                }

                if (!slotWhitelist.isEmpty() && (slot == null || !slotWhitelist.contains(slot)))
                {
                        return false;
                }

                if (!excludePatterns.isEmpty() && anyMatch(excludePatterns, itemName))
                {
                        return false;
                }

                final boolean whitelistMatch = idWhitelist.contains(itemId);
                final boolean includeMatch = includePatterns.isEmpty() || anyMatch(includePatterns, itemName);

                return whitelistMatch || includeMatch;
        }

        BankSubcategory matchSubcategory(final int itemId, final String itemName, final EquipmentInventorySlot slot)
        {
                for (BankSubcategory subcategory : subcategories)
                {
                        if (subcategory.matches(itemId, itemName, slot))
                        {
                                return subcategory;
                        }
                }

                return null;
        }

        private static boolean anyMatch(final List<Pattern> patterns, final String value)
        {
                for (Pattern pattern : patterns)
                {
                        if (pattern.matcher(value).find())
                        {
                                return true;
                        }
                }

                return false;
        }

        static Builder builder(final String name)
        {
                return new Builder(name);
        }

        static final class Builder
        {
                private final String name;
                private boolean requireEquipSlot;
                private Set<EquipmentInventorySlot> slotWhitelist = Collections.emptySet();
                private List<Pattern> includePatterns = Collections.emptyList();
                private List<Pattern> excludePatterns = Collections.emptyList();
                private Set<Integer> idWhitelist = Collections.emptySet();
                private Set<Integer> idBlacklist = Collections.emptySet();
                private List<BankSubcategory> subcategories = Collections.emptyList();

                private Builder(final String name)
                {
                        this.name = Objects.requireNonNull(name);
                }

                Builder requireEquipSlot(final boolean requireEquipSlot)
                {
                        this.requireEquipSlot = requireEquipSlot;
                        return this;
                }

                Builder slotWhitelist(final Set<EquipmentInventorySlot> slotWhitelist)
                {
                        this.slotWhitelist = slotWhitelist;
                        return this;
                }

                Builder includePatterns(final List<Pattern> includePatterns)
                {
                        this.includePatterns = includePatterns;
                        return this;
                }

                Builder excludePatterns(final List<Pattern> excludePatterns)
                {
                        this.excludePatterns = excludePatterns;
                        return this;
                }

                Builder idWhitelist(final Set<Integer> idWhitelist)
                {
                        this.idWhitelist = idWhitelist;
                        return this;
                }

                Builder idBlacklist(final Set<Integer> idBlacklist)
                {
                        this.idBlacklist = idBlacklist;
                        return this;
                }

                Builder subcategories(final List<BankSubcategory> subcategories)
                {
                        this.subcategories = subcategories;
                        return this;
                }

                BankCategory build()
                {
                        return new BankCategory(name, requireEquipSlot, slotWhitelist, includePatterns, excludePatterns, idWhitelist, idBlacklist, subcategories);
                }
        }
}
