package com.occultism.occultism.compat.crafttweaker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import com.occultism.occultism.ritual.RitualDefinition;
import com.occultism.occultism.ritual.RitualEntityTagMappings;
import com.occultism.occultism.ritual.RitualEntityToSummon;
import com.occultism.occultism.ritual.RitualIngredient;
import com.occultism.occultism.ritual.RitualRecipeManager;
import com.occultism.occultism.ritual.RitualResult;
import com.occultism.occultism.ritual.RitualSacrifice;
import com.occultism.occultism.spirit.SpiritJobType;

@ZenRegister
@ZenClass("mods.occultism.Ritual")
public class RitualTweaker {

    private RitualTweaker() {
    }

    @ZenMethod
    public static void addCrafting(String id, String displayName, String pentacleId, IIngredient activation,
            IItemStack result, int durationTicks, IIngredient[] ingredients) {
        applyAdd(new AddRitualAction(createDefinition(CraftTweakerUtil.normalizeId(id), displayName,
                CraftTweakerUtil.normalizeId(pentacleId), "craft", activation, result, result, durationTicks,
                null, null, null, null, -1, ingredients)));
    }

    @ZenMethod
    public static void addCrafting(String id, String pentacleId, IIngredient activation, IItemStack result,
            int durationTicks, IIngredient[] ingredients) {
        addCrafting(id, id, pentacleId, activation, result, durationTicks, ingredients);
    }

    @ZenMethod
    public static void addCraftingWithSacrifice(String id, String displayName, String pentacleId,
            IIngredient activation, IItemStack result, int durationTicks, String sacrificeTag,
            String sacrificeDisplayName, IIngredient[] ingredients) {
        applyAdd(new AddRitualAction(createDefinition(CraftTweakerUtil.normalizeId(id), displayName,
                CraftTweakerUtil.normalizeId(pentacleId), "craft", activation, result, result, durationTicks,
                new RitualSacrifice(sacrificeTag, sacrificeDisplayName), null, null, null, -1, ingredients)));
    }

    @ZenMethod
    public static void addCraftingWithItemUse(String id, String displayName, String pentacleId, IIngredient activation,
            IItemStack result, int durationTicks, IIngredient itemToUse, IIngredient[] ingredients) {
        applyAdd(new AddRitualAction(createDefinition(CraftTweakerUtil.normalizeId(id), displayName,
                CraftTweakerUtil.normalizeId(pentacleId), "craft", activation, result, result, durationTicks,
                null, itemToUse, null, null, -1, ingredients)));
    }

    @ZenMethod
    public static void addSummon(String id, String displayName, String pentacleId, IIngredient activation,
            IItemStack ritualDummy, int durationTicks, String entityId, int summonNumber,
            IIngredient[] ingredients) {
        applyAdd(new AddRitualAction(createDefinition(CraftTweakerUtil.normalizeId(id), displayName,
                CraftTweakerUtil.normalizeId(pentacleId), "summon", activation, ritualDummy, ritualDummy,
                durationTicks, null, null, createEntityToSummon(entityId, summonNumber), null, -1, ingredients)));
    }

    @ZenMethod
    public static void addSummonWithItemUse(String id, String displayName, String pentacleId, IIngredient activation,
            IItemStack ritualDummy, int durationTicks, String entityId, int summonNumber, IIngredient itemToUse,
            IIngredient[] ingredients) {
        applyAdd(new AddRitualAction(createDefinition(CraftTweakerUtil.normalizeId(id), displayName,
                CraftTweakerUtil.normalizeId(pentacleId), "summon", activation, ritualDummy, ritualDummy,
                durationTicks, null, itemToUse, createEntityToSummon(entityId, summonNumber), null, -1,
                ingredients)));
    }

    @ZenMethod
    public static void addSummonTag(String id, String displayName, String pentacleId, IIngredient activation,
            IItemStack ritualDummy, int durationTicks, String entityTagId, int summonNumber,
            IIngredient[] ingredients) {
        applyAdd(new AddRitualAction(createDefinition(CraftTweakerUtil.normalizeId(id), displayName,
                CraftTweakerUtil.normalizeId(pentacleId), "summon", activation, ritualDummy, ritualDummy,
                durationTicks, null, null, createEntityTagToSummon(entityTagId, summonNumber), null, -1,
                ingredients)));
    }

    @ZenMethod
    public static void addSummonTagWithItemUse(String id, String displayName, String pentacleId,
            IIngredient activation, IItemStack ritualDummy, int durationTicks, String entityTagId, int summonNumber,
            IIngredient itemToUse, IIngredient[] ingredients) {
        applyAdd(new AddRitualAction(createDefinition(CraftTweakerUtil.normalizeId(id), displayName,
                CraftTweakerUtil.normalizeId(pentacleId), "summon", activation, ritualDummy, ritualDummy,
                durationTicks, null, itemToUse, createEntityTagToSummon(entityTagId, summonNumber), null, -1,
                ingredients)));
    }

    @ZenMethod
    public static void addSpiritJob(String id, String displayName, String pentacleId, IIngredient activation,
            IItemStack result, int durationTicks, String spiritJobType, int spiritMaxAge,
            IIngredient[] ingredients) {
        applyAdd(new AddRitualAction(createDefinition(CraftTweakerUtil.normalizeId(id), displayName,
                CraftTweakerUtil.normalizeId(pentacleId), "summon_spirit_with_job", activation, result, result,
                durationTicks, null, null, null, readSpiritJobType(spiritJobType), spiritMaxAge, ingredients)));
    }

