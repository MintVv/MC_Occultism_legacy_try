package com.occultism.occultism.compat.crafttweaker;

import java.util.ArrayList;
import java.util.List;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import com.occultism.occultism.mineshaft.DimensionalMineshaftRecipe;
import com.occultism.occultism.mineshaft.DimensionalMineshaftRecipe.WeightedOutput;

@ZenRegister
@ZenClass("mods.occultism.DimensionalMineshaft")
public class DimensionalMineshaftTweaker {

    private DimensionalMineshaftTweaker() {
    }

    @ZenMethod
    public static void addRecipe(IItemStack miner, IItemStack[] outputs, int[] weights) {
        CraftTweakerAPI.apply(new AddRecipeAction(CraftTweakerUtil.toItemStack(miner),
                toWeightedOutputs(outputs, weights)));
    }

    @ZenMethod
    public static void addOutput(IItemStack miner, IItemStack output, int weight) {
        CraftTweakerAPI.apply(new AddOutputAction(CraftTweakerUtil.toItemStack(miner),
                new WeightedOutput(CraftTweakerUtil.toItemStack(output), weight)));
    }

    @ZenMethod
    public static void removeByMiner(IIngredient miner) {
        CraftTweakerAPI.apply(new RemoveByMinerAction(CraftTweakerUtil.toItemStacks(miner)));
    }

    @ZenMethod
    public static void removeByOutput(IIngredient output) {
        CraftTweakerAPI.apply(new RemoveByOutputAction(CraftTweakerUtil.toItemStacks(output)));
    }

    private static List<WeightedOutput> toWeightedOutputs(IItemStack[] outputs, int[] weights) {
        if (outputs == null || weights == null || outputs.length != weights.length) {
            throw new IllegalArgumentException("Occultism dimensional mineshaft outputs and weights must match.");
        }
        List<WeightedOutput> result = new ArrayList<>();
        for (int i = 0; i < outputs.length; i++) {
            result.add(new WeightedOutput(CraftTweakerUtil.toItemStack(outputs[i]), weights[i]));
        }
        return result;
    }

    private static final class AddRecipeAction implements IAction {

        private final ItemStack miner;
        private final List<WeightedOutput> outputs;

        private AddRecipeAction(ItemStack miner, List<WeightedOutput> outputs) {
            this.miner = miner;
            this.outputs = outputs;
        }

        @Override
        public void apply() {
            DimensionalMineshaftRecipe.addRecipe(this.miner, this.outputs);
        }

        @Override
        public String describe() {
            return "Adding Occultism dimensional mineshaft recipe for miner "
                    + CraftTweakerMC.getIItemStack(this.miner).toCommandString();
        }
    }

    private static final class AddOutputAction implements IAction {

        private final ItemStack miner;
        private final WeightedOutput output;

        private AddOutputAction(ItemStack miner, WeightedOutput output) {
            this.miner = miner;
            this.output = output;
        }

        @Override
        public void apply() {
            DimensionalMineshaftRecipe.addOutput(this.miner, this.output);
        }

        @Override
        public String describe() {
            return "Adding Occultism dimensional mineshaft output for miner "
                    + CraftTweakerMC.getIItemStack(this.miner).toCommandString();
        }
    }

    private static final class RemoveByMinerAction implements IAction {

        private final List<ItemStack> miners;

        private RemoveByMinerAction(List<ItemStack> miners) {
            this.miners = miners;
        }

        @Override
        public void apply() {
            DimensionalMineshaftRecipe.removeByMiner(this.miners);
        }

        @Override
        public String describe() {
            return "Removing Occultism dimensional mineshaft recipes by miner";
        }
    }

    private static final class RemoveByOutputAction implements IAction {

        private final List<ItemStack> outputs;

        private RemoveByOutputAction(List<ItemStack> outputs) {
            this.outputs = outputs;
        }

        @Override
        public void apply() {
            DimensionalMineshaftRecipe.removeOutputs(this.outputs);
        }

        @Override
        public String describe() {
            return "Removing Occultism dimensional mineshaft outputs";
        }
    }
}
