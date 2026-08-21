package com.occultism.occultism.util;

import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import com.occultism.occultism.spirit.MachineReference;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

public final class ItemNBTUtil {

    public static final String TAG_BOUND_SPIRIT_NAME = "BoundSpiritName";
    public static final String TAG_SPIRIT_ENTITY_UUID_MOST = "SpiritEntityUUIDMost";
    public static final String TAG_SPIRIT_ENTITY_UUID_LEAST = "SpiritEntityUUIDLeast";
    public static final String TAG_BOOK_OF_CALLING_MODE = "BookOfCallingMode";
    public static final String TAG_BOOK_OF_CALLING_WORK_AREA_SIZE = "BookOfCallingWorkAreaSize";
    public static final String TAG_WORK_AREA_POSITION = "WorkArea";
    public static final String TAG_DEPOSIT_POSITION = "Deposit";
    public static final String TAG_EXTRACT_POSITION = "Extract";
    public static final String TAG_STORAGE_CONTROLLER_POSITION = "StorageController";
    public static final String TAG_STORAGE_CONTROLLER_DIMENSION = "StorageControllerDimension";
    public static final String TAG_MANAGED_MACHINE = "ManagedMachine";

    private static final String SPIRIT_NAME_NOT_YET_KNOWN = TextFormatting.OBFUSCATED + "(NotYetKnown)";
    private static final Random RANDOM = new Random();
    private static final String[] EASTER_EGGS = {"KliKli", "Xalmas", "Toastbroat", "Najlitarvan", "TheBoo",
            "Ridanisaurus", "Legiaseth", "Vallen", "Vemerion", "EqisEdu", "Feellian", "Ginkrepeerc", "Kat",
            "Chonky", "Ciege", "Chanticleer"};
    private static final String[] SYLLABLE1 = {"Kr", "Ca", "Ra", "Mrok", "Cru", "Ray", "Bre", "Zed", "Drak",
            "Mor", "Jag", "Mer", "Jar", "Mjol", "Zork", "Mad", "Cry", "Zur", "Creo", "Azak", "Azur", "Rei",
            "Cro", "Mar", "Luk", "Bar", "Gor", "Rak", "Thr", "Nar", "Vor", "Fir", "Trin", "Drog", "Karn",
            "Gar", "Ulf", "Hroth", "Ald", "Yng", "Styr", "Eir", "Ein", "Sig", "Ket", "Erl", "Haf", "Bryn",
            "Nid", "Grim", "Hol", "Fen", "Sigr", "Geir", "Hyr", "Val", "Har", "Kol", "Eyr"};
    private static final String[] SYLLABLE2 = {"air", "ir", "mi", "sor", "mee", "clo", "red", "cra", "ark",
            "arc", "miri", "lori", "cres", "mur", "zer", "marac", "zoir", "slamar", "salmar", "urak", "tim",
            "jor", "vyr", "dor", "thor", "kyl", "lyn", "wyn", "wynn", "lond", "rond", "vond", "dorn", "korn",
            "morn", "gorn", "thorn", "worn", "norn", "rinn", "dell", "bell", "vell", "fell", "kell", "zell",
            "nir", "fir", "mir", "tir", "sir", "vir", "zir", "lir", "jyr", "ryl", "rym", "lym", "lon", "ron",
            "myr", "myl", "myn", "ryn"};
    private static final String[] SYLLABLE3 = {"d", "ed", "ark", "arc", "es", "er", "der", "tron", "med",
            "ure", "zur", "cred", "mur", "aeus", "th", "vyr", "dor", "morn", "born", "thork", "fyr", "lyr",
            "ryth", "ryn", "drin", "dryn", "kyr", "kyn", "lynd", "lind", "lyne", "line", "ryne", "rine",
            "thyr", "thyre", "vyn", "vin", "vyne", "vine", "rynne", "rinne", "syr", "syrn", "zirn", "zirne",
            "kyl", "kylle", "dyr", "dorne", "lor", "lorne", "mon", "morne", "thorn", "thorne", "vyrn",
            "vyrne", "wyrm"};

    private ItemNBTUtil() {
    }

    public static boolean generateBoundSpiritNameIfNone(ItemStack stack) {
        String name = getBoundSpiritName(stack);
        if (name.isEmpty() || SPIRIT_NAME_NOT_YET_KNOWN.equals(name)) {
            setBoundSpiritName(stack, generateName());
            return true;
        }
        return false;
    }

    public static String getOrCreateBoundSpiritName(ItemStack stack) {
        generateBoundSpiritNameIfNone(stack);
        return getBoundSpiritName(stack);
    }

    public static boolean hasBoundSpiritName(ItemStack stack) {
        return !getBoundSpiritName(stack).isEmpty();
    }

