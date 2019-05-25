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
import com.mcmiddleearth.mcme.editor.job.action.CountAction;
import com.sk89q.worldedit.regions.Region;
import java.util.Set;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Eriol_Eandur
 */
public class CountJob extends BlockJob {
    
    //Integer: blockDataId
    //private Set<BlockData,Integer> counts = new HashMap<>();
    
    public CountJob(EditCommandSender owner, int id, YamlConfiguration config) {
        super(owner, id, config);
        //actions.putAll(config.getObject("counts", HashMap.class));
        loadResultsFromFile();
        
        //counts = config.getObject("count", counts.getClass());
        //counts = config.getObject("count", counts.getClass());
        //owner.getCounts().forEach(blockData -> results.put(blockData, 0));
    }
    
    public CountJob(EditCommandSender owner, int id, World world, Region extraRegion, Set<Region> regions, int size) {
        super(owner, id, world, extraRegion, regions, size);
        int actionId = 0;
        for(BlockData blockData: owner.getCounts()) {
            actions.put(blockData,new CountAction(actionId,blockData));
            actionId++;
        }
        saveActionsToFile();
        /*try {
            getConfig().set("actions", actions);
            getConfig().save(getJobDataFile());
        } catch (IOException ex) {
            fail(ex);
        }*/
    }

    @Override
    public JobType getType() {
        return JobType.COUNT;
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
        config.set("count", counts);
    }

    @Override
    public void initJobData(EditCommandSender owner) {
        int blockId = 0;
        for(BlockData data: owner.getCounts()) {
            counts.put(data, blockId);
            results.put(data, new Integer[]{blockId,0});
            blockId++;
        }
    }*/


}
