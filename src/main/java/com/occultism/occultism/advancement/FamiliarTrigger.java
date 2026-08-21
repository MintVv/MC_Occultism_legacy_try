package com.occultism.occultism.advancement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.occultism.occultism.Tags;

import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

/**
 * 1.12 criterion used by familiar interactions. The enum deliberately keeps
 * the upstream event vocabulary so additional familiar advancements can be
 * added without changing the criterion wire format.
 */
public class FamiliarTrigger implements ICriterionTrigger<FamiliarTrigger.Instance> {

    private static final ResourceLocation ID = new ResourceLocation(Tags.MOD_ID, "familiar");

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
        String value = JsonUtils.getString(json, "type", "");
        Type type = Type.fromId(value);
        return new Instance(type);
    }

    public void trigger(EntityPlayerMP player, Type type) {
        if (player == null || type == null) {
            return;
        }
        Listeners playerListeners = this.listeners.get(player.getAdvancements());
        if (playerListeners != null) {
            playerListeners.trigger(type);
        }
    }

    public void trigger(EntityLivingBase entity, Type type) {
        if (entity instanceof EntityPlayerMP) {
            trigger((EntityPlayerMP) entity, type);
        }
    }

    public static Instance of(Type type) {
        return new Instance(type);
    }

    public enum Type {
        DEER_POOP, CTHULHU_SAD, BAT_EAT, DEVIL_FIRE, GREEDY_ITEM, RARE_VARIANT, PARTY, CAPTURE,
        DRAGON_NUGGET, DRAGON_RIDE, DRAGON_PET, DRAGON_FETCH, BLACKSMITH_UPGRADE,
        GUARDIAN_ULTIMATE_SACRIFICE, HEADLESS_CTHULHU_HEAD, HEADLESS_REBUILT, CHIMERA_RIDE,
        GOAT_DETACH, SHUB_NIGGURATH_SUMMON, SHUB_CTHULHU_FRIENDS, SHUB_NIGGURATH_SPAWN,
        BEHOLDER_RAY, BEHOLDER_EAT, FAIRY_SAVE, MUMMY_DODGE, BEAVER_WOODCHOP;

        private static Type fromId(String value) {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            try {
                return valueOf(value.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
    }

    public static class Instance extends AbstractCriterionInstance {

        private final Type type;

        public Instance(Type type) {
            super(ID);
            this.type = type;
        }

        private boolean test(Type actual) {
            return this.type != null && this.type == actual;
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

        private void trigger(Type type) {
            List<ICriterionTrigger.Listener<Instance>> matched = new ArrayList<>();
            for (ICriterionTrigger.Listener<Instance> listener : this.listeners) {
                if (listener.getCriterionInstance().test(type)) {
                    matched.add(listener);
                }
            }
            for (ICriterionTrigger.Listener<Instance> listener : matched) {
                listener.grantCriterion(this.advancements);
            }
        }
    }
}
