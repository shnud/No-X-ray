package com.shnud.noxray.Packets.PacketSenders;

import com.shnud.noxray.NoXray;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by Andrew on 28/12/2013.
 */
public abstract class GenericEntityPacketSender extends PacketSender {

    protected List<Entity> _subjects;

    public GenericEntityPacketSender(List<Player> receivers, List<Entity> subjects) {
        super(receivers);

        if(subjects == null || subjects.isEmpty())
            throw new IllegalArgumentException("Subjects cannot be empty or null");

        Iterator<Entity> it = subjects.iterator();

        while(it.hasNext()) {
            Entity subject = it.next();

            if(subject == null) {
                it.remove();
                // NoXray.getInstance().getLogger().log(Level.WARNING, "Entity was not sent with others as it was null");
            }
            else if(subject.isDead()) {
                it.remove();
                // NoXray.getInstance().getLogger().log(Level.WARNING, "Entity was not sent with others as it was dead");
            }
        }

        _subjects = subjects;
    }
}
