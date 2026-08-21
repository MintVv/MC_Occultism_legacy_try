package com.occultism.occultism;

import com.occultism.occultism.Tags;
import com.occultism.occultism.command.CommandOccultism;
import com.occultism.occultism.gui.ModGuiHandler;
import com.occultism.occultism.network.ModNetworking;
import com.occultism.occultism.registry.ModEntities;
import com.occultism.occultism.registry.ModAdvancements;
import com.occultism.occultism.registry.ModCapabilities;
import com.occultism.occultism.registry.ModOreDictionary;
import com.occultism.occultism.registry.ModRecipes;
import com.occultism.occultism.ritual.RitualRecipeManager;
import com.occultism.occultism.tileentity.TileEntityDimensionalBattlefield;
import com.occultism.occultism.tileentity.TileEntityDimensionalMineshaft;
import com.occultism.occultism.tileentity.TileEntityEntityWormhole;
import com.occultism.occultism.tileentity.TileEntitySacrificialBowl;
import com.occultism.occultism.tileentity.TileEntityStableWormhole;
import com.occultism.occultism.tileentity.TileEntityStorageController;
import com.occultism.occultism.world.ModWorldGenerator;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class Occultism {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    public static final DebugSettings DEBUG = new DebugSettings();

    @Mod.Instance(Tags.MOD_ID)
    public static Occultism INSTANCE;

    /**
     * <a href="https://cleanroommc.com/wiki/forge-mod-development/event#overview">
     *     Take a look at how many FMLStateEvents you can listen to via the @Mod.EventHandler annotation here
     * </a>
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Hello From {}!", Tags.MOD_NAME);
        ModAdvancements.register();
        ModEntities.register();
        ModCapabilities.register();
        if (event.getSide().isClient()) {
            com.occultism.occultism.client.render.ModEntityRenderers.register();
        }
        GameRegistry.registerWorldGenerator(new ModWorldGenerator(), 20);
        GameRegistry.registerTileEntity(TileEntitySacrificialBowl.class, Tags.MOD_ID + ":sacrificial_bowl");
        GameRegistry.registerTileEntity(TileEntityDimensionalMineshaft.class, Tags.MOD_ID + ":dimensional_mineshaft");
        GameRegistry.registerTileEntity(TileEntityDimensionalBattlefield.class,
                Tags.MOD_ID + ":dimensional_battlefield");
        GameRegistry.registerTileEntity(TileEntityStorageController.class, Tags.MOD_ID + ":storage_controller");
        GameRegistry.registerTileEntity(TileEntityEntityWormhole.class, Tags.MOD_ID + ":entity_wormhole");
        GameRegistry.registerTileEntity(TileEntityStableWormhole.class, Tags.MOD_ID + ":stable_wormhole");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModOreDictionary.register();
        ModRecipes.register();
        RitualRecipeManager.load();
        ModNetworking.register();
        if (event.getSide().isClient()) {
            com.occultism.occultism.client.ClientFamiliarSettingsHandler.register();
        }
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new ModGuiHandler());
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandOccultism());
    }

    public static class DebugSettings {
        public boolean debugAI;
    }

}
