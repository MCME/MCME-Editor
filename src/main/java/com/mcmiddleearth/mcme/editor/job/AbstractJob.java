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
import com.mcmiddleearth.mcme.editor.command.sender.EditConsoleSender;
import com.mcmiddleearth.mcme.editor.command.sender.EditPlayer;
import com.mcmiddleearth.mcme.editor.data.ChunkEditData;
import com.mcmiddleearth.mcme.editor.data.ChunkPosition;
import com.mcmiddleearth.mcme.editor.data.PluginData;
import com.mcmiddleearth.mcme.editor.queue.ReadingQueue;
import com.mcmiddleearth.mcme.editor.queue.WritingQueue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Eriol_Eandur
 */
public abstract class AbstractJob implements Comparable<AbstractJob>{
    
    
    private ReadingQueue readingQueue;
    protected WritingQueue writingQueue;
    
    @Getter
    private File jobDataFile;
    public final static String jobDataFileExt = ".job";
    
    private File chunkFile;
    private final static String chunkFileExt = ".chk";
    private DataInputStream chunkIn;
    
    private File progresFile;
    private final static String progresFileExt = ".pgr";
    //private DataOutputStream progresOut;
    
    @Getter
    private final EditCommandSender owner;
    
    @Getter
    private final int id;
    
    @Getter
    private final World world;
    
    @Getter
    @Setter
    private JobStatus status;
    
    @Getter
    private JobStatus statusRequested;
    
    @Getter
    private int size;
    @Getter
    private int current=0;
    private int unrequested;
    
    @Getter
    private long startTime;
    @Getter
    private long endTime;
    @Getter
    @Setter
    private long lastMessaged;
    
    @Getter
    private YamlConfiguration config;
    
    //Integer[0]: blockDataId; Integer[1]: blocks found
    //protected Map<BlockData,Integer[]> results = new HashMap<>();

    @Getter
    private static EnumSet<JobStatus> runnableStates = EnumSet.of(JobStatus.RUNNING,JobStatus.SUSPENDED,JobStatus.WAITING);
    @Getter
    private static EnumSet<JobStatus> dequeueableStates = EnumSet.of(JobStatus.CANCELLED,JobStatus.FINISHED,JobStatus.FAILED);

