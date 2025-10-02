package com.bankbrain;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.InventoryID;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
    name = "Bank Brain",
    description = "Smart bank organizer with plans, overlays, and presets",
    tags = {"bank", "organiser", "layout", "planner"}
)
public class BankBrainPlugin extends Plugin
{
    @Inject
    private BankBrainConfig config;

    @Inject
    private BankBrainService bankService;

    @Inject
    private BankBrainPanel panel;

    @Inject
    private BankGhostOverlay overlay;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ClientToolbar clientToolbar;

    private NavigationButton navButton;

    @Override
    protected void startUp()
    {
        panel.setRebuildAction(this::rebuildPlan);
        overlayManager.add(overlay);

        navButton = NavigationButton.builder()
            .tooltip("Bank Brain")
            .icon(createIcon())
            .priority(5)
            .panel(panel)
            .build();
        clientToolbar.addNavigation(navButton);

        if (config.autoPlan())
        {
            rebuildPlan();
        }
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        if (navButton != null)
        {
            clientToolbar.removeNavigation(navButton);
        }
    }

    private void rebuildPlan()
    {
        bankService.rebuildPlan(config);
        panel.updateSnapshot(bankService.getSnapshot());
        panel.updatePlan(bankService.getReorderPlan());
        panel.updateTimestamp(bankService.getLastRebuild());
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event)
    {
        if (event.getContainerId() == InventoryID.BANK.getId() && config.autoPlan())
        {
            rebuildPlan();
        }
    }

    private BufferedImage createIcon()
    {
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(24, 144, 255));
        g.fillRoundRect(0, 0, 32, 32, 8, 8);
        g.setColor(Color.WHITE);
        g.drawString("BB", 6, 20);
        g.dispose();
        return image;
    }

    @Provides
    BankBrainConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(BankBrainConfig.class);
    }
}
