# Resource Planner Plugin QA Notes

## Scope
Manual verification for the Resource Planner plugin covering resource synchronisation, recommendation updates, and configuration driven behaviour.

## Test Environment
- RuneLite client launched with the Resource Planner plugin enabled.
- Character with access to inventory and bank.

## Test Cases
1. **Login recommendation**
   - Configure `resourceFilters` with a low threshold (e.g. `314:500:400`).
   - Log in.
   - ✅ Overlay lists the tracked item and quantity.
   - ✅ Chat message recommends restocking on login when `notifyInChat` is enabled.

2. **Resource update handling**
   - While logged in, withdraw or deposit a tracked item.
   - ✅ Overlay updates counts immediately after the container change.
   - ✅ Recommendation message changes once quantities move above or below the configured threshold.

3. **Skill progress tracking**
   - Configure `monitoredSkills` with a reachable target (e.g. `FISHING:50`).
   - Gain a level in the monitored skill.
   - ✅ Overlay updates the skill progress row with the new level.
   - ✅ Recommendation switches to suggest training when resources are healthy but a skill goal is unmet.

4. **Config driven filtering**
   - Change `resourceFilters` to track a different item and set `preferredActivities` to a unique value.
   - ✅ Next evaluation references the updated item and activity without restarting the client.
   - ✅ Disabling `notifyInChat` stops chat notifications while overlay continues to refresh.

## Regression considerations
- Overlay detaches cleanly when the plugin is disabled.
- Chat notifications only post while logged in.
