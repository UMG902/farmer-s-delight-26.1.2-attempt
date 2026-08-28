# v25 — 26.1.2 recipe/resource repair

- Fixed checked-in recipe JSON ingredient syntax for 26.1.2.
- Fixed Cooking Pot recipe codec to use strict ItemStack codecs and the current `ingredients` shape.
- Fixed Cutting Board recipe codec to read the current one-element `ingredients` list.
- Fixed cutting tool JSON from the legacy item-ability+tag array into the corresponding current tool tag.
- Preserved the custom `farmersdelight:dough` recipe type.
- Added client item definitions to the main resource set so 26.1.2 ModelManager can find them reliably in dev.
- Replaced the empty Cooking Pot screen with a 26.1 extraction-phase background/labels implementation.
- Replaced the empty Canvas Sign renderer with the 26.1 vanilla sign render pipeline.
- Weapon-specific item assets/recipes were left untouched in this patch.
