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

import com.boydti.fawe.object.FawePlayer;
import com.mcmiddleearth.architect.serverResoucePack.RpManager;
import com.mcmiddleearth.architect.serverResoucePack.RpRegion;
import com.mcmiddleearth.mcme.editor.EditorPlugin;
import com.mcmiddleearth.mcme.editor.command.sender.EditCommandSender;
import com.mcmiddleearth.mcme.editor.command.sender.EditPlayer;
import com.mcmiddleearth.mcme.editor.data.ChunkPosition;
import com.mcmiddleearth.mcme.editor.data.PluginData;
import com.mcmiddleearth.mcme.editor.tasks.AsyncJobScheduler;
import com.mcmiddleearth.mcme.editor.tasks.SyncJobScheduler;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.Region;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

/**
 * Manages
 * @author Eriol_Eandur
 */
public class JobManager {
    
    private static BukkitTask asyncScheduler;
    private static BukkitTask syncScheduler;
    
    private static PriorityQueue<AbstractJob> jobQueue = new PriorityQueue<>();
    
    private static int nextId = 1;

    public static boolean enqueueBlockJob(EditCommandSender owner, boolean weSelection, Set<String> worlds, 
                                                                   Set<String> rps, JobType type) {
        boolean jobStarted = false;
        Region weRegion;
        if(weSelection && owner instanceof EditPlayer && owner.isOnline()) {
            weRegion = FawePlayer.wrap(((EditPlayer)owner).getPlayer()).getSelection();
        } else {
            weRegion = null;
        }
        Set<World> validWorlds = new HashSet<>();
        if(worlds.isEmpty()) {
            validWorlds.addAll(Bukkit.getWorlds());
        } else {
            worlds.forEach(world -> {
                if(Bukkit.getWorld(world)!=null) {
                    validWorlds.add(Bukkit.getWorld(world));
                }});
        }
        Set<RpRegion> rpRegions = new HashSet<>();
        rps.forEach(rpName -> {
            RpManager.getRegions().entrySet().stream()
                    .filter(entry -> entry.getValue().getRp().equalsIgnoreCase(rpName))
                                   //&& (validWorlds.isEmpty() || validWorlds.contains(entry.getValue().getRegion()
                                   //                                             .getWorld().getName())))
                    .forEach(entry -> rpRegions.add(entry.getValue()));
            });
        
        final Set<Vector2D> weSelectionChunks = (weRegion!=null && worlds.contains(weRegion.getWorld().getName())?
                                                 weRegion.getChunks():new HashSet<>());
        for(World world : validWorlds) {
            final Set<ChunkPosition> chunks = new HashSet<>();
            Set<Region> rpWeRegions = new HashSet<>();
            Region extraWeRegion = null;
            if(!rpRegions.isEmpty()) {
                rpRegions.stream().filter(rpRegion -> rpRegion.getRegion().getWorld().getName()
                                                              .equalsIgnoreCase(world.getName()))
                                  .forEach(rpRegion -> {
                    rpWeRegions.add(rpRegion.getRegion());
                    if(weRegion!=null) {
                        rpRegion.getRegion().getChunks().stream()
                                            .filter(chunk -> weSelectionChunks.contains(chunk))
                                            .forEach(chunk -> chunks.add(new ChunkPosition(chunk.getBlockX(),
                                                                                           chunk.getBlockZ())));
                    } else {
                        rpRegion.getRegion().getChunks().forEach(chunk -> 
                                            chunks.add(new ChunkPosition(chunk.getBlockX(),chunk.getBlockZ())));
                    }
                });
            } else if(weRegion!=null && weRegion.getWorld().getName().equalsIgnoreCase(world.getName())){
                weRegion.getChunks().forEach(chunk -> {
                   chunks.add(new ChunkPosition(chunk.getBlockX(),chunk.getBlockZ()));
                });
                extraWeRegion = weRegion;
            } else if(weRegion==null) {
                //TODO add all world chunks (stream to file)
            }
Logger.getGlobal().info("chunks: "+chunks.isEmpty());
            if(!chunks.isEmpty()) {
Logger.getGlobal().info("start job in  world: "+world.getName());
Logger.getGlobal().info("size: "+chunks.size());

                if(!AbstractJob.saveChunksToFile(nextId, chunks)) {
                    return false;
                }

                /*if(!BlockJob.createChunkFile(owner, nextId, weSelection, worlds, rps)) {
                    return false;
                }*/
                AbstractJob job = null;

                switch(type) {
                    case COUNT:
                        job = new CountJob(owner, nextId, world, extraWeRegion, rpWeRegions,chunks.size()); break;
                    case REPLACE:
                        job = new ReplaceJob(owner, nextId, world, extraWeRegion, rpWeRegions,chunks.size()); break;
                }
                nextId++;
                jobQueue.add(job);
                job.start();
                jobStarted = true;
            }
        }
        return jobStarted;
    }
    
    public static void loadJobs() {
        File[] files = PluginData.getJobFolder().listFiles(file -> file.getName()
                                                          .endsWith(AbstractJob.jobDataFileExt));
        for(File file: files) {
            AbstractJob job = AbstractJob.loadJob(file);
            nextId = Math.max(nextId, job.getId()+1);
            jobQueue.add(job);
            //job.start();
        }
    }
    
    public static void dequeueJob(int id) {
        AbstractJob job = getJob(id);
        if(job != null && AbstractJob.getDequeueableStates().contains(job.getStatus())) {
            job.cleanup();
            jobQueue.remove(job);
        }
    }
    
    public static Iterator<AbstractJob> getJobs() {
        return jobQueue.iterator();
    }

    public static AbstractJob getJob(int jobId) {
            return jobQueue.stream().filter(job -> job.getId()==jobId).findAny().orElse(null);
    }

    public static void suspendAllJobs(EditCommandSender sender, boolean all) {
        jobQueue.forEach(job -> job.suspend());
    }

    public static void suspendJob(EditCommandSender sender, int jobId) {
        getJob(jobId).suspend();
    }

    public static void resumeAllJobs(EditCommandSender sender, boolean all) {
        jobQueue.forEach(job -> job.resume());
    }

    public static void resumeJob(EditCommandSender sender, int jobId) {
        getJob(jobId).resume();
    }

    public static void cancelAllJobs(EditCommandSender sender, boolean all) {
        jobQueue.forEach(job -> job.cancel());
    }

    public static void cancelJob(EditCommandSender sender, int jobId) {
        getJob(jobId).cancel();
    }
  
    public static void dequeueAllJobs(EditCommandSender sender, boolean all) {
        jobQueue.forEach(job -> {
            dequeueJob(job.getId());
        });
    }

    /*public static void deleteJob(EditCommandSender sender, int jobId) {
        AbstractJob job = getJob(jobId);
        if(!job.isRunnable()) {
            job.cleanup();
            jobQueue.remove(job);
        }
    }*/
    
    public static boolean startJobScheduler() {
        if((syncScheduler==null && asyncScheduler==null)
                || syncScheduler.isCancelled() && asyncScheduler.isCancelled()) {
            syncScheduler = new SyncJobScheduler().runTaskTimer(EditorPlugin.getInstance(), 10, 1);
            asyncScheduler = new AsyncJobScheduler().runTaskLaterAsynchronously(EditorPlugin.getInstance(),5);
            return true;
        } else {
            return false;
        }
    }
    
    public static void stopJobScheduler() {
        syncScheduler.cancel();
        asyncScheduler.cancel();
    }
    
    
}
