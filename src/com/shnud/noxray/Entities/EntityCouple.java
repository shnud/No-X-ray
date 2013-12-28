package com.shnud.noxray.Entities;

import com.shnud.noxray.Utilities.MathHelper;
import org.bukkit.entity.Entity;

/**
 * Created by Andrew on 23/12/2013.
 */
public class EntityCouple {
    private Entity _entity1;
    private Entity _entity2;

    public EntityCouple(Entity entity1, Entity entity2) {
        if(entity1.equals(entity2))
            throw new IllegalArgumentException("Entities cannot be the same");

        _entity1 = entity1;
        _entity2 = entity2;
    }

    public static long uniqueIDFromEntityPair(Entity e1, Entity e2) {
        int x = e1.getEntityId();
        int y = e2.getEntityId();

        return MathHelper.cantorPair(x, y);
    }

    public long uniqueID() {
        return uniqueIDFromEntityPair(_entity1, _entity2);
    }

    public int hashCode() {
        return ((Long) uniqueID()).hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof EntityCouple))
            return false;

        EntityCouple compare = (EntityCouple) o;
        boolean entity1Shared = compare._entity1.equals(_entity1) || compare._entity1.equals(_entity2);
        boolean entity2Shared = compare._entity2.equals(_entity1) || compare._entity2.equals(_entity2);
        return entity1Shared && entity2Shared;
    }

    public Entity getEntity1() {
        return _entity1;
    }

    public Entity getEntity2() {
        return _entity2;
    }
}
