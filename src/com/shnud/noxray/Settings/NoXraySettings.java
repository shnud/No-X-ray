package com.shnud.noxray.Settings;

import com.shnud.noxray.Utilities.MagicValues;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Andrew on 22/12/2013.
 */
public class NoXraySettings {

    // Block iteration settings for player LOS
    public static final int                         MAX_PLAYER_VISIBLE_DISTANCE = 50;

    // Settings for hiding players
    public static final int                         PLAYER_TICK_CHECK_FREQUENCY = MagicValues.MINECRAFT_TICKS_PER_SECOND * 3;

    // Settings for hiding non-player entities
    public static final int                         MAXIMUM_Y_FOR_HIDING_NON_PLAYER_ENTITIES = 60;
    public static final int                         MINIMUM_XZ_DISTANCE_FOR_VISIBLE_ENTITIES = 20;
    public static final int                         MINIMUM_Y_DISTANCE_FOR_VISIBLE_ENTITIES = 8;
    public static final int                         ENTITY_TICK_CHECK_FREQUENCY = MagicValues.MINECRAFT_TICKS_PER_SECOND * 4;
    public static final int                         ENTITY_VISIBILITY_CHECKS_PER_PURGE = 10;
    private static HashSet<EntityType>              ENTITIES_TO_HIDE = null;
    public static final HashSet<EntityType> getEntitiesToHide() {
        if(ENTITIES_TO_HIDE == null) {

            ENTITIES_TO_HIDE = new HashSet<EntityType>();

            // Farm animals and related
            ENTITIES_TO_HIDE.add(EntityType.COW);
            ENTITIES_TO_HIDE.add(EntityType.CHICKEN);
            ENTITIES_TO_HIDE.add(EntityType.EGG);
            ENTITIES_TO_HIDE.add(EntityType.SHEEP);
            ENTITIES_TO_HIDE.add(EntityType.HORSE);
            ENTITIES_TO_HIDE.add(EntityType.LEASH_HITCH);

            // Monster spawner mobs
            ENTITIES_TO_HIDE.add(EntityType.SKELETON);
            ENTITIES_TO_HIDE.add(EntityType.ZOMBIE);
            ENTITIES_TO_HIDE.add(EntityType.BLAZE);
            ENTITIES_TO_HIDE.add(EntityType.CAVE_SPIDER);
            ENTITIES_TO_HIDE.add(EntityType.SPIDER);

            // Other friendly possibly collectible entities
            ENTITIES_TO_HIDE.add(EntityType.WOLF);
            ENTITIES_TO_HIDE.add(EntityType.OCELOT);
            ENTITIES_TO_HIDE.add(EntityType.MUSHROOM_COW);
            ENTITIES_TO_HIDE.add(EntityType.VILLAGER);

            // Information that could give away a location
            // of a base if packets were to be looked at
            ENTITIES_TO_HIDE.add(EntityType.DROPPED_ITEM);
            ENTITIES_TO_HIDE.add(EntityType.ENDER_PEARL);
            ENTITIES_TO_HIDE.add(EntityType.EXPERIENCE_ORB);
            ENTITIES_TO_HIDE.add(EntityType.FALLING_BLOCK);
            ENTITIES_TO_HIDE.add(EntityType.ARROW);
            ENTITIES_TO_HIDE.add(EntityType.ITEM_FRAME);
            ENTITIES_TO_HIDE.add(EntityType.MINECART);
            ENTITIES_TO_HIDE.add(EntityType.MINECART_CHEST);
            ENTITIES_TO_HIDE.add(EntityType.MINECART_COMMAND);
            ENTITIES_TO_HIDE.add(EntityType.MINECART_FURNACE);
            ENTITIES_TO_HIDE.add(EntityType.MINECART_HOPPER);
            ENTITIES_TO_HIDE.add(EntityType.MINECART_MOB_SPAWNER);
            ENTITIES_TO_HIDE.add(EntityType.MINECART_TNT);
            ENTITIES_TO_HIDE.add(EntityType.THROWN_EXP_BOTTLE);
            ENTITIES_TO_HIDE.add(EntityType.SPLASH_POTION);
        }

        return ENTITIES_TO_HIDE;
    }

    // Settings for room hiding
    public static final boolean                     LOAD_WHOLE_MIRROR_REGIONS_INTO_RAM = false;
}
