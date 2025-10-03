package com.bankorganizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.PluginPanel;

@Singleton
class BankOrganizerPanel extends PluginPanel
{
        private static final String HOW_TO_TEXT = "<html><b>How to tidy your bank:</b> We'll highlight one item at a time in cyan. "
                + "Move the glowing item into the tab named below, then click the button to jump to the next highlight.</html>";

        private final JPanel content = new JPanel();
        private final JPanel viewport = new JPanel(new BorderLayout());
        private final JPanel suggestionContainer = new JPanel();
        private final JPanel listContainer = new JPanel();
        private final JLabel placeholder = new JLabel();
        private final JLabel howToLabel = new JLabel(HOW_TO_TEXT);
        private final JLabel itemLabel = new JLabel();
        private final JLabel targetLabel = new JLabel();
        private final JLabel subcategoryLabel = new JLabel();
        private final JLabel progressLabel = new JLabel();
        private final JLabel highlightTipLabel = new JLabel("<html><i>Tip: scroll your bank if you don't immediately see the cyan glow.</i></html>");
        private final JLabel completionLabel = new JLabel();
        private final JLabel listHeaderLabel = new JLabel("<html><b>All suggested placements:</b></html>");
        private final JLabel emptyListLabel = new JLabel("No items to organise right now.");
        private final JButton actionButton = new JButton("Mark moved & highlight next");

        private Listener listener;
        private Mode mode = Mode.PLACEHOLDER;

