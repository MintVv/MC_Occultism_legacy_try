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

import com.occultism.occultism.spirit.SpiritJobRecipe;
import com.occultism.occultism.spirit.SpiritJobType;

@ZenRegister
@ZenClass("mods.occultism.SpiritJob")
public class SpiritJobTweaker {

    private SpiritJobTweaker() {
    }

    @ZenMethod
    public static void addCrushing(IIngredient input, IItemStack output) {
        addRecipe(SpiritJobType.CRUSHER, input, output);
    }

    @ZenMethod
    public static void addCrystallizing(IIngredient input, IItemStack output) {
        addRecipe(SpiritJobType.CRYSTALLIZER, input, output);
    }

    @ZenMethod
    public static void removeCrushing(IIngredient input) {
        removeByInput(SpiritJobType.CRUSHER, input);
    }

    @ZenMethod
    public static void removeCrystallizing(IIngredient input) {
        removeByInput(SpiritJobType.CRYSTALLIZER, input);
    }

    @ZenMethod
    public static void removeCrushingByOutput(IIngredient output) {
        removeByOutput(SpiritJobType.CRUSHER, output);
    }

    @ZenMethod
    public static void removeCrystallizingByOutput(IIngredient output) {
        removeByOutput(SpiritJobType.CRYSTALLIZER, output);
    }

    private static void addRecipe(SpiritJobType type, IIngredient input, IItemStack output) {
        CraftTweakerAPI.apply(new AddRecipeAction(type, CraftTweakerUtil.toItemStacks(input),
                CraftTweakerUtil.toItemStack(output)));
    }

    private static void removeByInput(SpiritJobType type, IIngredient input) {
        CraftTweakerAPI.apply(new RemoveByInputAction(type, CraftTweakerUtil.toItemStacks(input)));
    }

    private static void removeByOutput(SpiritJobType type, IIngredient output) {
        CraftTweakerAPI.apply(new RemoveByOutputAction(type, CraftTweakerUtil.toItemStacks(output)));
    }

    private static final class AddRecipeAction implements IAction {

        private final SpiritJobType type;
        private final List<net.minecraft.item.ItemStack> inputs;
        private final net.minecraft.item.ItemStack output;

        private AddRecipeAction(SpiritJobType type, List<net.minecraft.item.ItemStack> inputs,
                net.minecraft.item.ItemStack output) {
            this.type = type;
            this.inputs = inputs;
            this.output = output;
        }

        @Override
        public void apply() {
            SpiritJobRecipe.addRecipe(this.type, this.inputs, this.output);
        }

        @Override
        public String describe() {
            return "Adding Occultism " + this.type.getId() + " recipe for "
                    + CraftTweakerMC.getIItemStack(this.output).toCommandString();
        }
    }

    private static final class RemoveByInputAction implements IAction {

        private final SpiritJobType type;
        private final List<net.minecraft.item.ItemStack> inputs;

        private RemoveByInputAction(SpiritJobType type, List<net.minecraft.item.ItemStack> inputs) {
            this.type = type;
            this.inputs = inputs;
        }

        @Override
        public void apply() {
            SpiritJobRecipe.removeByInputs(this.type, this.inputs);
        }

        @Override
        public String describe() {
            return "Removing Occultism " + this.type.getId() + " recipes by input";
        }
    }

    private static final class RemoveByOutputAction implements IAction {

        private final SpiritJobType type;
        private final List<net.minecraft.item.ItemStack> outputs;

        private RemoveByOutputAction(SpiritJobType type, List<net.minecraft.item.ItemStack> outputs) {
            this.type = type;
            this.outputs = outputs;
        }

        @Override
        public void apply() {
            SpiritJobRecipe.removeByOutputs(this.type, this.outputs);
        }

        @Override
        public String describe() {
            return "Removing Occultism " + this.type.getId() + " recipes by output";
        }
    }
}
