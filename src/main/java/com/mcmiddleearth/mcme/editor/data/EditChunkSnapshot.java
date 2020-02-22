/*
 * Copyright (C) 2020 MCME
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

import com.mcmiddleearth.architect.specialBlockHandling.data.ItemBlockData;
import com.mcmiddleearth.architect.specialBlockHandling.data.SpecialBlockInventoryData;
import com.mcmiddleearth.architect.specialBlockHandling.specialBlocks.SpecialBlockItemBlock;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

/**
 *
 * @author Eriol_Eandur
 */
public class EditChunkSnapshot {
    
    private ChunkSnapshot vanillaBlockData;
    
    private Map<Vector, ItemBlockData> itemBlockData = new HashMap<>();
    
    public EditChunkSnapshot(Chunk chunk, boolean enableItemBlocks) {
        vanillaBlockData = chunk.getChunkSnapshot(true, true, true);
        if(enableItemBlocks) {
            for(Entity entity: chunk.getEntities()) {
                if(entity instanceof ArmorStand && SpecialBlockItemBlock.isItemBlockArmorStand((ArmorStand)entity)) {
                    //String id = SpecialBlockItemBlock.getIdFromArmorStand((ArmorStand)entity);
                    int contentDamage = SpecialBlockItemBlock.getContentDamage((ArmorStand)entity);
                    int[] coords = SpecialBlockItemBlock.getCoordinatesFromArmorStand((ArmorStand)entity);
                    SpecialBlockItemBlock specialBlock = (SpecialBlockItemBlock) SpecialBlockInventoryData
                                                                 .getSpecialBlock(SpecialBlockItemBlock
                                                                                  .getIdFromArmorStand((ArmorStand)entity));

                    BlockData blockData = chunk.getBlock(coords[0]%16, coords[1], coords[2]%16).getBlockData();
                    itemBlockData.put(new Vector(coords[0]%16,coords[1], coords[2]%16),
                                      new ItemBlockData(blockData,specialBlock,contentDamage,entity.getLocation().getYaw()));
                }
            }
        }
            
    }
    
    public ChunkSnapshot getChunkSnapshot() {
        return vanillaBlockData;
    }
    
    public ItemBlockData getItemBlockData(Vector location) {
        return itemBlockData.get(location);
    }
}
