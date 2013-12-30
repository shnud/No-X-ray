package com.shnud.noxray.Packets.PacketSenders;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.shnud.noxray.NoXray;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Andrew on 28/12/2013.
 */
public abstract class PacketSender implements Runnable {

    private static ProtocolManager _pm = ProtocolLibrary.getProtocolManager();
    protected List<Player> _receivers;

    /*
     * run() must be invoked to actually send the packet.
     */
    public PacketSender(List<Player> receivers) {
        if(receivers == null || receivers.isEmpty())
            throw new IllegalArgumentException("List of receivers cannot be empty or null");

        _receivers = receivers;
    }

    /*
     * Subclasses must explicity state whether they
     * can be called from anything other than the main thread.
     */
    protected abstract boolean isThreadSafe();
    protected abstract void sendImplementation();

    /*
     * Alias for the run() method because run() has to be made public to
     * implement the Runnable interface - which allows the PacketSender's
     * thread-safety mechanism, however 'run' is not the best method
     * name to indicate to the user that the packet will be sent
     */
    public final void send() {
        run();
    }

    /*
     * Sends the packet using the implemented sendImplementation function.
     * This cannot be overwritten, and ensures that if the packet
     * cannot be sent thread-safe, it will schedule it
     * to be sent synchronously ASAP through the scheduler.
     */
    public final void run() {
        if(!isThreadSafe() && !Bukkit.isPrimaryThread())
            Bukkit.getScheduler().scheduleSyncDelayedTask(NoXray.getInstance(), this);
        else
            sendImplementation();
    }

    protected ProtocolManager getProtocolManager() {
        return _pm;
    }
}
