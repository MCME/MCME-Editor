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

import lombok.Getter;
import org.bukkit.Location;


/**
 * Coordinates of a chunk.
 * @author Eriol_Eandur
 */
public class ChunkPosition {
    
    @Getter 
    private final int x;
    
    @Getter
    private final int z;
    
    public ChunkPosition(int x, int z) {
        this.x=x;
        this.z=z;
    }
    
    public ChunkPosition(Location loc) {
        this.x=loc.getChunk().getX();
        this.z=loc.getChunk().getZ();
    }
    
    @Override
    public boolean equals(Object other) {
        return other instanceof ChunkPosition && this.x==((ChunkPosition)other).x 
                                              && this.z== ((ChunkPosition)other).z;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.x;
        hash = 97 * hash + this.z;
        return hash;
    }
}
