package com.bankorganizer;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import net.runelite.api.EquipmentInventorySlot;

final class BankSubcategory
{
        private final String name;
        private final Set<EquipmentInventorySlot> slotWhitelist;
        private final List<Pattern> includePatterns;

        BankSubcategory(
                final String name,
                final Set<EquipmentInventorySlot> slotWhitelist,
                final List<Pattern> includePatterns)
        {
                this.name = name;
                this.slotWhitelist = slotWhitelist;
                this.includePatterns = includePatterns;
        }

        String getName()
        {
                return name;
        }

        String getDisplayName()
        {
                return name.replace('_', ' ');
        }

        boolean matches(final int itemId, final String itemName, final EquipmentInventorySlot slot)
        {
                if (!slotWhitelist.isEmpty() && (slot == null || !slotWhitelist.contains(slot)))
                {
                        return false;
                }

                return includePatterns.isEmpty() || anyMatch(includePatterns, itemName);
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
                private Set<EquipmentInventorySlot> slotWhitelist = Collections.emptySet();
                private List<Pattern> includePatterns = Collections.emptyList();

                private Builder(final String name)
                {
                        this.name = Objects.requireNonNull(name);
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

                BankSubcategory build()
                {
                        return new BankSubcategory(name, slotWhitelist, includePatterns);
                }
        }
}
