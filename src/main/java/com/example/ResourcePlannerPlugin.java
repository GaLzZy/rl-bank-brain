package com.example;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import net.runelite.api.InventoryID;
import net.runelite.client.events.ConfigChanged;

@Slf4j
@PluginDescriptor(
        name = "Resource Planner",
        description = "Tracks resources and goals to recommend the next RuneScape activity.",
        tags = {"planning", "resources", "goals"}
)
public class ResourcePlannerPlugin extends Plugin
{
    private static final InventoryID[] TRACKED_CONTAINERS = {
            InventoryID.INVENTORY,
            InventoryID.EQUIPMENT,
            InventoryID.BANK
    };

    @Inject
    private Client client;

    @Inject
    private ResourcePlannerConfig config;

    @Inject
    private ItemManager itemManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ResourcePlannerOverlay overlay;

    @Inject
    private ClientThread clientThread;

    private final Map<Integer, Integer> resourceQuantities = new HashMap<>();
    private final Map<Skill, Integer> skillProgress = new EnumMap<>(Skill.class);
    private List<ResourceStatus> lastResourceStatuses = Collections.emptyList();
    private List<SkillGoalStatus> lastSkillStatuses = Collections.emptyList();
    private String currentRecommendation;

    @Override
    protected void startUp()
    {
        overlayManager.add(overlay);
        clientThread.invoke(() ->
        {
            refreshTrackedResources();
            evaluateAndAnnounce("start-up");
        });
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        resourceQuantities.clear();
        skillProgress.clear();
        lastResourceStatuses = Collections.emptyList();
        lastSkillStatuses = Collections.emptyList();
        currentRecommendation = null;
    }

