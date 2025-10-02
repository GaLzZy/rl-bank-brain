package com.bankbrain;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
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
    private Consumer<Integer> stepSelectionListener = step -> {};
    private boolean suppressSelectionEvents;

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
        planList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        planList.addListSelectionListener(createSelectionListener());
        JScrollPane scrollPane = new JScrollPane(planList);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setRebuildAction(Runnable rebuildAction)
    {
        this.rebuildAction = rebuildAction == null ? () -> {} : rebuildAction;
    }

    public void setStepSelectionListener(Consumer<Integer> listener)
    {
        this.stepSelectionListener = listener == null ? step -> {} : listener;
    }

    public void updatePlan(ReorderPlan plan)
    {
        SwingUtilities.invokeLater(() -> {
            suppressSelectionEvents = true;
            try
            {
                planModel.clear();
                int stepIndex = 1;
                for (ReorderStep step : plan.getSteps())
                {
                    String entry = String.format("%d. %s #%d → %s #%d — %s x%d",
                        stepIndex++,
                        describeTab(step.getFromTab()),
                        step.getFromTabSlot(),
                        describeTab(step.getToTab()),
                        step.getToTabSlot(),
                        step.getItemName(),
                        step.getQuantity());
                    planModel.addElement(entry);
                }
                cyclesLabel.setText("Reorder cycles: " + plan.getCycleCount());
                if (planModel.isEmpty())
                {
                    planList.clearSelection();
                }
            }
            finally
            {
                suppressSelectionEvents = false;
            }
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

    public void setActiveStep(int step)
    {
        SwingUtilities.invokeLater(() -> {
            suppressSelectionEvents = true;
            try
            {
                if (step >= 0 && step < planModel.size())
                {
                    planList.setSelectedIndex(step);
                    planList.ensureIndexIsVisible(step);
                }
                else
                {
                    planList.clearSelection();
                }
            }
            finally
            {
                suppressSelectionEvents = false;
            }
        });
    }

    private ListSelectionListener createSelectionListener()
    {
        return event -> {
            if (event.getValueIsAdjusting())
            {
                return;
            }
            if (suppressSelectionEvents)
            {
                return;
            }
            int selected = planList.getSelectedIndex();
            stepSelectionListener.accept(selected);
        };
    }

    private String describeTab(int tab)
    {
        if (tab <= 0)
        {
            return "All Items";
        }
        if (tab == 1)
        {
            return "All Items (Tab 1)";
        }
        return "Tab " + tab;
    }
}
