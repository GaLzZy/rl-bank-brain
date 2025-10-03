package com.bankorganizer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
class BankOrganizerOverlay extends Overlay
{
        private static final Color HIGHLIGHT_FILL = new Color(64, 255, 235, 80);
        private static final Color HIGHLIGHT_BORDER = new Color(64, 255, 235, 180);

        private final Client client;
        private final BankOrganizerPlugin plugin;

        @Inject
        private BankOrganizerOverlay(final Client client, final BankOrganizerPlugin plugin)
        {
                this.client = client;
                this.plugin = plugin;

                setPosition(OverlayPosition.DYNAMIC);
                setLayer(OverlayLayer.ABOVE_WIDGETS);
        }

        @Override
        public Dimension render(final Graphics2D graphics)
        {
                final BankItemSuggestion suggestion = plugin.getActiveSuggestion();
                if (suggestion == null)
                {
                        return null;
                }

                final Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
                if (bankContainer == null)
                {
                        return null;
                }

                final Widget[] children = bankContainer.getDynamicChildren();
                final int slot = suggestion.getSlot();
                if (children == null || slot < 0 || slot >= children.length)
                {
                        return null;
                }

                final Widget itemWidget = children[slot];
                if (itemWidget == null || itemWidget.isHidden())
                {
                        return null;
                }

                final Rectangle bounds = itemWidget.getBounds();
                if (bounds == null || bounds.isEmpty())
                {
                        return null;
                }

                graphics.setColor(HIGHLIGHT_FILL);
                graphics.fill(bounds);

                graphics.setColor(HIGHLIGHT_BORDER);
                graphics.draw(bounds);

                return null;
        }
}
