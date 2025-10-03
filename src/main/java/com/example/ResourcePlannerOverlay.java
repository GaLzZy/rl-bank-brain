package com.example;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

@Singleton
public class ResourcePlannerOverlay extends OverlayPanel
{
    private final ResourcePlannerPlugin plugin;

    @Inject
    private ResourcePlannerOverlay(ResourcePlannerPlugin plugin)
    {
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Resource Planner")
                .build());

        String recommendation = plugin.getCurrentRecommendation();
        if (recommendation == null)
        {
            recommendation = "No recommendation yet";
        }

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Next")
                .right(recommendation)
                .build());

        for (ResourcePlannerPlugin.ResourceStatus status : plugin.getLastResourceStatuses())
        {
            String left = status.getItemName();
            String right = status.getQuantity() + "/" + status.getTargetQuantity();
            if (status.isBelowThreshold())
            {
                right += " (!)";
            }

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(left)
                    .right(right)
                    .build());
        }

        if (!plugin.getLastSkillStatuses().isEmpty())
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Skills")
                    .build());

            for (ResourcePlannerPlugin.SkillGoalStatus status : plugin.getLastSkillStatuses())
            {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left(status.getSkill().getName())
                        .right(status.getCurrentLevel() + "/" + status.getTargetLevel())
                        .build());
            }
        }

        return super.render(graphics);
    }
}