    @Provides
    ResourcePlannerConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ResourcePlannerConfig.class);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGGED_IN)
        {
            clientThread.invoke(() ->
            {
                refreshTrackedResources();
                evaluateAndAnnounce("login");
            });
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event)
    {
        if (!isTrackedContainer(event.getContainerId()))
        {
            return;
        }

        clientThread.invoke(() ->
        {
            refreshTrackedResources();
            evaluateAndAnnounce("inventory update");
        });
    }

    @Subscribe
    public void onStatChanged(StatChanged event)
    {
        Skill skill = event.getSkill();
        int level = event.getLevel();

        clientThread.invoke(() ->
        {
            skillProgress.put(skill, level);
            evaluateAndAnnounce("skill update");
        });
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (!ResourcePlannerConfig.GROUP.equals(event.getGroup()))
        {
            return;
        }

        clientThread.invoke(() -> evaluateAndAnnounce("config changed"));
    }

    public List<ResourceStatus> getLastResourceStatuses()
    {
        return lastResourceStatuses;
    }

    public List<SkillGoalStatus> getLastSkillStatuses()
    {
        return lastSkillStatuses;
    }

    public String getCurrentRecommendation()
    {
        return currentRecommendation;
    }

    private void refreshTrackedResources()
    {
        Map<Integer, Integer> aggregated = new HashMap<>();

        for (InventoryID inventoryID : TRACKED_CONTAINERS)
        {
            ItemContainer container = client.getItemContainer(inventoryID);
            if (container == null)
            {
                continue;
            }

            for (Item item : container.getItems())
            {
                if (item == null)
                {
                    continue;
                }

                aggregated.merge(item.getId(), item.getQuantity(), Integer::sum);
            }
        }

        resourceQuantities.clear();
        resourceQuantities.putAll(aggregated);
    }

    private void evaluateAndAnnounce(String reason)
    {
        Recommendation recommendation = evaluateRecommendation();
        if (recommendation == null)
        {
            return;
        }

        lastResourceStatuses = recommendation.resourceStatuses;
        lastSkillStatuses = recommendation.skillGoalStatuses;
        setCurrentRecommendation(recommendation.message, reason);
    }

    private Recommendation evaluateRecommendation()
    {
        List<ResourceFilter> filters = parseResourceFilters(config.resourceFilters());
        List<ResourceStatus> resourceStatuses = buildResourceStatuses(filters);
        List<SkillGoal> goals = parseSkillGoals(config.monitoredSkills());
        List<SkillGoalStatus> skillStatuses = buildSkillStatuses(goals);

        Optional<ResourceStatus> criticalResource = resourceStatuses.stream()
                .filter(ResourceStatus::isBelowThreshold)
                .min(Comparator.comparingInt(ResourceStatus::getQuantity));

        String activity = determinePreferredActivity();
        String activityDisplay = activity.isEmpty() ? "any activity" : activity;
        String message;

        if (criticalResource.isPresent())
        {
            ResourceStatus status = criticalResource.get();
            message = String.format(Locale.US, "Restock %s (%d/%d) via %s.",
                    status.getItemName(), status.getQuantity(), status.getTargetQuantity(), activityDisplay);
        }
        else
        {
            Optional<SkillGoalStatus> urgentSkill = skillStatuses.stream()
                    .filter(s -> s.getCurrentLevel() < s.getTargetLevel())
                    .min(Comparator.comparingInt(s -> s.getCurrentLevel() - s.getTargetLevel()));

            if (urgentSkill.isPresent())
            {
                SkillGoalStatus status = urgentSkill.get();
                message = String.format(Locale.US, "Train %s (%d/%d) while focusing on %s.",
                        status.getSkill().getName(), status.getCurrentLevel(), status.getTargetLevel(), activityDisplay);
            }
            else if (!activity.isEmpty())
            {
                message = String.format(Locale.US, "Focus on %s. Resources are healthy.", activityDisplay);
            }
            else
            {
                message = "Resources are healthy. Configure preferences for tailored recommendations.";
            }
        }

        return new Recommendation(message, resourceStatuses, skillStatuses);
    }

    private void setCurrentRecommendation(String message, String reason)
    {
        if (Objects.equals(currentRecommendation, message))
        {
            return;
        }

        currentRecommendation = message;
        log.debug("Updated recommendation ({}): {}", reason, message);

        if (config.notifyInChat() && client.getGameState() == GameState.LOGGED_IN)
        {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "[Resource Planner] " + message, null);
        }
    }

    private List<ResourceStatus> buildResourceStatuses(List<ResourceFilter> filters)
    {
        List<ResourceStatus> statuses = new ArrayList<>();

        for (ResourceFilter filter : filters)
        {
            int quantity = resourceQuantities.getOrDefault(filter.getItemId(), 0);
            String itemName = getItemName(filter.getItemId());
            statuses.add(new ResourceStatus(filter, quantity, itemName));
        }

        return statuses;
    }

    private List<SkillGoalStatus> buildSkillStatuses(List<SkillGoal> goals)
    {
        List<SkillGoalStatus> statuses = new ArrayList<>();

        for (SkillGoal goal : goals)
        {
            int currentLevel = skillProgress.computeIfAbsent(goal.getSkill(), skill ->
            {
                int level = client.getRealSkillLevel(skill);
                return level > 0 ? level : 1;
            });
            statuses.add(new SkillGoalStatus(goal.getSkill(), currentLevel, goal.getTargetLevel()));
        }

        return statuses;
    }

    private String determinePreferredActivity()
    {
        List<String> activities = Text.fromCSV(config.preferredActivities());
        return activities.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .findFirst()
                .orElse("");
    }

    private List<ResourceFilter> parseResourceFilters(String value)
    {
        if (value == null || value.trim().isEmpty())
        {
            return Collections.emptyList();
        }

        List<ResourceFilter> filters = new ArrayList<>();
        for (String token : Text.fromCSV(value))
        {
            String trimmed = token.trim();
            if (trimmed.isEmpty())
            {
                continue;
            }

            String[] parts = trimmed.split(":");
            if (parts.length < 2)
            {
                log.warn("Invalid resource filter entry: {}", trimmed);
                continue;
            }

            try
            {
                int itemId = Integer.parseInt(parts[0].trim());
                int target = Integer.parseInt(parts[1].trim());
                int threshold = parts.length > 2 ? Integer.parseInt(parts[2].trim()) : target;
                filters.add(new ResourceFilter(itemId, target, threshold));
            }
            catch (NumberFormatException ex)
            {
                log.warn("Unable to parse resource filter entry: {}", trimmed, ex);
            }
        }

        return filters;
    }

    private List<SkillGoal> parseSkillGoals(String value)
    {
        if (value == null || value.trim().isEmpty())
        {
            return Collections.emptyList();
        }

        List<SkillGoal> goals = new ArrayList<>();
        for (String token : Text.fromCSV(value))
        {
            String trimmed = token.trim();
            if (trimmed.isEmpty())
            {
                continue;
            }

            String[] parts = trimmed.split(":");
            if (parts.length < 2)
            {
                log.warn("Invalid skill goal entry: {}", trimmed);
                continue;
            }

            try
            {
                Skill skill = Skill.valueOf(parts[0].trim().toUpperCase(Locale.US));
                int target = Integer.parseInt(parts[1].trim());
                goals.add(new SkillGoal(skill, target));
            }
            catch (IllegalArgumentException ex)
            {
                log.warn("Unable to parse skill goal entry: {}", trimmed, ex);
            }
        }

        return goals;
    }

    private boolean isTrackedContainer(int containerId)
    {
        for (InventoryID id : TRACKED_CONTAINERS)
        {
            if (id.getId() == containerId)
            {
                return true;
            }
        }
        return false;
    }

    private String getItemName(int itemId)
    {
        try
        {
            return itemManager.getItemComposition(itemId).getName();
        }
        catch (IllegalArgumentException ex)
        {
            return "Item " + itemId;
        }
    }

    @Value
    private static class ResourceFilter
    {
        int itemId;
        int targetQuantity;
        int thresholdQuantity;
    }

    @Value
    public static class ResourceStatus
    {
        ResourceFilter filter;
        int quantity;
        String itemName;

        public int getItemId()
        {
            return filter.getItemId();
        }

        public int getTargetQuantity()
        {
            return filter.getTargetQuantity();
        }

        public int getThresholdQuantity()
        {
            return filter.getThresholdQuantity();
        }

        public boolean isBelowThreshold()
        {
            return quantity < filter.getThresholdQuantity();
        }
    }

    @Value
    private static class SkillGoal
    {
        Skill skill;
        int targetLevel;
    }

    @Value
    public static class SkillGoalStatus
    {
        Skill skill;
        int currentLevel;
        int targetLevel;
    }

    @Value
    private static class Recommendation
    {
        String message;
        List<ResourceStatus> resourceStatuses;
        List<SkillGoalStatus> skillGoalStatuses;
    }
}
