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

import com.mcmiddleearth.pluginutil.plotStoring.IStoragePlot;
import com.mcmiddleearth.pluginutil.plotStoring.InvalidRestoreDataException;
import com.mcmiddleearth.pluginutil.plotStoring.MCMEEntityFilter;
import com.mcmiddleearth.pluginutil.plotStoring.MCMEPlotFormat;
import com.mcmiddleearth.pluginutil.plotStoring.StoragePlotSnapshot;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;

/**
 *
 * @author Eriol_Eandur
 */
public class Clipboard implements IStoragePlot {
    
    @Getter
    private final Location referencePoint;
    private Location shift;
    
    @Getter
    private byte[] nbtData = null;
    
    @Getter
    protected int rotation; //0, 1, 2 or 3 (90 degree steps)
    
    private boolean[] flip = new boolean[3];
    @Getter
    private final Location lowCorner;
    @Getter
    private final Location highCorner;
    
    @Getter
    private final CuboidRegion weRegion;
    
    private final static String worldMismatch = "You need to be in the same world with your WE selection.";
    
    public Clipboard(Location referencePoint, CuboidRegion weRegion) throws CopyPasteException{
        World world = Bukkit.getWorld(weRegion.getWorld().getName());
        if(world==null || !referencePoint.getWorld().equals(world)) {
            throw new CopyPasteException(worldMismatch);
        }
        this.weRegion = weRegion;
        this.referencePoint = new Location(referencePoint.getWorld(),
                                           referencePoint.getBlockX(),
                                           referencePoint.getBlockY(),
                                           referencePoint.getBlockZ());
        lowCorner = new Location(world,weRegion.getMinimumPoint().getBlockX(),
                                       weRegion.getMinimumPoint().getBlockY(),
                                       weRegion.getMinimumPoint().getBlockZ());
        highCorner = new Location(world,weRegion.getMaximumPoint().getBlockX(),
                                        weRegion.getMaximumPoint().getBlockY(),
                                        weRegion.getMaximumPoint().getBlockZ());
log("reference",referencePoint);
        shift = lowCorner.clone().subtract(this.referencePoint);
log("shift",shift);
log("lowCorner",lowCorner);
    }
    
    public Clipboard(Location referencePoint, Location lowPoint, Location highPoint) throws CopyPasteException {
        if(referencePoint.getWorld()==null 
                || highPoint.getWorld()==null 
                || lowPoint.getWorld()==null 
                || !referencePoint.getWorld().equals(lowPoint.getWorld())
                || !referencePoint.getWorld().equals(highPoint.getWorld())) {
            throw new CopyPasteException(worldMismatch);
        }
        weRegion = new CuboidRegion(BukkitAdapter.adapt(referencePoint.getWorld()),
                                    BukkitAdapter.adapt(lowPoint).toVector().toBlockPoint(),
                                    BukkitAdapter.adapt(highPoint).toVector().toBlockPoint());
        this.referencePoint = referencePoint.clone();
        lowCorner = new Location (lowPoint.getWorld(), Math.min(lowPoint.getBlockX(), highPoint.getBlockX()),
                                                       Math.min(lowPoint.getBlockY(), highPoint.getBlockY()),
                                                       Math.min(lowPoint.getBlockZ(), highPoint.getBlockZ()));
        highCorner = new Location (lowPoint.getWorld(), Math.max(lowPoint.getBlockX(), highPoint.getBlockX()),
                                                       Math.max(lowPoint.getBlockY(), highPoint.getBlockY()),
                                                       Math.max(lowPoint.getBlockZ(), highPoint.getBlockZ()));
log("reference",referencePoint);
        shift = lowCorner.clone().subtract(this.referencePoint);
    }
    
