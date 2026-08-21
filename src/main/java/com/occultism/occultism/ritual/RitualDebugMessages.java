package com.occultism.occultism.ritual;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;

public final class RitualDebugMessages {

    private RitualDebugMessages() {
    }

    public static void sendPentacleRituals(EntityPlayer player, PentacleDefinition pentacle) {
        List<RitualDefinition> rituals = RitualRecipeManager.getForPentacle(pentacle.getId());
        if (rituals.isEmpty()) {
            player.sendStatusMessage(new TextComponentString("此法阵当前没有已迁入的仪式配方。"), false);
            return;
        }

        player.sendStatusMessage(new TextComponentString("此法阵可测试配方：" + rituals.size() + " 条"), false);
        for (RitualDefinition ritual : rituals) {
            player.sendStatusMessage(new TextComponentString(formatRitual(ritual)), false);
        }
    }

    public static void sendActivationMatches(EntityPlayer player, PentacleDefinition pentacle, ItemStack activationStack) {
        List<RitualDefinition> rituals = RitualRecipeManager.getForPentacleAndActivation(pentacle.getId(),
                activationStack);
        if (rituals.isEmpty()) {
            player.sendStatusMessage(new TextComponentString("这个启动物当前不能启动此法阵的任何 JSON 仪式。"), false);
            sendPentacleRituals(player, pentacle);
            return;
        }

        player.sendStatusMessage(new TextComponentString("当前启动物可匹配配方：" + rituals.size() + " 条"), false);
        for (RitualDefinition ritual : rituals) {
            player.sendStatusMessage(new TextComponentString(formatRitual(ritual)), false);
        }
    }

    private static String formatRitual(RitualDefinition ritual) {
        StringBuilder builder = new StringBuilder();
        builder.append("- ").append(ritual.getDisplayName())
                .append(" | 启动: ").append(ritual.getActivationIngredient().createDisplayStack().getDisplayName())
                .append(" | 图标: ").append(ritual.getRitualDummy().createStack().getDisplayName())
                .append(" | 材料: ").append(formatIngredients(ritual));
        if (ritual.requiresItemUse()) {
            builder.append(" | 需使用: ").append(ritual.getItemToUse().createDisplayStack().getDisplayName());
        }
        if (ritual.summonsEntity()) {
            builder.append(" | 召唤实体: ")
                    .append(ritual.getEntityToSummon().getDisplayId());
            if (ritual.getEntityToSummon().getSummonNumber() > 1) {
                builder.append(" x").append(ritual.getEntityToSummon().getSummonNumber());
            }
        }
        if (ritual.hasSpiritJobType()) {
            builder.append(" | 灵体职业: ").append(ritual.getSpiritJobType().getId());
        }
        if (ritual.hasSpiritMaxAge()) {
            builder.append(" | 灵体寿命: ").append(ritual.getSpiritMaxAge()).append(" 秒");
        }
        if (ritual.requiresSacrifice()) {
            builder.append(" | 祭品: ").append(ritual.getSacrifice().getDisplayName());
        }
        return builder.toString();
    }

    private static String formatIngredients(RitualDefinition ritual) {
        if (ritual.getAdditionalIngredients().isEmpty()) {
            return "无";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ritual.getAdditionalIngredients().size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            ItemStack stack = ritual.getAdditionalIngredients().get(i).createDisplayStack();
            if (stack.getCount() > 1) {
                builder.append(stack.getCount()).append("x ");
            }
            builder.append(stack.getDisplayName());
        }
        return builder.toString();
    }
}
