package com.occultism.occultism.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.player.EntityPlayer;
import com.occultism.occultism.entity.EntityIesniumGolem;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemSoulGem extends Item {

    private static final String TAG_ENTITY = "OccultismEntity";
    private static final String TAG_ENTITY_ID = "EntityId";
    private static final String TAG_ENTITY_NAME = "EntityName";

    private final boolean fragile;

    public ItemSoulGem(boolean fragile) {
        this.fragile = fragile;
        this.addPropertyOverride(new ResourceLocation("occultism", "has_entity"), new IItemPropertyGetter() {
            @Override
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                return ItemSoulGem.hasEntity(stack) ? 1.0F : 0.0F;
            }
        });
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (hand != EnumHand.MAIN_HAND || !hasEntity(stack)) {
            return EnumActionResult.PASS;
        }

        if (worldIn.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        NBTTagCompound storedData = getStoredEntityData(stack);
        String storedId = getStoredEntityId(stack);
        String storedName = getStoredEntityName(stack);
        BlockPos spawnPos = getSpawnPos(worldIn, pos, facing);
        Entity entity = createStoredEntity(stack, worldIn);
        clearEntity(stack);

        if (entity == null) {
            setStoredEntity(stack, storedData, storedId, storedName);
            player.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.release_failed"),
                    true);
            return EnumActionResult.FAIL;
        }

        float yaw = player.rotationYaw + 180.0F;
        entity.dimension = worldIn.provider.getDimension();
        entity.setPositionAndRotation(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, yaw, 0.0F);
        entity.rotationYaw = yaw;
        entity.rotationPitch = 0.0F;
        entity.setRotationYawHead(yaw);
        if (entity instanceof EntityLivingBase) {
            ((EntityLivingBase) entity).setRenderYawOffset(yaw);
        }

        if (!worldIn.spawnEntity(entity)) {
            setStoredEntity(stack, storedData, storedId, storedName);
            player.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.release_failed"),
                    true);
            return EnumActionResult.FAIL;
        }

        player.swingArm(hand);
        worldIn.playSound(null, spawnPos, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 0.8F, 1.0F);

        if (this.fragile) {
            player.setHeldItem(hand, ItemStack.EMPTY);
            worldIn.playSound(null, spawnPos, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1.0F,
                    1.0F + 0.5F * worldIn.rand.nextFloat());
        }

        return EnumActionResult.SUCCESS;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target,
            EnumHand hand) {
        return this.tryCapture(stack, playerIn, target, hand) == EnumActionResult.SUCCESS;
    }

    public EnumActionResult tryCapture(ItemStack stack, EntityPlayer player, Entity target, EnumHand hand) {
        if (hand != EnumHand.MAIN_HAND) {
            return EnumActionResult.PASS;
        }
        if (target instanceof MultiPartEntityPart) {
            Object parent = ((MultiPartEntityPart) target).parent;
            if (parent instanceof EntityLivingBase) {
                target = (Entity) parent;
            }
        }
        if (!(target instanceof EntityLivingBase)) {
            return EnumActionResult.PASS;
        }
        return this.tryCapture(stack, player, (EntityLivingBase) target, hand);
    }

    public EnumActionResult tryCapture(ItemStack stack, EntityPlayer player, EntityLivingBase target, EnumHand hand) {
        if (hand != EnumHand.MAIN_HAND) {
            return EnumActionResult.PASS;
        }
        if (!target.isEntityAlive()) {
            return EnumActionResult.PASS;
        }
        if (target instanceof EntityPlayer || hasEntity(stack)) {
            return EnumActionResult.FAIL;
        }
        if (target instanceof EntityIesniumGolem) {
            if (!player.world.isRemote) {
                player.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.entity_type_denied"),
                        true);
            }
            return EnumActionResult.FAIL;
        }
        if (player.world.isRemote) {
            return EnumActionResult.SUCCESS;
        }
        if (isDenied(target)) {
            player.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.entity_type_denied"),
                    true);
            return EnumActionResult.FAIL;
        }

        NBTTagCompound entityData = new NBTTagCompound();
        if (!target.writeToNBTOptional(entityData)) {
            return EnumActionResult.FAIL;
        }

        ResourceLocation id = EntityList.getKey(target);
        if (id == null) {
            return EnumActionResult.FAIL;
        }
        entityData.setString("id", id.toString());
        entityData.removeTag("Pos");
        entityData.removeTag("Motion");
        entityData.removeTag("Rotation");
        setStoredEntity(stack, entityData, id.toString(), target.getDisplayName().getUnformattedText());

        target.setDead();
        player.swingArm(hand);
        player.setHeldItem(hand, stack);
        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT,
                SoundCategory.PLAYERS, 0.6F, 1.2F);
        return EnumActionResult.SUCCESS;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String key = hasEntity(stack) ? getTranslationKey() + ".name" : getTranslationKey() + "_empty.name";
        if (I18n.canTranslate(key)) {
            return I18n.translateToLocal(key);
        }
        return super.getItemStackDisplayName(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip,
            net.minecraft.client.util.ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        String text;
        if (hasEntity(stack)) {
            String name = getStoredEntityName(stack);
            if (name.isEmpty()) {
                name = getStoredEntityId(stack);
            }
            text = I18n.translateToLocalFormatted(getTranslationKey() + ".tooltip_filled", name);
        } else {
            text = I18n.translateToLocal(getTranslationKey() + ".tooltip_empty");
        }
        for (String line : text.split("\\\\n|\\r?\\n")) {
            if (!line.isEmpty()) {
                tooltip.add(line);
            }
        }
    }

    public static boolean hasEntity(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTagCompound() && stack.getTagCompound().hasKey(TAG_ENTITY, 10);
    }

    /** Stores a healthy, position-independent snapshot in the canonical soul-gem format. */
    public static boolean storeEntity(ItemStack stack, EntityLivingBase target) {
        if (stack.isEmpty() || target == null || target.isDead) {
            return false;
        }
        ResourceLocation id = EntityList.getKey(target);
        if (id == null) {
            return false;
        }

        NBTTagCompound entityData = new NBTTagCompound();
        float health = target.getHealth();
        target.setHealth(target.getMaxHealth());
        boolean written;
        try {
            written = target.writeToNBTOptional(entityData);
        } finally {
            target.setHealth(health);
        }
        if (!written) {
            return false;
        }

        entityData.setString("id", id.toString());
        entityData.removeTag("Pos");
        entityData.removeTag("Motion");
        entityData.removeTag("Rotation");
        setStoredEntity(stack, entityData, id.toString(), target.getDisplayName().getUnformattedText());
        return hasEntity(stack);
    }

    private static void setStoredEntity(ItemStack stack, @Nullable NBTTagCompound entityData, String id, String name) {
        if (entityData == null || entityData.isEmpty()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        tag.setTag(TAG_ENTITY, entityData.copy());
        tag.setString(TAG_ENTITY_ID, id);
        tag.setString(TAG_ENTITY_NAME, name == null ? "" : name);
        stack.setTagCompound(tag);
    }

    private static void clearEntity(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            return;
        }
        tag.removeTag(TAG_ENTITY);
        tag.removeTag(TAG_ENTITY_ID);
        tag.removeTag(TAG_ENTITY_NAME);
        if (tag.isEmpty()) {
            stack.setTagCompound(null);
        }
    }

    @Nullable
    private static NBTTagCompound getStoredEntityData(ItemStack stack) {
        if (!hasEntity(stack)) {
            return null;
        }
        return stack.getTagCompound().getCompoundTag(TAG_ENTITY).copy();
    }

    private static String getStoredEntityId(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getString(TAG_ENTITY_ID) : "";
    }

    private static String getStoredEntityName(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getString(TAG_ENTITY_NAME) : "";
    }

    @Nullable
    private static Entity createStoredEntity(ItemStack stack, World world) {
        NBTTagCompound entityData = getStoredEntityData(stack);
        if (entityData == null) {
            return null;
        }
        String id = getStoredEntityId(stack);
        if (!id.isEmpty()) {
            entityData.setString("id", id);
        }
        entityData.removeTag("Pos");
        entityData.removeTag("Motion");
        entityData.removeTag("Rotation");
        return EntityList.createEntityFromNBT(entityData, world);
    }

    private static BlockPos getSpawnPos(World world, BlockPos pos, EnumFacing facing) {
        EnumFacing side = facing == null ? EnumFacing.UP : facing;
        IBlockState state = world.getBlockState(pos);
        AxisAlignedBB collision = state.getBlock().getCollisionBoundingBox(state, world, pos);
        BlockPos spawnPos = collision == null || collision == Block.NULL_AABB || state.getBlock().isReplaceable(world, pos)
                ? pos
                : pos.offset(side);
        return spawnPos;
    }

    private static boolean isDenied(EntityLivingBase target) {
        if (target instanceof EntityIesniumGolem) {
            return true;
        }
        if (!target.isNonBoss()) {
            return true;
        }
        if (target instanceof EntityDragon || target instanceof EntityWither) {
            return true;
        }
        ResourceLocation id = EntityList.getKey(target);
        if (id == null) {
            return true;
        }
        return false;
    }
}
