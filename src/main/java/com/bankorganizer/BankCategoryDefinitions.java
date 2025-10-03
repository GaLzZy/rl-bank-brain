package com.bankorganizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import net.runelite.api.EquipmentInventorySlot;

final class BankCategoryDefinitions
{
        static final List<BankCategory> CATEGORIES = buildCategories();

        private BankCategoryDefinitions()
        {
        }

        private static List<BankCategory> buildCategories()
        {
                final List<BankCategory> categories = new ArrayList<>();

                categories.add(BankCategory.builder("COMBAT_GEAR")
                        .requireEquipSlot(true)
                        .subcategories(List.of(
                                BankSubcategory.builder("MELEE")
                                        .includePatterns(patterns("(sword|scimitar|dagger|mace|hasta|spear|halberd|whip|claws|scythe|flail|maul|warhammer|katana|blade)"))
                                        .build(),
                                BankSubcategory.builder("RANGED")
                                        .includePatterns(patterns("(bow|crossbow|xbow|knife|dart|javelin|chinchompa|blowpipe|ballista|c'bow)"))
                                        .build(),
                                BankSubcategory.builder("MAGIC")
                                        .includePatterns(patterns("(staff|wand|tome|book of|codex|battlestaff)"))
                                        .build(),
                                BankSubcategory.builder("ARMOUR_HELM")
                                        .slotWhitelist(slots("HEAD"))
                                        .build(),
                                BankSubcategory.builder("ARMOUR_BODY")
                                        .slotWhitelist(slots("BODY"))
                                        .build(),
                                BankSubcategory.builder("ARMOUR_LEGS")
                                        .slotWhitelist(slots("LEGS"))
                                        .build(),
                                BankSubcategory.builder("SHIELD")
                                        .slotWhitelist(slots("SHIELD"))
                                        .build(),
                                BankSubcategory.builder("GLOVES")
                                        .slotWhitelist(slots("GLOVES"))
                                        .build(),
                                BankSubcategory.builder("BOOTS")
                                        .slotWhitelist(slots("BOOTS"))
                                        .build(),
                                BankSubcategory.builder("CAPE")
                                        .slotWhitelist(slots("CAPE"))
                                        .build(),
                                BankSubcategory.builder("AMULET")
                                        .slotWhitelist(slots("AMULET"))
                                        .build(),
                                BankSubcategory.builder("RING")
                                        .slotWhitelist(slots("RING"))
                                        .build(),
                                BankSubcategory.builder("AMMO")
                                        .slotWhitelist(slots("AMMO"))
                                        .includePatterns(patterns("(bolt|arrow|javelin|throwing|ammo|brutal)"))
                                        .build()))
                        .build());

                categories.add(BankCategory.builder("MAGIC_TELEPORTS")
                        .includePatterns(List.of(
                                compile("(^|\\s)(air|water|earth|fire|mind|body|chaos|death|blood|soul|nature|law|cosmic|astral|mud|lava|steam|smoke|mist|dust) rune$"),
                                compile("(teleport|tele tab|tablet)$"),
                                compile("(rune pouch|rune pouch (l)|rune pouch (t))"),
                                compile("(games necklace|ring of dueling|amulet of glory|skills necklace|combat bracelet|necklace of passage|ring of wealth|burning amulet|digsite pendant|xeric's talisman|duke's hourglass).*(\\(\\d+\\))"),
                                compile("(royal seed pod|ecto-token|chronicle)")))
                        .build());

                categories.add(BankCategory.builder("POTIONS")
                        .includePatterns(List.of(
                                compile("(\\(4\\)|\\(3\\)|\\(2\\)|\\(1\\))$"),
                                compile("(potion|brew|restore|overload|antidote|antipoison|anti-?venom|stamina|energy|strength|attack|defence|ranging|magic|prayer|imbue|divine|saradomin brew|super|extreme)"),
                                compile("(unf)$")))
                        .excludePatterns(patterns("(potion set|decorative)"))
                        .build());

                categories.add(BankCategory.builder("FOOD")
                        .includePatterns(List.of(
                                compile("^(shark|anglerfish|manta ray|karambwan|sea turtle|monkfish|lobster|swordfish|tuna|bass|trout|salmon|pike|cave eel|shrimps|anchovies|herring|mackerel|cod|sardine|sardines|bananas?)$"),
                                compile("(cake|pie|pizza|stew|curry|baked potato|potato with|wine|chocolate|guthix rest|tea|beer|rum|brandy|cocktail)"),
                                compile("(purple sweet|summer pie|karambwanji|egg potato|pineapple pizza|anchovy pizza|bread)")))
                        .build());

                categories.add(BankCategory.builder("SKILLING_RAW")
                        .includePatterns(List.of(
                                compile("(log|logs|oak|willow|teak|maple|mahogany|yew|magic|redwood) logs?$"),
                                compile("^(ore|tin ore|copper ore|iron ore|coal|mithril ore|adamantite ore|runite ore|gold ore)$"),
                                compile("^(uncut|raw|grimy) "),
                                compile("(raw (shrimps|anchovies|karambwan|tuna|lobster|swordfish|shark|anglerfish|manta|monkfish|salmon|trout))$"),
                                compile("(clean herb|grimy .+|herb)$"),
                                compile(" seeds?$"),
                                compile("(snape grass|eyes? of newt|unicorn horn dust|white berries|red spiders' eggs|limpwurt|blue dragon scale|mort myre fungus|bird nest)"),
                                compile("(rune essence|pure essence|soft clay|silver ore|sand|soda ash|flax)")))
                        .build());

                categories.add(BankCategory.builder("SKILLING_PROCESSED")
                        .includePatterns(List.of(
                                compile("(bars?|planks?|leather|hard leather|studs|bowstring|thread|molten glass|glassblowing pipe|clockwork)"),
                                compile("(unfinished|unstrung|u\\)|headless arrows?)$"),
                                compile("(cooked|baked)"),
                                compile("(jewellery|necklace|ring|amulet) (u)$"),
                                compile("(potions? (\\(4\\)|\\(3\\)|\\(2\\)|\\(1\\)))"),
                                compile("(cannonball|coal bag|ammo mould)")))
                        .build());

                categories.add(BankCategory.builder("CLUE_FASHION")
                        .includePatterns(List.of(
                                compile("(^clue scroll|reward casket|scroll box|torn page|god book|tattered tome|ancient page)"),
                                compile("(elegant|gilded|gold-trimmed|trimmed|saradomin|zamorak|guthix|ancient|bandos|armadyl).*"),
                                compile("(costume|festival|event|holiday|graceful|fancy boots|lederhosen|mime|zombie|capes? of accomplishment|skillcape)")))
                        .build());

                categories.add(BankCategory.builder("TOOLS_MISC")
                        .includePatterns(List.of(
                                compile("(axe|pickaxe|fishing rod|harpoon|net|lobster pot|knife|chisel|tinderbox|hammer|spade|rope|saw|machete|noose wand|butterfly net|hunting trap|bird snare|box trap|imp-in-a-box)"),
                                compile("(pestle and mortar|needle|thread|glassblowing pipe|whetstone)"),
                                compile("(small pouch|medium pouch|large pouch|giant pouch|colossal pouch|rune pouch|herb sack|seed box|coal bag|gem bag|looting bag|bolt pouch|fish barrel|open fish barrel|gricoller's can|watering can)"),
                                compile("(keys?|key ring|key token|totem|sigil|talisman|sceptre|pharaoh's sceptre|briar|chronicle)"),
                                compile("(graceful hood|graceful top|graceful legs|graceful gloves|graceful boots|graceful cape)")))
                        .build());

                categories.add(BankCategory.builder("STAGING_JUNK").build());

                return Collections.unmodifiableList(categories);
        }

        private static List<Pattern> patterns(final String... expressions)
        {
                if (expressions.length == 0)
                {
                        return Collections.emptyList();
                }

                final List<Pattern> patterns = new ArrayList<>(expressions.length);
                for (String expression : expressions)
                {
                        patterns.add(compile(expression));
                }
                return patterns;
        }

        private static Set<EquipmentInventorySlot> slots(final String... slots)
        {
                if (slots.length == 0)
                {
                        return Collections.emptySet();
                }

                final EquipmentInventorySlot[] values = new EquipmentInventorySlot[slots.length];
                for (int i = 0; i < slots.length; i++)
                {
                        values[i] = EquipmentInventorySlot.valueOf(slots[i].toUpperCase(Locale.ROOT));
                }

                return Set.of(values);
        }

        private static Pattern compile(final String regex)
        {
                return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        }
}
