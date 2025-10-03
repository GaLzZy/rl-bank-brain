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
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import net.runelite.http.api.item.ItemEquipmentStats;
import net.runelite.http.api.item.ItemStats;

@Slf4j
@PluginDescriptor(
        name = "Bank Organizer",
        description = "Highlights one bank item at a time and tells you which tab to move it into.",
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
        private OverlayManager overlayManager;

        @Inject
        private BankOrganizerPanel panel;

        @Inject
        private BankOrganizerOverlay overlay;

        @Inject
        private BankOrganizerConfig config;

        private final List<BankItemSuggestion> suggestionQueue = new ArrayList<>();
        private volatile BankItemSuggestion activeSuggestion;
        private int activeIndex = -1;

        private NavigationButton navigationButton;

        @Override
        protected void startUp()
        {
                panel.setListener(new BankOrganizerPanel.Listener()
                {
                        @Override
                        public void onAdvanceRequested()
                        {
                                advanceSuggestion();
                        }

                        @Override
                        public void onRestartRequested()
                        {
                                restartReview();
                        }
                });

                navigationButton = NavigationButton.builder()
                        .tooltip("Bank Organizer")
                        .priority(5)
                        .icon(NAVIGATION_ICON)
                        .panel(panel)
                        .build();

                clientToolbar.addNavigation(navigationButton);
                overlayManager.add(overlay);

                panel.showPlaceholder("Open your bank to begin reorganizing.");
                refreshSuggestions();
        }

        @Override
        protected void shutDown()
        {
                clientToolbar.removeNavigation(navigationButton);
                overlayManager.remove(overlay);
                navigationButton = null;
                panel.showPlaceholder("Bank Organizer is inactive.");
                clearSuggestions();
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

        BankItemSuggestion getActiveSuggestion()
        {
                return activeSuggestion;
        }

        private void refreshSuggestions()
        {
                clientThread.invokeLater(() -> {
                        final ItemContainer bank = client.getItemContainer(InventoryID.BANK);
                        if (bank == null)
                        {
                                clearSuggestions();
                                panel.showPlaceholder("Open your bank to begin reorganizing.");
                                return;
                        }

                        final Item[] items = bank.getItems();
                        if (!hasRealItems(items))
                        {
                                clearSuggestions();
                                panel.showPlaceholder("No items found in your bank.");
                                return;
                        }

                        final List<BankItemSuggestion> orderedSuggestions = buildOrderedSuggestions(items);

                        final BankItemSuggestion suggestionToDisplay;
                        final int total;

                        synchronized (suggestionQueue)
                        {
                                suggestionQueue.clear();
                                suggestionQueue.addAll(orderedSuggestions);
                                total = suggestionQueue.size();

                                if (total == 0)
                                {
                                        activeIndex = -1;
                                        activeSuggestion = null;
                                        suggestionToDisplay = null;
                                }
                                else
                                {
                                        activeIndex = 0;
                                        activeSuggestion = suggestionQueue.get(0);
                                        suggestionToDisplay = activeSuggestion;
                                }
                        }

                        updateSuggestionListDisplay();

                        if (total == 0 || suggestionToDisplay == null)
                        {
                                panel.showPlaceholder("No items found in your bank.");
                        }
                        else
                        {
                                panel.showSuggestion(suggestionToDisplay, 0, total);
                        }
                });
        }

        private void advanceSuggestion()
        {
                final BankItemSuggestion suggestionToDisplay;
                final int indexToDisplay;
                final int total;
                final boolean completed;

                synchronized (suggestionQueue)
                {
                        total = suggestionQueue.size();
                        if (total == 0)
                        {
                                activeIndex = -1;
                                activeSuggestion = null;
                                suggestionToDisplay = null;
                                indexToDisplay = -1;
                                completed = true;
                        }
                        else
                        {
                                final int nextIndex = activeIndex + 1;
                                if (nextIndex >= total)
                                {
                                        if (config.loopHighlights())
                                        {
                                                activeIndex = 0;
                                                activeSuggestion = suggestionQueue.get(0);
                                                suggestionToDisplay = activeSuggestion;
                                                indexToDisplay = activeIndex;
                                                completed = false;
                                        }
                                        else
                                        {
                                                activeIndex = total;
                                                activeSuggestion = null;
                                                suggestionToDisplay = null;
                                                indexToDisplay = -1;
                                                completed = true;
                                        }
                                }
                                else
                                {
                                        activeIndex = nextIndex;
                                        activeSuggestion = suggestionQueue.get(activeIndex);
                                        suggestionToDisplay = activeSuggestion;
                                        indexToDisplay = activeIndex;
                                        completed = false;
                                }
                        }
                }

                if (total == 0)
                {
                        panel.showPlaceholder("No items found in your bank.");
                        updateSuggestionListDisplay();
                        return;
                }

                if (completed)
                {
                        panel.showCompletion(total, config.loopHighlights());
                        updateSuggestionListDisplay();
                        return;
                }

                panel.showSuggestion(suggestionToDisplay, indexToDisplay, total);
                updateSuggestionListDisplay();
        }

        private void restartReview()
        {
                final BankItemSuggestion suggestionToDisplay;
                final int total;

                synchronized (suggestionQueue)
                {
                        total = suggestionQueue.size();
                        if (total == 0)
                        {
                                activeIndex = -1;
                                activeSuggestion = null;
                                suggestionToDisplay = null;
                        }
                        else
                        {
                                activeIndex = 0;
                                activeSuggestion = suggestionQueue.get(0);
                                suggestionToDisplay = activeSuggestion;
                        }
                }

                if (total == 0 || suggestionToDisplay == null)
                {
                        panel.showPlaceholder("No items found in your bank.");
                        updateSuggestionListDisplay();
                        return;
                }

                panel.showSuggestion(suggestionToDisplay, 0, total);
                updateSuggestionListDisplay();
        }

        private void clearSuggestions()
        {
                synchronized (suggestionQueue)
                {
                        suggestionQueue.clear();
                        activeIndex = -1;
                        activeSuggestion = null;
                }

                updateSuggestionListDisplay();
        }

        private boolean hasRealItems(final Item[] items)
        {
                if (items == null)
                {
                        return false;
                }

                for (Item item : items)
                {
                        if (item != null && item.getId() > 0)
                        {
                                return true;
                        }
                }

                return false;
        }

        private List<BankItemSuggestion> buildOrderedSuggestions(final Item[] items)
        {
                final Map<String, List<BankItemSuggestion>> categorized = new LinkedHashMap<>();
                for (BankCategory category : CATEGORIES)
                {
                        categorized.put(category.getDisplayName(), new ArrayList<>());
                }

                final List<BankItemSuggestion> uncategorised = new ArrayList<>();

                if (items == null)
                {
                        return new ArrayList<>();
                }

                for (int slot = 0; slot < items.length; slot++)
                {
                        final Item item = items[slot];
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
                        final EquipmentInventorySlot slotType = resolveEquipmentSlot(item.getId());

                        final BankCategory category = findCategory(item.getId(), cleanName, slotType);
                        final String categoryName = category != null ? category.getDisplayName() : "Uncategorised";
                        final BankSubcategory subcategory = category != null ? category.matchSubcategory(item.getId(), cleanName, slotType) : null;

                        final BankItemSuggestion suggestion = new BankItemSuggestion(
                                item.getId(),
                                cleanName,
                                item.getQuantity(),
                                categoryName,
                                subcategory != null ? subcategory.getDisplayName() : null,
                                slot);

                        if (category != null)
                        {
                                categorized.computeIfAbsent(categoryName, ignored -> new ArrayList<>()).add(suggestion);
                        }
                        else
                        {
                                uncategorised.add(suggestion);
                        }
                }

                final List<BankItemSuggestion> ordered = new ArrayList<>();
                for (BankCategory category : CATEGORIES)
                {
                        final List<BankItemSuggestion> matches = categorized.get(category.getDisplayName());
                        if (matches != null)
                        {
                                ordered.addAll(matches);
                        }
                }

                ordered.addAll(uncategorised);
                return ordered;
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

        private void updateSuggestionListDisplay()
        {
                final List<BankItemSuggestion> snapshot = new ArrayList<>();
                final int index;

                synchronized (suggestionQueue)
                {
                        snapshot.addAll(suggestionQueue);
                        index = activeIndex;
                }

                panel.updateSuggestionList(snapshot, index);
        }
}
