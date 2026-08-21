package com.occultism.occultism.advancement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.occultism.occultism.Tags;
import com.occultism.occultism.ritual.RitualDefinition;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

public class RitualTrigger implements ICriterionTrigger<RitualTrigger.Instance> {

    private static final ResourceLocation ID = new ResourceLocation(Tags.MOD_ID, "ritual");

    private final Map<PlayerAdvancements, Listeners> listeners = new HashMap<>();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addListener(PlayerAdvancements advancements, ICriterionTrigger.Listener<Instance> listener) {
        this.listeners.computeIfAbsent(advancements, Listeners::new).add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements advancements, ICriterionTrigger.Listener<Instance> listener) {
        Listeners playerListeners = this.listeners.get(advancements);
        if (playerListeners == null) {
            return;
        }
        playerListeners.remove(listener);
        if (playerListeners.isEmpty()) {
            this.listeners.remove(advancements);
        }
    }

    @Override
    public void removeAllListeners(PlayerAdvancements advancements) {
        this.listeners.remove(advancements);
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        JsonObject predicate = json.has("ritual_predicate")
                ? JsonUtils.getJsonObject(json, "ritual_predicate") : json;
        String ritualId = JsonUtils.getString(predicate, "ritual_id", "");
        return new Instance(ritualId.isEmpty() ? null : new ResourceLocation(ritualId));
    }

    public void trigger(EntityPlayerMP player, RitualDefinition ritual) {
        Listeners playerListeners = this.listeners.get(player.getAdvancements());
        if (playerListeners != null) {
            playerListeners.trigger(ritual);
        }
    }

    public static class Instance extends AbstractCriterionInstance {

        private final ResourceLocation ritualId;

        public Instance(ResourceLocation ritualId) {
            super(ID);
            this.ritualId = ritualId;
        }

        private boolean test(RitualDefinition ritual) {
            if (this.ritualId == null) {
                return true;
            }
            ResourceLocation actual = new ResourceLocation(Tags.MOD_ID, "ritual/" + ritual.getId());
            return this.ritualId.equals(actual);
        }
    }

    private static class Listeners {

        private final PlayerAdvancements advancements;
        private final Set<ICriterionTrigger.Listener<Instance>> listeners = new HashSet<>();

        private Listeners(PlayerAdvancements advancements) {
            this.advancements = advancements;
        }

        private void add(ICriterionTrigger.Listener<Instance> listener) {
            this.listeners.add(listener);
        }

        private void remove(ICriterionTrigger.Listener<Instance> listener) {
            this.listeners.remove(listener);
        }

        private boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        private void trigger(RitualDefinition ritual) {
            List<ICriterionTrigger.Listener<Instance>> matched = new ArrayList<>();
            for (ICriterionTrigger.Listener<Instance> listener : this.listeners) {
                if (listener.getCriterionInstance().test(ritual)) {
                    matched.add(listener);
                }
            }
            for (ICriterionTrigger.Listener<Instance> listener : matched) {
                listener.grantCriterion(this.advancements);
            }
        }
    }
}