    @ZenMethod
    public static void removeById(String id) {
        CraftTweakerAPI.apply(new RemoveByIdAction(CraftTweakerUtil.normalizeId(id)));
    }

    @ZenMethod
    public static void removeByPentacle(String pentacleId) {
        CraftTweakerAPI.apply(new RemoveByPentacleAction(CraftTweakerUtil.normalizeId(pentacleId)));
    }

    private static void applyAdd(AddRitualAction action) {
        CraftTweakerAPI.apply(action);
    }

    private static RitualDefinition createDefinition(String id, String displayName, String pentacleId,
            String ritualType, IIngredient activation, IItemStack result, IItemStack ritualDummy, int durationTicks,
            RitualSacrifice sacrifice, IIngredient itemToUse, RitualEntityToSummon entityToSummon,
            SpiritJobType spiritJobType, int spiritMaxAge, IIngredient[] ingredients) {
        return new RitualDefinition(id, displayName, pentacleId, ritualType, toRitualIngredient(activation),
                new RitualResult(CraftTweakerUtil.toItemStack(result)),
                new RitualResult(CraftTweakerUtil.toItemStack(ritualDummy)), durationTicks, sacrifice,
                itemToUse == null ? null : toRitualIngredient(itemToUse), entityToSummon, spiritJobType, spiritMaxAge,
                toRitualIngredients(ingredients));
    }

    private static RitualEntityToSummon createEntityToSummon(String entityId, int summonNumber) {
        return new RitualEntityToSummon(new ResourceLocation(entityId), summonNumber);
    }

    private static RitualEntityToSummon createEntityTagToSummon(String entityTagId, int summonNumber) {
        ResourceLocation tagId = new ResourceLocation(entityTagId);
        List<ResourceLocation> entityIds = RitualEntityTagMappings.getEntities(tagId);
        if (entityIds.isEmpty()) {
            throw new IllegalArgumentException("Unknown or empty Occultism ritual entity tag: " + entityTagId);
        }
        return new RitualEntityToSummon(tagId, entityIds, summonNumber);
    }

    private static SpiritJobType readSpiritJobType(String id) {
        SpiritJobType type = SpiritJobType.fromId(id);
        if (type == null) {
            throw new IllegalArgumentException("Unknown Occultism spirit job type: " + id);
        }
        return type;
    }

    private static RitualIngredient[] toRitualIngredients(IIngredient[] ingredients) {
        if (ingredients == null || ingredients.length == 0) {
            return new RitualIngredient[0];
        }
        RitualIngredient[] result = new RitualIngredient[ingredients.length];
        for (int i = 0; i < ingredients.length; i++) {
            result[i] = toRitualIngredient(ingredients[i]);
        }
        return result;
    }

    private static RitualIngredient toRitualIngredient(IIngredient ingredient) {
        List<RitualIngredient> alternatives = new ArrayList<>();
        for (ItemStack stack : CraftTweakerUtil.toItemStacks(ingredient)) {
            alternatives.add(toRitualIngredient(stack));
        }
        if (alternatives.isEmpty()) {
            throw new IllegalArgumentException("Occultism ritual ingredient has no item examples: "
                    + (ingredient == null ? "null" : ingredient.toCommandString()));
        }
        if (alternatives.size() == 1) {
            return alternatives.get(0);
        }
        return new RitualIngredient(Collections.unmodifiableList(alternatives));
    }

    private static RitualIngredient toRitualIngredient(ItemStack stack) {
        int metadata = stack.getMetadata();
        if (metadata == OreDictionary.WILDCARD_VALUE) {
            metadata = -1;
        }
        return new RitualIngredient(stack.getItem(), stack.getCount(), metadata);
    }

    private static final class AddRitualAction implements IAction {

        private final RitualDefinition definition;

        private AddRitualAction(RitualDefinition definition) {
            this.definition = definition;
        }

        @Override
        public void apply() {
            RitualRecipeManager.addDefinition(this.definition);
        }

        @Override
        public String describe() {
            return "Adding Occultism ritual " + this.definition.getId() + " for "
                    + CraftTweakerMC.getIItemStack(this.definition.getRitualDummy().createStack()).toCommandString();
        }
    }

    private static final class RemoveByIdAction implements IAction {

        private final String id;

        private RemoveByIdAction(String id) {
            this.id = id;
        }

        @Override
        public void apply() {
            RitualRecipeManager.removeById(this.id);
        }

        @Override
        public String describe() {
            return "Removing Occultism ritual " + this.id;
        }
    }

    private static final class RemoveByPentacleAction implements IAction {

        private final String pentacleId;

        private RemoveByPentacleAction(String pentacleId) {
            this.pentacleId = pentacleId;
        }

        @Override
        public void apply() {
            RitualRecipeManager.removeIf(definition -> this.pentacleId.equals(definition.getPentacleId()));
        }

        @Override
        public String describe() {
            return "Removing Occultism rituals for pentacle " + this.pentacleId;
        }
    }
}
