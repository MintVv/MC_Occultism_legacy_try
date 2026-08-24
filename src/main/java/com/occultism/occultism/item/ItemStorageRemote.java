package com.occultism.occultism.item;

import com.occultism.occultism.Occultism;
import com.occultism.occultism.api.common.blockentity.IStorageController;
import com.occultism.occultism.api.common.data.GlobalBlockPos;
import com.occultism.occultism.gui.ModGuiHandler;
import com.occultism.occultism.tileentity.TileEntityStorageController;
import com.occultism.occultism.util.ItemNBTUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.common.DimensionManager;

public class ItemStorageRemote extends Item {

    private static final String TAG_CONTROLLER_X = "StorageControllerX";
    private static final String TAG_CONTROLLER_Y = "StorageControllerY";
    private static final String TAG_CONTROLLER_Z = "StorageControllerZ";
    private static final String TAG_CONTROLLER_DIMENSION = "StorageControllerDimension";
    /** Upstream-compatible nested global position; flat keys remain for 1.12 saves. */
    public static final String TAG_LINKED_STORAGE_CONTROLLER = "linkedStorageController";
    /** Upstream terminal state persisted on the remote item itself. */
    public static final String TAG_CRAFTING_MATRIX = "craftingMatrix";
    public static final String TAG_ORDER_STACK = "orderStack";
    /** 1.12 stores ItemStack.Count as a byte; retain the configured super-stack size. */
    public static final String TAG_REAL_SIZE = "RealSize";
    public static final int CRAFTING_MATRIX_SIZE = 9;

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        TileEntity tile = worldIn.getTileEntity(pos);
        if (!(tile instanceof TileEntityStorageController) || !player.isSneaking()) {
            return EnumActionResult.PASS;
        }
        if (!worldIn.isRemote) {
            setLinkedController(stack, worldIn, pos);
            player.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.linked"), true);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (worldIn.isRemote) {
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        BlockPos pos = getLinkedControllerPos(stack);
        if (pos == null) {
            playerIn.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.unlinked"), true);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        int dimension = getLinkedDimension(stack);
        World targetWorld = DimensionManager.getWorld(dimension);
        if (targetWorld == null || !targetWorld.isBlockLoaded(pos)) {
            playerIn.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.not_loaded"), true);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        TileEntity tile = targetWorld.getTileEntity(pos);
        if (!(tile instanceof TileEntityStorageController) || tile.isInvalid()) {
            playerIn.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.missing"), true);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        int selectedSlot = findInventorySlot(playerIn, stack, handIn);
        if (selectedSlot < 0) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        // The GUI handler resolves the linked controller from this exact item
        // slot.  Passing the player's world keeps the slot identity stable
        // even when the controller lives in another loaded dimension.
        playerIn.openGui(Occultism.INSTANCE, ModGuiHandler.STORAGE_REMOTE, worldIn,
                selectedSlot, 0, 0);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    private static int findInventorySlot(EntityPlayer player, ItemStack held, EnumHand hand) {
        if (player == null || player.inventory == null) {
            return -1;
        }
        if (hand == EnumHand.MAIN_HAND) {
            int selected = player.inventory.currentItem;
            if (selected >= 0 && selected < player.inventory.getSizeInventory()
                    && player.inventory.getStackInSlot(selected) == held) {
                return selected;
            }
        }
        for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
            if (player.inventory.getStackInSlot(slot) == held) {
                return slot;
            }
        }
        return -1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        String name = ItemNBTUtil.getBoundSpiritName(stack);
        if (!name.isEmpty()) {
            tooltip.add(I18n.format(getTranslationKey() + ".tooltip.spirit", ItemNBTUtil.formatSpiritName(name)));
        }

        BlockPos pos = getLinkedControllerPos(stack);
        if (pos == null) {
            tooltip.add(I18n.format(getTranslationKey() + ".tooltip.unlinked"));
            return;
        }
        tooltip.add(I18n.format(getTranslationKey() + ".tooltip.linked",
                getLinkedDimension(stack), pos.getX(), pos.getY(), pos.getZ()));
    }

    private static void setLinkedController(ItemStack stack, World world, BlockPos pos) {
        setLinkedController(stack, new GlobalBlockPos(pos, world));
    }

    /** Writes both the legacy 1.12 keys and the nested upstream-compatible link. */
    public static void setLinkedController(ItemStack stack, GlobalBlockPos reference) {
        if (stack == null || stack.isEmpty() || reference == null || !reference.isPresent()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        BlockPos pos = reference.getPos();
        tag.setInteger(TAG_CONTROLLER_X, pos.getX());
        tag.setInteger(TAG_CONTROLLER_Y, pos.getY());
        tag.setInteger(TAG_CONTROLLER_Z, pos.getZ());
        tag.setInteger(TAG_CONTROLLER_DIMENSION, reference.getDimension());
        tag.setTag(TAG_LINKED_STORAGE_CONTROLLER,
                reference.serializeNBT());
        stack.setTagCompound(tag);
    }

    /** Clears all remote link schemas without touching unrelated item NBT. */
    public static void clearLinkedController(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        tag.removeTag(TAG_CONTROLLER_X);
        tag.removeTag(TAG_CONTROLLER_Y);
        tag.removeTag(TAG_CONTROLLER_Z);
        tag.removeTag(TAG_CONTROLLER_DIMENSION);
        tag.removeTag(TAG_LINKED_STORAGE_CONTROLLER);
        stack.setTagCompound(tag.isEmpty() ? null : tag);
    }

    /** Returns the linked reference without resolving a dimension or loading a chunk. */
    @Nullable
    public static GlobalBlockPos getLinkedControllerPosition(ItemStack stack) {
        NBTTagCompound tag = stack == null ? null : stack.getTagCompound();
        if (tag == null) {
            return null;
        }
        if (tag.hasKey(TAG_LINKED_STORAGE_CONTROLLER, 10)) {
            GlobalBlockPos nested = GlobalBlockPos.from(tag.getCompoundTag(TAG_LINKED_STORAGE_CONTROLLER));
            // The nested reference is authoritative when present.  Falling
            // back to flat keys after a malformed nested payload could pair a
            // stale position/dimension with a newer reference and bypass the
            // expected loaded-target validation.
            return nested.isPresent() ? nested : null;
        }
        if (!tag.hasKey(TAG_CONTROLLER_X, 3) || !tag.hasKey(TAG_CONTROLLER_Y, 3)
                || !tag.hasKey(TAG_CONTROLLER_Z, 3) || !tag.hasKey(TAG_CONTROLLER_DIMENSION, 3)) {
            return null;
        }
        return new GlobalBlockPos(new BlockPos(tag.getInteger(TAG_CONTROLLER_X), tag.getInteger(TAG_CONTROLLER_Y),
                tag.getInteger(TAG_CONTROLLER_Z)), tag.getInteger(TAG_CONTROLLER_DIMENSION));
    }

    /**
     * Resolves only an already loaded controller.  This mirrors the upstream
     * remote item contract and never forces a dimension or chunk load.
     */
    @Nullable
    public static IStorageController getStorageController(ItemStack stack, @Nullable World world) {
        GlobalBlockPos reference = getLinkedControllerPosition(stack);
        if (reference == null || reference.getPos() == null) {
            return null;
        }
        World targetWorld = world != null && world.provider.getDimension() == reference.getDimension()
                ? world : DimensionManager.getWorld(reference.getDimension());
        if (targetWorld == null || !targetWorld.isBlockLoaded(reference.getPos())) {
            return null;
        }
        TileEntity tile = targetWorld.getTileEntity(reference.getPos());
        return tile instanceof IStorageController ? (IStorageController) tile : null;
    }

    @Nullable
    private static BlockPos getLinkedControllerPos(ItemStack stack) {
        GlobalBlockPos reference = getLinkedControllerPosition(stack);
        return reference == null ? null : reference.getPos();
    }

    private static int getLinkedDimension(ItemStack stack) {
        GlobalBlockPos reference = getLinkedControllerPosition(stack);
        return reference == null ? 0 : reference.getDimension();
    }

    /**
     * Returns whether this stack is bound to the supplied controller.  The
     * server uses this check when accepting storage-terminal setting packets;
     * callers must still verify that the target tile exists and is usable.
     */
    public static boolean isLinkedToController(ItemStack stack, World targetWorld, BlockPos targetPos) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ItemStorageRemote)
                || targetWorld == null || targetPos == null) {
            return false;
        }
        GlobalBlockPos linked = getLinkedControllerPosition(stack);
        return linked != null && linked.getPos().equals(targetPos)
                && linked.getDimension() == targetWorld.provider.getDimension();
    }

    /** Reads the nine terminal crafting slots from a Remote ItemStack. */
    public static List<ItemStack> getCraftingMatrix(ItemStack stack) {
        List<ItemStack> result = new ArrayList<>(Collections.nCopies(CRAFTING_MATRIX_SIZE, ItemStack.EMPTY));
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()
                || !stack.getTagCompound().hasKey(TAG_CRAFTING_MATRIX, 9)) {
            return result;
        }
        NBTTagList encoded = stack.getTagCompound().getTagList(TAG_CRAFTING_MATRIX, 10);
        int limit = Math.min(CRAFTING_MATRIX_SIZE, encoded.tagCount());
        for (int slot = 0; slot < limit; slot++) {
            try {
                ItemStack decoded = new ItemStack(encoded.getCompoundTagAt(slot));
                if (encoded.getCompoundTagAt(slot).hasKey(TAG_REAL_SIZE, 3)) {
                    decoded.setCount(Math.max(0, encoded.getCompoundTagAt(slot).getInteger(TAG_REAL_SIZE)));
                }
                result.set(slot, decoded.isEmpty() ? ItemStack.EMPTY : decoded);
            } catch (RuntimeException ignored) {
                result.set(slot, ItemStack.EMPTY);
            }
        }
        return result;
    }

