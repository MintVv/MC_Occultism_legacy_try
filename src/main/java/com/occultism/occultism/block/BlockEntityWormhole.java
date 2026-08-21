package com.occultism.occultism.block;

import java.util.UUID;

import javax.annotation.Nullable;

import com.occultism.occultism.registry.ModItems;
import com.occultism.occultism.tileentity.TileEntityEntityWormhole;
import com.occultism.occultism.util.ItemNBTUtil;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockEntityWormhole extends BlockModelOnly {

    private static final String TELEPORT_COOLDOWN_KEY = "OccultismEntityWormholeCooldown";
    private static final int TELEPORT_COOLDOWN_TICKS = 40;
    private static final int[][] SAFE_TELEPORT_OFFSETS = new int[][] {
            {0, 0, 0},
            {0, 0, -1},
            {-1, 0, 0},
            {0, 0, 1},
            {1, 0, 0},
            {-1, 0, -1},
            {1, 0, -1},
            {-1, 0, 1},
            {1, 0, 1},
            {0, 1, 0},
            {0, 1, -1},
            {-1, 1, 0},
            {0, 1, 1},
            {1, 1, 0},
            {-1, 1, -1},
            {1, 1, -1},
            {-1, 1, 1},
            {1, 1, 1}
    };

    public BlockEntityWormhole() {
        super(Material.ROCK);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityEntityWormhole();
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (!(tile instanceof TileEntityEntityWormhole)) {
            return false;
        }

        TileEntityEntityWormhole wormhole = (TileEntityEntityWormhole) tile;
        ItemStack held = playerIn.getHeldItem(hand);

        if (playerIn.isSneaking() && wormhole.hasCompass()) {
            if (!worldIn.isRemote) {
                ItemStack removed = wormhole.removeCompass();
                if (!playerIn.inventory.addItemStackToInventory(removed)) {
                    InventoryHelper.spawnItemStack(worldIn, playerIn.posX, playerIn.posY, playerIn.posZ, removed);
                }
                worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 0.8F,
                        1.0F);
            }
            return true;
        }

        if (!held.isEmpty() && this.isAcceptedCompass(held) && !wormhole.hasCompass()) {
            if (!worldIn.isRemote) {
                ItemStack inserted = held.copy();
                inserted.setCount(1);
                wormhole.setCompass(inserted);
                held.shrink(1);
                worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, SoundCategory.BLOCKS, 0.8F,
                        1.0F);
            }
            return true;
        }

        return false;
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        if (worldIn.isRemote || entityIn.isDead || entityIn.isRiding() || entityIn.isBeingRidden()) {
            return;
        }
        if (this.isOnCooldown(worldIn, entityIn)) {
            return;
        }

        TileEntity tile = worldIn.getTileEntity(pos);
        if (!(tile instanceof TileEntityEntityWormhole)) {
            return;
        }

        ItemStack compass = ((TileEntityEntityWormhole) tile).getCompass();
        if (compass.isEmpty()) {
            return;
        }

        BlockPos target = this.findTarget(worldIn, compass);
        if (target == null) {
            if (entityIn instanceof EntityPlayer) {
                ((EntityPlayer) entityIn).sendStatusMessage(
                        new TextComponentTranslation("occultism.entity_wormhole.message.no_target"), true);
            }
            this.setCooldown(worldIn, entityIn);
            return;
        }

        BlockPos destination = this.findSafeDestination(worldIn, entityIn, target);
        if (destination == null) {
            this.setCooldown(worldIn, entityIn);
            return;
        }

        this.teleportEntity(worldIn, entityIn, destination);
        this.setCooldown(worldIn, entityIn);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityEntityWormhole) {
            ItemStack compass = ((TileEntityEntityWormhole) tile).removeCompass();
            if (!compass.isEmpty()) {
                InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), compass);
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

    private boolean isAcceptedCompass(ItemStack stack) {
        return stack.getItem() == Items.COMPASS || stack.getItem() == ModItems.VITALITY_COMPASS;
    }

    @Nullable
    private BlockPos findTarget(World world, ItemStack compass) {
        if (compass.getItem() == Items.COMPASS) {
            return world.getSpawnPoint();
        }
        if (compass.getItem() == ModItems.VITALITY_COMPASS) {
            UUID uuid = ItemNBTUtil.getSpiritEntityUUID(compass);
            if (uuid == null) {
                return null;
            }
            for (Entity entity : world.getLoadedEntityList()) {
                if (uuid.equals(entity.getUniqueID()) && !entity.isDead) {
                    return entity.getPosition();
                }
            }
        }
        return null;
    }

    @Nullable
    private BlockPos findSafeDestination(World world, Entity entity, BlockPos target) {
        for (int[] offset : SAFE_TELEPORT_OFFSETS) {
            BlockPos candidate = target.add(offset[0], offset[1], offset[2]);
            if (this.isSafeDestination(world, entity, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean isSafeDestination(World world, Entity entity, BlockPos feetPos) {
        if (!world.isBlockLoaded(feetPos)) {
            return false;
        }
        AxisAlignedBB currentBox = entity.getEntityBoundingBox();
        AxisAlignedBB targetBox = currentBox.offset(
                feetPos.getX() + 0.5D - entity.posX,
                feetPos.getY() - entity.posY,
                feetPos.getZ() + 0.5D - entity.posZ);
        return world.getCollisionBoxes(entity, targetBox).isEmpty() && !world.containsAnyLiquid(targetBox);
    }

    private void teleportEntity(World world, Entity entity, BlockPos destination) {
        double x = destination.getX() + 0.5D;
        double y = destination.getY();
        double z = destination.getZ() + 0.5D;
        entity.dismountRidingEntity();
        entity.motionX = 0.0D;
        entity.motionY = 0.0D;
        entity.motionZ = 0.0D;
        if (entity instanceof EntityPlayerMP) {
            ((EntityPlayerMP) entity).connection.setPlayerLocation(x, y, z, entity.rotationYaw, entity.rotationPitch);
        } else {
            entity.setPositionAndUpdate(x, y, z);
        }
        world.playSound(null, x, y, z, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.BLOCKS, 0.8F, 1.0F);
    }

    private boolean isOnCooldown(World world, Entity entity) {
        return entity.getEntityData().getLong(TELEPORT_COOLDOWN_KEY) > world.getTotalWorldTime();
    }

    private void setCooldown(World world, Entity entity) {
        entity.getEntityData().setLong(TELEPORT_COOLDOWN_KEY, world.getTotalWorldTime() + TELEPORT_COOLDOWN_TICKS);
    }
}
