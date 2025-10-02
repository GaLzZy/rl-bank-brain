package com.bankbrain;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
public class BankGhostOverlay extends Overlay
{
    private final Client client;
    private final BankBrainService service;
    private final BankBrainConfig config;

    @Inject
    public BankGhostOverlay(Client client, BankBrainService service, BankBrainConfig config)
    {
        this.client = client;
        this.service = service;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.ghostOverlay())
        {
            return null;
        }

        Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
        if (bankContainer == null || bankContainer.isHidden())
        {
            return null;
        }

        service.getLayoutResult().ifPresent(result -> {
            graphics.setColor(new Color(0, 255, 255, 120));
            graphics.drawString("Bank Brain target layout ready (" + result.getOrderedItems().size() + " items)",
                bankContainer.getCanvasLocation().getX() + 20,
                bankContainer.getCanvasLocation().getY() + 20);
        });

        return null;
    }
}