    /** Persists the terminal crafting slots without changing unrelated Remote NBT. */
    public static void setCraftingMatrix(ItemStack stack, List<ItemStack> matrix) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        NBTTagList encoded = new NBTTagList();
        for (int slot = 0; slot < CRAFTING_MATRIX_SIZE; slot++) {
            ItemStack value = matrix != null && slot < matrix.size() ? matrix.get(slot) : ItemStack.EMPTY;
            ItemStack safeValue = value == null || value.isEmpty() ? ItemStack.EMPTY : value;
            NBTTagCompound item = safeValue.writeToNBT(new NBTTagCompound());
            item.setInteger(TAG_REAL_SIZE, safeValue.getCount());
            encoded.appendTag(item);
        }
        tag.setTag(TAG_CRAFTING_MATRIX, encoded);
        stack.setTagCompound(tag);
    }

    /** Reads the terminal order slot from a Remote ItemStack. */
    public static ItemStack getOrderStack(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()
                || !stack.getTagCompound().hasKey(TAG_ORDER_STACK, 10)) {
            return ItemStack.EMPTY;
        }
        try {
            NBTTagCompound encoded = stack.getTagCompound().getCompoundTag(TAG_ORDER_STACK);
            ItemStack order = new ItemStack(encoded);
            if (encoded.hasKey(TAG_REAL_SIZE, 3)) {
                order.setCount(Math.max(0, encoded.getInteger(TAG_REAL_SIZE)));
            }
            return order;
        } catch (RuntimeException ignored) {
            return ItemStack.EMPTY;
        }
    }

    /** Persists or clears the terminal order slot on a Remote ItemStack. */
    public static void setOrderStack(ItemStack stack, ItemStack order) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        if (order == null || order.isEmpty()) {
            tag.removeTag(TAG_ORDER_STACK);
        } else {
            NBTTagCompound encoded = order.writeToNBT(new NBTTagCompound());
            encoded.setInteger(TAG_REAL_SIZE, order.getCount());
            tag.setTag(TAG_ORDER_STACK, encoded);
        }
        stack.setTagCompound(tag.isEmpty() ? null : tag);
    }
}
