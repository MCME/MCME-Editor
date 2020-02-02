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

import com.mcmiddleearth.mcme.editor.command.sender.EditCommandSender;
import com.mcmiddleearth.mcme.editor.job.action.ReplaceAction;
import com.sk89q.worldedit.regions.Region;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Eriol_Eandur
 */
public class ReplaceJob extends BlockSearchJob {
    
    //private Map<BlockData,BlockData> replaces = new HashMap<>();
    //private Map<BlockData,BlockData> switches = new HashMap<>();
   
    //private Map<BlockData,Integer> blockIds = new HashMap<>();
    
    public ReplaceJob(EditCommandSender owner, int id, YamlConfiguration config) {
        super(owner, id, config);
        //actions.putAll(config.getObject("replaces", HashMap.class));
        loadResultsFromFile();
        
        //blockIds = config.getObject("blockId", blockIds.getClass());
        //replaces = config.getObject("replace", replaces.getClass());
        //replaces = config.getObject("switch", switches.getClass());
    }
    
    public ReplaceJob(EditCommandSender owner, int id, World world, Region extraRegion, Set<Region> regions, 
                      boolean exactMatch, int size) {
        super(owner, id, world, extraRegion, regions, exactMatch, size);
        int actionId = 0;
        for(Entry<BlockData,BlockData> entry: owner.getReplaces().entrySet()) {
            actions.put(entry.getKey(),new ReplaceAction(actionId,entry.getKey(),entry.getValue()));
            actionId++;
        }
        for(Entry<BlockData,BlockData> entry: owner.getSwitches().entrySet()) {
            actions.put(entry.getKey(),new ReplaceAction(actionId,entry.getKey(),entry.getValue()));
            actionId++;
            actions.put(entry.getValue(),new ReplaceAction(actionId,entry.getValue(),entry.getKey()));
            actionId++;
        }
        saveActionsToFile();
        /*try {
            getConfig().set("replaces", actions);
            getConfig().save(getJobDataFile());
        } catch (IOException ex) {
            fail(ex);
        }*/
    }

    @Override
    public JobType getType() {
        return JobType.REPLACE;
    }

    /*@Override
    protected void saveResultsToFile() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getResults() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/

    /*@Override
    protected void writeBlockDataToFile(ConfigurationSection config) {
        config.set("replace", replaces);
        config.set("switch", switches);
        config.set("blockId", blockIds);
    }

    @Override
    public void initJobData(EditCommandSender owner) {
        replaces.putAll(owner.getReplaces());
        switches.putAll(owner.getSwitches());
        int blockId = 0;
        for(BlockData data: replaces.keySet()) {
            blockIds.put(data, blockId);
            results.put(data, new Integer[]{blockId,0});
            blockId++;
        }
        for(BlockData data: switches.keySet()) {
            blockIds.put(data, blockId);
            results.put(data, new Integer[]{blockId,0});
            blockId++;
        }
    }*/

}
