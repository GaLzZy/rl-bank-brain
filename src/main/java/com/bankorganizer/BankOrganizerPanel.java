package com.bankorganizer;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.PluginPanel;

@Singleton
class BankOrganizerPanel extends PluginPanel
{
        private static final String HOW_TO_TEXT = "<html><b>How to tidy your bank:</b> We'll highlight one item at a time in cyan. "
                + "Move the glowing item into the tab named below, then click the button to jump to the next highlight.</html>";

        private final JPanel content = new JPanel();
        private final JLabel placeholder = new JLabel();
        private final JLabel howToLabel = new JLabel(HOW_TO_TEXT);
        private final JLabel itemLabel = new JLabel();
        private final JLabel targetLabel = new JLabel();
        private final JLabel subcategoryLabel = new JLabel();
        private final JLabel progressLabel = new JLabel();
        private final JLabel highlightTipLabel = new JLabel("<html><i>Tip: scroll your bank if you don't immediately see the cyan glow.</i></html>");
        private final JLabel completionLabel = new JLabel();
        private final JButton actionButton = new JButton("Mark moved & highlight next");

        private Listener listener;
        private Mode mode = Mode.PLACEHOLDER;

        BankOrganizerPanel()
        {
                setLayout(new BorderLayout());

                content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
                content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

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

                final JScrollPane scrollPane = new JScrollPane(content);
                scrollPane.setBorder(BorderFactory.createEmptyBorder());
                scrollPane.getVerticalScrollBar().setUnitIncrement(16);
                add(scrollPane, BorderLayout.CENTER);

                showPlaceholder("Open your bank to begin reorganizing.");
        }

        void setListener(final Listener listener)
        {
                this.listener = listener;
        }

        void showPlaceholder(final String message)
        {
                SwingUtilities.invokeLater(() -> {
                        mode = Mode.PLACEHOLDER;
                        content.removeAll();
                        placeholder.setText(message);
                        content.add(placeholder);
                        actionButton.setEnabled(false);
                        actionButton.setText("Mark moved & highlight next");
                        content.revalidate();
                        content.repaint();
                });
        }

        void showSuggestion(final BankItemSuggestion suggestion, final int index, final int total)
        {
                SwingUtilities.invokeLater(() -> {
                        mode = Mode.ACTIVE;
                        content.removeAll();

                        content.add(howToLabel);
                        content.add(Box.createVerticalStrut(8));

                        itemLabel.setText(String.format(
                                "<html><b>Highlighted item:</b> %s <span style='color:gray'>(x%d)</span></html>",
                                suggestion.getItemName(),
                                suggestion.getQuantity()));
                        content.add(itemLabel);

                        targetLabel.setText(String.format("<html><b>Suggested tab:</b> %s</html>", suggestion.getCategoryName()));
                        targetLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
                        content.add(targetLabel);

                        if (suggestion.getSubcategoryName() != null)
                        {
                                subcategoryLabel.setText(String.format("<html><b>Focus:</b> %s</html>", suggestion.getSubcategoryName()));
                                subcategoryLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
                                content.add(subcategoryLabel);
                        }

                        progressLabel.setText(String.format("<html><i>Item %d of %d</i></html>", index + 1, total));
                        progressLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
                        content.add(progressLabel);

                        content.add(Box.createVerticalStrut(4));
                        content.add(highlightTipLabel);

                        actionButton.setEnabled(true);
                        actionButton.setText("Mark moved & highlight next");
                        content.add(Box.createVerticalStrut(12));
                        content.add(actionButton);

                        content.revalidate();
                        content.repaint();
                });
        }

        void showCompletion(final int reviewedCount, final boolean loopEnabled)
        {
                SwingUtilities.invokeLater(() -> {
                        mode = loopEnabled ? Mode.ACTIVE : Mode.COMPLETE;
                        content.removeAll();

                        content.add(howToLabel);
                        content.add(Box.createVerticalStrut(8));

                        final String completionText = loopEnabled
                                ? "<html><b>Highlights will keep cycling.</b> Use the button if you'd like to restart from the first item.</html>"
                                : String.format("<html><b>All items reviewed!</b> You walked through %d suggestions. Click below to start over.</html>", reviewedCount);
                        completionLabel.setText(completionText);
                        content.add(completionLabel);

                        if (loopEnabled)
                        {
                                progressLabel.setText(String.format("<html><i>Loop mode is on &ndash; you're back to item 1 of %d.</i></html>", reviewedCount));
                                progressLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
                                content.add(progressLabel);
                        }

                        actionButton.setEnabled(true);
                        actionButton.setText(loopEnabled ? "Highlight first item" : "Restart review");
                        content.add(Box.createVerticalStrut(12));
                        content.add(actionButton);

                        content.revalidate();
                        content.repaint();
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
}
