package com.bankorganizer;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.Text;
import net.runelite.http.api.item.ItemEquipmentStats;
import net.runelite.http.api.item.ItemStats;

@Slf4j
@PluginDescriptor(
        name = "Bank Organizer",
        description = "Suggests where each of your banked items should go using curated categories.",
        tags = {"bank", "organise", "inventory"}
)
public class BankOrganizerPlugin extends Plugin
{
        private static final List<BankCategory> CATEGORIES = BankCategoryDefinitions.CATEGORIES;
        private static final BufferedImage NAVIGATION_ICON = createNavigationIcon();

        @Inject
        private Client client;

        @Inject
        private ClientThread clientThread;

        @Inject
        private ItemManager itemManager;

        @Inject
        private ClientToolbar clientToolbar;

        @Inject
        private BankOrganizerPanel panel;

        @Inject
        private BankOrganizerConfig config;

        private NavigationButton navigationButton;

        @Override
        protected void startUp()
        {
                navigationButton = NavigationButton.builder()
                        .tooltip("Bank Organizer")
                        .priority(5)
                        .icon(NAVIGATION_ICON)
                        .panel(panel)
                        .build();

                clientToolbar.addNavigation(navigationButton);
                panel.showPlaceholder("Open your bank to see organization suggestions.");
                refreshSuggestions();
        }

        @Override
        protected void shutDown()
        {
                clientToolbar.removeNavigation(navigationButton);
                navigationButton = null;
                panel.showPlaceholder("Bank Organizer is inactive.");
        }

        @Subscribe
        public void onItemContainerChanged(final ItemContainerChanged event)
        {
                if (event.getContainerId() != InventoryID.BANK.getId())
                {
                        return;
                }

                refreshSuggestions();
        }

        private void refreshSuggestions()
        {
                clientThread.invokeLater(() -> {
                        final ItemContainer bank = client.getItemContainer(InventoryID.BANK);
                        if (bank == null)
                        {
                                panel.showPlaceholder("Open your bank to see organization suggestions.");
                                return;
                        }

                        final Map<String, List<BankItemSuggestion>> categorized = categorizeItems(bank.getItems(), config.showEmptyCategories());
                        panel.updateSuggestions(categorized);
                });
        }

        private Map<String, List<BankItemSuggestion>> categorizeItems(final Item[] items, final boolean includeEmptyCategories)
        {
                final Map<String, List<BankItemSuggestion>> categorized = new LinkedHashMap<>();
                for (BankCategory category : CATEGORIES)
                {
                        categorized.put(category.getDisplayName(), new ArrayList<>());
                }

                if (items == null)
                {
                        return categorized;
                }

                for (Item item : items)
                {
                        if (item == null || item.getId() <= 0)
                        {
                                continue;
                        }

                        final ItemComposition composition = itemManager.getItemComposition(item.getId());
                        if (composition == null)
                        {
                                continue;
                        }

                        final String cleanName = Text.removeTags(composition.getName()).trim();
                        final EquipmentInventorySlot slot = resolveEquipmentSlot(item.getId());

                        final BankCategory category = findCategory(item.getId(), cleanName, slot);
                        final String categoryName = category != null ? category.getDisplayName() : "Uncategorised";
                        final BankSubcategory subcategory = category != null ? category.matchSubcategory(item.getId(), cleanName, slot) : null;

                        final BankItemSuggestion suggestion = new BankItemSuggestion(
                                item.getId(),
                                cleanName,
                                item.getQuantity(),
                                categoryName,
                                subcategory != null ? subcategory.getDisplayName() : null);

                        categorized.computeIfAbsent(categoryName, key -> new ArrayList<>()).add(suggestion);
                }

                if (!includeEmptyCategories)
                {
                        categorized.entrySet().removeIf(entry -> entry.getValue().isEmpty());
                }

                return categorized;
        }

        private BankCategory findCategory(final int itemId, final String itemName, final EquipmentInventorySlot slot)
        {
                for (BankCategory category : CATEGORIES)
                {
                        if (category.matches(itemId, itemName, slot))
                        {
                                return category;
                        }
                }

                return null;
        }

        private EquipmentInventorySlot resolveEquipmentSlot(final int itemId)
        {
                final ItemStats stats = itemManager.getItemStats(itemId, false);
                if (stats == null)
                {
                        return null;
                }

                final ItemEquipmentStats equipment = stats.getEquipment();
                if (equipment == null)
                {
                        return null;
                }

                final int slot = equipment.getSlot();
                if (slot < 0 || slot >= EquipmentInventorySlot.values().length)
                {
                        return null;
                }

                return EquipmentInventorySlot.values()[slot];
        }

        private static BufferedImage createNavigationIcon()
        {
                final int size = 32;
                final BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                final Graphics2D graphics = image.createGraphics();

                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setColor(new Color(37, 99, 235));
                graphics.fillRoundRect(0, 0, size, size, 8, 8);

                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                graphics.setColor(Color.WHITE);
                graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));

                final String text = "BO";
                final FontMetrics metrics = graphics.getFontMetrics();
                final int textWidth = metrics.stringWidth(text);
                final int textHeight = metrics.getAscent();
                final int x = (size - textWidth) / 2;
                final int y = (size - metrics.getHeight()) / 2 + textHeight;

                graphics.drawString(text, x, y);
                graphics.dispose();

                return image;
        }

        @Provides
        BankOrganizerConfig provideConfig(final ConfigManager configManager)
        {
                return configManager.getConfig(BankOrganizerConfig.class);
        }
}
