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

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

/**
 * Collection of Block changes in one chunk.
 * @author Eriol_Eandur
 */
public class ChunkEditData {
    
    @Getter
    private int chunkX;
    
    @Getter
    private int chunkZ;
            
    @Getter
    private Map<Vector,BlockData> changes = new HashMap<>();
    
    public ChunkEditData(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }
    
    public void add(Vector vector, BlockData data) {
        changes.put(vector, data);
    }
    
    public boolean isEmpty() {
        return changes.isEmpty();
    }

    public void applyEdits(World world) {
//Logger.getGlobal().info("apply edits, size: "+changes.size());
        changes.forEach((vector, data) -> {
//Logger.getGlobal().info("change: "+vector+" "+data+" "+world);
            world.getChunkAt(chunkX, chunkZ).getBlock(vector.getBlockX(),
                                                      vector.getBlockY(),
                                                      vector.getBlockZ())
                    .setBlockData(data, false);
            //No entity tile found error here.
        });
    }
}
