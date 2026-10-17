package com.occultism.occultism.ritual;

import java.util.Locale;

import com.occultism.occultism.entity.EntityPossessedBee;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityVex;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.entity.passive.EntityMule;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class RitualSacrifice {

    private final String tag;
    private final String displayName;

    public RitualSacrifice(String tag, String displayName) {
        this.tag = tag;
        this.displayName = displayName;
    }

    public boolean matches(EntityLivingBase entity) {
        // Death notifications arrive after health reaches zero; this predicate checks type, not liveness.
        if (entity == null) {
            return false;
        }

        String normalizedTag = this.tag.toLowerCase(Locale.ROOT);
        switch (normalizedTag) {
            case "c:sheep":
                return entity instanceof EntitySheep;
            case "c:spiders":
                return entity instanceof EntitySpider;
            case "c:squids":
            case "c:fish":
                return entity instanceof EntitySquid;
            case "occultism:cubemob":
                return entity instanceof EntitySlime || entity instanceof EntityMagmaCube;
            case "c:endermen":
                return entity instanceof EntityEnderman;
            case "c:shulkers":
                return entity instanceof EntityShulker;
            case "occultism:humans":
                return entity instanceof EntityVillager || entity instanceof EntityPlayer;
            case "c:animals":
                return entity instanceof EntityAnimal;
            case "c:armadillos":
                return entity instanceof EntityAnimal;
            case "c:bees":
                return entity instanceof EntityBat || entity instanceof EntityPossessedBee;
            case "c:goats":
                return entity instanceof EntityAnimal;
            case "c:pigs":
                return entity instanceof EntityPig;
            case "c:chickens":
                return entity instanceof EntityChicken;
            case "c:cows":
                return entity instanceof EntityCow;
            case "c:horses":
                return entity instanceof EntityHorse || entity instanceof EntityDonkey || entity instanceof EntityMule
                        || entity instanceof EntityLlama;
            case "c:llamas":
                return entity instanceof EntityLlama;
            case "c:iron_golems":
                return entity instanceof EntityIronGolem;
            case "c:golems/snow":
                return entity instanceof EntitySnowman;
            case "c:wardens":
            case "c:ravagers":
                // Neither Warden nor Ravager exists in 1.12. Do not broaden these modern tags
                // to every hostile mob; that would consume unrelated entities in misc rituals.
                return false;
            case "c:bats":
                return entity instanceof EntityBat;
            case "occultism:flying_passive":
                // The 1.20 tag is bat, parrot, bee and allay. 1.12 has no vanilla bee/allay;
                // keep the local matcher strict so Familiar or possessed substitutes cannot be consumed.
                return entity instanceof EntityBat && !(entity instanceof EntityPossessedBee)
                        || entity instanceof EntityParrot;
            case "occultism:warden_sacrifice":
                // Axolotl is absent in 1.12; squid is the explicit local aquatic sacrifice.
                return entity instanceof EntitySquid;
            case "c:vex":
                return entity instanceof EntityVex;
            case "c:parrots":
                return entity instanceof EntityParrot;
            case "occultism:creepers":
                return entity instanceof EntityCreeper;
            default:
                return this.matchesRegistryName(entity, normalizedTag);
        }
    }

    public String getTag() {
        return this.tag;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public ITextComponent createNameComponent() {
        if (this.displayName == null || this.displayName.isEmpty()) {
            return new TextComponentString(this.tag);
        }
        return this.displayName.startsWith("ritual.") ? new TextComponentTranslation(this.displayName)
                : new TextComponentString(this.displayName);
    }

    private boolean matchesRegistryName(EntityLivingBase entity, String normalizedTag) {
        ResourceLocation key = EntityList.getKey(entity);
        if (key == null) {
            return false;
        }
        String entityId = key.toString().toLowerCase(Locale.ROOT);
        String path = key.getPath().toLowerCase(Locale.ROOT);
        String tagPath = normalizedTag.contains(":") ? normalizedTag.substring(normalizedTag.indexOf(':') + 1)
                : normalizedTag;
        return entityId.equals(normalizedTag) || path.equals(tagPath) || tagPath.equals(path + "s");
    }
}
