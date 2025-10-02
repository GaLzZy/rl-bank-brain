package com.bankbrain;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
    private final JComboBox<SlotTypeOption> typeFilter = new JComboBox<>();
    private final DefaultListModel<String> planModel = new DefaultListModel<>();
    private final JList<String> planList = new JList<>(planModel);
    private final List<Integer> visibleStepIndices = new ArrayList<>();

    private ReorderPlan currentPlan = new ReorderPlan(List.of(), 0);
    private int activeStepIndex = -1;

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

        c.gridy++;
        header.add(new JLabel("Item type filter"), c);

        c.gridy++;
        configureTypeFilter();
        header.add(typeFilter, c);

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
            currentPlan = plan == null ? new ReorderPlan(List.of(), 0) : plan;
            cyclesLabel.setText("Reorder cycles: " + currentPlan.getCycleCount());
            rebuildPlanListModel();
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
            activeStepIndex = step;
            suppressSelectionEvents = true;
            try
            {
                applyActiveSelection();
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
            if (selected < 0 || selected >= visibleStepIndices.size())
            {
                stepSelectionListener.accept(-1);
                return;
            }

            int actualStep = visibleStepIndices.get(selected);
            activeStepIndex = actualStep;
            stepSelectionListener.accept(actualStep);
        };
    }

    private void configureTypeFilter()
    {
        DefaultComboBoxModel<SlotTypeOption> model = new DefaultComboBoxModel<>();
        model.addElement(SlotTypeOption.all());
        for (SlotType type : SlotType.defaultOrder())
        {
            model.addElement(SlotTypeOption.of(type, formatSlotType(type)));
        }
        typeFilter.setModel(model);
        typeFilter.addActionListener(e -> SwingUtilities.invokeLater(this::rebuildPlanListModel));
    }

    private void rebuildPlanListModel()
    {
        suppressSelectionEvents = true;
        try
        {
            planModel.clear();
            visibleStepIndices.clear();

            List<ReorderStep> steps = currentPlan.getSteps();
            SlotType selectedType = getSelectedSlotType();
            int displayIndex = 1;
            for (int i = 0; i < steps.size(); i++)
            {
                ReorderStep step = steps.get(i);
                if (selectedType != null && step.getSlotType() != selectedType)
                {
                    continue;
                }

                visibleStepIndices.add(i);
                String entry = String.format("%d. %s #%d → %s #%d — %s x%d [%s]",
                    displayIndex++,
                    TabDescriptors.describe(step.getFromTab()),
                    step.getFromTabSlot(),
                    TabDescriptors.describe(step.getToTab()),
                    step.getToTabSlot(),
                    step.getItemName(),
                    step.getQuantity(),
                    formatSlotType(step.getSlotType()));
                planModel.addElement(entry);
            }

            if (planModel.isEmpty())
            {
                planList.clearSelection();
            }

            applyActiveSelection();
        }
        finally
        {
            suppressSelectionEvents = false;
        }
    }

    private void applyActiveSelection()
    {
        if (activeStepIndex < 0)
        {
            planList.clearSelection();
            return;
        }

        int viewIndex = visibleStepIndices.indexOf(activeStepIndex);
        if (viewIndex >= 0)
        {
            planList.setSelectedIndex(viewIndex);
            planList.ensureIndexIsVisible(viewIndex);
        }
        else
        {
            planList.clearSelection();
        }
    }

    private SlotType getSelectedSlotType()
    {
        SlotTypeOption option = (SlotTypeOption) typeFilter.getSelectedItem();
        return option != null ? option.slotType : null;
    }

    private String formatSlotType(SlotType slotType)
    {
        if (slotType == null)
        {
            return "Unknown";
        }

        String name = slotType.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        String[] parts = name.split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++)
        {
            if (parts[i].isEmpty())
            {
                continue;
            }
            if (i > 0)
            {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(parts[i].charAt(0)))
                .append(parts[i].substring(1));
        }
        return builder.toString();
    }

    private static final class SlotTypeOption
    {
        private final SlotType slotType;
        private final String label;

        private SlotTypeOption(SlotType slotType, String label)
        {
            this.slotType = slotType;
            this.label = label;
        }

        private static SlotTypeOption all()
        {
            return new SlotTypeOption(null, "All Item Types");
        }

        private static SlotTypeOption of(SlotType slotType, String label)
        {
            return new SlotTypeOption(slotType, label);
        }

        @Override
        public String toString()
        {
            return label;
        }
    }
}
