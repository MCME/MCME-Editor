/*
 * Copyright (C) 2019 Eriol_Eandur
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mcmiddleearth.mcme.editor.clipboard;

import com.mcmiddleearth.architect.ArchitectPlugin;
import com.mcmiddleearth.mcme.editor.Permissions;
import com.mcmiddleearth.mcme.editor.command.sender.EditPlayer;
import com.mcmiddleearth.pluginutil.NumericUtil;
import com.mcmiddleearth.pluginutil.plotStoring.IStoragePlot;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

/**
 *
 * @author Eriol_Eandur
 */
public class ClipboardManager {
 
    private static final String saveUndoFailed = "Failed to save undo data";
    
    private static final int maxSize = ArchitectPlugin.getPluginInstance().getConfig()
                                                       .getInt("CopyPasteUndoLimit",30);
    
    public static boolean copyToClipboard(EditPlayer player, CuboidRegion weRegion) throws CopyPasteException{
//Logger.getGlobal().info("2");
        Clipboard cb = new Clipboard(player.getPlayer().getLocation(),weRegion);
        player.setClipboard(cb);
        return cb.copyToClipboard();
    }
    
    public static boolean cutToClipboard(EditPlayer player, CuboidRegion weRegion) throws CopyPasteException {
        Clipboard cb = new Clipboard(player.getPlayer().getLocation(),weRegion);
        if(!saveUndoData(player,cb)) {
            throw new CopyPasteException(saveUndoFailed);
        }
        cb = new Clipboard(player.getPlayer().getLocation(),weRegion);
        player.setClipboard(cb);
        return cb.cutToClipboard();
    }

    public static boolean pasteClipboard(EditPlayer player, boolean withAir, boolean withBiome) throws CopyPasteException {
        if(player.hasClipboard()) {
            Clipboard cb = player.getClipboard();
            if(!saveUndoData(player,cb.getPastePlot(player.getPlayer().getLocation()))) {
               throw new CopyPasteException(saveUndoFailed);
            }
            return cb.paste(player.getPlayer().getLocation(), withAir, withBiome);
        }
        return false;
    }
    
    public static void rotateClipboard(EditPlayer player, int degree) {
        if(player.hasClipboard()) {
            player.getClipboard().rotate(degree);
        }
    }
    
    public static void flipClipboard(EditPlayer player, char axis) {
        if(player.hasClipboard()) {
            player.getClipboard().flip(axis);
        }
    }
    
    public static boolean isAxis(String axis) {
        switch(axis.charAt(0)) {
            case 'x':
            case 'y':
            case 'z':
                return true;
            default:
                return false;
        }
    }
    
    public static int maxAllowedSize(EditPlayer player) {
        Set<PermissionAttachmentInfo> permissions = player.getPlayer().getEffectivePermissions();
        int maxLimit = 0;
        for(PermissionAttachmentInfo info: permissions) {
            if(info.getPermission().startsWith(Permissions.CLIPBOARD_LIMIT.name())) {
                String limitString = info.getPermission().replace(Permissions.CLIPBOARD_LIMIT.name()+".", "");
                if(NumericUtil.isInt(limitString)) {
                    int limit = NumericUtil.getInt(limitString);
                    if(limit>maxLimit) {
                        maxLimit = limit;
                    }
                }
            }
        }
        return maxLimit;
    }
    
    public static int undoEdits(EditPlayer player, int undos) throws CopyPasteException {
        int count = 0;
        for(int i=0; i<undos; i++) {
            if(player.hasUndos()) {
                List<UndoData> list = player.getUndoData();
                UndoData data = list.get(list.size()-1);
                saveRedoData(player,data);
                if(!data.undo()) {
                    throw new CopyPasteException("Error while undoing edits (reverted "+count+" edits before the error occured.");
                }
                list.remove(list.size()-1);
                count++;
            } else {
                return count;
            }
        }
        return count;
    }
    
    public static int redoEdits(EditPlayer player, int redos) throws CopyPasteException {
        int count = 0;
        for(int i=0; i<redos; i++) {
            if(player.hasRedos()) {
                List<UndoData> list = player.getRedoData();
                UndoData data = list.get(list.size()-1);
                saveUndoData(player,data);
                if(!data.undo()) {
                    throw new CopyPasteException("Error while redoing edits (redone "+count+" edits before the error occured.");
                }
                list.remove(list.size()-1);
                count++;
            } else {
                return count;
            }
        }
        return count;
    }
    
    private static boolean saveUndoData(EditPlayer player, IStoragePlot plot) {
        Logger.getGlobal().info("saving undo data.");
        return saveData(player.getUndoData(), plot);
    }
    
    private static boolean saveRedoData(EditPlayer player, IStoragePlot plot) {
        Logger.getGlobal().info("saving redo data.");
        return saveData(player.getRedoData(), plot);
    }
    
    private static boolean saveData(List<UndoData> list, IStoragePlot plot) {
        try{
            UndoData data = new UndoData(plot);
            list.add(data);
            if(list.size()>maxSize) {
                list.remove(0);
            }
            Logger.getGlobal().info("Data size is: "+ getUndoDataSize(list)
                                                                         +" byte from "+list.size()+" entries (max "+maxSize+").");
        } catch (CopyPasteException ex) {
            return false;
        }
        return true;
    }
    
    public static int getUndoDataSize(List<UndoData> list) {
        if(list==null) {
            return 0;
        } else {
            int sum = 0;
            for(UndoData data: list) {
                sum=sum+data.getNbtData().length;
            }
            return sum;
        }
    }
    
}
