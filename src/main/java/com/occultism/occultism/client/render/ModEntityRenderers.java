package com.occultism.occultism.client.render;

import com.occultism.occultism.entity.EntityBatFamiliar;
import com.occultism.occultism.entity.EntityAfritWild;
import com.occultism.occultism.entity.EntityBeholderFamiliar;
import com.occultism.occultism.entity.EntityBeaverFamiliar;
import com.occultism.occultism.entity.EntityBlacksmithFamiliar;
import com.occultism.occultism.entity.EntityBoundSpirit;
import com.occultism.occultism.entity.EntityCthulhuFamiliar;
import com.occultism.occultism.entity.EntityChimeraFamiliar;
import com.occultism.occultism.entity.EntityDeerFamiliar;
import com.occultism.occultism.entity.EntityDemonicHusband;
import com.occultism.occultism.entity.EntityDemonicWife;
import com.occultism.occultism.entity.EntityDevilFamiliar;
import com.occultism.occultism.entity.EntityDragonFamiliar;
import com.occultism.occultism.entity.EntityFairyFamiliar;
import com.occultism.occultism.entity.EntityFoliotJob;
import com.occultism.occultism.entity.EntityGreedyFamiliar;
import com.occultism.occultism.entity.EntityGoatFamiliar;
import com.occultism.occultism.entity.EntityGuardianFamiliar;
import com.occultism.occultism.entity.EntityHeadlessFamiliar;
import com.occultism.occultism.entity.EntityMercyGoat;
import com.occultism.occultism.entity.EntityMummyFamiliar;
import com.occultism.occultism.entity.EntityOtherworldBird;
import com.occultism.occultism.entity.EntityPossessedBee;
import com.occultism.occultism.entity.EntityPossessedElderGuardian;
import com.occultism.occultism.entity.EntityPossessedEnderman;
import com.occultism.occultism.entity.EntityPossessedEndermite;
import com.occultism.occultism.entity.EntityPossessedGhast;
import com.occultism.occultism.entity.EntityPossessedHoglin;
import com.occultism.occultism.entity.EntityPossessedPhantom;
import com.occultism.occultism.entity.EntityPossessedWarden;
import com.occultism.occultism.entity.EntityPossessedShulker;
import com.occultism.occultism.entity.EntityPossessedSkeleton;
import com.occultism.occultism.entity.EntityPossessedWeakShulker;
import com.occultism.occultism.entity.EntityPurifiedAllay;
import com.occultism.occultism.entity.EntityShubNiggurathFamiliar;
import com.occultism.occultism.entity.EntityShubNiggurathSpawn;
import com.occultism.occultism.entity.EntityThrownSword;
import com.occultism.occultism.entity.EntityWildHuntSkeleton;
import com.occultism.occultism.entity.EntityWildHuntWitherSkeleton;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderElderGuardian;
import net.minecraft.client.renderer.entity.RenderEnderman;
import net.minecraft.client.renderer.entity.RenderEndermite;
import net.minecraft.client.renderer.entity.RenderGhast;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSheep;
import net.minecraft.client.renderer.entity.RenderShulker;
import net.minecraft.client.renderer.entity.RenderSkeleton;
import net.minecraft.client.renderer.entity.RenderWitherSkeleton;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ModEntityRenderers {

    private ModEntityRenderers() {
    }

    public static void register() {
        RenderingRegistry.registerEntityRenderingHandler(EntityFoliotJob.class,
                new IRenderFactory<EntityFoliotJob>() {
                    @Override
                    public Render<? super EntityFoliotJob> createRenderFor(RenderManager manager) {
                        return new RenderFoliotJob(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityBoundSpirit.class,
                new IRenderFactory<EntityBoundSpirit>() {
                    @Override
                    public Render<? super EntityBoundSpirit> createRenderFor(RenderManager manager) {
                        return new RenderBoundSpirit(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityAfritWild.class,
                new IRenderFactory<EntityAfritWild>() {
                    @Override
                    public Render<? super EntityAfritWild> createRenderFor(RenderManager manager) {
                        return new RenderAfritWild(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityOtherworldBird.class,
                new IRenderFactory<EntityOtherworldBird>() {
                    @Override
                    public Render<? super EntityOtherworldBird> createRenderFor(RenderManager manager) {
                        return new RenderOtherworldBird(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityMercyGoat.class,
                new IRenderFactory<EntityMercyGoat>() {
                    @Override
                    public Render<? super EntityMercyGoat> createRenderFor(RenderManager manager) {
                        return new RenderSheep(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityPossessedBee.class,
                new IRenderFactory<EntityPossessedBee>() {
                    @Override
                    public Render<? super EntityPossessedBee> createRenderFor(RenderManager manager) {
                        return new RenderPossessedBee(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityPurifiedAllay.class,
                new IRenderFactory<EntityPurifiedAllay>() {
                    @Override
                    public Render<? super EntityPurifiedAllay> createRenderFor(RenderManager manager) {
                        return new RenderPurifiedAllay(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityGreedyFamiliar.class,
                new IRenderFactory<EntityGreedyFamiliar>() {
                    @Override
                    public Render<? super EntityGreedyFamiliar> createRenderFor(RenderManager manager) {
                        return new RenderGreedyFamiliar(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityBatFamiliar.class,
                new IRenderFactory<EntityBatFamiliar>() {
                    @Override
                    public Render<? super EntityBatFamiliar> createRenderFor(RenderManager manager) {
                        return new RenderBatFamiliar(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityDeerFamiliar.class,
                new IRenderFactory<EntityDeerFamiliar>() {
                    @Override
                    public Render<? super EntityDeerFamiliar> createRenderFor(RenderManager manager) {
                        return new RenderDeerFamiliar(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityBeaverFamiliar.class,
                new IRenderFactory<EntityBeaverFamiliar>() {
                    @Override
                    public Render<? super EntityBeaverFamiliar> createRenderFor(RenderManager manager) {
                        return new RenderBeaverFamiliar(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityMummyFamiliar.class,
                new IRenderFactory<EntityMummyFamiliar>() {
                    @Override
                    public Render<? super EntityMummyFamiliar> createRenderFor(RenderManager manager) {
                        return new RenderMummyFamiliar(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityDevilFamiliar.class,
                new IRenderFactory<EntityDevilFamiliar>() {
                    @Override
                    public Render<? super EntityDevilFamiliar> createRenderFor(RenderManager manager) {
                        return new RenderDevilFamiliar(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityGuardianFamiliar.class,
                new IRenderFactory<EntityGuardianFamiliar>() {
                    @Override
                    public Render<? super EntityGuardianFamiliar> createRenderFor(RenderManager manager) {
                        return new RenderGuardianFamiliar(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityHeadlessFamiliar.class,
                new IRenderFactory<EntityHeadlessFamiliar>() {
                    @Override
                    public Render<? super EntityHeadlessFamiliar> createRenderFor(RenderManager manager) {
                        return new RenderHeadlessFamiliar(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityDemonicHusband.class,
                new IRenderFactory<EntityDemonicHusband>() {
                    @Override
                    public Render<? super EntityDemonicHusband> createRenderFor(RenderManager manager) {
                        return new RenderDemonicHusband(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityDemonicWife.class,
                new IRenderFactory<EntityDemonicWife>() {
                    @Override
                    public Render<? super EntityDemonicWife> createRenderFor(RenderManager manager) {
                        return new RenderDemonicWife(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityCthulhuFamiliar.class,
                new IRenderFactory<EntityCthulhuFamiliar>() {
                    @Override
                    public Render<? super EntityCthulhuFamiliar> createRenderFor(RenderManager manager) {
                        return new RenderCthulhuFamiliar(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityBlacksmithFamiliar.class,
                new IRenderFactory<EntityBlacksmithFamiliar>() {
                    @Override
                    public Render<? super EntityBlacksmithFamiliar> createRenderFor(RenderManager manager) {
                        return new RenderBlacksmithFamiliar(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityDragonFamiliar.class,
                new IRenderFactory<EntityDragonFamiliar>() {
                    @Override
                    public Render<? super EntityDragonFamiliar> createRenderFor(RenderManager manager) {
                        return new RenderDragonFamiliar(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityThrownSword.class,
                new IRenderFactory<EntityThrownSword>() {
                    @Override
                    public Render<? super EntityThrownSword> createRenderFor(RenderManager manager) {
                        return new RenderThrownSword(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityFairyFamiliar.class,
                new IRenderFactory<EntityFairyFamiliar>() {
                    @Override
                    public Render<? super EntityFairyFamiliar> createRenderFor(RenderManager manager) {
                        return new RenderFairyFamiliar(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityGoatFamiliar.class,
                new IRenderFactory<EntityGoatFamiliar>() {
                    @Override
                    public Render<? super EntityGoatFamiliar> createRenderFor(RenderManager manager) {
                        return new RenderGoatFamiliar(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityShubNiggurathFamiliar.class,
                new IRenderFactory<EntityShubNiggurathFamiliar>() {
                    @Override
                    public Render<? super EntityShubNiggurathFamiliar> createRenderFor(RenderManager manager) {
                        return new RenderShubNiggurathFamiliar(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityShubNiggurathSpawn.class,
                new IRenderFactory<EntityShubNiggurathSpawn>() {
                    @Override
                    public Render<? super EntityShubNiggurathSpawn> createRenderFor(RenderManager manager) {
                        return new RenderShubNiggurathSpawn(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityBeholderFamiliar.class,
                new IRenderFactory<EntityBeholderFamiliar>() {
                    @Override
                    public Render<? super EntityBeholderFamiliar> createRenderFor(RenderManager manager) {
                        return new RenderBeholderFamiliar(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityChimeraFamiliar.class,
                new IRenderFactory<EntityChimeraFamiliar>() {
                    @Override
                    public Render<? super EntityChimeraFamiliar> createRenderFor(RenderManager manager) {
                        return new RenderChimeraFamiliar(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityWildHuntSkeleton.class,
                new IRenderFactory<EntityWildHuntSkeleton>() {
                    @Override
                    public Render<? super EntityWildHuntSkeleton> createRenderFor(RenderManager manager) {
                        return new RenderSkeleton(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityWildHuntWitherSkeleton.class,
                new IRenderFactory<EntityWildHuntWitherSkeleton>() {
                    @Override
                    public Render<? super EntityWildHuntWitherSkeleton> createRenderFor(RenderManager manager) {
                        return new RenderWitherSkeleton(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityPossessedEndermite.class,
                new IRenderFactory<EntityPossessedEndermite>() {
                    @Override
                    public Render<? super EntityPossessedEndermite> createRenderFor(RenderManager manager) {
                        return new RenderEndermite(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityPossessedSkeleton.class,
                new IRenderFactory<EntityPossessedSkeleton>() {
                    @Override
                    public Render<? super EntityPossessedSkeleton> createRenderFor(RenderManager manager) {
                        return new RenderSkeleton(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityPossessedEnderman.class,
                new IRenderFactory<EntityPossessedEnderman>() {
                    @Override
                    public Render<? super EntityPossessedEnderman> createRenderFor(RenderManager manager) {
                        return new RenderEnderman(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityPossessedGhast.class,
                new IRenderFactory<EntityPossessedGhast>() {
                    @Override
                    public Render<? super EntityPossessedGhast> createRenderFor(RenderManager manager) {
                        return new RenderGhast(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityPossessedWeakShulker.class,
                new IRenderFactory<EntityPossessedWeakShulker>() {
                    @Override
                    public Render<? super EntityPossessedWeakShulker> createRenderFor(RenderManager manager) {
                        return new RenderShulker(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityPossessedShulker.class,
                new IRenderFactory<EntityPossessedShulker>() {
                    @Override
                    public Render<? super EntityPossessedShulker> createRenderFor(RenderManager manager) {
                        return new RenderShulker(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityPossessedElderGuardian.class,
                new IRenderFactory<EntityPossessedElderGuardian>() {
                    @Override
                    public Render<? super EntityPossessedElderGuardian> createRenderFor(RenderManager manager) {
                        return new RenderElderGuardian(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityPossessedPhantom.class,
                new IRenderFactory<EntityPossessedPhantom>() {
                    @Override
                    public Render<? super EntityPossessedPhantom> createRenderFor(RenderManager manager) {
                        return new RenderPossessedPhantom(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityPossessedHoglin.class,
                new IRenderFactory<EntityPossessedHoglin>() {
                    @Override
                    public Render<? super EntityPossessedHoglin> createRenderFor(RenderManager manager) {
                        return new RenderPossessedHoglin(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityPossessedWarden.class,
                new IRenderFactory<EntityPossessedWarden>() {
                    @Override
                    public Render<? super EntityPossessedWarden> createRenderFor(RenderManager manager) {
                        return new RenderPossessedWarden(manager);
                    }
                });
    }
}
