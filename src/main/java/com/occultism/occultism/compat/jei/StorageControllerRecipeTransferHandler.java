package com.occultism.occultism.compat.jei;

import java.util.Map;

import com.occultism.occultism.container.ContainerStorageController;
import com.occultism.occultism.network.ModNetworking;

import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

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
        NBTTagCompound payload = createRecipePayload(recipeLayout);
        if (payload.getKeySet().isEmpty()) {
            return this.handlerHelper.createUserErrorWithTooltip("jei.occultism.error.recipe_no_items");
        }
        if (doTransfer) {
            ModNetworking.sendStorageRecipe(payload);
        }
        return null;
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
