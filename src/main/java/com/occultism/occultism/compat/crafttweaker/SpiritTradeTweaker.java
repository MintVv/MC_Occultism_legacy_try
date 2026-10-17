package com.occultism.occultism.compat.crafttweaker;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTradeRecipeRegistry;
import com.occultism.occultism.spirit.SpiritTradeIngredient;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.oredict.IOreDictEntry;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/** The supplied stacks are alternatives for one consumed item, not a list of costs. */
@ZenRegister
@ZenClass("mods.occultism.SpiritTrade")
public final class SpiritTradeTweaker {
    private SpiritTradeTweaker() {
    }

    @ZenMethod
    public static void addRecipe(String id, String job, IItemStack[] alternatives, IItemStack output,
            int seconds, int tradesPerRound) {
        if (alternatives == null || alternatives.length == 0) {
            throw new IllegalArgumentException("Spirit trade input alternatives must not be empty");
        }
        for (IItemStack input : alternatives) {
            if (input == null || CraftTweakerUtil.toItemStack(input).isEmpty()) {
                throw new IllegalArgumentException("Spirit trade input alternatives must not contain empty stacks");
            }
        }
        applyAdd(id, job, CraftTweakerUtil.toItemStacks(alternatives), null, output, seconds, tradesPerRound);
    }

    @ZenMethod
    public static void addOreRecipe(String id, String job, String oreName, IItemStack output,
            int seconds, int tradesPerRound) {
        if (oreName == null || oreName.trim().isEmpty()) {
            throw new IllegalArgumentException("Spirit trade ore name must not be empty");
        }
        applyAdd(id, job, Collections.emptyList(), oreName, output, seconds, tradesPerRound);
    }

    /** Separate costs; amounts repeat a cost, unlike addRecipe's array of alternatives. */
    @ZenMethod
    public static void addShapelessRecipe(String id, String job, IIngredient[] costs, IItemStack output,
            int seconds, int tradesPerRound) {
        SpiritJobType type = SpiritJobType.fromId(job);
        if (!SpiritTradeRecipeRegistry.isTradeJob(type) || costs == null || costs.length == 0
                || costs.length > SpiritTradeRecipeRegistry.MAX_INGREDIENTS) {
            throw new IllegalArgumentException("Invalid spirit trade job or costs");
        }
        List<SpiritTradeIngredient> ingredients = new ArrayList<>();
        for (IIngredient cost : costs) {
            if (cost == null || cost.getAmount() < 1
                    || cost.getAmount() > SpiritTradeRecipeRegistry.MAX_INGREDIENTS - ingredients.size()
                    || cost.hasTransformers() || cost.hasNewTransformers()) {
                throw new IllegalArgumentException("Trade costs must total 1..9 items without transformers");
            }
            SpiritTradeIngredient ingredient;
            if (cost instanceof IOreDictEntry) {
                ingredient = SpiritTradeIngredient.ore(((IOreDictEntry) cost).getName());
            } else if (cost instanceof IItemStack) {
                ingredient = SpiritTradeIngredient.items(Collections.singletonList(
                        CraftTweakerUtil.toItemStack((IItemStack) cost)));
            } else {
                throw new IllegalArgumentException("Use plain item stacks or ore entries as trade costs");
            }
            for (int count = 0; count < cost.getAmount(); count++) {
                ingredients.add(ingredient);
            }
        }
        ItemStack result = CraftTweakerUtil.toItemStack(output);
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                SpiritTradeRecipeRegistry.registerIngredients(id, type, ingredients, result, seconds, tradesPerRound);
            }

            @Override
            public String describe() {
                return "Registering Occultism shapeless spirit trade " + id;
            }
        });
    }

    private static void applyAdd(String id, String job, List<ItemStack> alternatives, String oreName,
            IItemStack output, int seconds, int tradesPerRound) {
        SpiritJobType type = SpiritJobType.fromId(job);
        if (!SpiritTradeRecipeRegistry.isTradeJob(type)) {
            throw new IllegalArgumentException("Not a fixed trader job: " + job);
        }
        ItemStack result = CraftTweakerUtil.toItemStack(output);
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                SpiritTradeRecipeRegistry.register(id, type, alternatives, oreName, result, seconds, tradesPerRound);
            }

            @Override
            public String describe() {
                return "Registering Occultism spirit trade " + id;
            }
        });
    }

    @ZenMethod
    public static void removeRecipe(String id) {
        if (SpiritTradeRecipeRegistry.canonicalId(id) == null) {
            throw new IllegalArgumentException("Invalid spirit trade ID: " + id);
        }
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                SpiritTradeRecipeRegistry.remove(id);
            }

            @Override
            public String describe() {
                return "Removing Occultism spirit trade " + id;
            }
        });
    }
}
