package com.occultism.occultism.compat.crafttweaker;

import java.util.List;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import com.occultism.occultism.ritual.SpiritFireRecipeRegistry;

@ZenRegister
@ZenClass("mods.occultism.SpiritFire")
public class SpiritFireTweaker {

    private SpiritFireTweaker() {
    }

    @ZenMethod
    public static void addRecipe(IIngredient input, IItemStack output) {
        CraftTweakerAPI.apply(new AddRecipeAction(CraftTweakerUtil.toItemStacks(input),
                CraftTweakerUtil.toItemStack(output)));
    }

    @ZenMethod
    public static void removeByInput(IIngredient input) {
        CraftTweakerAPI.apply(new RemoveByInputAction(CraftTweakerUtil.toItemStacks(input)));
    }

    @ZenMethod
    public static void removeByOutput(IIngredient output) {
        CraftTweakerAPI.apply(new RemoveByOutputAction(CraftTweakerUtil.toItemStacks(output)));
    }

    private static final class AddRecipeAction implements IAction {

        private final List<net.minecraft.item.ItemStack> inputs;
        private final net.minecraft.item.ItemStack output;

        private AddRecipeAction(List<net.minecraft.item.ItemStack> inputs, net.minecraft.item.ItemStack output) {
            this.inputs = inputs;
            this.output = output;
        }

        @Override
        public void apply() {
            SpiritFireRecipeRegistry.addRecipe(this.inputs, this.output);
        }

        @Override
        public String describe() {
            return "Adding Occultism spirit fire recipe for " + CraftTweakerMC.getIItemStack(this.output)
                    .toCommandString();
        }
    }

    private static final class RemoveByInputAction implements IAction {

        private final List<net.minecraft.item.ItemStack> inputs;

        private RemoveByInputAction(List<net.minecraft.item.ItemStack> inputs) {
            this.inputs = inputs;
        }

        @Override
        public void apply() {
            SpiritFireRecipeRegistry.removeByInputs(this.inputs);
        }

        @Override
        public String describe() {
            return "Removing Occultism spirit fire recipes by input";
        }
    }

    private static final class RemoveByOutputAction implements IAction {

        private final List<net.minecraft.item.ItemStack> outputs;

        private RemoveByOutputAction(List<net.minecraft.item.ItemStack> outputs) {
            this.outputs = outputs;
        }

        @Override
        public void apply() {
            SpiritFireRecipeRegistry.removeByOutputs(this.outputs);
        }

        @Override
        public String describe() {
            return "Removing Occultism spirit fire recipes by output";
        }
    }
}
