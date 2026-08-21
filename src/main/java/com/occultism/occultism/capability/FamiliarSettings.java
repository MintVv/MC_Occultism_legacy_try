package com.occultism.occultism.capability;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.occultism.occultism.entity.EntityBatFamiliar;
import com.occultism.occultism.entity.EntityBeaverFamiliar;
import com.occultism.occultism.entity.EntityBeholderFamiliar;
import com.occultism.occultism.entity.EntityBlacksmithFamiliar;
import com.occultism.occultism.entity.EntityChimeraFamiliar;
import com.occultism.occultism.entity.EntityCthulhuFamiliar;
import com.occultism.occultism.entity.EntityDeerFamiliar;
import com.occultism.occultism.entity.EntityDevilFamiliar;
import com.occultism.occultism.entity.EntityDragonFamiliar;
import com.occultism.occultism.entity.EntityFairyFamiliar;
import com.occultism.occultism.entity.EntityGoatFamiliar;
import com.occultism.occultism.entity.EntityGreedyFamiliar;
import com.occultism.occultism.entity.EntityGuardianFamiliar;
import com.occultism.occultism.entity.EntityHeadlessFamiliar;
import com.occultism.occultism.entity.EntityMummyFamiliar;
import com.occultism.occultism.entity.EntityOtherworldBird;
import com.occultism.occultism.entity.EntityShubNiggurathFamiliar;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

/**
 * Persistent per-player switches for Familiar effects.
 *
 * <p>Forge 1.12 has no modern EntityType API, so settings are keyed by the
 * registered entity id resolved from the entity class. Unknown classes are
 * ignored rather than accepting arbitrary client-provided ids.</p>
 */
public final class FamiliarSettings {

    private static final Class<?>[] FAMILIAR_CLASSES = {
            EntityGreedyFamiliar.class,
            EntityOtherworldBird.class,
            EntityBatFamiliar.class,
            EntityDeerFamiliar.class,
            EntityCthulhuFamiliar.class,
            EntityDevilFamiliar.class,
            EntityDragonFamiliar.class,
            EntityBlacksmithFamiliar.class,
            EntityGuardianFamiliar.class,
            EntityHeadlessFamiliar.class,
            EntityChimeraFamiliar.class,
            EntityGoatFamiliar.class,
            EntityShubNiggurathFamiliar.class,
            EntityBeholderFamiliar.class,
            EntityFairyFamiliar.class,
            EntityMummyFamiliar.class,
            EntityBeaverFamiliar.class
    };

    private final Map<String, Boolean> familiarEnabled = new LinkedHashMap<>();

    public FamiliarSettings() {
        for (Class<?> familiarClass : FAMILIAR_CLASSES) {
            String key = keyFor(familiarClass);
            if (key != null) {
                this.familiarEnabled.put(key, Boolean.TRUE);
            }
        }
    }

    public static Class<?>[] getFamiliarClasses() {
        return FAMILIAR_CLASSES.clone();
    }

    public boolean isFamiliarEnabled(Class<?> familiarClass) {
        String key = keyFor(familiarClass);
        return key != null && this.familiarEnabled.getOrDefault(key, Boolean.TRUE);
    }

    public void setFamiliarEnabled(Class<?> familiarClass, boolean enabled) {
        String key = keyFor(familiarClass);
        if (key != null && isKnownKey(key)) {
            this.familiarEnabled.put(key, enabled);
        }
    }

    public boolean toggleFamiliar(Class<?> familiarClass) {
        boolean enabled = !isFamiliarEnabled(familiarClass);
        setFamiliarEnabled(familiarClass, enabled);
        return enabled;
    }

    public void copyFrom(FamiliarSettings other) {
        this.familiarEnabled.clear();
        this.familiarEnabled.putAll(other.familiarEnabled);
    }

    public Map<String, Boolean> getValues() {
        return Collections.unmodifiableMap(this.familiarEnabled);
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        for (Map.Entry<String, Boolean> entry : this.familiarEnabled.entrySet()) {
            nbt.setBoolean(entry.getKey(), entry.getValue());
        }
        return nbt;
    }

    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt == null) {
            return;
        }
        for (Class<?> familiarClass : FAMILIAR_CLASSES) {
            String key = keyFor(familiarClass);
            if (key != null && nbt.hasKey(key)) {
                this.familiarEnabled.put(key, nbt.getBoolean(key));
            }
        }
    }

    private boolean isKnownKey(String key) {
        return this.familiarEnabled.containsKey(key);
    }

    private static String keyFor(Class<?> familiarClass) {
        if (familiarClass == null || !Entity.class.isAssignableFrom(familiarClass)) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Class<? extends Entity> entityClass = (Class<? extends Entity>) familiarClass;
        ResourceLocation id = EntityList.getKey(entityClass);
        return id == null ? familiarClass.getName() : id.toString();
    }
}
