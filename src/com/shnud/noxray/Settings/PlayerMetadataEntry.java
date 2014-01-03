package com.shnud.noxray.Settings;

/**
 * Created by Andrew on 03/01/2014.
 */
public class PlayerMetadataEntry {
    private String _playerName;
    private boolean _autoProtectOn = true;
    private long _lastHideCommand = 0;

    public PlayerMetadataEntry(String playerName) {
        _playerName = playerName;
    }

    public String getPlayerName() {
        return _playerName;
    }

    public void setAutoProtect(boolean status) {
        _autoProtectOn = status;
    }

    public boolean isAutoProtectOn() {
        return _autoProtectOn;
    }

    public void useHideCommand() {
        _lastHideCommand = System.currentTimeMillis();
    }

    public long getMillisecondsSinceLastHideCommand() {
        return System.currentTimeMillis() - _lastHideCommand;
    }
}
