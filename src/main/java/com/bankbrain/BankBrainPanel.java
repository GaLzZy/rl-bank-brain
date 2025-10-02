package com.bankbrain;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.PluginPanel;

@Singleton
public class BankBrainPanel extends PluginPanel
{
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    private final JLabel lastUpdated = new JLabel("No plan generated yet");
    private final JLabel summaryLabel = new JLabel("Bank items scanned: 0");
    private final JLabel cyclesLabel = new JLabel("Reorder cycles: 0");
    private final JButton rebuildButton = new JButton("Build Plan");
    private final DefaultListModel<String> planModel = new DefaultListModel<>();
    private final JList<String> planList = new JList<>(planModel);

    private Runnable rebuildAction = () -> {};

    @Inject
    public BankBrainPanel()
    {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(320, 500));

        JPanel header = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        header.add(new JLabel("Bank Brain"), c);

        c.gridy++;
        header.add(lastUpdated, c);

        c.gridy++;
        header.add(summaryLabel, c);

        c.gridy++;
        header.add(cyclesLabel, c);

        c.gridy++;
        rebuildButton.addActionListener(e -> rebuildAction.run());
        header.add(rebuildButton, c);

        add(header, BorderLayout.NORTH);

        planList.setVisibleRowCount(12);
        JScrollPane scrollPane = new JScrollPane(planList);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setRebuildAction(Runnable rebuildAction)
    {
        this.rebuildAction = rebuildAction == null ? () -> {} : rebuildAction;
    }

    public void updatePlan(ReorderPlan plan)
    {
        SwingUtilities.invokeLater(() -> {
            planModel.clear();
            int stepIndex = 1;
            for (ReorderStep step : plan.getSteps())
            {
                String entry = String.format("%d. Move %s x%d from slot %d to slot %d",
                    stepIndex++,
                    step.getItemName(),
                    step.getQuantity(),
                    step.getFromIndex() + 1,
                    step.getToIndex() + 1);
                planModel.addElement(entry);
            }
            cyclesLabel.setText("Reorder cycles: " + plan.getCycleCount());
        });
    }

    public void updateSnapshot(List<BankItem> items)
    {
        SwingUtilities.invokeLater(() -> summaryLabel.setText("Bank items scanned: " + items.size()));
    }

    public void updateTimestamp(Instant instant)
    {
        SwingUtilities.invokeLater(() -> lastUpdated.setText("Last plan: " + FORMATTER.format(instant)));
    }
}
