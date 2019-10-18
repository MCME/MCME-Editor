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
import com.mcmiddleearth.mcme.editor.data.ChunkEditData;
import com.mcmiddleearth.mcme.editor.data.PluginData;
import com.mcmiddleearth.mcme.editor.job.action.CountAction;
import com.mcmiddleearth.mcme.editor.util.RegionUtil;
import com.sk89q.worldedit.regions.Region;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

/**
 *
 * @author Eriol_Eandur
 */
public abstract class BlockJob extends AbstractJob{
    
    private Region extraRegion;
    //private final Set<World> worlds = new HashSet<>();
    private final Set<Region> regions = new HashSet<>();
    
    private File resultsFile;
    private final static String resultsFileExt = ".res";
    //private DataOutputStream resultsOut;
    
    private int maxY, minY;
    
    private boolean exactMatch;
    
    protected final Map<BlockData,CountAction> actions = new HashMap<>();
    
    public BlockJob(EditCommandSender owner, int id, YamlConfiguration config) {
        super(owner, id, config);
        createFileObjects();
        ///extraRegion=null;
        List list = config.getList("actions");
        exactMatch = config.getBoolean("exactMatch",true);
        list.forEach(action->actions.put(((CountAction)action).getSearchData(), (CountAction)action));
        setYRange();
        loadResultsFromFile();
        loadRegionsFromFile();
    }
    
    public BlockJob(EditCommandSender owner, int id, World world, Region extraRegion, Set<Region> regions, boolean exactMatch, int size) {//boolean weSelection, Set<String> worlds, Set<String> rps) {
        super(owner, id, world, size);
        this.extraRegion = extraRegion;
        this.regions.addAll(regions);
        setYRange();
        this.exactMatch = exactMatch;
        saveJobDataToFile();
        saveRegionsToFile();
        createFileObjects();
    }
 
    private void setYRange() {
        if(regions.isEmpty() && extraRegion!=null) {
            maxY = extraRegion.getMaximumPoint().getBlockY();
            minY = extraRegion.getMinimumPoint().getBlockY();
        } else {
            maxY = 0;
            minY = getWorld().getMaxHeight();
            for(Region region: regions) {
                if(region.getMaximumPoint().getBlockY()>maxY) {
                    maxY = region.getMaximumPoint().getBlockY();
                }
                if(region.getMinimumPoint().getBlockY()<minY) {
                    minY = region.getMinimumPoint().getBlockY();
                }
            }
            if(extraRegion!=null) {
                maxY = Math.min(maxY, extraRegion.getMaximumPoint().getBlockY());
                minY = Math.max(minY, extraRegion.getMinimumPoint().getBlockY());
            }
        }
    }
    
    protected final void saveActionsToFile() {
        try {
            List<Object> list = new ArrayList<>();
            actions.values().forEach(action->list.add(action));
            getConfig().set("actions", list);
            getConfig().save(getJobDataFile());
        } catch (IOException ex) {
            fail(ex);
        }
    }
    
    @Override
    public synchronized void saveResultsToFile() {
        try(final DataOutputStream resultsOut = new DataOutputStream(new FileOutputStream(resultsFile))) {
            for(CountAction action: actions.values()) {
                resultsOut.writeInt(action.getId());
                resultsOut.writeInt(action.getApplicationCount());
            }
            resultsOut.flush();
            resultsOut.close();
        } catch (IOException ex) {
            fail(ex);
        }
    }
    
    protected synchronized final void loadResultsFromFile() {
        if(resultsFile.exists()) {
            try(final DataInputStream resultsIn = new DataInputStream(new FileInputStream(resultsFile))) {
                Map<Integer,Integer> results = new HashMap<>();
                for(int i=0; i<actions.size();i++) {
                    results.put(resultsIn.readInt(),resultsIn.readInt());
                }
                actions.values().forEach(action -> action.setApplicationCount(results.get(action.getId())));
                resultsIn.close();
            } catch (IOException ex) {
                fail(ex);
            }
        }
    }
    
    public String getResults() {
        return "not implemented";
    }

    private void createFileObjects() {
        resultsFile = new File(PluginData.getJobFolder(),getId()+resultsFileExt);
    }
    
    /*@Override
    protected void openFileStreams() {
        super.openFileStreams();
        try {
            resultsOut = new DataOutputStream(new FileOutputStream(resultsFile).);
        } catch (FileNotFoundException ex) {
            fail(ex);
        }
    }*/
    
    @Override
    public void cleanup() {
        super.cleanup();
        resultsFile.delete();
    }
    
