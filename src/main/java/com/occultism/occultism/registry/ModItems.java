package com.occultism.occultism.registry;

import com.occultism.occultism.Tags;
import com.occultism.occultism.block.BlockChalkGlyph;
import com.occultism.occultism.item.ItemBookOfBindingBound;
import com.occultism.occultism.item.ItemBookOfCallingFoliotJob;
import com.occultism.occultism.item.ItemBrush;
import com.occultism.occultism.item.ItemButcherKnife;
import com.occultism.occultism.item.ItemChalk;
import com.occultism.occultism.item.ItemDamageInCrafting;
import com.occultism.occultism.item.ItemDemonDreamFood;
import com.occultism.occultism.item.ItemDebugSummonSpirit;
import com.occultism.occultism.item.ItemDictionaryOfSpirits;
import com.occultism.occultism.item.ItemDimensionalMatrix;
import com.occultism.occultism.item.ItemDivinationRod;
import com.occultism.occultism.item.ItemEnderSatchel;
import com.occultism.occultism.item.ItemFamiliarRing;
import com.occultism.occultism.item.ItemFoliotJobToken;
import com.occultism.occultism.item.ItemIesniumPickaxe;
import com.occultism.occultism.item.ItemInfusedPickaxe;
import com.occultism.occultism.item.ItemKnowledgeTablet;
import com.occultism.occultism.item.ItemMagicLamp;
import com.occultism.occultism.item.ItemMinerSpirit;
import com.occultism.occultism.item.ItemNaturePaste;
import com.occultism.occultism.item.ItemOccultismHorseArmor;
import com.occultism.occultism.item.ItemOtherworldGoggles;
import com.occultism.occultism.item.ItemRitualDummy;
import com.occultism.occultism.item.ItemSatchel;
import com.occultism.occultism.item.ItemSoulGem;
import com.occultism.occultism.item.ItemSoulShard;
import com.occultism.occultism.item.ItemStorageRemote;
import com.occultism.occultism.item.ItemVitalityCompass;
import com.occultism.occultism.spirit.SpiritJobType;
import com.occultism.occultism.spirit.SpiritTier;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.entity.passive.HorseArmorType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSeeds;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ModItems {

    private static final Item.ToolMaterial SPIRIT_ATTUNED_TOOL_MATERIAL =
            EnumHelper.addToolMaterial("OCCULTISM_SPIRIT_ATTUNED", 3, Item.ToolMaterial.STONE.getMaxUses(),
                    9.0F, 3.0F, 22);
    private static final Item.ToolMaterial IESNIUM_TOOL_MATERIAL =
            EnumHelper.addToolMaterial("OCCULTISM_IESNIUM", 3, 1561, 10.0F, 4.0F, 24);
    private static final ItemArmor.ArmorMaterial OTHERWORLD_GOGGLES_MATERIAL =
            EnumHelper.addArmorMaterial("OCCULTISM_OTHERWORLD_GOGGLES", Tags.MOD_ID + ":otherworld_goggles",
                    5, new int[] {0, 0, 0, 0}, 12, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.0F);
    private static final HorseArmorType COPPER_HORSE_ARMOR_TYPE =
            EnumHelper.addHorseArmor("OCCULTISM_COPPER", "textures/entity/horse/armor/horse_armor_iron.png", 4);

    public static final Item DICTIONARY_OF_SPIRITS = dictionary("dictionary_of_spirits");
    public static final Item BOOK_OF_BINDING_EMPTY = item("book_of_binding_empty", 1);
    public static final Item BOOK_OF_BINDING_FOLIOT = item("book_of_binding_foliot", 1);
    public static final Item BOOK_OF_BINDING_BOUND_FOLIOT = boundBook("book_of_binding_bound_foliot");
    public static final Item BOOK_OF_BINDING_DJINNI = item("book_of_binding_djinni", 1);
    public static final Item BOOK_OF_BINDING_AFRIT = item("book_of_binding_afrit", 1);
    public static final Item BOOK_OF_BINDING_MARID = item("book_of_binding_marid", 1);
    public static final Item BOOK_OF_CALLING_FOLIOT_CLEANER =
            callingBook("book_of_calling_foliot_cleaner", SpiritJobType.CLEANER);
    public static final Item BOOK_OF_CALLING_FOLIOT_FARMER =
            callingBook("book_of_calling_foliot_farmer", SpiritJobType.FARMER);
    public static final Item BOOK_OF_CALLING_FOLIOT_LUMBERJACK =
            callingBook("book_of_calling_foliot_lumberjack", SpiritJobType.LUMBERJACK);
    public static final Item BOOK_OF_CALLING_FOLIOT_TRANSPORT_ITEMS =
            callingBook("book_of_calling_foliot_transport_items", SpiritJobType.TRANSPORT_ITEMS);
    public static final Item BOOK_OF_CALLING_FOLIOT_SMELTER =
            callingBook("book_of_calling_foliot_smelter", SpiritJobType.SMELTER);
    public static final Item BOOK_OF_CALLING_FOLIOT_CRUSHER =
            callingBook("book_of_calling_foliot_crusher", SpiritJobType.CRUSHER);
    public static final Item BOOK_OF_CALLING_FOLIOT_CRYSTALLIZER =
            callingBook("book_of_calling_foliot_crystallizer", SpiritJobType.CRYSTALLIZER);
    public static final Item BOOK_OF_CALLING_DJINNI_SMELTER =
            callingBook("book_of_calling_djinni_smelter", SpiritJobType.SMELTER, SpiritTier.DJINNI);
    public static final Item BOOK_OF_CALLING_DJINNI_CRUSHER =
            callingBook("book_of_calling_djinni_crusher", SpiritJobType.CRUSHER, SpiritTier.DJINNI);
    public static final Item BOOK_OF_CALLING_DJINNI_CRYSTALLIZER =
            callingBook("book_of_calling_djinni_crystallizer", SpiritJobType.CRYSTALLIZER, SpiritTier.DJINNI);
    public static final Item BOOK_OF_CALLING_DJINNI_GAMBLER =
            callingBook("book_of_calling_djinni_gambler", SpiritJobType.GAMBLER, SpiritTier.DJINNI);
    public static final Item BOOK_OF_CALLING_DJINNI_MANAGE_MACHINE =
            callingBook("book_of_calling_djinni_manage_machine", SpiritJobType.MANAGE_MACHINE, SpiritTier.DJINNI);
    public static final Item BOOK_OF_CALLING_AFRIT_SMELTER =
            callingBook("book_of_calling_afrit_smelter", SpiritJobType.SMELTER, SpiritTier.AFRIT);
    public static final Item BOOK_OF_CALLING_AFRIT_CRUSHER =
            callingBook("book_of_calling_afrit_crusher", SpiritJobType.CRUSHER, SpiritTier.AFRIT);
    public static final Item BOOK_OF_CALLING_AFRIT_CRYSTALLIZER =
            callingBook("book_of_calling_afrit_crystallizer", SpiritJobType.CRYSTALLIZER, SpiritTier.AFRIT);
    public static final Item BOOK_OF_CALLING_MARID_SMELTER =
            callingBook("book_of_calling_marid_smelter", SpiritJobType.SMELTER, SpiritTier.MARID);
    public static final Item BOOK_OF_CALLING_MARID_CRUSHER =
            callingBook("book_of_calling_marid_crusher", SpiritJobType.CRUSHER, SpiritTier.MARID);
    public static final Item BOOK_OF_CALLING_MARID_CRYSTALLIZER =
            callingBook("book_of_calling_marid_crystallizer", SpiritJobType.CRYSTALLIZER, SpiritTier.MARID);
    public static final Item BOOK_OF_CALLING_FOLIOT_OTHERSTONE_TRADER =
            callingBook("book_of_calling_foliot_otherstone_trader", SpiritJobType.TRADER_OTHERSTONE);
    public static final Item BOOK_OF_CALLING_FOLIOT_OTHERROCK_TRADER =
            callingBook("book_of_calling_foliot_otherrock_trader", SpiritJobType.TRADER_OTHERROCK);
    public static final Item BOOK_OF_CALLING_FOLIOT_SAPLING_TRADER =
            callingBook("book_of_calling_foliot_sapling_trader", SpiritJobType.TRADER_SAPLING);
    public static final Item FOLIOT_SPAWN_TOKEN = item("foliot_spawn_token", 1);
    public static final Item FOLIOT_CRUSHER_TOKEN = jobToken("foliot_crusher_token", SpiritJobType.CRUSHER);
    public static final Item FOLIOT_CRYSTALLIZER_TOKEN =
            jobToken("foliot_crystallizer_token", SpiritJobType.CRYSTALLIZER);
    public static final Item RITUAL_DUMMY_SUMMON_FOLIOT_CLEANER =
            item("ritual_dummy/summon_foliot_cleaner", "ritual_dummy.summon_foliot_cleaner", 1);
    public static final Item RITUAL_DUMMY_SUMMON_FOLIOT_TRANSPORT_ITEMS =
            item("ritual_dummy/summon_foliot_transport_items", "ritual_dummy.summon_foliot_transport_items", 1);
    public static final Item RITUAL_DUMMY_SUMMON_FOLIOT_FARMER =
            item("ritual_dummy/summon_foliot_farmer", "ritual_dummy.summon_foliot_farmer", 1);
    public static final Item RITUAL_DUMMY_SUMMON_FOLIOT_LUMBERJACK =
            item("ritual_dummy/summon_foliot_lumberjack", "ritual_dummy.summon_foliot_lumberjack", 1);
    public static final Item RITUAL_DUMMY_SUMMON_FOLIOT_SMELTER =
            item("ritual_dummy/summon_foliot_smelter", "ritual_dummy.summon_foliot_smelter", 1);
    public static final Item RITUAL_DUMMY_SUMMON_FOLIOT_CRUSHER =
            item("ritual_dummy/summon_foliot_crusher", "ritual_dummy.summon_foliot_crusher", 1);
    public static final Item RITUAL_DUMMY_SUMMON_FOLIOT_CRYSTALLIZER =
            item("ritual_dummy/summon_foliot_crystallizer", "ritual_dummy.summon_foliot_crystallizer", 1);
    public static final Item RITUAL_DUMMY_SUMMON_FOLIOT_OTHERSTONE_TRADER =
            item("ritual_dummy/summon_foliot_otherstone_trader", "ritual_dummy.summon_foliot_otherstone_trader", 1);
    public static final Item RITUAL_DUMMY_SUMMON_FOLIOT_OTHERROCK_TRADER =
            item("ritual_dummy/summon_foliot_otherrock_trader", "ritual_dummy.summon_foliot_otherrock_trader", 1);
    public static final Item RITUAL_DUMMY_SUMMON_FOLIOT_SAPLING_TRADER =
            item("ritual_dummy/summon_foliot_sapling_trader", "ritual_dummy.summon_foliot_sapling_trader", 1);
    public static final Item RITUAL_DUMMY_POSSESS_SKELETON =
            item("ritual_dummy/possess_skeleton", "ritual_dummy.possess_skeleton", 1);
    public static final Item RITUAL_DUMMY_POSSESS_ENDERMITE =
            item("ritual_dummy/possess_endermite", "ritual_dummy.possess_endermite", 1);
    public static final Item RITUAL_DUMMY_POSSESS_WITCH =
            item("ritual_dummy/possess_witch", "ritual_dummy.possess_witch", 1);
    public static final Item RITUAL_DUMMY_POSSESS_PHANTOM =
            item("ritual_dummy/possess_phantom", "ritual_dummy.possess_phantom", 1);
    public static final Item RITUAL_DUMMY_POSSESS_ENDERMAN =
            item("ritual_dummy/possess_enderman", "ritual_dummy.possess_enderman", 1);
    public static final Item RITUAL_DUMMY_POSSESS_BLAZE =
            item("ritual_dummy/possess_blaze", "ritual_dummy.possess_blaze", 1);
    public static final Item RITUAL_DUMMY_POSSESS_GHAST =
            item("ritual_dummy/possess_ghast", "ritual_dummy.possess_ghast", 1);
    public static final Item RITUAL_DUMMY_POSSESS_UNBOUND_PARROT =
            item("ritual_dummy/possess_unbound_parrot", "ritual_dummy.possess_unbound_parrot", 1);
    public static final Item RITUAL_DUMMY_FAMILIAR_PARROT =
            item("ritual_dummy/familiar_parrot", "ritual_dummy.familiar_parrot", 1);
    public static final Item RITUAL_DUMMY_FAMILIAR_BAT =
            item("ritual_dummy/familiar_bat", "ritual_dummy.familiar_bat", 1);
    public static final Item RITUAL_DUMMY_FAMILIAR_CTHULHU =
            item("ritual_dummy/familiar_cthulhu", "ritual_dummy.familiar_cthulhu", 1);
    public static final Item RITUAL_DUMMY_FAMILIAR_BLACKSMITH =
            item("ritual_dummy/familiar_blacksmith", "ritual_dummy.familiar_blacksmith", 1);
    public static final Item RITUAL_DUMMY_FAMILIAR_GREEDY =
            item("ritual_dummy/familiar_greedy", "ritual_dummy.familiar_greedy", 1);
    public static final Item RITUAL_DUMMY_FAMILIAR_DRAGON =
            item("ritual_dummy/familiar_dragon", "ritual_dummy.familiar_dragon", 1);
    public static final Item RITUAL_DUMMY_FAMILIAR_FAIRY =
            item("ritual_dummy/familiar_fairy", "ritual_dummy.familiar_fairy", 1);
    public static final Item RITUAL_DUMMY_FAMILIAR_BEHOLDER =
            item("ritual_dummy/familiar_beholder", "ritual_dummy.familiar_beholder", 1);
    public static final Item RITUAL_DUMMY_FAMILIAR_CHIMERA =
            item("ritual_dummy/familiar_chimera", "ritual_dummy.familiar_chimera", 1);
    public static final Item RITUAL_DUMMY_FAMILIAR_DEER =
            item("ritual_dummy/familiar_deer", "ritual_dummy.familiar_deer", 1);
    public static final Item RITUAL_DUMMY_FAMILIAR_BEAVER =
            item("ritual_dummy/familiar_beaver", "ritual_dummy.familiar_beaver", 1);
    public static final Item RITUAL_DUMMY_FAMILIAR_MUMMY =
            item("ritual_dummy/familiar_mummy", "ritual_dummy.familiar_mummy", 1);
    public static final Item RITUAL_DUMMY_FAMILIAR_DEVIL =
            item("ritual_dummy/familiar_devil", "ritual_dummy.familiar_devil", 1);
    public static final Item RITUAL_DUMMY_FAMILIAR_GUARDIAN =
            item("ritual_dummy/familiar_guardian", "ritual_dummy.familiar_guardian", 1);
    public static final Item RITUAL_DUMMY_FAMILIAR_HEADLESS =
            item("ritual_dummy/familiar_headless", "ritual_dummy.familiar_headless", 1);
    public static final Item RITUAL_DUMMY_FAMILIAR_OTHERWORLD_BIRD =
            item("ritual_dummy/familiar_otherworld_bird", "ritual_dummy.familiar_otherworld_bird", 1);
    public static final Item RITUAL_DUMMY_SUMMON_WILD_OTHERWORLD_BIRD =
            item("ritual_dummy/summon_wild_otherworld_bird", "ritual_dummy.summon_wild_otherworld_bird", 1);
    public static final Item RITUAL_DUMMY_SUMMON_DEMONIC_HUSBAND =
            item("ritual_dummy/summon_demonic_husband", "ritual_dummy.summon_demonic_husband", 1);
    public static final Item RITUAL_DUMMY_SUMMON_DEMONIC_WIFE =
            item("ritual_dummy/summon_demonic_wife", "ritual_dummy.summon_demonic_wife", 1);
    public static final Item RITUAL_DUMMY_POSSESS_UNBOUND_OTHERWORLD_BIRD =
            item("ritual_dummy/possess_unbound_otherworld_bird",
                    "ritual_dummy.possess_unbound_otherworld_bird", 1);
    public static final Item RITUAL_DUMMY_POSSESS_WEAK_SHULKER =
            item("ritual_dummy/possess_weak_shulker", "ritual_dummy.possess_weak_shulker", 1);
    public static final Item RITUAL_DUMMY_POSSESS_SHULKER =
            item("ritual_dummy/possess_shulker", "ritual_dummy.possess_shulker", 1);
    public static final Item RITUAL_DUMMY_POSSESS_GUARDIAN =
            item("ritual_dummy/possess_guardian", "ritual_dummy.possess_guardian", 1);
    public static final Item RITUAL_DUMMY_POSSESS_ELDER_GUARDIAN =
            item("ritual_dummy/possess_elder_guardian", "ritual_dummy.possess_elder_guardian", 1);
    public static final Item RITUAL_DUMMY_POSSESS_ZOMBIFIED_PIGLIN =
            item("ritual_dummy/possess_zombified_piglin", "ritual_dummy.possess_zombified_piglin", 1);
    public static final Item RITUAL_DUMMY_POSSESS_HOGLIN =
            item("ritual_dummy/possess_hoglin", "ritual_dummy.possess_hoglin", 1);
    public static final Item RITUAL_DUMMY_POSSESS_WARDEN =
            item("ritual_dummy/possess_warden", "ritual_dummy.possess_warden", 1);
    public static final Item RITUAL_DUMMY_POSSESS_GOAT =
            item("ritual_dummy/possess_goat", "ritual_dummy.possess_goat", 1);
    public static final Item RITUAL_DUMMY_POSSESS_BEE =
            item("ritual_dummy/possess_bee", "ritual_dummy.possess_bee", 1);
    public static final Item RITUAL_DUMMY_POSSESS_IESNIUM_GOLEM =
            item("ritual_dummy/possess_iesnium_golem", "ritual_dummy.possess_iesnium_golem", 1);
    public static final Item RITUAL_DUMMY_POSSESS_RANDOM_ANIMAL_COMMON =
            item("ritual_dummy/possess_random_animal_common", "ritual_dummy.possess_random_animal_common", 1);
    public static final Item RITUAL_DUMMY_POSSESS_RANDOM_ANIMAL_RIDEABLE =
            item("ritual_dummy/possess_random_animal_rideable", "ritual_dummy.possess_random_animal_rideable", 1);
    public static final Item RITUAL_DUMMY_POSSESS_RANDOM_ANIMAL_SMALL =
            item("ritual_dummy/possess_random_animal_small", "ritual_dummy.possess_random_animal_small", 1);
    public static final Item RITUAL_DUMMY_POSSESS_RANDOM_ANIMAL_SPECIAL =
            item("ritual_dummy/possess_random_animal_special", "ritual_dummy.possess_random_animal_special", 1);
    public static final Item RITUAL_DUMMY_POSSESS_RANDOM_ANIMAL_WATER =
            item("ritual_dummy/possess_random_animal_water", "ritual_dummy.possess_random_animal_water", 1);
    public static final Item RITUAL_DUMMY_POSSESS_VILLAGER =
            item("ritual_dummy/possess_villager", "ritual_dummy.possess_villager", 1);
    public static final Item RITUAL_DUMMY_WILD_RANDOM_ANIMAL_COMMON =
            item("ritual_dummy/wild_random_animal_common", "ritual_dummy.wild_random_animal_common", 1);
    public static final Item RITUAL_DUMMY_WILD_RANDOM_ANIMAL_RIDEABLE =
            item("ritual_dummy/wild_random_animal_rideable", "ritual_dummy.wild_random_animal_rideable", 1);
    public static final Item RITUAL_DUMMY_WILD_RANDOM_ANIMAL_SMALL =
            item("ritual_dummy/wild_random_animal_small", "ritual_dummy.wild_random_animal_small", 1);
    public static final Item RITUAL_DUMMY_WILD_RANDOM_ANIMAL_SPECIAL =
            item("ritual_dummy/wild_random_animal_special", "ritual_dummy.wild_random_animal_special", 1);
    public static final Item RITUAL_DUMMY_WILD_RANDOM_ANIMAL_WATER =
            item("ritual_dummy/wild_random_animal_water", "ritual_dummy.wild_random_animal_water", 1);
    public static final Item RITUAL_DUMMY_WILD_VILLAGER =
            item("ritual_dummy/wild_villager", "ritual_dummy.wild_villager", 1);
    public static final Item RITUAL_DUMMY_WILD_HUNT =
            legacyRitualDummy("ritual_dummy/wild_hunt", "ritual_dummy.wild_hunt");
    public static final Item RITUAL_DUMMY_WILD_HORDE_ILLAGER =
            item("ritual_dummy/wild_horde_illager", "ritual_dummy.wild_horde_illager", 1);
    public static final Item RITUAL_DUMMY_WILD_CREEPER =
            item("ritual_dummy/wild_creeper", "ritual_dummy.wild_creeper", 1);
    public static final Item RITUAL_DUMMY_WILD_HUSK =
            item("ritual_dummy/wild_husk", "ritual_dummy.wild_husk", 1);
    public static final Item RITUAL_DUMMY_WILD_PARCHED =
            item("ritual_dummy/wild_parched", "ritual_dummy.wild_parched", 1);
    public static final Item RITUAL_DUMMY_WILD_DROWNED =
            item("ritual_dummy/wild_drowned", "ritual_dummy.wild_drowned", 1);
    public static final Item RITUAL_DUMMY_WILD_SILVERFISH =
            item("ritual_dummy/wild_silverfish", "ritual_dummy.wild_silverfish", 1);
    public static final Item RITUAL_DUMMY_RESURRECT_FAMILIAR =
            item("ritual_dummy/resurrect_familiar", "ritual_dummy.resurrect_familiar", 1);
    public static final Item RITUAL_DUMMY_RESURRECT_ALLAY =
            item("ritual_dummy/resurrect_allay", "ritual_dummy.resurrect_allay", 1);
    public static final Item RITUAL_DUMMY_RESURRECT_MOB =
            item("ritual_dummy/resurrect_mob", "ritual_dummy.resurrect_mob", 1);
    public static final Item RITUAL_DUMMY_SUMMON_DJINNI_CLEAR_WEATHER =
            item("ritual_dummy/summon_djinni_clear_weather", "ritual_dummy.summon_djinni_clear_weather", 1);
    public static final Item RITUAL_DUMMY_SUMMON_DJINNI_DAY_TIME =
            item("ritual_dummy/summon_djinni_day_time", "ritual_dummy.summon_djinni_day_time", 1);
    public static final Item RITUAL_DUMMY_SUMMON_DJINNI_NIGHT_TIME =
            item("ritual_dummy/summon_djinni_night_time", "ritual_dummy.summon_djinni_night_time", 1);
    public static final Item RITUAL_DUMMY_SUMMON_DJINNI_SMELTER =
            item("ritual_dummy/summon_djinni_smelter", "ritual_dummy.summon_djinni_smelter", 1);
    public static final Item RITUAL_DUMMY_SUMMON_DJINNI_CRUSHER =
            item("ritual_dummy/summon_djinni_crusher", "ritual_dummy.summon_djinni_crusher", 1);
    public static final Item RITUAL_DUMMY_SUMMON_DJINNI_CRYSTALLIZER =
            item("ritual_dummy/summon_djinni_crystallizer", "ritual_dummy.summon_djinni_crystallizer", 1);
    public static final Item RITUAL_DUMMY_SUMMON_DJINNI_GAMBLER =
            item("ritual_dummy/summon_djinni_gambler", "ritual_dummy.summon_djinni_gambler", 1);
    public static final Item RITUAL_DUMMY_SUMMON_DJINNI_MANAGE_MACHINE =
            item("ritual_dummy/summon_djinni_manage_machine", "ritual_dummy.summon_djinni_manage_machine", 1);
    public static final Item RITUAL_DUMMY_SUMMON_AFRIT_SMELTER =
            item("ritual_dummy/summon_afrit_smelter", "ritual_dummy.summon_afrit_smelter", 1);
    public static final Item RITUAL_DUMMY_SUMMON_AFRIT_CRUSHER =
            item("ritual_dummy/summon_afrit_crusher", "ritual_dummy.summon_afrit_crusher", 1);
    public static final Item RITUAL_DUMMY_SUMMON_AFRIT_CRYSTALLIZER =
            item("ritual_dummy/summon_afrit_crystallizer", "ritual_dummy.summon_afrit_crystallizer", 1);
    public static final Item RITUAL_DUMMY_SUMMON_MARID_SMELTER =
            item("ritual_dummy/summon_marid_smelter", "ritual_dummy.summon_marid_smelter", 1);
    public static final Item RITUAL_DUMMY_SUMMON_MARID_CRUSHER =
            item("ritual_dummy/summon_marid_crusher", "ritual_dummy.summon_marid_crusher", 1);
    public static final Item RITUAL_DUMMY_SUMMON_MARID_CRYSTALLIZER =
            item("ritual_dummy/summon_marid_crystallizer", "ritual_dummy.summon_marid_crystallizer", 1);
    public static final Item RITUAL_DUMMY_SUMMON_WONDERING_TRADER =
            item("ritual_dummy/summon_wondering_trader", "ritual_dummy.summon_wondering_trader", 1);
    public static final Item RITUAL_DUMMY_SUMMON_AFRIT_RAIN_WEATHER =
            item("ritual_dummy/summon_afrit_rain_weather", "ritual_dummy.summon_afrit_rain_weather", 1);
    public static final Item RITUAL_DUMMY_SUMMON_AFRIT_THUNDER_WEATHER =
            item("ritual_dummy/summon_afrit_thunder_weather", "ritual_dummy.summon_afrit_thunder_weather", 1);
    public static final Item RITUAL_DUMMY_SUMMON_DJINNI_PROTOTYPE =
            item("ritual_dummy/summon_djinni_prototype", "ritual_dummy.summon_djinni_prototype", 1);
    public static final Item RITUAL_DUMMY_SUMMON_UNBOUND_AFRIT =
            item("ritual_dummy/summon_unbound_afrit", "ritual_dummy.summon_unbound_afrit", 1);
    public static final Item RITUAL_DUMMY_SUMMON_UNBOUND_AFRIT_PROTOTYPE =
            item("ritual_dummy/summon_unbound_afrit_prototype",
                    "ritual_dummy.summon_unbound_afrit_prototype", 1);
    public static final Item RITUAL_DUMMY_SUMMON_AFRIT_PROTOTYPE =
            item("ritual_dummy/summon_afrit_prototype", "ritual_dummy.summon_afrit_prototype", 1);
    public static final Item RITUAL_DUMMY_SUMMON_UNBOUND_MARID =
            item("ritual_dummy/summon_unbound_marid", "ritual_dummy.summon_unbound_marid", 1);
    public static final Item RITUAL_DUMMY_SUMMON_MARID_PROTOTYPE =
            item("ritual_dummy/summon_marid_prototype", "ritual_dummy.summon_marid_prototype", 1);
    public static final Item RITUAL_DUMMY_CRAFT_MINER_FOLIOT_UNSPECIALIZED =
            item("ritual_dummy/craft_miner_foliot_unspecialized",
                    "ritual_dummy.craft_miner_foliot_unspecialized", 1);
    public static final Item RITUAL_DUMMY_CRAFT_FRAGILE_SOUL_GEM =
            item("ritual_dummy/craft_fragile_soul_gem", "ritual_dummy.craft_fragile_soul_gem", 1);
    public static final Item RITUAL_DUMMY_CRAFT_NATURE_PASTE =
            item("ritual_dummy/craft_nature_paste", "ritual_dummy.craft_nature_paste", 1);
    public static final Item RITUAL_DUMMY_CRAFT_RESEARCH_FRAGMENT_DUST =
            item("ritual_dummy/craft_research_fragment_dust", "ritual_dummy.craft_research_fragment_dust", 1);
    public static final Item RITUAL_DUMMY_CRAFT_SATCHEL =
            item("ritual_dummy/craft_satchel", "ritual_dummy.craft_satchel", 1);
    public static final Item RITUAL_DUMMY_CRAFT_INFUSED_LENSES =
            item("ritual_dummy/craft_infused_lenses", "ritual_dummy.craft_infused_lenses", 1);
    public static final Item RITUAL_DUMMY_CRAFT_KNOWLEDGE_TABLET =
            item("ritual_dummy/craft_knowledge_tablet", "ritual_dummy.craft_knowledge_tablet", 1);
    public static final Item RITUAL_DUMMY_CRAFT_RITUAL_SATCHEL_T1 =
            item("ritual_dummy/craft_ritual_satchel_t1", "ritual_dummy.craft_ritual_satchel_t1", 1);
    public static final Item RITUAL_DUMMY_CRAFT_VITALITY_COMPASS =
            item("ritual_dummy/craft_vitality_compass", "ritual_dummy.craft_vitality_compass", 1);
    public static final Item RITUAL_DUMMY_CRAFT_STABLE_WORMHOLE =
            item("ritual_dummy/craft_stable_wormhole", "ritual_dummy.craft_stable_wormhole", 1);
    public static final Item RITUAL_DUMMY_CRAFT_STABILIZER_TIER1 =
            item("ritual_dummy/craft_stabilizer_tier1", "ritual_dummy.craft_stabilizer_tier1", 1);
    public static final Item RITUAL_DUMMY_CRAFT_STORAGE_CONTROLLER_BASE =
            item("ritual_dummy/craft_storage_controller_base", "ritual_dummy.craft_storage_controller_base", 1);
    public static final Item RITUAL_DUMMY_CRAFT_STABLE_WORMHOLE_DARK =
            item("ritual_dummy/craft_stable_wormhole_dark", "ritual_dummy.craft_stable_wormhole_dark", 1);
    public static final Item RITUAL_DUMMY_CRAFT_STABILIZER_TIER1_DARK =
            item("ritual_dummy/craft_stabilizer_tier1_dark", "ritual_dummy.craft_stabilizer_tier1_dark", 1);
    public static final Item RITUAL_DUMMY_CRAFT_STORAGE_CONTROLLER_BASE_DARK =
            item("ritual_dummy/craft_storage_controller_base_dark",
                    "ritual_dummy.craft_storage_controller_base_dark", 1);
    public static final Item RITUAL_DUMMY_CRAFT_STABILIZER_TIER2 =
            item("ritual_dummy/craft_stabilizer_tier2", "ritual_dummy.craft_stabilizer_tier2", 1);
    public static final Item RITUAL_DUMMY_CRAFT_STABILIZER_TIER2_DARK =
            item("ritual_dummy/craft_stabilizer_tier2_dark", "ritual_dummy.craft_stabilizer_tier2_dark", 1);
    public static final Item RITUAL_DUMMY_CRAFT_STABILIZER_TIER3 =
            item("ritual_dummy/craft_stabilizer_tier3", "ritual_dummy.craft_stabilizer_tier3", 1);
    public static final Item RITUAL_DUMMY_CRAFT_STABILIZER_TIER3_DARK =
            item("ritual_dummy/craft_stabilizer_tier3_dark", "ritual_dummy.craft_stabilizer_tier3_dark", 1);
    public static final Item RITUAL_DUMMY_CRAFT_STABILIZER_TIER4 =
            item("ritual_dummy/craft_stabilizer_tier4", "ritual_dummy.craft_stabilizer_tier4", 1);
    public static final Item RITUAL_DUMMY_CRAFT_STABILIZER_TIER4_DARK =
            item("ritual_dummy/craft_stabilizer_tier4_dark", "ritual_dummy.craft_stabilizer_tier4_dark", 1);
    public static final Item RITUAL_DUMMY_CRAFT_STORAGE_REMOTE =
            item("ritual_dummy/craft_storage_remote", "ritual_dummy.craft_storage_remote", 1);
    public static final Item RITUAL_DUMMY_CRAFT_SOUL_GEM =
            item("ritual_dummy/craft_soul_gem", "ritual_dummy.craft_soul_gem", 1);
    public static final Item RITUAL_DUMMY_CRAFT_INFUSED_PICKAXE =
            item("ritual_dummy/craft_infused_pickaxe", "ritual_dummy.craft_infused_pickaxe", 1);
    public static final Item RITUAL_DUMMY_CRAFT_SPIRIT_GRINDSTONE =
            item("ritual_dummy/craft_spirit_grindstone", "ritual_dummy.craft_spirit_grindstone", 1);
    public static final Item RITUAL_DUMMY_CRAFT_RITUAL_SATCHEL_T2 =
            item("ritual_dummy/craft_ritual_satchel_t2", "ritual_dummy.craft_ritual_satchel_t2", 1);
    public static final Item RITUAL_DUMMY_CRAFT_UPGRADE_RITUAL_SATCHEL =
            item("ritual_dummy/craft_upgrade_ritual_satchel", "ritual_dummy.craft_upgrade_ritual_satchel", 1);
    public static final Item RITUAL_DUMMY_CRAFT_DIMENSIONAL_MATRIX =
            item("ritual_dummy/craft_dimensional_matrix", "ritual_dummy.craft_dimensional_matrix", 1);
    public static final Item RITUAL_DUMMY_CRAFT_DIMENSIONAL_MINESHAFT =
            item("ritual_dummy/craft_dimensional_mineshaft", "ritual_dummy.craft_dimensional_mineshaft", 1);
    public static final Item RITUAL_DUMMY_CRAFT_DIMENSIONAL_EXTRACTOR =
            item("ritual_dummy/craft_dimensional_extractor", "ritual_dummy.craft_dimensional_extractor", 1);
    public static final Item RITUAL_DUMMY_CRAFT_ENTITY_WORMHOLE =
            item("ritual_dummy/craft_entity_wormhole", "ritual_dummy.craft_entity_wormhole", 1);
    public static final Item RITUAL_DUMMY_CRAFT_ENTITY_WORMHOLE_DARK =
            item("ritual_dummy/craft_entity_wormhole_dark", "ritual_dummy.craft_entity_wormhole_dark", 1);
    public static final Item RITUAL_DUMMY_CRAFT_GRAY_PASTE =
            item("ritual_dummy/craft_gray_paste", "ritual_dummy.craft_gray_paste", 1);
    public static final Item RITUAL_DUMMY_CRAFT_FAMILIAR_RING =
            item("ritual_dummy/craft_familiar_ring", "ritual_dummy.craft_familiar_ring", 1);
    public static final Item RITUAL_DUMMY_CRAFT_ENDER_SATCHEL =
            item("ritual_dummy/craft_ender_satchel", "ritual_dummy.craft_ender_satchel", 1);
    public static final Item RITUAL_DUMMY_CRAFT_WITHERITE_DUST =
            item("ritual_dummy/craft_witherite_dust", "ritual_dummy.craft_witherite_dust", 1);
    public static final Item RITUAL_DUMMY_CRAFT_DRAGONYST_DUST =
            item("ritual_dummy/craft_dragonyst_dust", "ritual_dummy.craft_dragonyst_dust", 1);
    public static final Item RITUAL_DUMMY_CRAFT_TRUE_SIGHT_STAFF =
            item("ritual_dummy/craft_true_sight_staff", "ritual_dummy.craft_true_sight_staff", 1);
    public static final Item RITUAL_DUMMY_CRAFT_MINER_DJINNI_ORES =
            item("ritual_dummy/craft_miner_djinni_ores", "ritual_dummy.craft_miner_djinni_ores", 1);
    public static final Item RITUAL_DUMMY_CRAFT_MINER_AFRIT_DEEPS =
            item("ritual_dummy/craft_miner_afrit_deeps", "ritual_dummy.craft_miner_afrit_deeps", 1);
    public static final Item RITUAL_DUMMY_CRAFT_MINER_MARID_MASTER =
            item("ritual_dummy/craft_miner_marid_master", "ritual_dummy.craft_miner_marid_master", 1);
    public static final Item RITUAL_DUMMY_CRAFT_DARK_IESNIUM_SACRIFICIAL_BOWL =
            item("ritual_dummy/craft_dark_iesnium_sacrificial_bowl",
                    "ritual_dummy.craft_dark_iesnium_sacrificial_bowl", 1);
    public static final Item RITUAL_DUMMY_CRAFT_DIMENSIONAL_BATTLEFIELD =
            item("ritual_dummy/craft_dimensional_battlefield", "ritual_dummy.craft_dimensional_battlefield", 1);
    public static final Item RITUAL_DUMMY_CRAFT_IESNIUM_ANVIL =
            item("ritual_dummy/craft_iesnium_anvil", "ritual_dummy.craft_iesnium_anvil", 1);
    public static final Item RITUAL_DUMMY_CRAFT_IESNIUM_BUTCHER_KNIFE =
            item("ritual_dummy/craft_iesnium_butcher_knife", "ritual_dummy.craft_iesnium_butcher_knife", 1);
    public static final Item RITUAL_DUMMY_CRAFT_IESNIUM_SACRIFICIAL_BOWL =
            item("ritual_dummy/craft_iesnium_sacrificial_bowl",
                    "ritual_dummy.craft_iesnium_sacrificial_bowl", 1);
    public static final Item RITUAL_DUMMY_MISC_CHALK_RAINBOW =
            item("ritual_dummy/misc_chalk_rainbow", "ritual_dummy.misc_chalk_rainbow", 1);
    public static final Item RITUAL_DUMMY_MISC_CHALK_VOID =
            item("ritual_dummy/misc_chalk_void", "ritual_dummy.misc_chalk_void", 1);
    public static final Item RITUAL_DUMMY_MISC_TRINITY_GEM =
            item("ritual_dummy/misc_trinity_gem", "ritual_dummy.misc_trinity_gem", 1);
    public static final Item RITUAL_DUMMY_MISC_STABILIZER_TIER5 =
            item("ritual_dummy/misc_stabilizer_tier5", "ritual_dummy.misc_stabilizer_tier5", 1);
    public static final Item RITUAL_DUMMY_MISC_STABILIZER_TIER5_DARK =
            item("ritual_dummy/misc_stabilizer_tier5_dark", "ritual_dummy.misc_stabilizer_tier5_dark", 1);
    public static final Item RITUAL_DUMMY_MISC_STABILIZED_STORAGE =
            item("ritual_dummy/misc_stabilized_storage", "ritual_dummy.misc_stabilized_storage", 1);
    public static final Item RITUAL_DUMMY_MISC_STABILIZED_STORAGE_DARK =
            item("ritual_dummy/misc_stabilized_storage_dark", "ritual_dummy.misc_stabilized_storage_dark", 1);
    public static final Item RITUAL_DUMMY_MISC_MINER_ANCIENT_ELDRITCH =
            item("ritual_dummy/misc_miner_ancient_eldritch", "ritual_dummy.misc_miner_ancient_eldritch", 1);
    public static final Item RITUAL_DUMMY_MISC_IRON_HORSE_ARMOR =
            item("ritual_dummy/misc_iron_horse_armor", "ritual_dummy.misc_iron_horse_armor", 1);
    public static final Item RITUAL_DUMMY_MISC_GOLDEN_HORSE_ARMOR =
            item("ritual_dummy/misc_golden_horse_armor", "ritual_dummy.misc_golden_horse_armor", 1);
    public static final Item RITUAL_DUMMY_MISC_DIAMOND_HORSE_ARMOR =
            item("ritual_dummy/misc_diamond_horse_armor", "ritual_dummy.misc_diamond_horse_armor", 1);
    public static final Item RITUAL_DUMMY_MISC_COPPER_HORSE_ARMOR =
            item("ritual_dummy/misc_copper_horse_armor", "ritual_dummy.misc_copper_horse_armor", 1);
    public static final Item RITUAL_DUMMY_MISC_BEE_NEST =
            item("ritual_dummy/misc_bee_nest", "ritual_dummy.misc_bee_nest", 1);
    public static final Item RITUAL_DUMMY_MISC_BELL =
            item("ritual_dummy/misc_bell", "ritual_dummy.misc_bell", 1);
    public static final Item RITUAL_DUMMY_MISC_BUDDING_AMETHYST =
            item("ritual_dummy/misc_budding_amethyst", "ritual_dummy.misc_budding_amethyst", 1);
    public static final Item RITUAL_DUMMY_MISC_REINFORCED_DEEPSLATE =
            item("ritual_dummy/misc_reinforced_deepslate", "ritual_dummy.misc_reinforced_deepslate", 1);
    public static final Item RITUAL_DUMMY_MISC_WILD_TRIM =
            item("ritual_dummy/misc_wild_trim", "ritual_dummy.misc_wild_trim", 1);
    public static final Item RITUAL_DUMMY_MISC_CELESTIAL_CHALICE =
            item("ritual_dummy/misc_celestial_chalice", "ritual_dummy.misc_celestial_chalice", 1);
    public static final Item RITUAL_DUMMY_MISC_ELDRITCH_CHALICE =
            item("ritual_dummy/misc_eldritch_chalice", "ritual_dummy.misc_eldritch_chalice", 1);
    public static final Item RITUAL_DUMMY_REPAIR_TOOLS =
            item("ritual_dummy/repair_tools", "ritual_dummy.repair_tools", 1);
    public static final Item RITUAL_DUMMY_REPAIR_ARMORS =
            item("ritual_dummy/repair_armors", "ritual_dummy.repair_armors", 1);
    public static final Item RITUAL_DUMMY_REPAIR_CHALKS =
            item("ritual_dummy/repair_chalks", "ritual_dummy.repair_chalks", 1);
    public static final Item RITUAL_DUMMY_REPAIR_MINERS =
            item("ritual_dummy/repair_miners", "ritual_dummy.repair_miners", 1);
    public static final Item REPAIR_ICON = item("repair_icon");
    public static final Item MAGIC_LAMP_EMPTY = magicLamp("magic_lamp_empty");
    public static final Item MINER_FOLIOT_UNSPECIALIZED = miner("miner_foliot_unspecialized", 400, 1, 1, 1000);
    public static final Item MINER_DJINNI_ORES = miner("miner_djinni_ores", 300, 1, 1, 400);
    public static final Item MINER_AFRIT_DEEPS = miner("miner_afrit_deeps", 200, 1, 1, 800);
    public static final Item MINER_MARID_MASTER = miner("miner_marid_master", 100, 1, 1, 1600);
    public static final Item MINER_ANCIENT_ELDRITCH = miner("miner_ancient_eldritch", 15, 1, 9, 3200);
    public static final Item DEBUG_WAND = item("debug_wand", 1).setCreativeTab(null);
    public static final Item DEBUG_FOLIOT_LUMBERJACK =
            debugSummon("debug_foliot_lumberjack", SpiritJobType.LUMBERJACK, SpiritTier.FOLIOT,
                    "Testspirit Lumberjack");
    public static final Item DEBUG_FOLIOT_FARMER =
            debugSummon("debug_foliot_farmer", SpiritJobType.FARMER, SpiritTier.FOLIOT, "Testspirit Farmer");
    public static final Item DEBUG_FOLIOT_TRANSPORT_ITEMS =
            debugSummon("debug_foliot_transport_items", SpiritJobType.TRANSPORT_ITEMS, SpiritTier.FOLIOT,
                    "Testspirit Transporter");
    public static final Item DEBUG_FOLIOT_CLEANER =
            debugSummon("debug_foliot_cleaner", SpiritJobType.CLEANER, SpiritTier.FOLIOT, "Testspirit Cleaner");
    public static final Item DEBUG_FOLIOT_TRADER =
            debugSummon("debug_foliot_trader", SpiritJobType.TRADER_OTHERSTONE, SpiritTier.FOLIOT,
                    "Testspirit Trader");
    public static final Item DEBUG_DJINNI_MANAGE_MACHINE =
            debugSummon("debug_djinni_manage_machine", SpiritJobType.MANAGE_MACHINE, SpiritTier.DJINNI,
                    "Testspirit Manage Machine");
    public static final Item DEBUG_DJINNI_TEST =
            debugSummon("debug_djinni_test", null, SpiritTier.DJINNI, "Djinni Test");
    public static final Item MINER_DEBUG_UNSPECIALIZED =
            miner("miner_debug_unspecialized", 100, 10, 1, 10000).setCreativeTab(null);
    public static final Item MINING_DIM_CORE = damageInCrafting("mining_dim_core", Integer.MAX_VALUE);
    public static final Item NAUTILUS_SHELL = item("nautilus_shell");
    public static final Item COPPER_HORSE_ARMOR = horseArmor("copper_horse_armor", COPPER_HORSE_ARMOR_TYPE,
            "textures/entity/horse/armor/horse_armor_iron.png");
    public static final Item WILD_TRIM = item("wild_trim", 1);
    public static final Item FRAGILE_SOUL_GEM = soulGem("fragile_soul_gem", true);
    public static final Item NATURE_PASTE = naturePaste("nature_paste");
    public static final Item RESEARCH_FRAGMENT_DUST = item("research_fragment_dust");
    public static final Item SATCHEL = satchel("satchel");
    public static final Item KNOWLEDGE_TABLET = knowledgeTablet("knowledge_tablet");
    public static final Item RITUAL_SATCHEL_T1 = satchel("ritual_satchel_t1");
    public static final Item RITUAL_SATCHEL_T2 = satchel("ritual_satchel_t2");
    public static final Item VITALITY_COMPASS = vitalityCompass("vitality_compass");
    public static final Item BOOK_OF_BINDING_BOUND_DJINNI = boundBook("book_of_binding_bound_djinni");
    public static final Item BOOK_OF_BINDING_BOUND_AFRIT = boundBook("book_of_binding_bound_afrit");
    public static final Item BOOK_OF_BINDING_BOUND_MARID = boundBook("book_of_binding_bound_marid");
    public static final Item DATURA = demonDreamFood("datura", 2)
            .addEffect(ModPotions.THIRD_EYE, 15 * 20, 1, 0.7F)
            .addEffect(MobEffects.HUNGER, 15 * 20, 1, 1.0F)
            .addEffect(MobEffects.NAUSEA, 15 * 20, 1, 0.3F);
    public static final Item DATURA_SEEDS = seeds("datura_seeds");
    public static final Item DEMONS_DREAM_ESSENCE = demonDreamFood("demons_dream_essence", 10)
            .addEffect(ModPotions.THIRD_EYE, 60 * 20, 1, 1.0F)
            .addEffect(MobEffects.HUNGER, 15 * 20, 1, 1.0F)
            .addEffect(MobEffects.BLINDNESS, 15 * 20, 1, 0.2F)
            .addEffect(MobEffects.GLOWING, 15 * 20, 1, 0.2F)
            .addEffect(MobEffects.LUCK, 5 * 60 * 20, 1, 0.2F)
            .addEffect(MobEffects.UNLUCK, 5 * 60 * 20, 1, 0.2F)
            .addEffect(MobEffects.NAUSEA, 15 * 20, 1, 0.2F)
            .addEffect(MobEffects.WEAKNESS, 15 * 20, 1, 0.2F)
            .addEffect(MobEffects.LEVITATION, 15 * 20, 1, 0.2F);
    public static final Item TALLOW = item("tallow");
    public static final Item AFRIT_ESSENCE = item("afrit_essence");
    public static final Item MARID_ESSENCE = item("marid_essence");
    public static final Item OTHERWORLD_ASHES = item("otherworld_ashes");
    public static final Item BURNT_OTHERSTONE = item("burnt_otherstone");
    public static final Item BURNT_OTHERROCK = item("burnt_otherrock");
    public static final Item OTHERSTONE_FRAME = item("otherstone_frame");
    public static final Item OTHERROCK_FRAME = item("otherrock_frame");
    public static final Item WORMHOLE_FRAME = item("wormhole_frame");
    public static final Item OTHERSTONE_TABLET = item("otherstone_tablet");
    public static final Item COPPER_INGOT = item("copper_ingot");
    public static final Item COPPER_NUGGET = item("copper_nugget");
    public static final Item COPPER_DUST = item("copper_dust");
    public static final Item RAW_SILVER = item("raw_silver");
    public static final Item RAW_IESNIUM = item("raw_iesnium");
    public static final Item SILVER_INGOT = item("silver_ingot");
    public static final Item IESNIUM_INGOT = item("iesnium_ingot");
    public static final Item SILVER_NUGGET = item("silver_nugget");
    public static final Item IESNIUM_NUGGET = item("iesnium_nugget");
    public static final Item NETHERITE_SCRAP_DUST = item("netherite_scrap_dust");
    public static final Item NETHERITE_DUST = item("netherite_dust");
    public static final Item OBSIDIAN_DUST = item("obsidian_dust");
    public static final Item LAPIS_DUST = item("lapis_dust");
    public static final Item AMETHYST_DUST = item("amethyst_dust");
    public static final Item EMERALD_DUST = item("emerald_dust");
    public static final Item ECHO_DUST = item("echo_dust");
    public static final Item IRON_DUST = item("iron_dust");
    public static final Item GOLD_DUST = item("gold_dust");
    public static final Item SILVER_DUST = item("silver_dust");
    public static final Item IESNIUM_DUST = item("iesnium_dust");
    public static final Item CRUSHED_END_STONE = item("crushed_end_stone");
    public static final Item CRUSHED_CALCITE = item("crushed_calcite");
    public static final Item CRUSHED_BLACKSTONE = item("crushed_blackstone");
    public static final Item CRUSHED_ICE = item("crushed_ice");
    public static final Item CRUSHED_PACKED_ICE = item("crushed_packed_ice");
    public static final Item CRUSHED_BLUE_ICE = item("crushed_blue_ice");
    public static final Item LENSES = item("lenses");
    public static final Item INFUSED_LENSES = item("infused_lenses");
    public static final Item LENS_FRAME = item("lens_frame");
    public static final Item SPIRIT_ATTUNED_GEM = item("spirit_attuned_gem", 1);
    public static final Item SPIRIT_ATTUNED_PICKAXE_HEAD = item("spirit_attuned_pickaxe_head");
    public static final Item INFUSED_PICKAXE = infusedPickaxe("infused_pickaxe");
    public static final Item STORAGE_REMOTE_INERT = item("storage_remote_inert", 1);
    public static final Item STORAGE_REMOTE = storageRemote("storage_remote");
    public static final Item DIMENSIONAL_MATRIX = dimensionalMatrix("dimensional_matrix");
    public static final Item GRAY_PASTE = damageInCrafting("gray_paste", 64);
    public static final Item FAMILIAR_RING = familiarRing("familiar_ring");
    public static final Item ENDER_SATCHEL = enderSatchel("ender_satchel");
    public static final Item WITHERITE_DUST = item("witherite_dust");
    public static final Item DRAGONYST_DUST = item("dragonyst_dust");
    public static final Item IESNIUM_PICKAXE = iesniumPickaxe("iesnium_pickaxe");
    public static final Item DIVINATION_ROD = divinationRod("divination_rod", false);
    public static final Item OTHERWORLD_ESSENCE = demonDreamFood("otherworld_essence", 20)
            .addEffect(ModPotions.THIRD_EYE, 60 * 20, 1, 1.0F)
            .addEffect(MobEffects.LUCK, 5 * 60 * 20, 1, 1.0F);
    public static final Item OTHERWORLD_GOGGLES = otherworldGoggles("otherworld_goggles");
    public static final Item TRUE_SIGHT_STAFF = divinationRod("true_sight_staff", true);
    public static final Item BUTCHER_KNIFE = butcherKnife("butcher_knife", Item.ToolMaterial.IRON, false, 5.0D,
            -2.4000000953674316D);
    public static final Item IESNIUM_BUTCHER_KNIFE = butcherKnife("iesnium_butcher_knife", IESNIUM_TOOL_MATERIAL,
            true, 14.0D, -1.8D);
    public static final Item CRUELTY_ESSENCE = item("cruelty_essence");
    public static final Item CURSED_HONEY = demonDreamFood("cursed_honey", 0)
            .addEffect(MobEffects.REGENERATION, 8 * 20, 0, 1.0F)
            .addEffect(MobEffects.ABSORPTION, 45 * 20, 0, 1.0F);
    public static final Item SWEET_HONEY_HEART = demonDreamFood("sweet_honey_heart", 0)
            .addEffect(MobEffects.ABSORPTION, Integer.MAX_VALUE, 9, 1.0F);
    public static final Item TRINITY_GEM = soulGem("trinity_gem", false);
    public static final Item BRUSH = brush("brush");
    public static final Item CHALK_BLACK_IMPURE = item("chalk_black_impure");
    public static final Item CHALK_BLACK = chalk("chalk_black", ModBlocks.CHALK_GLYPH_BLACK);
    public static final Item CHALK_BLUE_IMPURE = item("chalk_blue_impure");
    public static final Item CHALK_BLUE = chalk("chalk_blue", ModBlocks.CHALK_GLYPH_BLUE);
    public static final Item CHALK_BROWN_IMPURE = item("chalk_brown_impure");
    public static final Item CHALK_BROWN = chalk("chalk_brown", ModBlocks.CHALK_GLYPH_BROWN);
    public static final Item CHALK_CYAN_IMPURE = item("chalk_cyan_impure");
    public static final Item CHALK_CYAN = chalk("chalk_cyan", ModBlocks.CHALK_GLYPH_CYAN);
    public static final Item CHALK_GRAY_IMPURE = item("chalk_gray_impure");
    public static final Item CHALK_GRAY = chalk("chalk_gray", ModBlocks.CHALK_GLYPH_GRAY);
    public static final Item CHALK_GREEN_IMPURE = item("chalk_green_impure");
    public static final Item CHALK_GREEN = chalk("chalk_green", ModBlocks.CHALK_GLYPH_GREEN);
    public static final Item CHALK_LIGHT_BLUE_IMPURE = item("chalk_light_blue_impure");
    public static final Item CHALK_LIGHT_BLUE = chalk("chalk_light_blue", ModBlocks.CHALK_GLYPH_LIGHT_BLUE);
    public static final Item CHALK_LIGHT_GRAY_IMPURE = item("chalk_light_gray_impure");
    public static final Item CHALK_LIGHT_GRAY = chalk("chalk_light_gray", ModBlocks.CHALK_GLYPH_LIGHT_GRAY);
    public static final Item CHALK_LIME_IMPURE = item("chalk_lime_impure");
    public static final Item CHALK_LIME = chalk("chalk_lime", ModBlocks.CHALK_GLYPH_LIME);
    public static final Item CHALK_MAGENTA_IMPURE = item("chalk_magenta_impure");
    public static final Item CHALK_MAGENTA = chalk("chalk_magenta", ModBlocks.CHALK_GLYPH_MAGENTA);
    public static final Item CHALK_ORANGE_IMPURE = item("chalk_orange_impure");
    public static final Item CHALK_ORANGE = chalk("chalk_orange", ModBlocks.CHALK_GLYPH_ORANGE);
    public static final Item CHALK_PINK_IMPURE = item("chalk_pink_impure");
    public static final Item CHALK_PINK = chalk("chalk_pink", ModBlocks.CHALK_GLYPH_PINK);
    public static final Item CHALK_PURPLE_IMPURE = item("chalk_purple_impure");
    public static final Item CHALK_PURPLE = chalk("chalk_purple", ModBlocks.CHALK_GLYPH_PURPLE);
    public static final Item CHALK_RED_IMPURE = item("chalk_red_impure");
    public static final Item CHALK_RED = chalk("chalk_red", ModBlocks.CHALK_GLYPH_RED);
    public static final Item CHALK_WHITE_IMPURE = item("chalk_white_impure");
    public static final Item CHALK_WHITE = chalk("chalk_white", ModBlocks.CHALK_GLYPH_WHITE);
    public static final Item CHALK_YELLOW_IMPURE = item("chalk_yellow_impure");
    public static final Item CHALK_YELLOW = chalk("chalk_yellow", ModBlocks.CHALK_GLYPH_YELLOW);
    public static final Item CHALK_RAINBOW = chalk("chalk_rainbow", ModBlocks.CHALK_GLYPH_RAINBOW);
    public static final Item CHALK_VOID = chalk("chalk_void", ModBlocks.CHALK_GLYPH_VOID);
    public static final Item SOUL_GEM = soulGem("soul_gem", false);
    public static final Item SOUL_SHARD = soulShard("soul_shard", false);
    public static final Item SOUL_SHATTERED = soulShard("soul_shattered", true);
    public static final Item PURIFIED_INK = item("purified_ink");
    public static final Item AWAKENED_FEATHER = item("awakened_feather");
    public static final Item TABOO_BOOK = item("taboo_book", 1);
    public static final Item BEAVER_NUGGET = food("beaver_nugget", 8, 0.8F);
    public static final Item PHANTOM_MEMBRANE = item("phantom_membrane");
    public static final Item RITUAL_DUMMY_SUMMON_WILD_HUNT =
            item("ritual_dummy/summon_wild_hunt", "ritual_dummy.summon_wild_hunt", 1);
    public static final Item RITUAL_DUMMY_SUMMON_WILD_AFRIT =
            item("ritual_dummy/summon_wild_afrit", "ritual_dummy.summon_wild_afrit", 1);
    public static final Item RITUAL_DUMMY_SUMMON_WILD_PARROT =
            item("ritual_dummy/summon_wild_parrot", "ritual_dummy.summon_wild_parrot", 1);

    public static final Item[] ITEMS = {
            DICTIONARY_OF_SPIRITS,
            BOOK_OF_BINDING_EMPTY,
            BOOK_OF_BINDING_FOLIOT,
            BOOK_OF_BINDING_BOUND_FOLIOT,
            BOOK_OF_BINDING_DJINNI,
            BOOK_OF_BINDING_AFRIT,
            BOOK_OF_BINDING_MARID,
            BOOK_OF_CALLING_FOLIOT_CLEANER,
            BOOK_OF_CALLING_FOLIOT_FARMER,
            BOOK_OF_CALLING_FOLIOT_LUMBERJACK,
            BOOK_OF_CALLING_FOLIOT_TRANSPORT_ITEMS,
            BOOK_OF_CALLING_FOLIOT_SMELTER,
            BOOK_OF_CALLING_FOLIOT_CRUSHER,
            BOOK_OF_CALLING_FOLIOT_CRYSTALLIZER,
            BOOK_OF_CALLING_DJINNI_SMELTER,
            BOOK_OF_CALLING_DJINNI_CRUSHER,
            BOOK_OF_CALLING_DJINNI_CRYSTALLIZER,
            BOOK_OF_CALLING_DJINNI_GAMBLER,
            BOOK_OF_CALLING_DJINNI_MANAGE_MACHINE,
            BOOK_OF_CALLING_AFRIT_SMELTER,
            BOOK_OF_CALLING_AFRIT_CRUSHER,
            BOOK_OF_CALLING_AFRIT_CRYSTALLIZER,
            BOOK_OF_CALLING_MARID_SMELTER,
            BOOK_OF_CALLING_MARID_CRUSHER,
            BOOK_OF_CALLING_MARID_CRYSTALLIZER,
            BOOK_OF_CALLING_FOLIOT_OTHERSTONE_TRADER,
            BOOK_OF_CALLING_FOLIOT_OTHERROCK_TRADER,
            BOOK_OF_CALLING_FOLIOT_SAPLING_TRADER,
            FOLIOT_SPAWN_TOKEN,
            FOLIOT_CRUSHER_TOKEN,
            FOLIOT_CRYSTALLIZER_TOKEN,
            RITUAL_DUMMY_SUMMON_FOLIOT_CLEANER,
            RITUAL_DUMMY_SUMMON_FOLIOT_TRANSPORT_ITEMS,
            RITUAL_DUMMY_SUMMON_FOLIOT_FARMER,
            RITUAL_DUMMY_SUMMON_FOLIOT_LUMBERJACK,
            RITUAL_DUMMY_SUMMON_FOLIOT_SMELTER,
            RITUAL_DUMMY_SUMMON_FOLIOT_CRUSHER,
            RITUAL_DUMMY_SUMMON_FOLIOT_CRYSTALLIZER,
            RITUAL_DUMMY_SUMMON_FOLIOT_OTHERSTONE_TRADER,
            RITUAL_DUMMY_SUMMON_FOLIOT_OTHERROCK_TRADER,
            RITUAL_DUMMY_SUMMON_FOLIOT_SAPLING_TRADER,
            RITUAL_DUMMY_POSSESS_SKELETON,
            RITUAL_DUMMY_POSSESS_ENDERMITE,
            RITUAL_DUMMY_POSSESS_WITCH,
            RITUAL_DUMMY_POSSESS_PHANTOM,
            RITUAL_DUMMY_POSSESS_ENDERMAN,
            RITUAL_DUMMY_POSSESS_BLAZE,
            RITUAL_DUMMY_POSSESS_GHAST,
            RITUAL_DUMMY_POSSESS_UNBOUND_PARROT,
            RITUAL_DUMMY_FAMILIAR_PARROT,
            RITUAL_DUMMY_FAMILIAR_BAT,
            RITUAL_DUMMY_FAMILIAR_GREEDY,
            RITUAL_DUMMY_FAMILIAR_DEER,
            RITUAL_DUMMY_FAMILIAR_BEAVER,
            RITUAL_DUMMY_FAMILIAR_MUMMY,
            RITUAL_DUMMY_FAMILIAR_DEVIL,
            RITUAL_DUMMY_FAMILIAR_GUARDIAN,
            RITUAL_DUMMY_FAMILIAR_HEADLESS,
            RITUAL_DUMMY_FAMILIAR_OTHERWORLD_BIRD,
            RITUAL_DUMMY_SUMMON_WILD_OTHERWORLD_BIRD,
            RITUAL_DUMMY_SUMMON_DEMONIC_HUSBAND,
            RITUAL_DUMMY_SUMMON_DEMONIC_WIFE,
            RITUAL_DUMMY_POSSESS_UNBOUND_OTHERWORLD_BIRD,
            RITUAL_DUMMY_POSSESS_WEAK_SHULKER,
            RITUAL_DUMMY_POSSESS_SHULKER,
            RITUAL_DUMMY_POSSESS_GUARDIAN,
            RITUAL_DUMMY_POSSESS_ELDER_GUARDIAN,
            RITUAL_DUMMY_POSSESS_ZOMBIFIED_PIGLIN,
            RITUAL_DUMMY_POSSESS_HOGLIN,
            RITUAL_DUMMY_POSSESS_GOAT,
            RITUAL_DUMMY_POSSESS_BEE,
            RITUAL_DUMMY_POSSESS_IESNIUM_GOLEM,
            RITUAL_DUMMY_POSSESS_RANDOM_ANIMAL_COMMON,
            RITUAL_DUMMY_POSSESS_RANDOM_ANIMAL_RIDEABLE,
            RITUAL_DUMMY_POSSESS_RANDOM_ANIMAL_SMALL,
            RITUAL_DUMMY_POSSESS_RANDOM_ANIMAL_SPECIAL,
            RITUAL_DUMMY_POSSESS_RANDOM_ANIMAL_WATER,
            RITUAL_DUMMY_POSSESS_VILLAGER,
            RITUAL_DUMMY_WILD_RANDOM_ANIMAL_COMMON,
            RITUAL_DUMMY_WILD_RANDOM_ANIMAL_RIDEABLE,
            RITUAL_DUMMY_WILD_RANDOM_ANIMAL_SMALL,
            RITUAL_DUMMY_WILD_RANDOM_ANIMAL_SPECIAL,
            RITUAL_DUMMY_WILD_RANDOM_ANIMAL_WATER,
            RITUAL_DUMMY_WILD_VILLAGER,
            RITUAL_DUMMY_WILD_HUNT,
            RITUAL_DUMMY_WILD_HORDE_ILLAGER,
            RITUAL_DUMMY_WILD_CREEPER,
            RITUAL_DUMMY_WILD_HUSK,
            RITUAL_DUMMY_WILD_PARCHED,
            RITUAL_DUMMY_WILD_DROWNED,
            RITUAL_DUMMY_WILD_SILVERFISH,
            RITUAL_DUMMY_RESURRECT_FAMILIAR,
            RITUAL_DUMMY_RESURRECT_ALLAY,
            RITUAL_DUMMY_RESURRECT_MOB,
            RITUAL_DUMMY_SUMMON_DJINNI_CLEAR_WEATHER,
            RITUAL_DUMMY_SUMMON_DJINNI_DAY_TIME,
            RITUAL_DUMMY_SUMMON_DJINNI_NIGHT_TIME,
            RITUAL_DUMMY_SUMMON_DJINNI_SMELTER,
            RITUAL_DUMMY_SUMMON_DJINNI_CRUSHER,
            RITUAL_DUMMY_SUMMON_DJINNI_CRYSTALLIZER,
            RITUAL_DUMMY_SUMMON_DJINNI_GAMBLER,
            RITUAL_DUMMY_SUMMON_DJINNI_MANAGE_MACHINE,
            RITUAL_DUMMY_SUMMON_AFRIT_SMELTER,
            RITUAL_DUMMY_SUMMON_AFRIT_CRUSHER,
            RITUAL_DUMMY_SUMMON_AFRIT_CRYSTALLIZER,
            RITUAL_DUMMY_SUMMON_MARID_SMELTER,
            RITUAL_DUMMY_SUMMON_MARID_CRUSHER,
            RITUAL_DUMMY_SUMMON_MARID_CRYSTALLIZER,
            RITUAL_DUMMY_SUMMON_WONDERING_TRADER,
            RITUAL_DUMMY_SUMMON_AFRIT_RAIN_WEATHER,
            RITUAL_DUMMY_SUMMON_AFRIT_THUNDER_WEATHER,
            RITUAL_DUMMY_SUMMON_DJINNI_PROTOTYPE,
            RITUAL_DUMMY_SUMMON_UNBOUND_AFRIT,
            RITUAL_DUMMY_SUMMON_UNBOUND_AFRIT_PROTOTYPE,
            RITUAL_DUMMY_SUMMON_AFRIT_PROTOTYPE,
            RITUAL_DUMMY_SUMMON_UNBOUND_MARID,
            RITUAL_DUMMY_SUMMON_MARID_PROTOTYPE,
            RITUAL_DUMMY_CRAFT_MINER_FOLIOT_UNSPECIALIZED,
            RITUAL_DUMMY_CRAFT_FRAGILE_SOUL_GEM,
            RITUAL_DUMMY_CRAFT_NATURE_PASTE,
            RITUAL_DUMMY_CRAFT_RESEARCH_FRAGMENT_DUST,
            RITUAL_DUMMY_CRAFT_SATCHEL,
            RITUAL_DUMMY_CRAFT_INFUSED_LENSES,
            RITUAL_DUMMY_CRAFT_KNOWLEDGE_TABLET,
            RITUAL_DUMMY_CRAFT_RITUAL_SATCHEL_T1,
            RITUAL_DUMMY_CRAFT_VITALITY_COMPASS,
            RITUAL_DUMMY_CRAFT_STABLE_WORMHOLE,
            RITUAL_DUMMY_CRAFT_STABILIZER_TIER1,
            RITUAL_DUMMY_CRAFT_STORAGE_CONTROLLER_BASE,
            RITUAL_DUMMY_CRAFT_STABLE_WORMHOLE_DARK,
            RITUAL_DUMMY_CRAFT_STABILIZER_TIER1_DARK,
            RITUAL_DUMMY_CRAFT_STORAGE_CONTROLLER_BASE_DARK,
            RITUAL_DUMMY_CRAFT_STABILIZER_TIER2,
            RITUAL_DUMMY_CRAFT_STABILIZER_TIER2_DARK,
            RITUAL_DUMMY_CRAFT_STABILIZER_TIER3,
            RITUAL_DUMMY_CRAFT_STABILIZER_TIER3_DARK,
            RITUAL_DUMMY_CRAFT_STABILIZER_TIER4,
            RITUAL_DUMMY_CRAFT_STABILIZER_TIER4_DARK,
            RITUAL_DUMMY_CRAFT_STORAGE_REMOTE,
            RITUAL_DUMMY_CRAFT_SOUL_GEM,
            RITUAL_DUMMY_CRAFT_INFUSED_PICKAXE,
            RITUAL_DUMMY_CRAFT_SPIRIT_GRINDSTONE,
            RITUAL_DUMMY_CRAFT_RITUAL_SATCHEL_T2,
            RITUAL_DUMMY_CRAFT_UPGRADE_RITUAL_SATCHEL,
            RITUAL_DUMMY_CRAFT_DIMENSIONAL_MATRIX,
            RITUAL_DUMMY_CRAFT_DIMENSIONAL_MINESHAFT,
            RITUAL_DUMMY_CRAFT_DIMENSIONAL_EXTRACTOR,
            RITUAL_DUMMY_CRAFT_ENTITY_WORMHOLE,
            RITUAL_DUMMY_CRAFT_ENTITY_WORMHOLE_DARK,
            RITUAL_DUMMY_CRAFT_GRAY_PASTE,
            RITUAL_DUMMY_CRAFT_FAMILIAR_RING,
            RITUAL_DUMMY_CRAFT_ENDER_SATCHEL,
            RITUAL_DUMMY_CRAFT_WITHERITE_DUST,
            RITUAL_DUMMY_CRAFT_DRAGONYST_DUST,
            RITUAL_DUMMY_CRAFT_TRUE_SIGHT_STAFF,
            RITUAL_DUMMY_CRAFT_MINER_DJINNI_ORES,
            RITUAL_DUMMY_CRAFT_MINER_AFRIT_DEEPS,
            RITUAL_DUMMY_CRAFT_MINER_MARID_MASTER,
            RITUAL_DUMMY_CRAFT_DARK_IESNIUM_SACRIFICIAL_BOWL,
            RITUAL_DUMMY_CRAFT_DIMENSIONAL_BATTLEFIELD,
            RITUAL_DUMMY_CRAFT_IESNIUM_ANVIL,
            RITUAL_DUMMY_CRAFT_IESNIUM_BUTCHER_KNIFE,
            RITUAL_DUMMY_CRAFT_IESNIUM_SACRIFICIAL_BOWL,
            RITUAL_DUMMY_MISC_CHALK_RAINBOW,
            RITUAL_DUMMY_MISC_CHALK_VOID,
            RITUAL_DUMMY_MISC_TRINITY_GEM,
            RITUAL_DUMMY_MISC_STABILIZER_TIER5,
            RITUAL_DUMMY_MISC_STABILIZER_TIER5_DARK,
            RITUAL_DUMMY_MISC_STABILIZED_STORAGE,
            RITUAL_DUMMY_MISC_STABILIZED_STORAGE_DARK,
            RITUAL_DUMMY_MISC_MINER_ANCIENT_ELDRITCH,
            RITUAL_DUMMY_MISC_IRON_HORSE_ARMOR,
            RITUAL_DUMMY_MISC_GOLDEN_HORSE_ARMOR,
            RITUAL_DUMMY_MISC_DIAMOND_HORSE_ARMOR,
            RITUAL_DUMMY_MISC_COPPER_HORSE_ARMOR,
            RITUAL_DUMMY_MISC_BEE_NEST,
            RITUAL_DUMMY_MISC_BELL,
            RITUAL_DUMMY_MISC_BUDDING_AMETHYST,
            RITUAL_DUMMY_MISC_REINFORCED_DEEPSLATE,
            RITUAL_DUMMY_MISC_WILD_TRIM,
            RITUAL_DUMMY_MISC_CELESTIAL_CHALICE,
            RITUAL_DUMMY_MISC_ELDRITCH_CHALICE,
            RITUAL_DUMMY_REPAIR_TOOLS,
            RITUAL_DUMMY_REPAIR_ARMORS,
            RITUAL_DUMMY_REPAIR_CHALKS,
            RITUAL_DUMMY_REPAIR_MINERS,
            REPAIR_ICON,
            MAGIC_LAMP_EMPTY,
            MINER_FOLIOT_UNSPECIALIZED,
            MINER_DJINNI_ORES,
            MINER_AFRIT_DEEPS,
            MINER_MARID_MASTER,
            MINER_ANCIENT_ELDRITCH,
            DEBUG_WAND,
            DEBUG_FOLIOT_LUMBERJACK,
            DEBUG_FOLIOT_FARMER,
            DEBUG_FOLIOT_TRANSPORT_ITEMS,
            DEBUG_FOLIOT_CLEANER,
            DEBUG_FOLIOT_TRADER,
            DEBUG_DJINNI_MANAGE_MACHINE,
            DEBUG_DJINNI_TEST,
            MINER_DEBUG_UNSPECIALIZED,
            MINING_DIM_CORE,
            NAUTILUS_SHELL,
            COPPER_HORSE_ARMOR,
            WILD_TRIM,
            FRAGILE_SOUL_GEM,
            NATURE_PASTE,
            RESEARCH_FRAGMENT_DUST,
            SATCHEL,
            KNOWLEDGE_TABLET,
            RITUAL_SATCHEL_T1,
            RITUAL_SATCHEL_T2,
            VITALITY_COMPASS,
            BOOK_OF_BINDING_BOUND_DJINNI,
            BOOK_OF_BINDING_BOUND_AFRIT,
            BOOK_OF_BINDING_BOUND_MARID,
            DATURA,
            DATURA_SEEDS,
            DEMONS_DREAM_ESSENCE,
            TALLOW,
            AFRIT_ESSENCE,
            MARID_ESSENCE,
            OTHERWORLD_ASHES,
            BURNT_OTHERSTONE,
            BURNT_OTHERROCK,
            OTHERSTONE_FRAME,
            OTHERROCK_FRAME,
            WORMHOLE_FRAME,
            OTHERSTONE_TABLET,
            COPPER_INGOT,
            COPPER_NUGGET,
            COPPER_DUST,
            RAW_SILVER,
            RAW_IESNIUM,
            SILVER_INGOT,
            IESNIUM_INGOT,
            SILVER_NUGGET,
            IESNIUM_NUGGET,
            NETHERITE_SCRAP_DUST,
            NETHERITE_DUST,
            OBSIDIAN_DUST,
            LAPIS_DUST,
            AMETHYST_DUST,
            EMERALD_DUST,
            ECHO_DUST,
            IRON_DUST,
            GOLD_DUST,
            SILVER_DUST,
            IESNIUM_DUST,
            CRUSHED_END_STONE,
            CRUSHED_CALCITE,
            CRUSHED_BLACKSTONE,
            CRUSHED_ICE,
            CRUSHED_PACKED_ICE,
            CRUSHED_BLUE_ICE,
            LENSES,
            INFUSED_LENSES,
            LENS_FRAME,
            SPIRIT_ATTUNED_GEM,
            SPIRIT_ATTUNED_PICKAXE_HEAD,
            INFUSED_PICKAXE,
            STORAGE_REMOTE_INERT,
            STORAGE_REMOTE,
            DIMENSIONAL_MATRIX,
            GRAY_PASTE,
            FAMILIAR_RING,
            ENDER_SATCHEL,
            WITHERITE_DUST,
            DRAGONYST_DUST,
            IESNIUM_PICKAXE,
            DIVINATION_ROD,
            OTHERWORLD_ESSENCE,
            OTHERWORLD_GOGGLES,
            TRUE_SIGHT_STAFF,
            BUTCHER_KNIFE,
            IESNIUM_BUTCHER_KNIFE,
            CRUELTY_ESSENCE,
            CURSED_HONEY,
            SWEET_HONEY_HEART,
            TRINITY_GEM,
            BRUSH,
            CHALK_BLACK_IMPURE,
            CHALK_BLACK,
            CHALK_BLUE_IMPURE,
            CHALK_BLUE,
            CHALK_BROWN_IMPURE,
            CHALK_BROWN,
            CHALK_CYAN_IMPURE,
            CHALK_CYAN,
            CHALK_GRAY_IMPURE,
            CHALK_GRAY,
            CHALK_GREEN_IMPURE,
            CHALK_GREEN,
            CHALK_LIGHT_BLUE_IMPURE,
            CHALK_LIGHT_BLUE,
            CHALK_LIGHT_GRAY_IMPURE,
            CHALK_LIGHT_GRAY,
            CHALK_LIME_IMPURE,
            CHALK_LIME,
            CHALK_MAGENTA_IMPURE,
            CHALK_MAGENTA,
            CHALK_ORANGE_IMPURE,
            CHALK_ORANGE,
            CHALK_PINK_IMPURE,
            CHALK_PINK,
            CHALK_PURPLE_IMPURE,
            CHALK_PURPLE,
            CHALK_RED_IMPURE,
            CHALK_RED,
            CHALK_WHITE_IMPURE,
            CHALK_WHITE,
            CHALK_YELLOW_IMPURE,
            CHALK_YELLOW,
            CHALK_RAINBOW,
            CHALK_VOID,
            SOUL_GEM,
            SOUL_SHARD,
            SOUL_SHATTERED,
            PURIFIED_INK,
            AWAKENED_FEATHER,
            TABOO_BOOK,
            RITUAL_DUMMY_FAMILIAR_CTHULHU,
            RITUAL_DUMMY_FAMILIAR_BLACKSMITH,
            BEAVER_NUGGET,
            RITUAL_DUMMY_FAMILIAR_DRAGON,
            RITUAL_DUMMY_FAMILIAR_FAIRY,
            RITUAL_DUMMY_FAMILIAR_BEHOLDER,
            RITUAL_DUMMY_FAMILIAR_CHIMERA,
            RITUAL_DUMMY_SUMMON_WILD_HUNT,
            RITUAL_DUMMY_SUMMON_WILD_AFRIT,
            RITUAL_DUMMY_SUMMON_WILD_PARROT,
            PHANTOM_MEMBRANE,
            RITUAL_DUMMY_POSSESS_WARDEN
    };

    private ModItems() {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(ITEMS);
    }

    private static Item item(String name) {
        return item(name, 64);
    }

    private static Item item(String name, int stackSize) {
        return item(name, name, stackSize);
    }

    private static Item item(String name, String translationPath, int stackSize) {
        Item item = name.startsWith("ritual_dummy/") ? new ItemRitualDummy() : new Item();
        return item.setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + translationPath)
                .setCreativeTab(ModCreativeTabs.OCCULTISM)
                .setMaxStackSize(stackSize);
    }

    private static Item legacyRitualDummy(String name, String translationPath) {
        return new ItemRitualDummy()
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + translationPath)
                .setMaxStackSize(1);
    }

    private static Item food(String name, int amount, float saturation) {
        return new ItemFood(amount, saturation, false)
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM);
    }

    private static Item miner(String name, int maxMiningTime, int rollsPerOperation, int outputMultiplier,
            int durability) {
        return new ItemMinerSpirit(maxMiningTime, rollsPerOperation, outputMultiplier, durability)
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM);
    }

    private static Item magicLamp(String name) {
        return new ItemMagicLamp()
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM);
    }

    private static Item familiarRing(String name) {
        return new ItemFamiliarRing()
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM);
    }

    private static Item chalk(String name, BlockChalkGlyph glyphBlock) {
        return new ItemChalk(glyphBlock)
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM);
    }

    private static Item brush(String name) {
        return new ItemBrush()
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM)
                .setMaxStackSize(1);
    }

    private static Item infusedPickaxe(String name) {
        return new ItemInfusedPickaxe(SPIRIT_ATTUNED_TOOL_MATERIAL)
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM);
    }

    private static Item iesniumPickaxe(String name) {
        return new ItemIesniumPickaxe(IESNIUM_TOOL_MATERIAL)
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM);
    }

    private static Item butcherKnife(String name, Item.ToolMaterial material, boolean iesnium, double attackDamage,
            double attackSpeed) {
        return new ItemButcherKnife(material, iesnium, attackDamage, attackSpeed)
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM);
    }

    private static Item vitalityCompass(String name) {
        return new ItemVitalityCompass()
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM);
    }

    private static Item dimensionalMatrix(String name) {
        return new ItemDimensionalMatrix()
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM);
    }

    private static Item divinationRod(String name, boolean trueSightStaff) {
        return new ItemDivinationRod(trueSightStaff)
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM);
    }

    private static Item otherworldGoggles(String name) {
        return new ItemOtherworldGoggles(OTHERWORLD_GOGGLES_MATERIAL)
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM);
    }

    private static Item jobToken(String name, SpiritJobType jobType) {
        return new ItemFoliotJobToken(jobType)
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM)
                .setMaxStackSize(1);
    }

    private static Item dictionary(String name) {
        return new ItemDictionaryOfSpirits()
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM)
                .setMaxStackSize(1);
    }

    private static Item satchel(String name) {
        return new ItemSatchel()
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM)
                .setMaxStackSize(1);
    }

    private static Item enderSatchel(String name) {
        return new ItemEnderSatchel()
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM)
                .setMaxStackSize(1);
    }

    private static Item soulGem(String name, boolean fragile) {
        return new ItemSoulGem(fragile)
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM)
                .setMaxStackSize(1);
    }

    private static Item soulShard(String name, boolean dropsLootOnUse) {
        return new ItemSoulShard(dropsLootOnUse)
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM);
    }

    private static Item boundBook(String name) {
        return new ItemBookOfBindingBound()
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM)
                .setMaxStackSize(1);
    }

    private static Item callingBook(String name, SpiritJobType jobType) {
        return callingBook(name, jobType, SpiritTier.FOLIOT);
    }

    private static Item callingBook(String name, SpiritJobType jobType, SpiritTier spiritTier) {
        return new ItemBookOfCallingFoliotJob(jobType, spiritTier)
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM)
                .setMaxStackSize(1);
    }

    private static Item debugSummon(String name, SpiritJobType jobType, SpiritTier spiritTier, String debugName) {
        return new ItemDebugSummonSpirit(jobType, spiritTier, debugName)
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(null)
                .setMaxStackSize(1);
    }

    private static Item knowledgeTablet(String name) {
        return new ItemKnowledgeTablet()
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM)
                .setMaxStackSize(1);
    }

    private static Item naturePaste(String name) {
        return new ItemNaturePaste()
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM);
    }

    private static Item damageInCrafting(String name, int durability) {
        return new ItemDamageInCrafting(durability)
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM);
    }

    private static Item horseArmor(String name, HorseArmorType armorType, String armorTexture) {
        return new ItemOccultismHorseArmor(armorType, armorTexture)
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM);
    }

    private static ItemDemonDreamFood demonDreamFood(String name, int spiritHealing) {
        return (ItemDemonDreamFood) new ItemDemonDreamFood(spiritHealing)
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM);
    }

    private static Item storageRemote(String name) {
        return new ItemStorageRemote()
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM)
                .setMaxStackSize(1);
    }

    private static Item seeds(String name) {
        return new ItemSeeds(ModBlocks.DATURA_CROP, Blocks.FARMLAND)
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTabs.OCCULTISM);
    }
}