    public static String getBoundSpiritName(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()) {
            return "";
        }
        return stack.getTagCompound().getString(TAG_BOUND_SPIRIT_NAME);
    }

    public static void setBoundSpiritName(ItemStack stack, String name) {
        if (stack.isEmpty() || name == null || name.isEmpty()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        tag.setString(TAG_BOUND_SPIRIT_NAME, name);
        stack.setTagCompound(tag);
    }

    public static void copyBoundSpiritName(ItemStack source, ItemStack target) {
        String name = getOrCreateBoundSpiritName(source);
        if (!name.isEmpty()) {
            setBoundSpiritName(target, name);
        }
    }

    public static boolean hasSpiritEntityUUID(ItemStack stack) {
        return getSpiritEntityUUID(stack) != null;
    }

    public static UUID getSpiritEntityUUID(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (!tag.hasKey(TAG_SPIRIT_ENTITY_UUID_MOST) || !tag.hasKey(TAG_SPIRIT_ENTITY_UUID_LEAST)) {
            return null;
        }
        return new UUID(tag.getLong(TAG_SPIRIT_ENTITY_UUID_MOST), tag.getLong(TAG_SPIRIT_ENTITY_UUID_LEAST));
    }

    public static void setSpiritEntityUUID(ItemStack stack, UUID uuid) {
        if (stack.isEmpty() || uuid == null) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        tag.setLong(TAG_SPIRIT_ENTITY_UUID_MOST, uuid.getMostSignificantBits());
        tag.setLong(TAG_SPIRIT_ENTITY_UUID_LEAST, uuid.getLeastSignificantBits());
        stack.setTagCompound(tag);
    }

    public static void removeSpiritEntityUUID(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        tag.removeTag(TAG_SPIRIT_ENTITY_UUID_MOST);
        tag.removeTag(TAG_SPIRIT_ENTITY_UUID_LEAST);
        if (tag.isEmpty()) {
            stack.setTagCompound(null);
        }
    }

    public static int getBookOfCallingMode(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()) {
            return 0;
        }
        return stack.getTagCompound().getInteger(TAG_BOOK_OF_CALLING_MODE);
    }

    public static void setBookOfCallingMode(ItemStack stack, int mode) {
        if (stack.isEmpty()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        tag.setInteger(TAG_BOOK_OF_CALLING_MODE, Math.max(0, mode));
        stack.setTagCompound(tag);
    }

    public static int getBookOfCallingWorkAreaSize(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()) {
            return 0;
        }
        return stack.getTagCompound().getInteger(TAG_BOOK_OF_CALLING_WORK_AREA_SIZE);
    }

    public static void setBookOfCallingWorkAreaSize(ItemStack stack, int size) {
        if (stack.isEmpty()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        tag.setInteger(TAG_BOOK_OF_CALLING_WORK_AREA_SIZE, Math.max(0, size));
        stack.setTagCompound(tag);
    }

    public static BlockPos getWorkAreaPosition(ItemStack stack) {
        return getBlockPos(stack, TAG_WORK_AREA_POSITION);
    }

    public static void setWorkAreaPosition(ItemStack stack, BlockPos pos) {
        setBlockPos(stack, TAG_WORK_AREA_POSITION, pos);
    }

    public static BlockPos getDepositPosition(ItemStack stack) {
        return getBlockPos(stack, TAG_DEPOSIT_POSITION);
    }

    public static void setDepositPosition(ItemStack stack, BlockPos pos) {
        setBlockPos(stack, TAG_DEPOSIT_POSITION, pos);
    }

    public static BlockPos getExtractPosition(ItemStack stack) {
        return getBlockPos(stack, TAG_EXTRACT_POSITION);
    }

    public static void setExtractPosition(ItemStack stack, BlockPos pos) {
        setBlockPos(stack, TAG_EXTRACT_POSITION, pos);
    }

    @Nullable
    public static BlockPos getStorageControllerPosition(ItemStack stack) {
        return getBlockPos(stack, TAG_STORAGE_CONTROLLER_POSITION);
    }

    public static int getStorageControllerDimension(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()) {
            return 0;
        }
        return stack.getTagCompound().getInteger(TAG_STORAGE_CONTROLLER_DIMENSION);
    }

    public static void setStorageControllerPosition(ItemStack stack, @Nullable BlockPos pos, int dimension) {
        setBlockPos(stack, TAG_STORAGE_CONTROLLER_POSITION, pos);
        if (!stack.isEmpty() && pos != null) {
            NBTTagCompound tag = stack.getTagCompound();
            tag.setInteger(TAG_STORAGE_CONTROLLER_DIMENSION, dimension);
            stack.setTagCompound(tag);
        }
    }

    @Nullable
    public static MachineReference getManagedMachine(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()
                || !stack.getTagCompound().hasKey(TAG_MANAGED_MACHINE, 10)) {
            return null;
        }
        return MachineReference.from(stack.getTagCompound().getCompoundTag(TAG_MANAGED_MACHINE));
    }

    public static void setManagedMachine(ItemStack stack, @Nullable MachineReference reference) {
        if (stack.isEmpty()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        if (reference == null) {
            tag.removeTag(TAG_MANAGED_MACHINE);
        } else {
            tag.setTag(TAG_MANAGED_MACHINE, reference.writeToNBT(new NBTTagCompound()));
        }
        stack.setTagCompound(tag);
    }

    private static BlockPos getBlockPos(ItemStack stack, String key) {
        if (stack.isEmpty() || !stack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound tag = stack.getTagCompound();
        String xKey = key + "X";
        String yKey = key + "Y";
        String zKey = key + "Z";
        if (!tag.hasKey(xKey) || !tag.hasKey(yKey) || !tag.hasKey(zKey)) {
            return null;
        }
        return new BlockPos(tag.getInteger(xKey), tag.getInteger(yKey), tag.getInteger(zKey));
    }

    private static void setBlockPos(ItemStack stack, String key, BlockPos pos) {
        if (stack.isEmpty() || pos == null) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        tag.setInteger(key + "X", pos.getX());
        tag.setInteger(key + "Y", pos.getY());
        tag.setInteger(key + "Z", pos.getZ());
        stack.setTagCompound(tag);
    }

    public static String formatSpiritName(String name) {
        return TextFormatting.GOLD.toString() + TextFormatting.BOLD + name + TextFormatting.RESET;
    }

    public static String generateName() {
        if (RANDOM.nextInt(20) == 0) {
            return EASTER_EGGS[RANDOM.nextInt(EASTER_EGGS.length)];
        }
        return SYLLABLE1[RANDOM.nextInt(SYLLABLE1.length)] + SYLLABLE2[RANDOM.nextInt(SYLLABLE2.length)]
                + SYLLABLE3[RANDOM.nextInt(SYLLABLE3.length)];
    }
}
