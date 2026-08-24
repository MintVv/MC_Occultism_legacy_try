package com.occultism.occultism.entity;

import javax.annotation.Nullable;

import com.occultism.occultism.block.IOtherworldBlock;
import com.occultism.occultism.item.ItemOtherworldGoggles;
import com.occultism.occultism.registry.ModPotions;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 1.12 merchant counterpart of the upstream Wondering Trader.
 *
 * <p>There is no Wandering Trader entity in 1.12. This entity keeps the
 * vanilla villager lifecycle and renderer, while selecting a persistent
 * Occultism trade table when the player can see the otherworld.</p>
 */
public class EntityWonderingTrader extends EntityVillager {

    private static final String COMMON_OFFERS_TAG = "CommonOffers";
    private static final String OTHERWORLD_OFFERS_TAG = "OtherworldOffers";

    @Nullable
    private MerchantRecipeList commonOffers;

    @Nullable
    private MerchantRecipeList otherworldOffers;

    @Nullable
    @SideOnly(Side.CLIENT)
    private MerchantRecipeList clientOffers;

    public EntityWonderingTrader(World worldIn) {
        super(worldIn);
        this.enablePersistence();
    }

    @Override
    @Nullable
    public MerchantRecipeList getRecipes(EntityPlayer player) {
        if (this.world != null && this.world.isRemote && this.clientOffers != null) {
            return this.clientOffers;
        }
        return hasOtherworldSight(player) ? this.getOtherworldOffers() : this.getCommonOffers();
    }

    /** Returns the stable per-entity mundane trade list. */
    public MerchantRecipeList getCommonOffers() {
        if (this.commonOffers == null) {
            this.commonOffers = WonderingTrades.createCommonTrades();
        }
        return this.commonOffers;
    }

    /** Returns the stable per-entity special trade list used for otherworld sight. */
    public MerchantRecipeList getOtherworldOffers() {
        if (this.otherworldOffers == null) {
            this.otherworldOffers = WonderingTrades.createOtherworldTrades();
        }
        return this.otherworldOffers;
    }

    /**
     * Matches the upstream reveal rules: Third Eye, equipped goggles, or a
     * True Sight Staff in either hand exposes the special offers.
     */
    public static boolean hasOtherworldSight(@Nullable EntityPlayer player) {
        if (player == null) {
            return false;
        }
        return player.isPotionActive(ModPotions.THIRD_EYE)
                || ItemOtherworldGoggles.isGoggles(player.getItemStackFromSlot(EntityEquipmentSlot.HEAD))
                || IOtherworldBlock.isTrueSightStaff(player.getHeldItemMainhand())
                || IOtherworldBlock.isTrueSightStaff(player.getHeldItemOffhand());
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (this.commonOffers != null) {
            compound.setTag(COMMON_OFFERS_TAG, this.commonOffers.getRecipiesAsTags());
        }
        if (this.otherworldOffers != null) {
            compound.setTag(OTHERWORLD_OFFERS_TAG, this.otherworldOffers.getRecipiesAsTags());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey(COMMON_OFFERS_TAG, 10)) {
            this.commonOffers = new MerchantRecipeList(compound.getCompoundTag(COMMON_OFFERS_TAG));
        }
        if (compound.hasKey(OTHERWORLD_OFFERS_TAG, 10)) {
            this.otherworldOffers = new MerchantRecipeList(compound.getCompoundTag(OTHERWORLD_OFFERS_TAG));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setRecipes(@Nullable MerchantRecipeList recipeList) {
        // EntityVillager's 1.12 implementation is empty; retain the server
        // packet so GuiMerchant can render the selected table client-side.
        this.clientOffers = recipeList;
    }
}
