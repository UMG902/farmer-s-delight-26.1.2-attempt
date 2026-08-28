# v31

- Added the missing 26.1 client-item definition for `skillet`, pointing to the existing skillet item model.
- Replaced the empty Cutting Board renderer with NeoForge/Minecraft 26.1 `ItemModelResolver` + `ItemStackRenderState` rendering so stored items are visible again.
- Did not modify weapon-specific registration or behavior.
