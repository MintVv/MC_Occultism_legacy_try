package com.occultism.occultism.item;

import java.util.List;

import javax.annotation.Nullable;

import com.occultism.occultism.entity.EntityBoundSpirit;
import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.spirit.SpiritTier;
import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.util.ItemNBTUtil;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMagicLamp extends Item {

    private static final String TAG_STORED_SPIRIT = "StoredSpiritEntity";
    private static final String TAG_STORED_SPIRIT_ID = "StoredSpiritEntityId";
    private static final String TAG_STORED_SPIRIT_NAME = "StoredSpiritEntityName";
    private static final String TAG_STORED_SPIRIT_JOB = "StoredSpiritJob";

    public ItemMagicLamp() {
        this.setMaxStackSize(1);
        this.addPropertyOverride(new ResourceLocation("occultism", "has_spirit"), new IItemPropertyGetter() {
            @Override
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                return hasStoredSpirit(stack) ? 1.0F : 0.0F;
            }
        });
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (hand != EnumHand.MAIN_HAND || !hasStoredSpirit(stack)) {
            return EnumActionResult.PASS;
        }
        if (worldIn.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        NBTTagCompound storedData = getStoredSpiritData(stack);
        String storedId = getStoredSpiritId(stack);
        String storedName = getStoredSpiritName(stack);
        String storedJob = getStoredSpiritJob(stack);
        Entity entity = createStoredSpirit(stack, worldIn);
        clearStoredSpirit(stack);
        clearSpiritIdentity(stack);

        if (!(entity instanceof EntityFoliotJob) && !(entity instanceof EntityBoundSpirit)) {
            setStoredSpirit(stack, storedData, storedId, storedName, storedJob);
            player.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.release_failed"),
                    true);
            return EnumActionResult.FAIL;
        }

        BlockPos spawnPos = getSpawnPos(worldIn, pos, facing);
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
            setStoredSpirit(stack, storedData, storedId, storedName, storedJob);
            player.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.release_failed"),
                    true);
            return EnumActionResult.FAIL;
        }

        player.swingArm(hand);
        player.setHeldItem(hand, stack);
        worldIn.playSound(null, spawnPos, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 0.75F, 1.0F);
        player.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.released"), true);
        return EnumActionResult.SUCCESS;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target,
            EnumHand hand) {
        return this.tryCapture(stack, playerIn, target, hand) == EnumActionResult.SUCCESS;
    }

    public EnumActionResult tryCapture(ItemStack stack, EntityPlayer player, Entity target, EnumHand hand) {
        if (hand != EnumHand.MAIN_HAND || !(target instanceof EntityFoliotJob)
                && !(target instanceof EntityBoundSpirit)) {
            return EnumActionResult.PASS;
        }
        if (hasStoredSpirit(stack)) {
            if (!player.world.isRemote) {
                player.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.already_stored"),
                        true);
            }
            return EnumActionResult.FAIL;
        }
        EntityLivingBase spirit = (EntityLivingBase) target;
        if (!spirit.isEntityAlive()) {
            return EnumActionResult.PASS;
        }
        if (player.world.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        if (!storeSpirit(stack, spirit)) {
            player.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.store_failed"), true);
            return EnumActionResult.FAIL;
        }

        spirit.setDead();
        player.swingArm(hand);
        player.setHeldItem(hand, stack);
        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT,
                SoundCategory.PLAYERS, 0.6F, 1.2F);
        player.sendStatusMessage(new TextComponentTranslation(getTranslationKey() + ".message.stored",
                getStoredSpiritName(stack)), true);
        return EnumActionResult.SUCCESS;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String key = hasStoredSpirit(stack) ? getTranslationKey().replace("empty", "filled") + ".name"
                : getTranslationKey() + ".name";
        if (net.minecraft.util.text.translation.I18n.canTranslate(key)) {
            return net.minecraft.util.text.translation.I18n.translateToLocal(key);
        }
        return super.getItemStackDisplayName(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        String text;
        if (hasStoredSpirit(stack)) {
            String name = getStoredSpiritName(stack);
            if (name.isEmpty()) {
                name = I18n.format("item.occultism.bound_spirit.unknown");
            } else {
                name = ItemNBTUtil.formatSpiritName(name);
            }
            text = I18n.format(getTranslationKey() + ".tooltip_filled", name,
                    I18n.format(getSpiritTypeTranslationKey(getStoredSpiritJob(stack))));
        } else {
            text = I18n.format(getTranslationKey() + ".tooltip_empty");
        }
        for (String line : text.split("\\\\n|\\r?\\n")) {
            if (!line.isEmpty()) {
                tooltip.add(line);
            }
        }
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return hasStoredSpirit(stack);
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
        if (worldIn.isRemote || !hasStoredSpirit(stack) || !(entityIn instanceof EntityPlayer)) {
            return;
        }
        if (worldIn.getTotalWorldTime() % 20L != 0L || worldIn.rand.nextInt(100) != 0) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entityIn;
        player.sendStatusMessage(new TextComponentTranslation(
                getTranslationKey() + ".spirit_message_" + worldIn.rand.nextInt(10), getStoredSpiritName(stack)),
                false);
        if (player.isPlayerSleeping()) {
            player.attackEntityFrom(DamageSource.MAGIC, 1.0F);
        }
    }

    public static boolean hasStoredSpirit(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTagCompound() && stack.getTagCompound().hasKey(TAG_STORED_SPIRIT, 10);
    }

    private static boolean storeSpirit(ItemStack stack, EntityLivingBase spirit) {
        NBTTagCompound entityData = new NBTTagCompound();
        if (!spirit.writeToNBTOptional(entityData)) {
            return false;
        }
        ResourceLocation id = EntityList.getKey(spirit);
        if (id == null) {
            return false;
        }

        entityData.setString("id", id.toString());
        entityData.removeTag("Pos");
        entityData.removeTag("Motion");
        entityData.removeTag("Rotation");

        String name = getSpiritDisplayName(spirit);
        String job = getSpiritTypeId(spirit);
        setStoredSpirit(stack, entityData, id.toString(), name, job);
        if (!name.isEmpty()) {
            ItemNBTUtil.setBoundSpiritName(stack, name);
        }
        return true;
    }

    private static String getSpiritDisplayName(EntityLivingBase spirit) {
        return spirit.hasCustomName() ? spirit.getCustomNameTag() : spirit.getDisplayName().getUnformattedText();
    }

    private static String getSpiritTypeId(EntityLivingBase spirit) {
        if (spirit instanceof EntityFoliotJob) {
            EntityFoliotJob job = (EntityFoliotJob) spirit;
            if (job.getTier() == SpiritTier.FOLIOT) {
                return job.getJobType().getId().toString();
            }
            return job.getTier().getId() + "/" + job.getJobType().getId().getPath();
        }
        if (spirit instanceof EntityBoundSpirit) {
            return ((EntityBoundSpirit) spirit).getTier().getId().toString();
        }
        return "";
    }

    private static void setStoredSpirit(ItemStack stack, @Nullable NBTTagCompound entityData, String id, String name,
            String job) {
        if (stack.isEmpty() || entityData == null || entityData.isEmpty()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        tag.setTag(TAG_STORED_SPIRIT, entityData.copy());
        tag.setString(TAG_STORED_SPIRIT_ID, id == null ? "" : id);
        tag.setString(TAG_STORED_SPIRIT_NAME, name == null ? "" : name);
        tag.setString(TAG_STORED_SPIRIT_JOB, job == null ? "" : job);
        stack.setTagCompound(tag);
    }

    private static void clearStoredSpirit(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        tag.removeTag(TAG_STORED_SPIRIT);
        tag.removeTag(TAG_STORED_SPIRIT_ID);
        tag.removeTag(TAG_STORED_SPIRIT_NAME);
        tag.removeTag(TAG_STORED_SPIRIT_JOB);
        if (tag.isEmpty()) {
            stack.setTagCompound(null);
        }
    }

    private static void clearSpiritIdentity(ItemStack stack) {
        ItemNBTUtil.removeSpiritEntityUUID(stack);
        if (!stack.hasTagCompound()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        tag.removeTag(ItemNBTUtil.TAG_BOUND_SPIRIT_NAME);
        if (tag.isEmpty()) {
            stack.setTagCompound(null);
        }
    }

    @Nullable
    private static NBTTagCompound getStoredSpiritData(ItemStack stack) {
        if (!hasStoredSpirit(stack)) {
            return null;
        }
        return stack.getTagCompound().getCompoundTag(TAG_STORED_SPIRIT).copy();
    }

    private static String getStoredSpiritId(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getString(TAG_STORED_SPIRIT_ID) : "";
    }

    private static String getStoredSpiritName(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getString(TAG_STORED_SPIRIT_NAME) : "";
    }

    private static String getStoredSpiritJob(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getString(TAG_STORED_SPIRIT_JOB) : "";
    }

    @Nullable
    private static Entity createStoredSpirit(ItemStack stack, World world) {
        NBTTagCompound entityData = getStoredSpiritData(stack);
        if (entityData == null) {
            return null;
        }
        String id = getStoredSpiritId(stack);
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
        return collision == null || collision == Block.NULL_AABB || state.getBlock().isReplaceable(world, pos)
                ? pos
                : pos.offset(side);
    }

    private static String getSpiritTypeTranslationKey(String jobId) {
        int separator = jobId == null ? -1 : jobId.indexOf('/');
        if (separator >= 0) {
            String tierId = jobId.substring(0, separator);
            String jobPath = jobId.substring(separator + 1);
            SpiritTier tier = SpiritTier.fromId(tierId);
            SpiritJobType jobType = SpiritJobType.fromId("occultism:" + jobPath);
            if (tier != null && jobType != null) {
                return "job.occultism." + tier.getId().getPath() + "." + jobType.getId().getPath();
            }
        }
        SpiritJobType jobType = SpiritJobType.fromId(jobId);
        if (jobType != null) {
            return "job.occultism." + jobType.getId().getPath();
        }
        SpiritTier tier = SpiritTier.fromId(jobId);
        if (tier != null) {
            return "spirit_tier.occultism." + tier.getId().getPath();
        }
        return "job.occultism.unknown";
    }
}
