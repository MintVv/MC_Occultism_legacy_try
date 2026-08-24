package com.occultism.occultism.registry;

import com.occultism.occultism.Occultism;
import com.occultism.occultism.Tags;
import com.occultism.occultism.entity.EntityBatFamiliar;
import com.occultism.occultism.entity.EntityAfritWild;
import com.occultism.occultism.entity.EntityMaridWild;
import com.occultism.occultism.entity.EntityBeholderFamiliar;
import com.occultism.occultism.entity.EntityBeaverFamiliar;
import com.occultism.occultism.entity.EntityBlacksmithFamiliar;
import com.occultism.occultism.entity.EntityBoundSpirit;
import com.occultism.occultism.entity.EntityDjinni;
import com.occultism.occultism.entity.EntityAfrit;
import com.occultism.occultism.entity.EntityMarid;
import com.occultism.occultism.entity.EntityDjinniJob;
import com.occultism.occultism.entity.EntityAfritJob;
import com.occultism.occultism.entity.EntityMaridJob;
import com.occultism.occultism.entity.EntityCthulhuFamiliar;
import com.occultism.occultism.entity.EntityChimeraFamiliar;
import com.occultism.occultism.entity.EntityDeerFamiliar;
import com.occultism.occultism.entity.EntityDemonicHusband;
import com.occultism.occultism.entity.EntityDemonicWife;
import com.occultism.occultism.entity.EntityDevilFamiliar;
import com.occultism.occultism.entity.EntityDragonFamiliar;
import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.entity.EntityFoliot;
import com.occultism.occultism.entity.EntityFairyFamiliar;
import com.occultism.occultism.entity.EntityGreedyFamiliar;
import com.occultism.occultism.entity.EntityGoatFamiliar;
import com.occultism.occultism.entity.EntityGuardianFamiliar;
import com.occultism.occultism.entity.EntityHeadlessFamiliar;
import com.occultism.occultism.entity.EntityIesniumGolem;
import com.occultism.occultism.entity.EntityMercyGoat;
import com.occultism.occultism.entity.EntityMummyFamiliar;
import com.occultism.occultism.entity.EntityOtherworldBird;
import com.occultism.occultism.entity.EntityPossessedBee;
import com.occultism.occultism.entity.EntityPossessedBlaze;
import com.occultism.occultism.entity.EntityPossessedGuardian;
import com.occultism.occultism.entity.EntityPossessedElderGuardian;
import com.occultism.occultism.entity.EntityPossessedEnderman;
import com.occultism.occultism.entity.EntityPossessedEndermite;
import com.occultism.occultism.entity.EntityPossessedGhast;
import com.occultism.occultism.entity.EntityPossessedHoglin;
import com.occultism.occultism.entity.EntityPossessedPhantom;
import com.occultism.occultism.entity.EntityPossessedWarden;
import com.occultism.occultism.entity.EntityPossessedWitch;
import com.occultism.occultism.entity.EntityPossessedZombifiedPiglin;
import com.occultism.occultism.entity.EntityPossessedShulker;
import com.occultism.occultism.entity.EntityPossessedSkeleton;
import com.occultism.occultism.entity.EntityPossessedWeakShulker;
import com.occultism.occultism.entity.EntityPurifiedAllay;
import com.occultism.occultism.entity.EntityShubNiggurathFamiliar;
import com.occultism.occultism.entity.EntityShubNiggurathSpawn;
import com.occultism.occultism.entity.EntityThrownSword;
import com.occultism.occultism.entity.EntityWildHuntSkeleton;
import com.occultism.occultism.entity.EntityWildHuntWitherSkeleton;
import com.occultism.occultism.entity.EntityWonderingTrader;
import com.occultism.occultism.entity.EntityWildHordeCreeper;
import com.occultism.occultism.entity.EntityWildHordeDrowned;
import com.occultism.occultism.entity.EntityWildHordeEvoker;
import com.occultism.occultism.entity.EntityWildHordeHusk;
import com.occultism.occultism.entity.EntityWildHordeParched;
import com.occultism.occultism.entity.EntityWildHordeSilverfish;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public final class ModEntities {

    private static int nextEntityId;

    private ModEntities() {
    }

    public static void register() {
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "foliot_job"), EntityFoliotJob.class,
                "foliot_job", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "bound_spirit"), EntityBoundSpirit.class,
                "bound_spirit", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "mercy_goat"), EntityMercyGoat.class,
                "mercy_goat", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "possessed_bee"), EntityPossessedBee.class,
                "possessed_bee", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "purified_allay"),
                EntityPurifiedAllay.class, "purified_allay", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "greedy_familiar"),
                EntityGreedyFamiliar.class, "greedy_familiar", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "bat_familiar"),
                EntityBatFamiliar.class, "bat_familiar", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "deer_familiar"),
                EntityDeerFamiliar.class, "deer_familiar", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "beaver_familiar"),
                EntityBeaverFamiliar.class, "beaver_familiar", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "mummy_familiar"),
                EntityMummyFamiliar.class, "mummy_familiar", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "devil_familiar"),
                EntityDevilFamiliar.class, "devil_familiar", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "guardian_familiar"),
                EntityGuardianFamiliar.class, "guardian_familiar", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "headless_familiar"),
                EntityHeadlessFamiliar.class, "headless_familiar", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "demonic_husband"),
                EntityDemonicHusband.class, "demonic_husband", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "demonic_wife"),
                EntityDemonicWife.class, "demonic_wife", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "cthulhu_familiar"),
                EntityCthulhuFamiliar.class, "cthulhu_familiar", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "blacksmith_familiar"),
                EntityBlacksmithFamiliar.class, "blacksmith_familiar", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "dragon_familiar"),
                EntityDragonFamiliar.class, "dragon_familiar", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "thrown_sword"),
                EntityThrownSword.class, "thrown_sword", nextEntityId++, Occultism.INSTANCE, 64, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "fairy_familiar"),
                EntityFairyFamiliar.class, "fairy_familiar", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "goat_familiar"),
                EntityGoatFamiliar.class, "goat_familiar", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "shub_niggurath_familiar"),
                EntityShubNiggurathFamiliar.class, "shub_niggurath_familiar", nextEntityId++, Occultism.INSTANCE,
                64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "shub_niggurath_spawn"),
                EntityShubNiggurathSpawn.class, "shub_niggurath_spawn", nextEntityId++, Occultism.INSTANCE,
                64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "beholder_familiar"),
                EntityBeholderFamiliar.class, "beholder_familiar", nextEntityId++, Occultism.INSTANCE, 64, 3,
                true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "chimera_familiar"),
                EntityChimeraFamiliar.class, "chimera_familiar", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "wild_hunt_skeleton"),
                EntityWildHuntSkeleton.class, "wild_hunt_skeleton", nextEntityId++, Occultism.INSTANCE, 128, 3,
                true, 0xC1C1C1, 0x494949);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "wild_hunt_wither_skeleton"),
                EntityWildHuntWitherSkeleton.class, "wild_hunt_wither_skeleton", nextEntityId++, Occultism.INSTANCE,
                128, 3, true, 0x141414, 0x474D4D);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "possessed_endermite"),
                EntityPossessedEndermite.class, "possessed_endermite", nextEntityId++, Occultism.INSTANCE,
                128, 3, true, 0x161616, 0x6E6E6E);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "possessed_skeleton"),
                EntityPossessedSkeleton.class, "possessed_skeleton", nextEntityId++, Occultism.INSTANCE,
                128, 3, true, 0xC1C1C1, 0x494949);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "possessed_enderman"),
                EntityPossessedEnderman.class, "possessed_enderman", nextEntityId++, Occultism.INSTANCE,
                128, 3, true, 0x161616, 0x000000);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "possessed_ghast"),
                EntityPossessedGhast.class, "possessed_ghast", nextEntityId++, Occultism.INSTANCE,
                160, 3, true, 0xE2E2E2, 0xC1C1C1);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "possessed_weak_shulker"),
                EntityPossessedWeakShulker.class, "possessed_weak_shulker", nextEntityId++, Occultism.INSTANCE,
                128, 3, true, 0x8C628C, 0x342638);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "possessed_shulker"),
                EntityPossessedShulker.class, "possessed_shulker", nextEntityId++, Occultism.INSTANCE,
                128, 3, true, 0x8C628C, 0x342638);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "possessed_elder_guardian"),
                EntityPossessedElderGuardian.class, "possessed_elder_guardian", nextEntityId++, Occultism.INSTANCE,
                384, 3, true, 0xB5B3A3, 0x39D6E0);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "possessed_phantom"),
                EntityPossessedPhantom.class, "possessed_phantom", nextEntityId++, Occultism.INSTANCE,
                256, 3, true, 0x3F4C81, 0x6CCC00);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "possessed_hoglin"),
                EntityPossessedHoglin.class, "possessed_hoglin", nextEntityId++, Occultism.INSTANCE,
                384, 3, true, 0x592A10, 0xF9F3A4);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "possessed_warden"),
                EntityPossessedWarden.class, "possessed_warden", nextEntityId++, Occultism.INSTANCE,
                768, 3, true, 0x0F4649, 0x39D6E0);
        // Append new registrations so existing 1.12 entity IDs remain stable for old worlds.
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "afrit_wild"), EntityAfritWild.class,
                "afrit_wild", nextEntityId++, Occultism.INSTANCE, 128, 3, true, 0xD94B22, 0xFFB347);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "otherworld_bird"),
                EntityOtherworldBird.class, "otherworld_bird", nextEntityId++, Occultism.INSTANCE, 64, 3, true,
                0x343A75, 0xA9B9FF);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "iesnium_golem"),
                EntityIesniumGolem.class, "iesnium_golem", nextEntityId++, Occultism.INSTANCE, 128, 3, true,
                0x6B6B6B, 0xD9A9FF);
        // Append specialized high-tier spirits so all existing 1.12 entity IDs remain stable.
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "djinni"), EntityDjinni.class,
                "djinni", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "afrit"), EntityAfrit.class,
                "afrit", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "marid"), EntityMarid.class,
                "marid", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "djinni_job"), EntityDjinniJob.class,
                "djinni_job", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "afrit_job"), EntityAfritJob.class,
                "afrit_job", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "marid_job"), EntityMaridJob.class,
                "marid_job", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "foliot"), EntityFoliot.class,
                "foliot", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "wondering_trader"),
                EntityWonderingTrader.class, "wondering_trader", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        // Append newly completed possessed variants after every prior 1.12 registration.
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "possessed_witch"),
                EntityPossessedWitch.class, "possessed_witch", nextEntityId++, Occultism.INSTANCE, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "possessed_blaze"),
                EntityPossessedBlaze.class, "possessed_blaze", nextEntityId++, Occultism.INSTANCE, 128, 3, true,
                0xF37B21, 0xFFF1A8);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "possessed_guardian"),
                EntityPossessedGuardian.class, "possessed_guardian", nextEntityId++, Occultism.INSTANCE, 128, 3,
                true, 0x7A9DAA, 0xD6E8E8);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "possessed_zombie_piglin"),
                EntityPossessedZombifiedPiglin.class, "possessed_zombie_piglin", nextEntityId++,
                Occultism.INSTANCE, 128, 3, true, 0xEA9393, 0x4E0E0E);
        // Append wild horde entities so every previously assigned 1.12 entity ID remains stable.
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "wild_horde_illager"),
                EntityWildHordeEvoker.class, "wild_horde_illager", nextEntityId++, Occultism.INSTANCE, 128, 3,
                true, 0x6A3C50, 0xA97489);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "wild_horde_husk"),
                EntityWildHordeHusk.class, "wild_horde_husk", nextEntityId++, Occultism.INSTANCE, 128, 3, true,
                0xB8875A, 0xD9B88C);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "wild_horde_parched"),
                EntityWildHordeParched.class, "wild_horde_parched", nextEntityId++, Occultism.INSTANCE, 128, 3,
                true, 0x6D7888, 0xB8C4D2);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "wild_horde_drowned"),
                EntityWildHordeDrowned.class, "wild_horde_drowned", nextEntityId++, Occultism.INSTANCE, 128, 3,
                true, 0x2F6F6F, 0x9DD6C0);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "wild_horde_creeper"),
                EntityWildHordeCreeper.class, "wild_horde_creeper", nextEntityId++, Occultism.INSTANCE, 128, 3,
                true, 0x2E8B57, 0x9BEF80);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "wild_horde_silverfish"),
                EntityWildHordeSilverfish.class, "wild_horde_silverfish", nextEntityId++, Occultism.INSTANCE, 128,
                3, true, 0x808080, 0xC0C0C0);
        // Append the unbound Marid after all existing registrations.
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MOD_ID, "marid_wild"), EntityMaridWild.class,
                "marid_wild", nextEntityId++, Occultism.INSTANCE, 128, 3, true, 0x2F7F9F, 0xA7E7F2);
    }
}
