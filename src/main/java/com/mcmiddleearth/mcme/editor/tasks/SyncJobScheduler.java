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
package com.mcmiddleearth.mcme.editor.tasks;

import com.mcmiddleearth.mcme.editor.job.AbstractJob;
import com.mcmiddleearth.mcme.editor.job.JobManager;
import com.mcmiddleearth.mcme.editor.job.JobStatus;
import java.util.Iterator;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Synchronous running Scheduler that fills ReadingQueues with requested 
 * Chunksnapshots and executes queued chunk changes in WritingQueues.
 * Runs each server tick for 20 ms, 10 ms, 5 ms or 0 ms depending on measured 
 * server tps.
 * 
 * @author Eriol_Eandur
 */
public class SyncJobScheduler extends BukkitRunnable{

    long lastStartTime=-1;
    
    @Override
    public void run() {
//Logger.getGlobal().info("SyncJobScheduler");
        if(lastStartTime==-1) {
            lastStartTime = System.nanoTime();
        } else {
            long startTime = System.nanoTime();
            long serverTick = startTime-lastStartTime;
            long endTime;
            if(serverTick>90000000) {
                endTime = startTime;
            } else if(serverTick>70000000) {
                endTime = startTime+10000000;
            } else {
                endTime = startTime+20000000;
            }
            Iterator<AbstractJob> jobs = JobManager.getJobs();
//Logger.getGlobal().info("jobs: "+jobs.hasNext());
            while(jobs.hasNext()) {
                AbstractJob job = jobs.next();
//Logger.getGlobal().info("job status: "+job.getId()+" "+job.getStatus().name());
                if(job.getStatus().equals(JobStatus.RUNNING)) {
//Logger.getGlobal().info("run job status: "+job.getId()+" hasEdit: "+job.hasEdit());
//Logger.getGlobal().info("time: "+System.nanoTime()+" "+endTime);
                    while(System.nanoTime()<endTime && job.hasEdit()) {
                        job.editChunk();
//Logger.getGlobal().info("time: "+System.nanoTime()+" "+endTime);
                    }
//Logger.getGlobal().info("run job status: "+job.getId()+" hasRequest: "+job.hasRequests());
//Logger.getGlobal().info("time: "+System.nanoTime()+" "+endTime);
                    while(System.nanoTime()<endTime && job.hasRequests()) {
                        job.serveChunkRequest();
//Logger.getGlobal().info("time: "+System.nanoTime()+" "+endTime);
                    }
                }
            }
            lastStartTime = startTime;
        }
    }
    
}
