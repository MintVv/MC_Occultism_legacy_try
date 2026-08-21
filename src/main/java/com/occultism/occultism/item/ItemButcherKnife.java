package com.occultism.occultism.item;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;
import com.occultism.occultism.registry.ModItems;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemButcherKnife extends ItemSword {

    private final boolean iesnium;
    private final double attackDamage;
    private final double attackSpeed;

    public ItemButcherKnife(Item.ToolMaterial material, boolean iesnium, double attackDamage, double attackSpeed) {
        super(material);
        this.iesnium = iesnium;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
    }

    public boolean isIesnium() {
        return this.iesnium;
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> modifiers = super.getItemAttributeModifiers(equipmentSlot);
        if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
            modifiers.removeAll(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
            modifiers.removeAll(SharedMonsterAttributes.ATTACK_SPEED.getName());
            modifiers.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                    new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", this.attackDamage, 0));
            modifiers.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", this.attackSpeed, 0));
        }
        return modifiers;
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        if (repair.isEmpty()) {
            return false;
        }
        if (this.iesnium) {
            return repair.getItem() == ModItems.IESNIUM_INGOT || super.getIsRepairable(toRepair, repair);
        }
        return repair.getItem() == Items.IRON_INGOT || super.getIsRepairable(toRepair, repair);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        String key = this.getTranslationKey() + ".auto_tooltip";
        if (!I18n.hasKey(key)) {
            return;
        }
        for (String line : I18n.format(key).split("\\\\n|\\r?\\n")) {
            if (!line.isEmpty()) {
                tooltip.add(line);
            }
        }
    }
}
