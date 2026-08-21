package com.occultism.occultism.command;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.occultism.occultism.Occultism;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class CommandOccultism extends CommandBase {

    @Override
    public String getName() {
        return "occultism";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/occultism debug ai";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 1;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 2 && "debug".equalsIgnoreCase(args[0]) && "ai".equalsIgnoreCase(args[1])) {
            Occultism.DEBUG.debugAI = !Occultism.DEBUG.debugAI;
            sender.sendMessage(new TextComponentString("AI Debugging enabled: " + Occultism.DEBUG.debugAI));
            return;
        }
        throw new WrongUsageException(this.getUsage(sender));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
            @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "debug");
        }
        if (args.length == 2 && "debug".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, "ai");
        }
        return Collections.emptyList();
    }
}
