 /*
 * Copyright (C) 2019 MCME
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
package com.mcmiddleearth.mcme.editor.data;

import com.mcmiddleearth.mcme.editor.EditorPlugin;
import com.mcmiddleearth.mcme.editor.command.sender.EditConsoleSender;
import com.mcmiddleearth.mcme.editor.command.sender.EditPlayer;
import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 *
 * @author Eriol_Eandur
 */
public class PluginData {
    
    private final static Map<UUID,EditPlayer> editPlayers = new HashMap<>();
    
    @Getter
    private final static EditConsoleSender consoleSender = new EditConsoleSender(Bukkit.getConsoleSender());
    
    @Getter
    private final static File jobFolder = new File(EditorPlugin.getInstance().getDataFolder(),
                                                        "jobs");
    
    @Getter
    private final static File blockSelectionFolder = new File(EditorPlugin.getInstance().getDataFolder(),
                                                        "blockSelections");
    @Getter
    private final static String blockSelectionFileExtension = ".sel";
    
    @Getter
    private final static long jobStorageTime = 1000*3600*24*2;
    
    static {
        if(!blockSelectionFolder.exists()) {
            blockSelectionFolder.mkdirs();
        }
        if(!jobFolder.exists()) {
            jobFolder.mkdirs();
        }
    }
    
    public static FileFilter getBlockSelectionFileFilter() {
        return file -> {return file.getName().endsWith(blockSelectionFileExtension);};
    }
    
    public static EditPlayer getOrCreateEditPlayer(Player player) {
        return getOrCreateEditPlayer(player.getUniqueId());
    }
    
    public static EditPlayer getOrCreateEditPlayer(UUID uuid) {
        if(editPlayers.containsKey(uuid)) {
            return editPlayers.get(uuid);
        } else {
            EditPlayer ePlayer = new EditPlayer(uuid);
            editPlayers.put(uuid,ePlayer);
            return ePlayer;
        }
    }
}