        BankOrganizerPanel()
        {
                setLayout(new BorderLayout());
                setMinimumSize(new Dimension(PluginPanel.PANEL_WIDTH, 200));
                setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH, 320));

                content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
                content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

                suggestionContainer.setLayout(new BoxLayout(suggestionContainer, BoxLayout.Y_AXIS));
                suggestionContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
                suggestionContainer.setOpaque(false);

                listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
                listContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
                listContainer.setOpaque(false);

                viewport.setOpaque(false);
                viewport.add(content, BorderLayout.NORTH);

                howToLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                howToLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

                placeholder.setAlignmentX(Component.LEFT_ALIGNMENT);
                itemLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                targetLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                subcategoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                progressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                highlightTipLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                completionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

                actionButton.setAlignmentX(Component.LEFT_ALIGNMENT);
                actionButton.addActionListener(event -> {
                        if (listener == null)
                        {
                                return;
                        }

                        if (mode == Mode.ACTIVE)
                        {
                                listener.onAdvanceRequested();
                        }
                        else if (mode == Mode.COMPLETE)
                        {
                                listener.onRestartRequested();
                        }
                });

                final JScrollPane scrollPane = new JScrollPane(viewport);
                scrollPane.setBorder(BorderFactory.createEmptyBorder());
                scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.getVerticalScrollBar().setUnitIncrement(16);
                scrollPane.setMinimumSize(new Dimension(PluginPanel.PANEL_WIDTH, 200));
                add(scrollPane, BorderLayout.CENTER);

                listHeaderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                listHeaderLabel.setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));
                emptyListLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                emptyListLabel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

                content.add(suggestionContainer);
                content.add(Box.createVerticalStrut(12));
                content.add(listHeaderLabel);
                content.add(listContainer);

                showPlaceholder("Open your bank to begin reorganizing.");
                updateSuggestionList(new ArrayList<>(), -1);
        }

        void setListener(final Listener listener)
        {
                this.listener = listener;
        }

        void showPlaceholder(final String message)
        {
                SwingUtilities.invokeLater(() -> {
                        mode = Mode.PLACEHOLDER;
                        suggestionContainer.removeAll();
                        placeholder.setText(message);
                        suggestionContainer.add(placeholder);
                        actionButton.setEnabled(false);
                        actionButton.setText("Mark moved & highlight next");
                        suggestionContainer.revalidate();
                        suggestionContainer.repaint();
                });
        }

        void showSuggestion(final BankItemSuggestion suggestion, final int index, final int total)
        {
                SwingUtilities.invokeLater(() -> {
                        mode = Mode.ACTIVE;
                        suggestionContainer.removeAll();

                        suggestionContainer.add(howToLabel);
                        suggestionContainer.add(Box.createVerticalStrut(8));

                        itemLabel.setText(String.format(
                                "<html><b>Highlighted item:</b> %s <span style='color:gray'>(x%d)</span></html>",
                                suggestion.getItemName(),
                                suggestion.getQuantity()));
                        suggestionContainer.add(itemLabel);

                        targetLabel.setText(String.format("<html><b>Suggested tab:</b> %s</html>", suggestion.getCategoryName()));
                        targetLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
                        suggestionContainer.add(targetLabel);

                        if (suggestion.getSubcategoryName() != null)
                        {
                                subcategoryLabel.setText(String.format("<html><b>Focus:</b> %s</html>", suggestion.getSubcategoryName()));
                                subcategoryLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
                                suggestionContainer.add(subcategoryLabel);
                        }

                        progressLabel.setText(String.format("<html><i>Item %d of %d</i></html>", index + 1, total));
                        progressLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
                        suggestionContainer.add(progressLabel);

                        suggestionContainer.add(Box.createVerticalStrut(4));
                        suggestionContainer.add(highlightTipLabel);

                        actionButton.setEnabled(true);
                        actionButton.setText("Mark moved & highlight next");
                        suggestionContainer.add(Box.createVerticalStrut(12));
                        suggestionContainer.add(actionButton);

                        suggestionContainer.revalidate();
                        suggestionContainer.repaint();
                });
        }

        void showCompletion(final int reviewedCount, final boolean loopEnabled)
        {
                SwingUtilities.invokeLater(() -> {
                        mode = loopEnabled ? Mode.ACTIVE : Mode.COMPLETE;
                        suggestionContainer.removeAll();

                        suggestionContainer.add(howToLabel);
                        suggestionContainer.add(Box.createVerticalStrut(8));

                        final String completionText = loopEnabled
                                ? "<html><b>Highlights will keep cycling.</b> Use the button if you'd like to restart from the first item.</html>"
                                : String.format("<html><b>All items reviewed!</b> You walked through %d suggestions. Click below to start over.</html>", reviewedCount);
                        completionLabel.setText(completionText);
                        suggestionContainer.add(completionLabel);

                        if (loopEnabled)
                        {
                                progressLabel.setText(String.format("<html><i>Loop mode is on &ndash; you're back to item 1 of %d.</i></html>", reviewedCount));
                                progressLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
                                suggestionContainer.add(progressLabel);
                        }

                        actionButton.setEnabled(true);
                        actionButton.setText(loopEnabled ? "Highlight first item" : "Restart review");
                        suggestionContainer.add(Box.createVerticalStrut(12));
                        suggestionContainer.add(actionButton);

                        suggestionContainer.revalidate();
                        suggestionContainer.repaint();
                });
        }

        void updateSuggestionList(final List<BankItemSuggestion> suggestions, final int activeIndex)
        {
                SwingUtilities.invokeLater(() -> {
                        listContainer.removeAll();

                        if (suggestions == null || suggestions.isEmpty())
                        {
                                listHeaderLabel.setVisible(false);
                                listContainer.add(emptyListLabel);
                                listContainer.revalidate();
                                listContainer.repaint();
                                return;
                        }

                        listHeaderLabel.setVisible(true);

                        final Map<String, List<SuggestionEntry>> grouped = new LinkedHashMap<>();
                        for (int i = 0; i < suggestions.size(); i++)
                        {
                                final BankItemSuggestion suggestion = suggestions.get(i);
                                grouped.computeIfAbsent(suggestion.getCategoryName(), ignored -> new ArrayList<>())
                                        .add(new SuggestionEntry(suggestion, i));
                        }

                        boolean firstCategory = true;

                        for (Map.Entry<String, List<SuggestionEntry>> entry : grouped.entrySet())
                        {
                                if (!firstCategory)
                                {
                                        listContainer.add(Box.createVerticalStrut(8));
                                }

                                firstCategory = false;

                                final JLabel categoryLabel = new JLabel(String.format("<html><b>%s</b></html>", entry.getKey()));
                                categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                                categoryLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
                                listContainer.add(categoryLabel);

                                for (SuggestionEntry suggestionEntry : entry.getValue())
                                {
                                        final BankItemSuggestion suggestion = suggestionEntry.suggestion;
                                        final boolean isActive = suggestionEntry.index == activeIndex;
                                        final StringBuilder builder = new StringBuilder("<html>&bull; ");

                                        if (isActive)
                                        {
                                                builder.append("<span style='color:#2563eb;font-weight:bold;'>");
                                        }

                                        builder.append(suggestion.getItemName());

                                        if (isActive)
                                        {
                                                builder.append("</span>");
                                        }

                                        builder.append(String.format(" <span style='color:gray'>(x%d)</span>", suggestion.getQuantity()));

                                        if (suggestion.getSubcategoryName() != null)
                                        {
                                                builder.append(String.format(" <span style='color:#6b7280'>[%s]</span>", suggestion.getSubcategoryName()));
                                        }

                                        builder.append("</html>");

                                        final JLabel itemEntryLabel = new JLabel(builder.toString());
                                        itemEntryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                                        itemEntryLabel.setBorder(BorderFactory.createEmptyBorder(0, 12, 4, 0));
                                        listContainer.add(itemEntryLabel);
                                }
                        }

                        listContainer.revalidate();
                        listContainer.repaint();
                });
        }

        interface Listener
        {
                void onAdvanceRequested();

                void onRestartRequested();
        }

        private enum Mode
        {
                PLACEHOLDER,
                ACTIVE,
                COMPLETE
        }

        private static final class SuggestionEntry
        {
                private final BankItemSuggestion suggestion;
                private final int index;

                private SuggestionEntry(final BankItemSuggestion suggestion, final int index)
                {
                        this.suggestion = suggestion;
                        this.index = index;
                }
        }
}
