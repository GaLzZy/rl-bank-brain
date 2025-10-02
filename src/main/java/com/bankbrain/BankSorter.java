package com.bankbrain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Singleton;

@Singleton
public class BankSorter
{
    private static final Comparator<BankItem> NATURAL = Comparator.comparingInt(o -> 0);

    public List<BankSortRule> parseRules(String ruleConfig)
    {
        if (ruleConfig == null || ruleConfig.trim().isEmpty())
        {
            return List.of(new BankSortRule(BankSortRule.RuleType.SLOT, false, null),
                new BankSortRule(BankSortRule.RuleType.GE_PRICE, true, null),
                new BankSortRule(BankSortRule.RuleType.ALPHABETICAL, false, null));
        }

        String[] tokens = ruleConfig.split(",");
        List<BankSortRule> rules = new ArrayList<>();
        for (String token : tokens)
        {
            rules.add(BankSortRule.parse(token));
        }
        return rules;
    }

    public Comparator<BankItem> buildComparator(List<BankSortRule> rules)
    {
        Comparator<BankItem> comparator = NATURAL;
        for (BankSortRule rule : rules)
        {
            comparator = comparator.thenComparing(toComparator(rule));
        }
        return comparator;
    }

    private Comparator<BankItem> toComparator(BankSortRule rule)
    {
        Comparator<BankItem> comparator;
        switch (rule.getType())
        {
            case GE_PRICE:
                comparator = Comparator.comparingLong(BankItem::getGePrice);
                break;
            case HIGH_ALCH:
                comparator = Comparator.comparingInt(BankItem::getHighAlchValue);
                break;
            case SLOT:
                comparator = Comparator.comparingInt(item -> item.getSlotType().sortIndex());
                break;
            case RECENT_USE:
                comparator = Comparator.comparing(item -> {
                    Instant last = item.getLastWithdrawn();
                    return last == null ? Instant.EPOCH : last;
                });
                break;
            case WEIGHT:
                comparator = Comparator.comparingDouble(BankItem::getWeight);
                break;
            case STACK_SIZE:
                comparator = Comparator.comparingInt(BankItem::getQuantity);
                break;
            case CUSTOM:
                comparator = customComparator(rule.getParameter());
                break;
            case ALPHABETICAL:
            default:
                comparator = Comparator.comparing(item -> item.getName().toLowerCase(Locale.ROOT));
                break;
        }

        if (rule.isDescending())
        {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    private Comparator<BankItem> customComparator(String parameter)
    {
        if (parameter == null || parameter.isEmpty())
        {
            return Comparator.comparing(BankItem::getName, String.CASE_INSENSITIVE_ORDER);
        }

        String[] orderedTokens = parameter.split("\\|");
        List<String> priority = Arrays.stream(orderedTokens)
            .map(token -> token.trim().toLowerCase(Locale.ROOT))
            .filter(token -> !token.isEmpty())
            .collect(Collectors.toList());

        if (priority.isEmpty())
        {
            return Comparator.comparing(BankItem::getName, String.CASE_INSENSITIVE_ORDER);
        }

        return Comparator.<BankItem>comparingInt(item -> {
            for (int i = 0; i < priority.size(); i++)
            {
                if (matchesPriority(item, priority.get(i)))
                {
                    return i;
                }
            }
            return priority.size();
        }).thenComparing(BankItem::getName, String.CASE_INSENSITIVE_ORDER);
    }

    private boolean matchesPriority(BankItem item, String token)
    {
        if (token.startsWith("tag:"))
        {
            String tagName = token.substring(4);
            return item.getTags().stream().map(String::toLowerCase).anyMatch(tag -> tag.equals(tagName));
        }
        if (token.startsWith("category:"))
        {
            String category = token.substring(9);
            return Objects.equals(item.getCategory().name().toLowerCase(Locale.ROOT), category);
        }
        if (token.startsWith("slot:"))
        {
            String slot = token.substring(5);
            return Objects.equals(item.getSlotType().name().toLowerCase(Locale.ROOT), slot);
        }
        return item.getName().toLowerCase(Locale.ROOT).contains(token);
    }
}
