package com.bankorganizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.PluginPanel;

@Singleton
class BankOrganizerPanel extends PluginPanel
{
        private static final String HOW_TO_TEXT = "<html><b>How to tidy your bank:</b> "
                + "Create tabs named after the sections below and drag each listed item into its suggested tab. "
                + "Sub-headings break the tab into smaller piles so you can stage equipment or supplies together.</html>";

        private final JPanel content = new JPanel();
        private final JLabel placeholder = new JLabel("Open your bank to see organization suggestions.");
        private final JLabel howToLabel = new JLabel(HOW_TO_TEXT);

        BankOrganizerPanel()
        {
                setLayout(new BorderLayout());

                content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
                content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

                howToLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                howToLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

                final JScrollPane scrollPane = new JScrollPane(content);
                scrollPane.setBorder(BorderFactory.createEmptyBorder());
                scrollPane.getVerticalScrollBar().setUnitIncrement(16);
                add(scrollPane, BorderLayout.CENTER);

                showPlaceholder("Open your bank to see organization suggestions.");
        }

        void showPlaceholder(final String message)
        {
                SwingUtilities.invokeLater(() -> {
                        content.removeAll();
                        placeholder.setText(message);
                        placeholder.setAlignmentX(Component.LEFT_ALIGNMENT);
                        content.add(placeholder);
                        content.revalidate();
                        content.repaint();
                });
        }

        void updateSuggestions(final Map<String, List<BankItemSuggestion>> categorizedItems)
        {
                SwingUtilities.invokeLater(() -> {
                        content.removeAll();

                        if (categorizedItems.isEmpty())
                        {
                                placeholder.setText("No items found in your bank.");
                                placeholder.setAlignmentX(Component.LEFT_ALIGNMENT);
                                content.add(placeholder);
                        }
                        else
                        {
                                content.add(howToLabel);

                                for (Map.Entry<String, List<BankItemSuggestion>> entry : categorizedItems.entrySet())
                                {
                                        final JPanel categoryPanel = new JPanel();
                                        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
                                        categoryPanel.setBorder(BorderFactory.createTitledBorder(String.format(
                                                "Move into: %s tab",
                                                entry.getKey())));
                                        categoryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                                        if (entry.getValue().isEmpty())
                                        {
                                                final JLabel emptyLabel = new JLabel("<html><i>No items matched yet. Drop future finds here.</i></html>");
                                                emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                                                categoryPanel.add(emptyLabel);
                                                content.add(categoryPanel);
                                                continue;
                                        }

                                        final Map<String, List<BankItemSuggestion>> groupedBySubcategory = entry.getValue()
                                                .stream()
                                                .collect(Collectors.groupingBy(
                                                        suggestion -> suggestion.getSubcategoryName() == null ? "General" : suggestion.getSubcategoryName(),
                                                        LinkedHashMap::new,
                                                        Collectors.toList()));

                                        for (Map.Entry<String, List<BankItemSuggestion>> subEntry : groupedBySubcategory.entrySet())
                                        {
                                                final JLabel subcategoryLabel = new JLabel(String.format(
                                                        "<html><b>%s focus</b> &ndash; stage these together:</html>",
                                                        subEntry.getKey()));
                                                subcategoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                                                subcategoryLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
                                                categoryPanel.add(subcategoryLabel);

                                                for (BankItemSuggestion suggestion : subEntry.getValue())
                                                {
                                                        final String labelText = String.format(
                                                                "<html>&bull; %s <span style='color:gray'>(x%d)</span></html>",
                                                                suggestion.getItemName(),
                                                                suggestion.getQuantity());
                                                        final JLabel itemLabel = new JLabel(labelText);
                                                        itemLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                                                        itemLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
                                                        categoryPanel.add(itemLabel);
                                                }
                                        }

                                        content.add(categoryPanel);
                                }
                        }

                        content.revalidate();
                        content.repaint();
                });
        }
}
