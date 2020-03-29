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

import com.mcmiddleearth.mcme.editor.data.PluginData;
import com.mcmiddleearth.mcme.editor.job.AbstractJob;
import com.mcmiddleearth.mcme.editor.job.JobManager;
import com.mcmiddleearth.mcme.editor.job.JobStatus;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Asynchronous job execution.
 * @author Eriol_Eandur
 */
public class AsyncJobScheduler extends BukkitRunnable{

    @Override
    public synchronized void run() {
        while(!isCancelled()) {
            Iterator<AbstractJob> jobIterator = JobManager.getJobs();
            while(jobIterator.hasNext()) {
                AbstractJob job = jobIterator.next();
                if(!checkFail(job)) {
                    switch(job.getStatus()) {
                        case CREATION:
                            if(!job.getStatusRequested().equals(JobStatus.CREATION)) {
                                job.setStatus(job.getStatusRequested());
                                if(job.isRunnable()) {
                                    job.openFileStreams();
                                    checkFail(job);
                                }
                            }
                            break;
                        case RUNNING:
                        case WAITING:
                            if(!job.getStatus().equals(job.getStatusRequested())) {
                                switch(job.getStatusRequested()) {
                                    case CANCELLED:
                                        cancel(job);
                                        break;
                                    case SUSPENDED:
                                        suspend(job);
                                        break;
                                    case FINISHED:
                                        finish(job);
                                }
                            }
                            break;
                        case SUSPENDED:
                            if(!job.getStatus().equals(job.getStatusRequested())) {
                                switch(job.getStatusRequested()) {
                                    case CANCELLED:
                                        cancel(job);
                                        break;
                                    case WAITING:
                                        resume(job);
                                        break;
                                    case FINISHED:
                                        finish(job);
                                }
                            }
                    }
                }
            }
            jobIterator = JobManager.getJobs();
            boolean doneSomething = false;
            while(jobIterator.hasNext()) {
                AbstractJob  job = jobIterator.next();
                switch(job.getStatus()) {
                    case WAITING:
                    case RUNNING:
                        if(!doneSomething) {
                            if(job.getStatus().equals(JobStatus.WAITING)) {
                                job.getOwner().info("Working on your job #"+job.getId()+" now. "+job.progresMessage());
                                job.setLastMessaged(System.currentTimeMillis());
                            } else {
                                if(System.currentTimeMillis()>job.getLastMessaged()+10000) {
                                    job.getOwner().info("Working on your job #"+job.getId()+". "+job.progresMessage());
                                    job.setLastMessaged(System.currentTimeMillis());
                                }
                            }
                            job.setStatus(JobStatus.RUNNING);
                            job.work();
                            //job.saveResultsToFile();
                            job.requestChunks();
                            doneSomething = true;
                        } else {
                            if(job.getStatus().equals(JobStatus.RUNNING)) {
                                job.getOwner().info("Your job #"+job.getId()+" is now waiting for another job to finish. "+job.progresMessage());
                            }
                            job.setStatus(JobStatus.WAITING);
                        }
                }
            }
            if(!doneSomething) {
                try {
                    jobIterator = JobManager.getJobs();
                    while(jobIterator.hasNext()) {
                        AbstractJob job = jobIterator.next();
                        if(!job.isRunnable() && job.getEndTime() > System.currentTimeMillis() - PluginData.getJobStorageTime()) {
                            JobManager.dequeueJob(job.getId());
                        }
                    }
                    this.wait(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(AsyncJobScheduler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    private boolean checkFail(AbstractJob job) {
        if(job.getStatusRequested().equals(JobStatus.FAILED)
           && !job.getStatus().equals(JobStatus.FAILED)) {
            job.closeFileStreams();
            job.setStatus(JobStatus.FAILED);
            job.getOwner().error("Your job #"+job.getId()+" failed. See console for details.");
            return true;
        }
        return false;
    }
    
    private void cancel(AbstractJob job) {
        job.setStatus(JobStatus.CANCELLED);
        job.closeFileStreams();
        if(!checkFail(job)) {
            job.getOwner().info("Your job #"+job.getId()+" was cancelled.");
        }
    }
    
    private void suspend(AbstractJob job) {
        job.setStatus(JobStatus.SUSPENDED);
            job.getOwner().info("Your job #"+job.getId()+" was suspended.");
    }
    
    private void resume(AbstractJob job) {
        job.setStatus(JobStatus.WAITING);
            job.getOwner().info("Your job #"+job.getId()+" was resumed.");
    }

    private void finish(AbstractJob job) {
        job.setStatus(JobStatus.FINISHED);
        job.closeFileStreams();
        if(!checkFail(job)) {
            job.getOwner().info("Your job #"+job.getId()+" was finished."+job.getResultMessage());
        }
    }

}
