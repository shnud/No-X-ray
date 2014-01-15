package com.shnud.noxray.Settings;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Created by Andrew on 03/01/2014.
 */
@ThreadSafe
public class PlayerMetadataEntry {
    private final String _playerName;
    volatile private boolean _autohide = true;
    volatile private long _lastHideCommand = 0;

    public PlayerMetadataEntry(String playerName) {
        _playerName = playerName;
    }

    public String getPlayerName() {
        return _playerName;
    }

    public void setAutohide(boolean status) {
        _autohide = status;
    }

    public boolean isAutohideOn() {
        return _autohide;
    }

    public void useHideCommand() {
        _lastHideCommand = System.currentTimeMillis();
    }

    public long getMillisecondsSinceLastHideCommand() {
        return System.currentTimeMillis() - _lastHideCommand;
    }
}
