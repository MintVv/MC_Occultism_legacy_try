package com.occultism.occultism.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.occultism.occultism.entity.IFamiliar;
import com.occultism.occultism.capability.FamiliarRingCapability;
import com.occultism.occultism.capability.FamiliarSettings;
import com.occultism.occultism.handler.FamiliarRingHandler;
import com.occultism.occultism.registry.ModCapabilities;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.EnumFacing;

/**
 * Shared Familiar queries matching the 1.81.4 ownership semantics.
 *
 * <p>The 1.12 port uses a native one-slot player capability as its accessory
 * adapter. Ordinary inventory rings are never included in equipped queries.</p>
 */
public final class FamiliarUtil {

    private static final double NEARBY_RANGE = 10.0D;

    private FamiliarUtil() {
    }

    /**
     * Settings are enabled by default until the player settings capability is
     * installed. A null owner is always disabled.
     */
    public static boolean isFamiliarEnabled(@Nullable EntityLivingBase owner,
            @Nullable Class<?> familiarType) {
        if (owner == null || familiarType == null || ModCapabilities.FAMILIAR_SETTINGS == null
                || !owner.hasCapability(ModCapabilities.FAMILIAR_SETTINGS, (EnumFacing) null)) {
            return owner != null && familiarType != null;
        }
        FamiliarSettings settings = owner.getCapability(ModCapabilities.FAMILIAR_SETTINGS, (EnumFacing) null);
        return settings == null || settings.isFamiliarEnabled(familiarType);
    }

    public static <T extends EntityLivingBase & IFamiliar> boolean hasFamiliar(@Nullable EntityLivingBase owner,
            Class<T> familiarType) {
        return getFamiliar(owner, familiarType, familiar -> true) != null;
    }

    public static <T extends EntityLivingBase & IFamiliar> boolean hasFamiliar(@Nullable EntityLivingBase owner,
            Class<T> familiarType, Predicate<T> predicate) {
        return getFamiliar(owner, familiarType, predicate) != null;
    }

    public static <T extends EntityLivingBase & IFamiliar> List<T> getAllFamiliars(
            @Nullable EntityLivingBase owner, Class<T> familiarType) {
        return getAllFamiliars(owner, familiarType, familiar -> true);
    }

    /**
     * Returns nearby familiars. Equipped familiars are added only after a
     * 1.12 accessory-slot adapter exists, so this method is safe for the
     * current transport-only Familiar Ring.
     */
    public static <T extends EntityLivingBase & IFamiliar> List<T> getAllFamiliars(
            @Nullable EntityLivingBase owner, Class<T> familiarType, Predicate<T> predicate) {
        List<T> result = getAllNearbyFamiliars(owner, familiarType, predicate);
        result.addAll(getAllEquippedFamiliars(owner, familiarType, predicate));
        return result;
    }

    public static <T extends EntityLivingBase & IFamiliar> T getFamiliar(@Nullable EntityLivingBase owner,
            Class<T> familiarType) {
        return getFamiliar(owner, familiarType, familiar -> true);
    }

    public static <T extends EntityLivingBase & IFamiliar> T getFamiliar(@Nullable EntityLivingBase owner,
            Class<T> familiarType, Predicate<T> predicate) {
        T equipped = getEquippedFamiliar(owner, familiarType, predicate);
        return equipped == null ? getNearbyFamiliar(owner, familiarType, predicate) : equipped;
    }

    public static <T extends EntityLivingBase & IFamiliar> List<T> getAllNearbyFamiliars(
            @Nullable EntityLivingBase owner, Class<T> familiarType, Predicate<T> predicate) {
        if (owner == null || owner.world == null || familiarType == null || predicate == null) {
            return new ArrayList<>();
        }
        AxisAlignedBB bounds = owner.getEntityBoundingBox().grow(NEARBY_RANGE);
        return owner.world.getEntitiesWithinAABB(familiarType, bounds,
                familiar -> familiar != null && familiar.isEntityAlive()
                        && familiar.getFamiliarOwner() == owner && predicate.test(familiar));
    }

    public static <T extends EntityLivingBase & IFamiliar> T getNearbyFamiliar(@Nullable EntityLivingBase owner,
            Class<T> familiarType, Predicate<T> predicate) {
        List<T> nearby = getAllNearbyFamiliars(owner, familiarType, predicate);
        return nearby.isEmpty() ? null : nearby.get(0);
    }

    public static <T extends EntityLivingBase & IFamiliar> List<T> getAllEquippedFamiliars(
            @Nullable EntityLivingBase owner, Class<T> familiarType, Predicate<T> predicate) {
        if (!(owner instanceof EntityPlayer) || familiarType == null || predicate == null) {
            return Collections.emptyList();
        }
        FamiliarRingCapability capability = FamiliarRingHandler.get((EntityPlayer) owner);
        if (capability == null) {
            return Collections.emptyList();
        }
        IFamiliar familiar = capability.getActiveFamiliar((EntityPlayer) owner);
        if (familiar == null || !familiarType.isInstance(familiar.getFamiliarEntity())) {
            return Collections.emptyList();
        }
        T typed = familiarType.cast(familiar.getFamiliarEntity());
        return predicate.test(typed) ? Collections.singletonList(typed) : Collections.emptyList();
    }

    public static <T extends EntityLivingBase & IFamiliar> T getEquippedFamiliar(@Nullable EntityLivingBase owner,
            Class<T> familiarType, Predicate<T> predicate) {
        List<T> equipped = getAllEquippedFamiliars(owner, familiarType, predicate);
        return equipped.isEmpty() ? null : equipped.get(0);
    }

    /**
     * Returns the owner's recent revenge and attack targets that are close to
     * the familiar. The upstream compares squared distance directly with the
     * supplied range; that quirk is retained for compatibility.
     */
    public static List<EntityLivingBase> getOwnerEnemies(@Nullable EntityLivingBase owner,
            @Nullable EntityLivingBase familiar, float range) {
        if (owner == null) {
            return new ArrayList<>();
        }
        List<EntityLivingBase> enemies = new ArrayList<>();
        addIfClose(enemies, owner.getRevengeTarget(), familiar, range);
        addIfClose(enemies, owner.getLastAttackedEntity(), familiar, range);
        return enemies;
    }

    private static void addIfClose(List<EntityLivingBase> result, @Nullable EntityLivingBase candidate,
            @Nullable EntityLivingBase familiar, float range) {
        if (candidate != null && candidate != familiar && familiar != null
                && candidate.getDistanceSq(familiar) < range) {
            result.add(candidate);
        }
    }
}
