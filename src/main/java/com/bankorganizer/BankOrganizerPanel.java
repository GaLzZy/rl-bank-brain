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
        private final JPanel content = new JPanel();
        private final JLabel placeholder = new JLabel("Open your bank to see organization suggestions.");

        BankOrganizerPanel()
        {
                setLayout(new BorderLayout());

                content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
                final JScrollPane scrollPane = new JScrollPane(content);
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
                                for (Map.Entry<String, List<BankItemSuggestion>> entry : categorizedItems.entrySet())
                                {
                                        final JPanel categoryPanel = new JPanel();
                                        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
                                        categoryPanel.setBorder(BorderFactory.createTitledBorder(entry.getKey()));
                                        categoryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                                        final Map<String, List<BankItemSuggestion>> groupedBySubcategory = entry.getValue()
                                                .stream()
                                                .collect(Collectors.groupingBy(
                                                        suggestion -> suggestion.getSubcategoryName() == null ? "General" : suggestion.getSubcategoryName(),
                                                        LinkedHashMap::new,
                                                        Collectors.toList()));

                                        for (Map.Entry<String, List<BankItemSuggestion>> subEntry : groupedBySubcategory.entrySet())
                                        {
                                                final JLabel subcategoryLabel = new JLabel(subEntry.getKey());
                                                subcategoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                                                categoryPanel.add(subcategoryLabel);

                                                for (BankItemSuggestion suggestion : subEntry.getValue())
                                                {
                                                        final String labelText = String.format("- %s (x%d)", suggestion.getItemName(), suggestion.getQuantity());
                                                        final JLabel itemLabel = new JLabel(labelText);
                                                        itemLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
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
