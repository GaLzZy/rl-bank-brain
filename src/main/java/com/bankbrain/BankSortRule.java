package com.bankbrain;

import java.util.Locale;
import lombok.Value;

@Value
public class BankSortRule
{
    public enum RuleType
    {
        ALPHABETICAL,
        GE_PRICE,
        HIGH_ALCH,
        SLOT,
        RECENT_USE,
        WEIGHT,
        STACK_SIZE,
        CUSTOM
    }

    RuleType type;
    boolean descending;
    String parameter;

    public static BankSortRule parse(String token)
    {
        String trimmed = token.trim();
        if (trimmed.isEmpty())
        {
            return new BankSortRule(RuleType.ALPHABETICAL, false, null);
        }

        String parameter = null;
        int colonIdx = trimmed.indexOf(':');
        if (colonIdx > -1)
        {
            parameter = trimmed.substring(colonIdx + 1);
            trimmed = trimmed.substring(0, colonIdx);
        }

        boolean descending = false;
        String normalised = trimmed.toLowerCase(Locale.ROOT).replace('_', '-');
        if (normalised.endsWith("-desc"))
        {
            descending = true;
            normalised = normalised.substring(0, normalised.length() - 5);
        }
        else if (normalised.endsWith("-asc"))
        {
            normalised = normalised.substring(0, normalised.length() - 4);
        }

        RuleType type;
        switch (normalised)
        {
            case "alpha":
            case "alphabetical":
                type = RuleType.ALPHABETICAL;
                break;
            case "ge":
            case "price":
            case "ge-price":
                type = RuleType.GE_PRICE;
                break;
            case "ha":
            case "highalch":
            case "high-alch":
                type = RuleType.HIGH_ALCH;
                break;
            case "slot":
                type = RuleType.SLOT;
                break;
            case "recent":
            case "recent-use":
                type = RuleType.RECENT_USE;
                break;
            case "weight":
                type = RuleType.WEIGHT;
                break;
            case "stack":
            case "stack-size":
                type = RuleType.STACK_SIZE;
                break;
            default:
                type = RuleType.CUSTOM;
                parameter = parameter == null ? trimmed : parameter;
                break;
        }

        return new BankSortRule(type, descending, parameter);
    }
}