    public static AbstractJob loadJob(File jobFile) {
        int id = Integer.parseInt(jobFile.getName().substring(0, jobFile.getName().indexOf(jobDataFileExt)));
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(new File(PluginData.getJobFolder(),id+jobDataFileExt));
            String ownerUuid = config.getString("owner","console");
            EditCommandSender owner;
            if(ownerUuid.equals("console")) {
                owner = PluginData.getConsoleSender();
            } else {
                 owner = PluginData.getOrCreateEditPlayer(UUID.fromString(ownerUuid));
            }
            switch(JobType.valueOf(config.getString("type"))) {
                case COUNT:
                    return new CountJob(owner,id, config);
                case REPLACE:
                    return new ReplaceJob(owner,id, config);
                default:
                    Logger.getLogger(AbstractJob.class.getName()).log(Level.SEVERE, "Invalid job type.");
            }
        } catch (IOException | InvalidConfigurationException ex) {
            Logger.getLogger(AbstractJob.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public AbstractJob(EditCommandSender owner, int id, YamlConfiguration config) {
        status = JobStatus.CREATION;
        statusRequested = status;
        this.config = config;
        startTime = config.getLong("start");
        endTime = config.getLong("end");
        this.owner = owner;
        this.id = id;
        this.world = Bukkit.getWorld(config.getString("world"));
        if(world==null) {
            fail(null);
        }
        //weRegion = null;
        createFilesObjects();
        readSizeFromFile();
        createQueues();
        try(DataInputStream in = new DataInputStream(new FileInputStream(progresFile))) {
            current = in.readInt();
        } catch (IOException ex) {
Logger.getGlobal().info("no Progress file found");
            current = 0;
        }
        unrequested=size-current;
        statusRequested = JobStatus.valueOf(config.getString("status", JobStatus.SUSPENDED.name()));
Logger.getGlobal().info("job status: "+id+" "+statusRequested);
    }
    
    public AbstractJob(EditCommandSender owner, int id, World world, int size) {
        status = JobStatus.CREATION;
        statusRequested = status;
        startTime = System.currentTimeMillis();
        this.owner = owner;
        this.id = id;
        this.world = world;
        createFilesObjects();
        //readSizeFromFile();
        this.size = size;
        unrequested = size;
        createQueues();
        //initJobData(owner);
        config = new YamlConfiguration();
        try {
            if(owner instanceof EditPlayer) {
                config.set("owner", ((EditPlayer)owner).getUuid().toString());
            } else {
                config.set("owner", "console");
            }
            config.set("start",startTime);
            config.set("end", endTime);
            config.set("world", world.getName());
            config.set("type",getType().name());
            config.set("status", status);
            config.save(jobDataFile);
        } catch (IOException ex) {
            fail(ex);
        }
    }
    
    /*protected void initialize() {
        openFileStreams();
        saveProgresToFile();
    }*/
    
    public abstract JobType getType();
    
    public int getRemaining() {
        return size - current;
    }

    private void createQueues() {
        readingQueue = new ReadingQueue(world);
        writingQueue = new WritingQueue(world);
    }    
    
    public static File getChunkFile(int id) {
        return new File(PluginData.getJobFolder(),id+chunkFileExt);
    }

    public void suspend() {
        if(!runnableStates.contains(status)) return;
        statusRequested = JobStatus.SUSPENDED;
        saveJobStatus();
    }

    public void resume() {
        if(!runnableStates.contains(status)) return;
        statusRequested = JobStatus.WAITING;
        saveJobStatus();
    }

    public void cancel() {
        if(!runnableStates.contains(status)) return;
        statusRequested = JobStatus.CANCELLED;
        endTime = System.currentTimeMillis();
        saveJobStatus();
    }
    
    public void start() {
        if(!status.equals(JobStatus.CREATION)) return;
        statusRequested = JobStatus.WAITING;
        saveJobStatus();
    }
    
    public boolean isRunnable() {
        return runnableStates.contains(status);
    }
    
    public boolean isDequeueable() {
        return dequeueableStates.contains(status);
    }
    
    @Override
    public int compareTo(AbstractJob other) {
        return (size-current < other.size-other.current?-1:
                    (size-current > other.size-other.current?1:0));
    }
    
    public static boolean saveChunksToFile(int id, Set<ChunkPosition> chunks) {
        try(DataOutputStream out = new DataOutputStream(new FileOutputStream(getChunkFile(id)))) {
Logger.getGlobal().info("write chunkFile: "+getChunkFile(id).getName());
Logger.getGlobal().info("write size: "+chunks.size());
            out.writeInt(chunks.size());
            for(ChunkPosition chunk: chunks) {
                out.writeInt(chunk.getX());
//Logger.getGlobal().info("write x: "+chunk.getX());
                out.writeInt(chunk.getZ());
//Logger.getGlobal().info("write z: "+chunk.getZ());
            }
            out.flush();
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(AbstractJob.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    private void readSizeFromFile() {
        try(DataInputStream in = new DataInputStream(new FileInputStream(chunkFile))) {
Logger.getGlobal().info("read chunkFile: "+chunkFile);
           size = in.readInt();
Logger.getGlobal().info("read size: "+size);
        } catch (IOException ex) {
            size = 0;
            fail(ex);
        }
    }
    
    private void saveProgresToFile() {
        try(DataOutputStream progresOut = new DataOutputStream(new FileOutputStream(progresFile))) {
            progresOut.writeInt(current);
            progresOut.flush();
            progresOut.close();
        } catch (IOException ex) {
            fail(ex);
        }
    }

    private void createFilesObjects() {
        jobDataFile = new File(PluginData.getJobFolder(),id+jobDataFileExt);
        chunkFile = new File(PluginData.getJobFolder(),id+chunkFileExt);
        progresFile = new File(PluginData.getJobFolder(),id+progresFileExt);
    }
    
    public void openFileStreams() {
        try {
            chunkIn = new DataInputStream(new FileInputStream(chunkFile));
            chunkIn.skipBytes(4*(1+current*2));
            //current++;
            //progresOut = new DataOutputStream(new FileOutputStream(progresFile));
        } catch (IOException ex) {
            size = 0;
            fail(ex);
        }
    }
    
    public void cleanup() {
        closeFileStreams();
        jobDataFile.delete();
        chunkFile.delete();
        progresFile.delete();
    }
    
    public void closeFileStreams() {
        try {
            chunkIn.close();
        } catch (IOException ex) {
            fail(ex);
        }
        /*try {
            progresOut.flush();
            progresOut.close();
        } catch (IOException ex) {
            fail(ex);
        }*/
    }
    
    protected final void fail(Exception ex) {
        statusRequested = JobStatus.FAILED;
        endTime = System.currentTimeMillis();
        Logger.getLogger(AbstractJob.class.getName()).log(Level.SEVERE, null, ex);
    }

    public abstract ChunkEditData handle(ChunkSnapshot chunk);
    
    public void work() {
        while(readingQueue.hasChunk()) {
//Logger.getGlobal().info("handle chunk (current unrequested):" +current+" "+unrequested);
            writingQueue.put(handle(readingQueue.pollChunk()));
        }
    }

    public void requestChunks() {
        int remaining = readingQueue.remainingRequestsCapacity();
        List<ChunkPosition> request = new ArrayList<>();
        for(int i = 0; i < remaining && i < unrequested; i++) {
            try {
//Logger.getGlobal().info("request chunk (still unrequested):"+unrequested);
                request.add(new ChunkPosition(chunkIn.readInt(),chunkIn.readInt()));
            } catch (IOException ex) {
                fail(ex);
            }
        }
        readingQueue.request(request);
        unrequested -= request.size();
    }

    public boolean hasEdit() {
        return writingQueue.hasEdit();
    }

    public void editChunk() {
        writingQueue.poll().applyEdits(world);
//Logger.getGlobal().info("Edit Chunk: "+current);
        current++;
        saveProgresToFile();
        if(current == size) {
            statusRequested = JobStatus.FINISHED;
            endTime = System.currentTimeMillis();
            saveJobStatus();
        }
    }

    public boolean hasRequests() {
        return readingQueue.hasRequest();
    }

    public void serveChunkRequest() {
//Logger.getGlobal().info("serveing Chunk request: ");
        ChunkPosition chunk = readingQueue.nextRequest();
        readingQueue.putChunk(world.getChunkAt(chunk.getX(),chunk.getZ()).getChunkSnapshot());
    }
    
    public String progresMessage() {
        String ratio = ""+(getCurrent()*1.0/getSize()*100);
        ratio = ratio.substring(0,Math.min(5, ratio.length()));
        return "Done: "+ratio+"% ("+getCurrent()+" of "+getSize()+" chunks)";
    }
    
    public abstract String getResultMessage();
    
    private void saveJobStatus() {
        try {
Logger.getGlobal().info("saveJobStatus: "+statusRequested.name());
            config.set("status", statusRequested.name());
            config.save(jobDataFile);
        } catch (IOException ex) {
            fail(ex);
        }
    }
    
    public abstract void saveResultsToFile();
    
    public boolean isOwner(EditCommandSender sender) {
        return (sender instanceof EditConsoleSender && owner instanceof EditConsoleSender)
                || (sender instanceof EditPlayer 
                    && owner instanceof EditPlayer
                    && ((EditPlayer)sender).getUuid().equals(((EditPlayer)owner).getUuid()));
    }
    

}