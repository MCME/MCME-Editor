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
package com.mcmiddleearth.mcme.editor.job;

import com.mcmiddleearth.architect.specialBlockHandling.data.ItemBlockData;
import com.mcmiddleearth.mcme.editor.command.sender.EditCommandSender;
import com.mcmiddleearth.mcme.editor.data.ChunkEditData;
import com.mcmiddleearth.mcme.editor.data.ChunkLightEditData;
import com.mcmiddleearth.mcme.editor.data.EditChunkSnapshot;
import com.mcmiddleearth.mcme.editor.job.action.CountAction;
import com.sk89q.worldedit.regions.Region;
import java.util.List;
import java.util.Map.Entry;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

/**
 *
 * @author Eriol_Eandur
 */
public class LightJob extends AbstractJob{
    
    public LightJob(EditCommandSender owner, int id, YamlConfiguration config) {
        super(owner, id, config);
    }
    
    public LightJob(EditCommandSender owner, int id, World world, Region extraRegion, List<Region> regions, 
                          int size, boolean refreshChunks) {//boolean weSelection, Set<String> worlds, Set<String> rps) {
        super(owner, id, world, extraRegion, regions, size, false, refreshChunks);
    }
 
    @Override
    public ChunkEditData handle(EditChunkSnapshot editChunk) {
        ChunkSnapshot chunk = editChunk.getChunkSnapshot();
        boolean complete= false;
        //int maxY = getWorld().getMaxHeight();
//Logger.getGlobal().info("handle chunk: "+chunk);
        if(isInside(chunk.getX(), chunk.getZ(),0,getMinY(),0)
            && isInside(chunk.getX(), chunk.getZ(),0,getMinY(),15)
            && isInside(chunk.getX(), chunk.getZ(),15,getMinY(),0)
            && isInside(chunk.getX(), chunk.getZ(),15,getMinY(),15)
            && isInside(chunk.getX(), chunk.getZ(),0,getMaxY(),0)
            && isInside(chunk.getX(), chunk.getZ(),0,getMaxY(),15)
            && isInside(chunk.getX(), chunk.getZ(),15,getMaxY(),0)
            && isInside(chunk.getX(), chunk.getZ(),15,getMaxY(),15)) {
            complete = true;
        }
//Logger.getGlobal().info("complete: "+complete);
        ChunkLightEditData edit = new ChunkLightEditData(chunk.getX(),chunk.getZ());
        int step = 2;
        for(int i=0; i<16; i+=step) {
            for(int j=0; j<16; j+=step) {
//Logger.getGlobal().info("maxY: "+getMaxY());
                for(int k=getMinY(); k<=Math.min(chunk.getHighestBlockYAt(i, j),getMaxY());k+=step) {
//Logger.getGlobal().info("inside: "+isInside(chunk.getX(),chunk.getZ(),i,k,j)+" "+complete);
                    if(complete || isInside(chunk.getX(),chunk.getZ(),i,k,j)) {
//Logger.getGlobal().info("checking: "+i+" "+k+" "+j);
                        edit.add(new Vector(i,k,j));
                    }
                }
            }
        }
        return edit;
    }
    
    @Override
    public String getResultMessage() {
        String result = "Recalculated light in "+getSize()+" chunks.";
        return result;
    }
    
    @Override
    public JobType getType() {
        return JobType.LIGHT;
    }

    @Override
    public void saveResultsToFile() {}

    @Override
    public void saveLogsToFile() {}
}
