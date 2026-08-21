package com.occultism.occultism.ritual;

import java.util.List;

import com.occultism.occultism.item.ItemMagicLamp;
import com.occultism.occultism.registry.ModItems;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class RitualIngredient {

    private final Item item;
    private final int count;
    private final int metadata;
    private final String oreName;
    private final List<RitualIngredient> alternatives;

    public RitualIngredient(Item item, int count) {
        this(item, count, -1);
    }

    public RitualIngredient(Item item, int count, int metadata) {
        this.item = item;
        this.count = count;
        this.metadata = metadata;
        this.oreName = null;
        this.alternatives = null;
    }

    public RitualIngredient(String oreName, int count) {
        this.item = null;
        this.count = count;
        this.metadata = -1;
        this.oreName = oreName;
        this.alternatives = null;
    }

    public RitualIngredient(List<RitualIngredient> alternatives) {
        this.item = null;
        this.count = 1;
        this.metadata = -1;
        this.oreName = null;
        this.alternatives = alternatives;
    }

    public boolean matches(ItemStack stack) {
        if (this.alternatives != null) {
            return this.alternatives.stream().anyMatch(ingredient -> ingredient.matches(stack));
        }
        if (this.oreName != null) {
            return this.matchesOre(stack);
        }
        if (this.item == ModItems.MAGIC_LAMP_EMPTY && ItemMagicLamp.hasStoredSpirit(stack)) {
            return false;
        }
        return !stack.isEmpty() && stack.getItem() == this.item && stack.getCount() >= this.count
                && (this.metadata < 0 || this.metadata == OreDictionary.WILDCARD_VALUE
                        || stack.getMetadata() == this.metadata);
    }

    public void consume(ItemStack stack) {
        if (this.alternatives != null) {
            for (RitualIngredient ingredient : this.alternatives) {
                if (ingredient.matches(stack)) {
                    ingredient.consume(stack);
                    return;
                }
            }
        }
        stack.shrink(this.count);
    }

    public ItemStack createDisplayStack() {
        if (this.alternatives != null) {
            for (RitualIngredient ingredient : this.alternatives) {
                ItemStack stack = ingredient.createDisplayStack();
                if (!stack.isEmpty()) {
                    return stack;
                }
            }
            return ItemStack.EMPTY;
        }
        if (this.oreName != null) {
            List<ItemStack> ores = OreDictionary.getOres(this.oreName);
            if (!ores.isEmpty()) {
                ItemStack stack = ores.get(0).copy();
                if (stack.getMetadata() == OreDictionary.WILDCARD_VALUE) {
                    stack.setItemDamage(0);
                }
                stack.setCount(this.count);
                return stack;
            }
            return ItemStack.EMPTY;
        }
        if (this.metadata < 0) {
            return new ItemStack(this.item, this.count);
        }
        int displayMetadata = this.metadata == OreDictionary.WILDCARD_VALUE ? 0 : this.metadata;
        return new ItemStack(this.item, this.count, displayMetadata);
    }

    public int getCount() {
        return this.count;
    }

    public Item getItem() {
        return this.item;
    }

    public int getMetadata() {
        return this.metadata;
    }

    public String getOreName() {
        return this.oreName;
    }

    public List<RitualIngredient> getAlternatives() {
        return this.alternatives;
    }

    private boolean matchesOre(ItemStack stack) {
        if (stack.isEmpty() || stack.getCount() < this.count) {
            return false;
        }
        for (ItemStack oreStack : OreDictionary.getOres(this.oreName)) {
            if (OreDictionary.itemMatches(oreStack, stack, false)) {
                return true;
            }
        }
        return false;
    }
}
