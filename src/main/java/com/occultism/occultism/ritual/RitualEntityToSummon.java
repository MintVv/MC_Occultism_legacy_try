package com.occultism.occultism.ritual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.util.ResourceLocation;

public class RitualEntityToSummon {

    private final ResourceLocation entityId;
    private final ResourceLocation entityTagId;
    private final List<ResourceLocation> entityIds;
    private final int summonNumber;

    public RitualEntityToSummon(ResourceLocation entityId, int summonNumber) {
        this(entityId, null, Collections.singletonList(entityId), summonNumber);
    }

    public RitualEntityToSummon(ResourceLocation entityTagId, List<ResourceLocation> entityIds, int summonNumber) {
        this(null, entityTagId, entityIds, summonNumber);
    }

    private RitualEntityToSummon(ResourceLocation entityId, ResourceLocation entityTagId,
            List<ResourceLocation> entityIds, int summonNumber) {
        this.entityId = entityId;
        this.entityTagId = entityTagId;
        this.entityIds = Collections.unmodifiableList(new ArrayList<>(entityIds));
        this.summonNumber = Math.max(1, summonNumber);
    }

    public ResourceLocation getEntityId() {
        return this.entityId;
    }

    public ResourceLocation getEntityTagId() {
        return this.entityTagId;
    }

    public boolean isTagBased() {
        return this.entityTagId != null;
    }

    public List<ResourceLocation> getEntityIds() {
        return this.entityIds;
    }

    public ResourceLocation pickEntityId(Random random) {
        if (this.entityIds.isEmpty()) {
            return null;
        }
        if (this.entityIds.size() == 1) {
            return this.entityIds.get(0);
        }
        return this.entityIds.get(random.nextInt(this.entityIds.size()));
    }

    public String getDisplayId() {
        return this.isTagBased() ? "#" + this.entityTagId.toString()
                : this.entityId.toString();
    }

    public int getSummonNumber() {
        return this.summonNumber;
    }
}
