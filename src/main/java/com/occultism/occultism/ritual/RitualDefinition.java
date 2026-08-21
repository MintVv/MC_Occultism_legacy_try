package com.occultism.occultism.ritual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTier;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class RitualDefinition {

    private final String id;
    private final String displayName;
    private final String displayNameKey;
    private final String pentacleId;
    private final String ritualType;
    private final RitualIngredient activationIngredient;
    private final List<RitualIngredient> additionalIngredients;
    private final RitualResult result;
    private final RitualResult ritualDummy;
    private final int durationTicks;
    private final RitualSacrifice sacrifice;
    private final RitualIngredient itemToUse;
    private final RitualEntityToSummon entityToSummon;
    private final SpiritJobType spiritJobType;
    private final SpiritTier spiritTier;
    private final int spiritMaxAge;

    public RitualDefinition(String id, String displayName, String pentacleId, RitualIngredient activationIngredient,
            RitualResult result, int durationTicks, RitualIngredient... additionalIngredients) {
        this(id, displayName, pentacleId, "craft", activationIngredient, result, result, durationTicks,
                null, null, additionalIngredients);
    }

    public RitualDefinition(String id, String displayName, String pentacleId, String ritualType,
            RitualIngredient activationIngredient, RitualResult result, int durationTicks,
            RitualIngredient... additionalIngredients) {
        this(id, displayName, pentacleId, ritualType, activationIngredient, result, result, durationTicks,
                null, null, additionalIngredients);
    }

    public RitualDefinition(String id, String displayName, String pentacleId, String ritualType,
            RitualIngredient activationIngredient, RitualResult result, RitualResult ritualDummy, int durationTicks,
            RitualIngredient... additionalIngredients) {
        this(id, displayName, null, pentacleId, ritualType, activationIngredient, result, ritualDummy, durationTicks,
                null, null, null, null, null, -1, additionalIngredients);
    }

    public RitualDefinition(String id, String displayName, String pentacleId, String ritualType,
            RitualIngredient activationIngredient, RitualResult result, RitualResult ritualDummy, int durationTicks,
            RitualSacrifice sacrifice, RitualIngredient... additionalIngredients) {
        this(id, displayName, pentacleId, ritualType, activationIngredient, result, ritualDummy, durationTicks,
                sacrifice, null, null, additionalIngredients);
    }

    public RitualDefinition(String id, String displayName, String pentacleId, String ritualType,
            RitualIngredient activationIngredient, RitualResult result, RitualResult ritualDummy, int durationTicks,
            RitualSacrifice sacrifice, RitualIngredient itemToUse, RitualIngredient... additionalIngredients) {
        this(id, displayName, pentacleId, ritualType, activationIngredient, result, ritualDummy, durationTicks,
                sacrifice, itemToUse, null, additionalIngredients);
    }

    public RitualDefinition(String id, String displayName, String pentacleId, String ritualType,
            RitualIngredient activationIngredient, RitualResult result, RitualResult ritualDummy, int durationTicks,
            RitualSacrifice sacrifice, RitualIngredient itemToUse, RitualEntityToSummon entityToSummon,
            RitualIngredient... additionalIngredients) {
        this(id, displayName, pentacleId, ritualType, activationIngredient, result, ritualDummy, durationTicks,
                sacrifice, itemToUse, entityToSummon, null, null, additionalIngredients);
    }

    public RitualDefinition(String id, String displayName, String pentacleId, String ritualType,
            RitualIngredient activationIngredient, RitualResult result, RitualResult ritualDummy, int durationTicks,
            RitualSacrifice sacrifice, RitualIngredient itemToUse, RitualEntityToSummon entityToSummon,
            SpiritJobType spiritJobType, RitualIngredient... additionalIngredients) {
        this(id, displayName, pentacleId, ritualType, activationIngredient, result, ritualDummy, durationTicks,
                sacrifice, itemToUse, entityToSummon, spiritJobType, null, -1, additionalIngredients);
    }

    public RitualDefinition(String id, String displayName, String pentacleId, String ritualType,
            RitualIngredient activationIngredient, RitualResult result, RitualResult ritualDummy, int durationTicks,
            RitualSacrifice sacrifice, RitualIngredient itemToUse, RitualEntityToSummon entityToSummon,
            SpiritJobType spiritJobType, SpiritTier spiritTier, RitualIngredient... additionalIngredients) {
        this(id, displayName, pentacleId, ritualType, activationIngredient, result, ritualDummy, durationTicks,
                sacrifice, itemToUse, entityToSummon, spiritJobType, spiritTier, -1, additionalIngredients);
    }

    public RitualDefinition(String id, String displayName, String pentacleId, String ritualType,
            RitualIngredient activationIngredient, RitualResult result, RitualResult ritualDummy, int durationTicks,
            RitualSacrifice sacrifice, RitualIngredient itemToUse, RitualEntityToSummon entityToSummon,
            SpiritJobType spiritJobType, int spiritMaxAge, RitualIngredient... additionalIngredients) {
        this(id, displayName, pentacleId, ritualType, activationIngredient, result, ritualDummy, durationTicks,
                sacrifice, itemToUse, entityToSummon, spiritJobType, null, spiritMaxAge, additionalIngredients);
    }

    public RitualDefinition(String id, String displayName, String pentacleId, String ritualType,
            RitualIngredient activationIngredient, RitualResult result, RitualResult ritualDummy, int durationTicks,
            RitualSacrifice sacrifice, RitualIngredient itemToUse, RitualEntityToSummon entityToSummon,
            SpiritJobType spiritJobType, SpiritTier spiritTier, int spiritMaxAge,
            RitualIngredient... additionalIngredients) {
        this(id, displayName, null, pentacleId, ritualType, activationIngredient, result, ritualDummy, durationTicks,
                sacrifice, itemToUse, entityToSummon, spiritJobType, spiritTier, spiritMaxAge, additionalIngredients);
    }

    /**
     * Full constructor including the optional translation key for the ritual name. Definitions created without a key
     * (CraftTweaker scripts, runtime injections, tests) keep falling back to the raw English display name.
     */
    public RitualDefinition(String id, String displayName, String displayNameKey, String pentacleId, String ritualType,
            RitualIngredient activationIngredient, RitualResult result, RitualResult ritualDummy, int durationTicks,
            RitualSacrifice sacrifice, RitualIngredient itemToUse, RitualEntityToSummon entityToSummon,
            SpiritJobType spiritJobType, SpiritTier spiritTier, int spiritMaxAge,
            RitualIngredient... additionalIngredients) {
        this.id = id;
        this.displayName = displayName;
        this.displayNameKey = displayNameKey;
        this.pentacleId = pentacleId;
        this.ritualType = ritualType;
        this.activationIngredient = activationIngredient;
        this.additionalIngredients = Collections.unmodifiableList(Arrays.asList(additionalIngredients));
        this.result = result;
        this.ritualDummy = ritualDummy;
        this.durationTicks = durationTicks;
        this.sacrifice = sacrifice;
        this.itemToUse = itemToUse;
        this.entityToSummon = entityToSummon;
        this.spiritJobType = spiritJobType;
        this.spiritTier = spiritTier;
        this.spiritMaxAge = spiritMaxAge;
    }

    public boolean matchesActivation(PentacleMatch pentacle, ItemStack stack) {
        return pentacle.getDefinition().getId().equals(this.pentacleId) && this.activationIngredient.matches(stack);
    }

    public boolean matchesAdditionalIngredients(List<ItemStack> stacks) {
        List<ItemStack> remainingStacks = new ArrayList<>();
        for (ItemStack stack : stacks) {
            remainingStacks.add(stack.copy());
        }

        for (RitualIngredient ingredient : this.additionalIngredients) {
            boolean found = false;
            for (ItemStack stack : remainingStacks) {
                if (ingredient.matches(stack)) {
                    ingredient.consume(stack);
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    public String getId() {
        return this.id;
    }

    /**
     * The raw English name from the ritual JSON. JEI job classification derives spirit jobs from English keywords in
     * this string, so it must stay in English; use {@link #createNameComponent()} for anything player facing.
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * The translation key for the localized ritual name, or null when this definition has no key.
     */
    public String getDisplayNameKey() {
        return this.displayNameKey;
    }

    public boolean hasDisplayNameKey() {
        return this.displayNameKey != null;
    }

    /**
     * A text component for the ritual name that clients resolve in their own language. Passing this nested component
     * as a chat message argument keeps the ritual name translated instead of pasting the English literal.
     */
    public ITextComponent createNameComponent() {
        return this.displayNameKey != null
                ? new TextComponentTranslation(this.displayNameKey)
                : new TextComponentString(this.displayName);
    }

    public String getPentacleId() {
        return this.pentacleId;
    }

    public String getRitualType() {
        return this.ritualType;
    }

    public RitualIngredient getActivationIngredient() {
        return this.activationIngredient;
    }

    public List<RitualIngredient> getAdditionalIngredients() {
        return this.additionalIngredients;
    }

    public RitualResult getResult() {
        return this.result;
    }

    public RitualResult getRitualDummy() {
        return this.ritualDummy;
    }

    public int getDurationTicks() {
        return this.durationTicks;
    }

    public RitualSacrifice getSacrifice() {
        return this.sacrifice;
    }

    public boolean requiresSacrifice() {
        return this.sacrifice != null;
    }

    public RitualIngredient getItemToUse() {
        return this.itemToUse;
    }

    public boolean requiresItemUse() {
        return this.itemToUse != null;
    }

    public RitualEntityToSummon getEntityToSummon() {
        return this.entityToSummon;
    }

    public boolean summonsEntity() {
        return this.entityToSummon != null;
    }

    public SpiritJobType getSpiritJobType() {
        return this.spiritJobType;
    }

    public boolean hasSpiritJobType() {
        return this.spiritJobType != null;
    }

    public SpiritTier getSpiritTier() {
        return this.spiritTier;
    }

    public boolean hasSpiritTier() {
        return this.spiritTier != null;
    }

    public int getSpiritMaxAge() {
        return this.spiritMaxAge;
    }

    public boolean hasSpiritMaxAge() {
        return this.spiritMaxAge > 0;
    }
}
