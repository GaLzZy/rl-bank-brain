package com.bankbrain;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

@Singleton
public class BankGhostOverlay extends WidgetItemOverlay
{
    private static final Color SOURCE_COLOR = new Color(255, 85, 85, 180);
    private static final Color TARGET_COLOR = new Color(85, 255, 170, 180);
    private static final Color TEXT_COLOR = new Color(0, 255, 255, 200);

    private final Client client;
    private final BankBrainService service;
    private final BankBrainConfig config;

    @Inject
    public BankGhostOverlay(Client client, BankBrainService service, BankBrainConfig config)
    {
        this.client = client;
        this.service = service;
        this.config = config;

        showOnBank();
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem item)
    {
        if (!config.ghostOverlay())
        {
            return;
        }

        ReorderPlan plan = service.getReorderPlan();
        if (plan.isEmpty())
        {
            return;
        }

        int activeStep = service.getActiveStep();
        if (activeStep < 0 || activeStep >= plan.getSteps().size())
        {
            return;
        }

        ReorderStep step = plan.getSteps().get(activeStep);
        int index = item.getWidget().getIndex();
        Rectangle bounds = item.getCanvasBounds();
        if (bounds == null)
        {
            return;
        }

        if (index == step.getFromIndex())
        {
            highlightSlot(graphics, bounds, SOURCE_COLOR);
        }
        else if (index == step.getToIndex())
        {
            highlightSlot(graphics, bounds, TARGET_COLOR);
        }
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        Dimension dimension = super.render(graphics);

        if (!config.ghostOverlay())
        {
            return dimension;
        }

        Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
        if (bankContainer == null || bankContainer.isHidden())
        {
            return dimension;
        }

        ReorderPlan plan = service.getReorderPlan();
        int activeStep = service.getActiveStep();
        String message;
        if (plan.isEmpty())
        {
            message = "Bank Brain target layout ready";
        }
        else if (activeStep >= 0 && activeStep < plan.getSteps().size())
        {
            ReorderStep step = plan.getSteps().get(activeStep);
            message = String.format("Step %d/%d: %s x%d (%s #%d → %s #%d)",
                activeStep + 1,
                plan.getSteps().size(),
                step.getItemName(),
                step.getQuantity(),
                describeTab(step.getFromTab()),
                step.getFromTabSlot(),
                describeTab(step.getToTab()),
                step.getToTabSlot());
        }
        else
        {
            message = "Plan ready — select a step";
        }

        graphics.setColor(TEXT_COLOR);
        graphics.drawString(
            message,
            bankContainer.getCanvasLocation().getX() + 20,
            bankContainer.getCanvasLocation().getY() + 20);

        return dimension;
    }

    private void highlightSlot(Graphics2D graphics, Rectangle bounds, Color color)
    {
        Color previousColor = graphics.getColor();
        java.awt.Stroke previousStroke = graphics.getStroke();
        graphics.setColor(color);
        graphics.setStroke(new BasicStroke(2f));
        graphics.draw(bounds);
        graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60));
        graphics.fill(bounds);
        graphics.setColor(previousColor);
        graphics.setStroke(previousStroke);
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
