package com.occultism.occultism.spirit;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * A small, persistent input request for the 1.12 machine manager.
 *
 * <p>The modern implementation stores an ItemStackComparator.  1.12 does
 * not have that API, so this port keeps the filter stack and matches its item
 * and tag data.  The stack count is deliberately not part of the comparator;
 * {@link #amount} is the remaining amount for this request.</p>
 */
public final class DepositOrder {

    private static final String TAG_STACK = "Stack";
    private static final String TAG_AMOUNT = "Amount";
    private static final String TAG_MATCH_NBT = "MatchNbt";
    private static final String TAG_ATTEMPTS = "Attempts";
    private static final String TAG_MAX_RETRIES = "MaxRetries";
    private static final String TAG_STATUS = "Status";

    /** A bounded retry count keeps an invalid machine from retaining a job forever. */
    public static final int DEFAULT_MAX_RETRIES = 8;

    public enum Status {
        PENDING,
        COMPLETE,
        EXHAUSTED
    }

    private ItemStack filterStack;
    private boolean matchNbt;
    /** Remaining amount, kept public to mirror the upstream job contract. */
    public int amount;
    private int attempts;
    private int maxRetries = DEFAULT_MAX_RETRIES;
    private Status status = Status.PENDING;

    private DepositOrder() {
        this.filterStack = ItemStack.EMPTY;
    }

    public DepositOrder(ItemStack filterStack, int amount) {
        this(filterStack, amount, true);
    }

    public DepositOrder(ItemStack filterStack, int amount, boolean matchNbt) {
        this.filterStack = normalizeFilter(filterStack);
        this.amount = Math.max(0, amount);
        this.matchNbt = matchNbt;
    }

    @Nullable
    public static DepositOrder from(NBTTagCompound compound) {
        if (compound == null || !compound.hasKey(TAG_STACK, 10)) {
            return null;
        }
        DepositOrder order = new DepositOrder();
        ItemStack stack = new ItemStack(compound.getCompoundTag(TAG_STACK));
        if (stack.isEmpty()) {
            return null;
        }
        order.filterStack = normalizeFilter(stack);
        order.amount = Math.max(0, compound.getInteger(TAG_AMOUNT));
        order.matchNbt = !compound.hasKey(TAG_MATCH_NBT, 1) || compound.getBoolean(TAG_MATCH_NBT);
        order.attempts = clampAttempts(compound.hasKey(TAG_ATTEMPTS, 3) ? compound.getInteger(TAG_ATTEMPTS) : 0);
        order.maxRetries = clampMaxRetries(
                compound.hasKey(TAG_MAX_RETRIES, 3) ? compound.getInteger(TAG_MAX_RETRIES) : DEFAULT_MAX_RETRIES);
        order.status = readStatus(compound.hasKey(TAG_STATUS, 8) ? compound.getString(TAG_STATUS) : null);
        order.normalizeStatus();
        return order.isComplete() ? null : order;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        if (compound == null) {
            compound = new NBTTagCompound();
        }
        compound.setTag(TAG_STACK, this.filterStack.writeToNBT(new NBTTagCompound()));
        compound.setInteger(TAG_AMOUNT, Math.max(0, this.amount));
        compound.setBoolean(TAG_MATCH_NBT, this.matchNbt);
        compound.setInteger(TAG_ATTEMPTS, this.attempts);
        compound.setInteger(TAG_MAX_RETRIES, this.maxRetries);
        compound.setString(TAG_STATUS, this.status.name());
        return compound;
    }

    public ItemStack getFilterStack() {
        return this.filterStack.copy();
    }

    public boolean isMatchNbt() {
        return this.matchNbt;
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int amount) {
        this.amount = Math.max(0, amount);
        this.normalizeStatus();
    }

    public boolean isComplete() {
        return this.amount <= 0 || this.filterStack.isEmpty() || this.status == Status.COMPLETE
                || this.status == Status.EXHAUSTED;
    }

    public Status getStatus() {
        return this.status;
    }

    public int getAttempts() {
        return this.attempts;
    }

    public int getMaxRetries() {
        return this.maxRetries;
    }

    public boolean isExhausted() {
        return this.status == Status.EXHAUSTED;
    }

    /**
     * Records a successful transfer and clears transient failures.  A caller must
     * pass the actual amount moved; simulated or zero-size transfers do not
     * complete an order.
     */
    public void recordSuccess(int moved) {
        if (moved <= 0 || this.isComplete()) {
            return;
        }
        this.amount = Math.max(0, this.amount - moved);
        this.attempts = 0;
        this.normalizeStatus();
    }

    /**
     * Records a failed transaction.  Returns {@code true} while another retry is
     * allowed, and marks the order exhausted when the bounded budget is reached.
     */
    public boolean recordFailure() {
        if (this.isComplete()) {
            return false;
        }
        this.attempts = Math.min(this.maxRetries, this.attempts + 1);
        if (this.attempts >= this.maxRetries) {
            this.status = Status.EXHAUSTED;
            this.amount = 0;
            return false;
        }
        this.status = Status.PENDING;
        return true;
    }

    public void resetAttempts() {
        this.attempts = 0;
        if (!this.isComplete()) {
            this.status = Status.PENDING;
        }
    }

    public boolean matches(ItemStack stack) {
        if (stack == null || stack.isEmpty() || this.filterStack.isEmpty()) {
            return false;
        }
        if (!ItemStack.areItemsEqual(this.filterStack, stack)) {
            return false;
        }
        return !this.matchNbt || ItemStack.areItemStackTagsEqual(this.filterStack, stack);
    }

    public int take(int count) {
        int taken = Math.min(Math.max(0, count), this.amount);
        this.amount -= taken;
        if (taken > 0) {
            this.attempts = 0;
        }
        this.normalizeStatus();
        return taken;
    }

    private static ItemStack normalizeFilter(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = stack.copy();
        result.setCount(1);
        return result;
    }

    private void normalizeStatus() {
        if (this.filterStack.isEmpty() || this.amount <= 0) {
            this.amount = 0;
            this.status = Status.COMPLETE;
        } else if (this.status == Status.COMPLETE) {
            this.status = Status.PENDING;
        }
        this.attempts = clampAttempts(this.attempts);
        this.maxRetries = clampMaxRetries(this.maxRetries);
    }

    private static Status readStatus(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return Status.PENDING;
        }
        try {
            return Status.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return Status.PENDING;
        }
    }

    private static int clampAttempts(int value) {
        return Math.max(0, Math.min(DEFAULT_MAX_RETRIES, value));
    }

    private static int clampMaxRetries(int value) {
        // Do not permit malformed NBT to create an unbounded retry loop.
        return Math.max(1, Math.min(DEFAULT_MAX_RETRIES, value));
    }
}