    public boolean copyToClipboard() {
//Logger.getGlobal().info("3");
        StoragePlotSnapshot snapshot = new StoragePlotSnapshot(this);
        try(ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            DataOutputStream outStream = new DataOutputStream(
                                         new BufferedOutputStream(
                                         new GZIPOutputStream(
                                         byteOut)))) {
            new MCMEPlotFormat().save(this, outStream, snapshot);
            outStream.flush();
            outStream.close();
            nbtData = byteOut.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(Clipboard.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    public boolean cutToClipboard() {
        if(copyToClipboard()) {
            Collection<Entity> entities = lowCorner.getWorld()
                     .getNearbyEntities(new BoundingBox(lowCorner.getBlockX(),
                                                        lowCorner.getBlockY(),
                                                        lowCorner.getBlockZ(),
                                                        highCorner.getBlockX()+1,
                                                        highCorner.getBlockY()+1,
                                                        highCorner.getBlockZ()+1),
                            new MCMEEntityFilter());
            entities.forEach((entity) -> {
                entity.remove();
            });
            BlockData air = Bukkit.createBlockData(Material.AIR);
            World world = lowCorner.getWorld();
            for(int i = lowCorner.getBlockX(); i <= highCorner.getBlockX();i++) {
                for(int j = lowCorner.getBlockY(); j <= highCorner.getBlockY();j++) {
                    for(int k = lowCorner.getBlockZ(); k <= highCorner.getBlockZ();k++) {
                        world.getBlockAt(i,j,k).setBlockData(air,false);
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    public void rotate(int degree) {
        while(degree<0) {
            degree = degree + 360;
        }
        while(degree>360) {
            degree = degree - 360;
        }
        int steps = degree/90;
        for(int i = 0; i< steps; i++) {
            rotate90();
        }
        //rotation = (rotation + degree/90)%4;
    }
    
    private void rotate90() {
Logger.getGlobal().info("rot90");
        Location size = getHighCorner().clone().subtract(getLowCorner());
        shift = new Location(shift.getWorld(),-shift.getBlockZ()-(rotation%2==0?size.getBlockZ():size.getBlockX()),
                                               shift.getBlockY(),
                                               shift.getBlockX());
        rotation = (rotation + 1)%4;
        boolean temp_flip = flip[0];
        flip[0] = flip[2];
        flip[2] = temp_flip;
    }
    
    public void flip(char axis) {
        Location size = getHighCorner().clone().subtract(getLowCorner());
        Location rotatedSize;
        if(rotation%2==0) {
            rotatedSize = size;
        } else {
            rotatedSize = new Location(size.getWorld(),size.getBlockZ(),size.getBlockY(),size.getBlockX());
        }
        switch(axis) {
            case 'x':
                shift = new Location(shift.getWorld(),-shift.getBlockX()-rotatedSize.getBlockX(),
                                                       shift.getBlockY(),
                                                       shift.getBlockZ());
                flip[0] = !flip[0];
                break;
            case 'y':
                shift = new Location(shift.getWorld(), shift.getBlockX(),
                                                      -shift.getBlockY()-rotatedSize.getBlockY(),
                                                       shift.getBlockZ());
                flip[1] = !flip[1];
                break;
            case 'z':
                shift = new Location(shift.getWorld(), shift.getBlockX(),
                                                       shift.getBlockY(),
                                                      -shift.getBlockZ()-rotatedSize.getBlockZ());
                flip[2] = !flip[2];
        }
log("shift",shift);
    }
    
    private void log(String name, Location loc) {
        Logger.getGlobal().info(name+" "+loc.getBlockX()+" "+loc.getBlockY()+" "+loc.getBlockZ());
    }
    
    public IStoragePlot getPastePlot(Location location) throws CopyPasteException {
        Location lowPoint = getPasteLocation(location);
        Location highPoint;
        switch(rotation) {
            case 1:
            case 3:
                highPoint = new Location(lowPoint.getWorld(),
                                         lowPoint.getBlockX()+highCorner.getBlockZ()-lowCorner.getBlockZ(),
                                         lowPoint.getBlockY()+highCorner.getBlockY()-lowCorner.getBlockY(),
                                         lowPoint.getBlockZ()+highCorner.getBlockX()-lowCorner.getBlockX());
                break;
            default:
                highPoint = lowPoint.clone().toVector()
                                .add(highCorner.clone().subtract(lowCorner).toVector())
                                .toLocation(lowPoint.getWorld());
        }
Logger.getGlobal().info("low: "+lowPoint.getBlockX()+" "+lowPoint.getBlockZ()+"high: "+highPoint.getBlockX()+" "+highPoint.getBlockZ());
        return new Clipboard(lowPoint,lowPoint,highPoint);
    }
    
    private Location getPasteLocation(Location location) {
        Location paste = location.getBlock().getLocation().toVector()
                                .add(shift.toVector()).toLocation(location.getWorld());
        return paste;
/*//        log("ref",referencePoint);
//        log("low",getLowCorner());
        Location localShift = getLowCorner().clone().subtract(referencePoint);
//        log("shift",shift);
log("localshift",localShift);
log("reference",referencePoint);
log("lowCorner",lowCorner);
        Location size = getHighCorner().clone().subtract(getLowCorner());
//        log("size",size);
        switch(rotation) {
            case 1:
                localShift = new Location(localShift.getWorld(),-localShift.getBlockZ()-size.getBlockZ(),
                                                       localShift.getBlockY(),
                                                       localShift.getBlockX());
                break;
            case 2:
                localShift = new Location(localShift.getWorld(),-localShift.getBlockX()-size.getBlockX(),
                                                       localShift.getBlockY(),
                                                      -localShift.getBlockZ()-size.getBlockZ());
                break;
            case 3:
                localShift = new Location(localShift.getWorld(),localShift.getBlockZ(),
                                                      localShift.getBlockY(),
                                                     -localShift.getBlockX()-size.getBlockX());
                break;
        }
//log("shisft2",shift);
//log("location",location);
//log("blockloc",location.getBlock().getLocation());
        Location localPaste = location.getBlock().getLocation().toVector()
                                 .add(localShift.toVector()).toLocation(location.getWorld());
log("localshift",localShift);
log("local",localPaste);
        Location paste = location.getBlock().getLocation().toVector()
                                .add(localShift.toVector()).toLocation(location.getWorld());
log("shift",shift);
log("paste",paste);
        return paste;*/
    }
    
    public boolean paste(Location location, boolean withAir, boolean withBiome) {
        Location paste = getPasteLocation(location);
log("paste",paste);
        try(DataInputStream in = new DataInputStream(
                                 new BufferedInputStream(
                                 new GZIPInputStream(
                                 new ByteArrayInputStream(nbtData))))) {
            new MCMEPlotFormat().load(paste, rotation, flip, withAir, withBiome, null, in);
        } catch (IOException | InvalidRestoreDataException ex) {
            Logger.getLogger(Clipboard.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    @Override
    public World getWorld() {
        return referencePoint.getWorld();
    }

    @Override
    public boolean isInside(Location lctn) {
        return getLowCorner()!=null && getHighCorner()!=null
                && lctn.getWorld().equals(getWorld())
                && lctn.getBlockX()>=getLowCorner().getBlockX() && lctn.getBlockX()<=getHighCorner().getBlockX()
                && lctn.getBlockY()>=getLowCorner().getBlockY() && lctn.getBlockY()<=getHighCorner().getBlockY()
                && lctn.getBlockZ()>=getLowCorner().getBlockZ() && lctn.getBlockZ()<=getHighCorner().getBlockZ();
    }
    
    
}