    /*@Override
    public void closeFileStreams() {
        super.closeFileStreams();
        try {
            resultsOut.flush();
            resultsOut.close();
        } catch (IOException ex) {
            fail(ex);
        }
    }*/
    
    protected final boolean isInside(int chunkX, int chunkZ, int x, int y, int z) {
        x = 16*chunkX + x;
        z = 16*chunkZ + z;
        if(extraRegion!=null && !extraRegion.contains(x,y,z)) {
//Logger.getGlobal().info("not in extra region: "+chunkX+ " "+ chunkZ+" "+x+" "+y+" "+z+" "+extraRegion);
            return false;
        }
        if(regions.isEmpty()) {
//Logger.getGlobal().info("in extra region: "+chunkX+ " "+ chunkZ+" "+x+" "+y+" "+z+" "+extraRegion);
            return true;
        }
        for(Region region:regions) {
            if(region.contains(x, y, z)) {
//Logger.getGlobal().info("is inside: "+chunkX+ " "+ chunkZ+" "+x+" "+y+" "+z+" "+region);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public ChunkEditData handle(ChunkSnapshot chunk) {
        boolean complete= false;
        //int maxY = getWorld().getMaxHeight();
//Logger.getGlobal().info("handle chunk: "+chunk);
        if(isInside(chunk.getX(), chunk.getZ(),0,minY,0)
            && isInside(chunk.getX(), chunk.getZ(),0,minY,15)
            && isInside(chunk.getX(), chunk.getZ(),15,minY,0)
            && isInside(chunk.getX(), chunk.getZ(),15,minY,15)
            && isInside(chunk.getX(), chunk.getZ(),0,maxY,0)
            && isInside(chunk.getX(), chunk.getZ(),0,maxY,15)
            && isInside(chunk.getX(), chunk.getZ(),15,maxY,0)
            && isInside(chunk.getX(), chunk.getZ(),15,maxY,15)) {
            complete = true;
        }
//Logger.getGlobal().info("complete: "+complete);
        ChunkEditData edit = new ChunkEditData(chunk.getX(),chunk.getZ());
        for(int i=0; i<16; i++) {
            for(int j=0; j<16; j++) {
//Logger.getGlobal().info("maxY: "+maxY);
                for(int k=minY; k<maxY;k++) {
//Logger.getGlobal().info("inside: "+isInside(chunk.getX(),chunk.getZ(),i,k,j));
                    if(complete || isInside(chunk.getX(),chunk.getZ(),i,k,j)) {
                        BlockData data = chunk.getBlockData(i, k, j);
                        CountAction action = null;
                        if(exactMatch) {
                            action = actions.get(data);
                        } else {
                            for(Entry<BlockData,CountAction> search: actions.entrySet()) {
                                if(data.matches(search.getKey())) {
                                    action = search.getValue();
                                    break;
                                }
                            }
                        }
//Logger.getGlobal().info("action: "+action);
                        if(action!=null) {
                            BlockData replace = action.apply();
//Logger.getGlobal().info("replace: "+replace);
                            if(replace!=null) {
                                edit.add(new Vector(i,k,j), replace);
                            }
                        }
                    }
                }
            }
        }
        return edit;
    }
    
    @Override
    public String getResultMessage() {
        String result = "\nFound blocks:\n";
        for(CountAction action: actions.values()) {
            result = result + ChatColor.AQUA+action.getSearchData().getAsString(true).replace("minecraft:", "")
                            + ChatColor.GRAY+"->"
                            + ChatColor.GREEN+action.getApplicationCount()+"\n";
        }
        return result;
    }
    
    private void loadRegionsFromFile() {
        if(getConfig().contains("extraRegion")) {
            extraRegion = RegionUtil.loadFromMap(getConfig().getConfigurationSection("extraRegion")
                                                            .getValues(true));
        } else {
            extraRegion = null;
        }
        List<Map<?,?>> regionMaps = getConfig().getMapList("regions");
        for(Map<?,?> map: regionMaps) {
            helper(map);
        }
    }
    private <T,V> void helper(Map<T,V> map) {
        regions.add(RegionUtil.loadFromMap(map));
    }
    
    private void saveRegionsToFile() {
        if(extraRegion!=null) {
            getConfig().set("extraRegion", RegionUtil.saveToMap(extraRegion));
        }
        List<Map<String,Object>> regionMaps = new ArrayList<>();
        for(Region region: regions) {
            regionMaps.add(RegionUtil.saveToMap(region));
        }
        getConfig().set("regions", regionMaps);
    }
    
    private void saveJobDataToFile() {
        getConfig().set("exactMatch", exactMatch);
    }
}
