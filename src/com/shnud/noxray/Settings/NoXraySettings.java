package com.shnud.noxray.Settings;

import com.shnud.noxray.NoXray;
import com.shnud.noxray.Utilities.MagicValues;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Andrew on 22/12/2013.
 */
public class NoXraySettings {

    private static final NoXray _plugin = NoXray.getInstance();

    private static final HashSet<EntityType> _entitiesToHide    = new HashSet<EntityType>();
    private static final List<String> _entityHideWorlds         = new ArrayList<String>();
    private static final List<String> _playerHideWorlds         = new ArrayList<String>();
    private static final List<String> _roomHideWorlds           = new ArrayList<String>();

    public static HashSet<EntityType> getHiddenEntities() {
        return _entitiesToHide;
    }

    public static List<String> getEntityHideWorlds() {
        return _plugin.getConfig().getStringList("worlds.entity-hiding");
    }

    public static List<String> getPlayerHideWorlds() {
        return _plugin.getConfig().getStringList("worlds.player-hiding");
    }

    public static List<String> getRoomHideWorlds() {
        return _plugin.getConfig().getStringList("worlds.room-hiding");
    }

    public static void initSettings() {
        if(_plugin.getConfig() == null)
            _plugin.saveDefaultConfig();

        loadEntitySettings();
    }

    private static void loadEntitySettings() {
        Configuration config = _plugin.getConfig();
        List<?> hiding = config.getStringList("hide");

        boolean defaults = false;
        if(hiding == null || hiding.isEmpty())
            defaults = true;

        if(hiding.contains("farm") || defaults) {
            _entitiesToHide.add(EntityType.COW);
            _entitiesToHide.add(EntityType.MUSHROOM_COW);
            _entitiesToHide.add(EntityType.CHICKEN);
            _entitiesToHide.add(EntityType.EGG);
            _entitiesToHide.add(EntityType.SHEEP);
            _entitiesToHide.add(EntityType.HORSE);
            _entitiesToHide.add(EntityType.LEASH_HITCH);
        }

        if(hiding.contains("spawner-mobs") || defaults) {
            _entitiesToHide.add(EntityType.SKELETON);
            _entitiesToHide.add(EntityType.ZOMBIE);
            _entitiesToHide.add(EntityType.BLAZE);
            _entitiesToHide.add(EntityType.CAVE_SPIDER);
            _entitiesToHide.add(EntityType.SPIDER);
        }

        if(hiding.contains("pets") || defaults) {
            _entitiesToHide.add(EntityType.WOLF);
            _entitiesToHide.add(EntityType.OCELOT);
        }

        if(hiding.contains("villagers") || defaults)
            _entitiesToHide.add(EntityType.VILLAGER);

        if(hiding.contains("misc") || defaults) {
            _entitiesToHide.add(EntityType.DROPPED_ITEM);
            _entitiesToHide.add(EntityType.ENDER_PEARL);
            _entitiesToHide.add(EntityType.EXPERIENCE_ORB);
            _entitiesToHide.add(EntityType.FALLING_BLOCK);
            _entitiesToHide.add(EntityType.ARROW);
            _entitiesToHide.add(EntityType.ITEM_FRAME);
            _entitiesToHide.add(EntityType.MINECART);
            _entitiesToHide.add(EntityType.MINECART_CHEST);
            _entitiesToHide.add(EntityType.MINECART_COMMAND);
            _entitiesToHide.add(EntityType.MINECART_FURNACE);
            _entitiesToHide.add(EntityType.MINECART_HOPPER);
            _entitiesToHide.add(EntityType.MINECART_MOB_SPAWNER);
            _entitiesToHide.add(EntityType.MINECART_TNT);
            _entitiesToHide.add(EntityType.THROWN_EXP_BOTTLE);
            _entitiesToHide.add(EntityType.SPLASH_POTION);
        }
    }
}
