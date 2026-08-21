package com.occultism.occultism.network;

import java.util.Locale;

import com.occultism.occultism.container.ContainerStorageController;
import com.occultism.occultism.tileentity.StorageSortDirection;
import com.occultism.occultism.tileentity.StorageSortType;
import com.occultism.occultism.tileentity.TileEntityStorageController;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Client request for the storage terminal's query and ordering settings.
 *
 * <p>The packet deliberately carries no block position.  The server resolves
 * the controller from the player's current container and validates that the
 * container still represents a reachable controller or valid remote link.
 * This prevents a client from loading or mutating an arbitrary tile entity.</p>
 */
public class MessageUpdateStorageSettings implements IMessage {

    public static final int MAX_SEARCH_QUERY_LENGTH = 128;

    private String searchQuery;
    private StorageSortType sortType;
    private StorageSortDirection sortDirection;

    public MessageUpdateStorageSettings() {
        this("", StorageSortType.AMOUNT, StorageSortDirection.DOWN);
    }

    public MessageUpdateStorageSettings(String searchQuery, StorageSortType sortType,
            StorageSortDirection sortDirection) {
        this.searchQuery = normalizeSearchQuery(searchQuery);
        this.sortType = sortType == null ? StorageSortType.AMOUNT : sortType;
        this.sortDirection = sortDirection == null ? StorageSortDirection.DOWN : sortDirection;
    }

    /** Compatibility overload matching the upstream sort-only call sites. */
    public MessageUpdateStorageSettings(StorageSortDirection sortDirection, StorageSortType sortType) {
        this("", sortType, sortDirection);
    }

    public String getSearchQuery() {
        return this.searchQuery;
    }

    public StorageSortType getSortType() {
        return this.sortType;
    }

    public StorageSortDirection getSortDirection() {
        return this.sortDirection;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.searchQuery = normalizeSearchQuery(ByteBufUtils.readUTF8String(buf));
        this.sortType = StorageSortType.byValue(buf.readUnsignedByte());
        this.sortDirection = StorageSortDirection.byValue(buf.readUnsignedByte());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.searchQuery == null ? "" : this.searchQuery);
        buf.writeByte(this.sortType == null ? StorageSortType.AMOUNT.getValue() : this.sortType.getValue());
        buf.writeByte(this.sortDirection == null ? StorageSortDirection.DOWN.getValue()
                : this.sortDirection.getValue());
    }

    private static String normalizeSearchQuery(String query) {
        if (query == null) {
            return "";
        }
        String normalized = query.trim();
        normalized = normalized.toLowerCase(Locale.ROOT);
        return normalized.length() <= MAX_SEARCH_QUERY_LENGTH
                ? normalized
                : normalized.substring(0, MAX_SEARCH_QUERY_LENGTH);
    }

    public static class Handler implements IMessageHandler<MessageUpdateStorageSettings, IMessage> {

        @Override
        public IMessage onMessage(MessageUpdateStorageSettings message, MessageContext context) {
            if (context.side != Side.SERVER) {
                return null;
            }
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> applyOnServer(message, player));
            return null;
        }

        private static void applyOnServer(MessageUpdateStorageSettings message, EntityPlayerMP player) {
            if (message == null || player == null || !(player.openContainer instanceof ContainerStorageController)) {
                return;
            }
            ContainerStorageController container = (ContainerStorageController) player.openContainer;
            if (!container.canUpdateStorageSettings(player)) {
                return;
            }

            TileEntityStorageController controller = container.getStorageController();
            controller.setSearchQuery(message.searchQuery);
            controller.setSortType(message.sortType);
            controller.setSortDirection(message.sortDirection);
            controller.sortStorageSlots();
            container.refreshStorageView();
            controller.markSettingsDirtyAndSync();
            ModNetworking.syncStorageStacks(player, controller);
            player.openContainer.detectAndSendChanges();
        }
    }
}
