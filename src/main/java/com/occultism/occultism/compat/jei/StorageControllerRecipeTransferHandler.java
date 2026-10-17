package com.occultism.occultism.compat.jei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.occultism.occultism.container.ContainerStorageController;
import com.occultism.occultism.container.StorageRecipeLayout;
import com.occultism.occultism.network.ModNetworking;

import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

/** Transfers vanilla 3x3 crafting recipes into the storage terminal. */
public final class StorageControllerRecipeTransferHandler
        implements IRecipeTransferHandler<ContainerStorageController> {

    private static final int CRAFTING_INPUT_OFFSET = 1;
    private static final int CRAFTING_SLOT_COUNT = 9;
    private static final int MAX_ALTERNATIVES_PER_SLOT = 5;

    private final IRecipeTransferHandlerHelper handlerHelper;

    public StorageControllerRecipeTransferHandler(IRecipeTransferHandlerHelper handlerHelper) {
        this.handlerHelper = handlerHelper;
    }

    @Override
    public Class<ContainerStorageController> getContainerClass() {
        return ContainerStorageController.class;
    }

    @Override
    public IRecipeTransferError transferRecipe(ContainerStorageController container, IRecipeLayout recipeLayout,
            EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        if (recipeLayout != null && recipeLayout.getItemStacks().getGuiIngredients().entrySet().stream()
                .anyMatch(entry -> entry.getValue().isInput() && hasItems(entry.getValue().getAllIngredients())
                        && (entry.getKey() < 1 || entry.getKey() > CRAFTING_SLOT_COUNT))) {
            return this.handlerHelper.createUserErrorWithTooltip("jei.occultism.error.recipe_too_large");
        }
        NBTTagCompound payload = createRecipePayload(recipeLayout);
        if (payload.getKeySet().isEmpty()) {
            return this.handlerHelper.createUserErrorWithTooltip("jei.occultism.error.recipe_no_items");
        }
        if (doTransfer) {
            ResourceLocation recipeId = findRegisteredRecipeId(recipeLayout, CraftingManager.REGISTRY);
            if (recipeId != null) {
                ModNetworking.sendStorageRecipeById(recipeId);
            } else {
                ModNetworking.sendStorageRecipe(payload);
            }
        }
        return null;
    }

    /**
     * JEI 4 does not expose the wrapper/recipe ID through IRecipeLayout. Use only an unambiguous full signature;
     * never guess an ID from the output alone, the displayed alternative, or a truncated network payload.
     */
    static ResourceLocation findRegisteredRecipeId(IRecipeLayout layout, Iterable<IRecipe> recipes) {
        if (layout == null) {
            return null;
        }
        Map<Integer, ? extends IGuiIngredient<ItemStack>> slots = layout.getItemStacks().getGuiIngredients();
        IGuiIngredient<ItemStack> output = slots.get(0);
        if (output == null || output.isInput() || !hasItems(output.getAllIngredients())) {
            return null;
        }
        Map<Integer, List<ItemStack>> signature = new java.util.HashMap<>();
        for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : slots.entrySet()) {
            IGuiIngredient<ItemStack> ingredient = entry.getValue();
            if (ingredient != null) {
                signature.put(entry.getKey(), new ArrayList<>(ingredient.getAllIngredients()));
            }
        }
        return findRegisteredRecipeId(signature, recipes);
    }

    /**
     * Pure-data resolver used by the JEI adapter and tests. Keeping this overload independent from JEI GUI
     * interfaces makes the recipe identity policy reusable without initializing client-only rendering classes.
     */
    static ResourceLocation findRegisteredRecipeId(Map<Integer, List<ItemStack>> slots, Iterable<IRecipe> recipes) {
        if (slots == null) {
            return null;
        }
        List<ItemStack> output = slots.get(0);
        if (!hasItems(output)) {
            return null;
        }
        ResourceLocation found = null;
        for (IRecipe recipe : recipes) {
            if (recipe.getRegistryName() == null || !hasMatchingOutput(recipe, output)) {
                continue;
            }
            NonNullList<Ingredient> matrix = StorageRecipeLayout.getMatrixIngredients(recipe);
            if (matrix == null) {
                continue;
            }
            boolean matches = true;
            for (int index = 0; index < matrix.size(); index++) {
                List<ItemStack> displayed = slots.get(index + CRAFTING_INPUT_OFFSET);
                if (displayed == null) {
                    displayed = Collections.emptyList();
                }
                List<ItemStack> expected = matrix.get(index) == Ingredient.EMPTY ? Collections.emptyList()
                        : Arrays.asList(matrix.get(index).getMatchingStacks());
                if (!sameAlternatives(expected, displayed)) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                if (found != null) {
                    return null;
                }
                found = recipe.getRegistryName();
            }
        }
        return found;
    }

    private static boolean hasMatchingOutput(IRecipe recipe, List<ItemStack> displayed) {
        try {
            ItemStack output = recipe.getRecipeOutput();
            return output != null && !output.isEmpty() && hasItems(displayed)
                    && displayed.stream().filter(stack -> stack != null && !stack.isEmpty())
                            .allMatch(stack -> ItemStack.areItemStacksEqual(output, stack));
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static boolean hasItems(List<ItemStack> stacks) {
        return stacks.stream().anyMatch(stack -> stack != null && !stack.isEmpty());
    }

    private static boolean sameAlternatives(List<ItemStack> left, List<ItemStack> right) {
        List<ItemStack> uniqueLeft = uniqueAlternatives(left);
        List<ItemStack> uniqueRight = uniqueAlternatives(right);
        return uniqueLeft.size() == uniqueRight.size() && uniqueLeft.stream().allMatch(
                stack -> uniqueRight.stream().anyMatch(other -> sameItemAndTag(stack, other)));
    }

    private static List<ItemStack> uniqueAlternatives(List<ItemStack> stacks) {
        List<ItemStack> unique = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (stack != null && !stack.isEmpty()
                    && unique.stream().noneMatch(existing -> sameItemAndTag(stack, existing))) {
                unique.add(stack);
            }
        }
        return unique;
    }

    private static boolean sameItemAndTag(ItemStack left, ItemStack right) {
        return ItemStack.areItemsEqual(left, right) && ItemStack.areItemStackTagsEqual(left, right);
    }

    /**
     * JEI's vanilla crafting layout reserves ingredient index 0 for the result;
     * indices 1 through 9 map directly to the storage terminal's 3x3 matrix.
     */
    static NBTTagCompound createRecipePayload(IRecipeLayout recipeLayout) {
        NBTTagCompound payload = new NBTTagCompound();
        if (recipeLayout == null) {
            return payload;
        }

        Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = recipeLayout.getItemStacks()
                .getGuiIngredients();
        for (int matrixSlot = 0; matrixSlot < CRAFTING_SLOT_COUNT; matrixSlot++) {
            IGuiIngredient<ItemStack> ingredient = ingredients.get(matrixSlot + CRAFTING_INPUT_OFFSET);
            if (ingredient == null || !ingredient.isInput()) {
                continue;
            }

            NBTTagList alternatives = new NBTTagList();
            int written = 0;
            for (ItemStack stack : ingredient.getAllIngredients()) {
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                alternatives.appendTag(stack.writeToNBT(new NBTTagCompound()));
                written++;
                if (written >= MAX_ALTERNATIVES_PER_SLOT) {
                    break;
                }
            }
            if (alternatives.tagCount() > 0) {
                payload.setTag("s" + matrixSlot, alternatives);
            }
        }
        return payload;
    }
}
