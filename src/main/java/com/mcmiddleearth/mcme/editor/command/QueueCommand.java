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
package com.mcmiddleearth.mcme.editor.command;

import com.mcmiddleearth.mcme.editor.EditorPlugin;
import com.mcmiddleearth.mcme.editor.Permissions;
import com.mcmiddleearth.mcme.editor.command.sender.EditCommandSender;
import com.mcmiddleearth.mcme.editor.command.sender.EditPlayer;
import com.mcmiddleearth.mcme.editor.job.AbstractJob;
import com.mcmiddleearth.mcme.editor.job.JobManager;
import com.mcmiddleearth.mcme.editor.job.JobStatus;
import com.mcmiddleearth.pluginutil.message.FancyMessage;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;
import com.mojang.brigadier.context.CommandContext;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Eriol_Eandur
 */
public class QueueCommand extends AbstractEditorCommand {

    @Override
    public LiteralArgumentBuilder getCommandTree() {
        return literal("queue")
            .requires(s -> ((EditCommandSender)s).hasPermissions(Permissions.BLOCK_QUEUE))
            .executes(c -> {
                ((EditCommandSender)c.getSource()).info("See manual at...");
                return 0;})
            .then(literal("list")
                .executes(c -> {return displayJobs(c,1,"");})
                .then(argument("<page>", integer())
                    .executes(c -> {
                        int page = c.getArgument("<page>", Integer.class);
                        return displayJobs(c,page,"");})
                    .then(argument("<job states>", word())
                        .executes(c -> {
                            String states = c.getArgument("<job states>",String.class);
                            int page = c.getArgument("<page>", Integer.class);
                            return displayJobs(c,page,states);})))
                .then(argument("<job states>", word())
                    .executes(c -> {
                        String states = c.getArgument("<job states>",String.class);
                        return displayJobs(c,1,states);})))
            .then(literal("suspend")
                .executes(c -> {return suspendJob(c,false);})
                .then(argument("<jobID>", integer())
                    .executes(c -> {return suspendJob(c,false);}))
                .then(literal("-a")
                    .requires(s -> ((EditCommandSender)s).hasPermissions(Permissions.BLOCK_QUEUE_OTHER))
                    .executes(c -> {return suspendJob(c,true);})))
            .then(literal("resume")
                .executes(c -> {return resumeJob(c,false);})
                .then(argument("<jobID>", integer())
                    .executes(c -> {return resumeJob(c,false);}))
                .then(literal("-a")
                    .requires(s -> ((EditCommandSender)s).hasPermissions(Permissions.BLOCK_QUEUE_OTHER))
                    .executes(c -> {return resumeJob(c,true);})))
            .then(literal("cancel")
                .executes(c -> {return cancelJob(c,false);})
                .then(argument("<jobID>", integer())
                    .executes(c -> {return cancelJob(c,false);}))
                .then(literal("-a")
                    .requires(s -> ((EditCommandSender)s).hasPermissions(Permissions.BLOCK_QUEUE_OTHER))
                    .executes(c -> {return cancelJob(c,true);
                })))
            .then(literal("delete")
                .executes(c -> {return cancelJob(c,false);})
                .then(argument("<jobID>", integer())
                    .executes(c -> {return cancelJob(c,false);}))
                .then(literal("-a")
                    .requires(s -> ((EditCommandSender)s).hasPermissions(Permissions.BLOCK_QUEUE_OTHER))
                    .executes(c -> {return deleteJob(c,true);
                })));
    }
    
    private int displayJobs(CommandContext c, int page, String states) {
        Iterator<AbstractJob> jobs = JobManager.getJobs();
        List<String[]> jobStrings = new ArrayList<>();
        Set<JobStatus> selectedStates = getSelectedStates(states);
        while(jobs.hasNext()) {
            AbstractJob job = jobs.next();
            if(!selectedStates.contains(job.getStatus())) {
                continue;
            }
            if(((EditCommandSender)c.getSource()).hasPermissions(Permissions.BLOCK_QUEUE_OTHER)
                    || job.isOwner(((EditCommandSender)c.getSource()))) {
                String ownerName = (job.getOwner() instanceof EditPlayer?
                                        ((EditPlayer)job.getOwner()).getPlayer().getName():
                                        "CONSOLE");
                jobStrings.add(new String[]{"ID: "+job.getId()+" ("+job.getStatus()+") "+job.progresMessage(),
                                            "Owner: "+ownerName+"\n"
                                            +"Started: "+LocalDateTime.ofEpochSecond(job.getStartTime()/1000,0, ZoneOffset.UTC)+"\n"
                                            +"Ended: "+(job.getEndTime()>0?
                                                        LocalDateTime.ofEpochSecond(job.getEndTime()/1000,0, ZoneOffset.UTC)+"\n":
                                                        "---\n")
                                            +"Type: "+job.getType()//+"\n"
                                            +job.getResultMessage()
                                            });
            }      
        }
        if(c.getSource() instanceof EditPlayer && (((EditPlayer)c.getSource()).isOnline())) {
            List<FancyMessage> messages = new ArrayList<>();
            jobStrings.forEach(msg -> messages.add(new FancyMessage(EditorPlugin.getMessageUtil())
                                                   .addTooltipped(msg[0],msg[1])));
            EditorPlugin.getMessageUtil()
                        .sendFancyListMessage(((EditPlayer)c.getSource()).getPlayer(), 
                                new FancyMessage(EditorPlugin.getMessageUtil())
                                        .addSimple("Current editor jobs:"), 
                                messages,"/queue list", page);
        } else {
            ((EditCommandSender)c.getSource()).info("Current editor jobs:");
            jobStrings.forEach(msg -> ((EditCommandSender)c.getSource()).info(msg[0]+"\n"+msg[1]));
        }
        return 0;
    }
    
    private Set<JobStatus> getSelectedStates(String selection) {
        boolean negation = false;
        Set<JobStatus> states = new HashSet<>();
        if(selection.equals("")) {
            states.addAll(Arrays.asList(JobStatus.values()));
            states.remove(JobStatus.FINISHED);
        } else if( selection.startsWith("!")) {
            states.addAll(Arrays.asList(JobStatus.values()));
        }
        for(int i = 0; i < selection.length(); i++) {
            if(selection.charAt(i)=='!') {
                negation = true;
                continue;
            }
            JobStatus status = null;
            switch(selection.charAt(i)) {
                case 'f':
                    status = JobStatus.FINISHED;
                    break;
                case 'F':
                    status = JobStatus.FAILED;
                    break;
                case 'c':
                    status = JobStatus.CANCELLED;
                    break;
                case 'C':
                    status = JobStatus.CREATION;
                    break;
                case 'w':
                    status = JobStatus.WAITING;
                    break;
                case 's':
                    status = JobStatus.SUSPENDED;
                    break;
                case 'r':
                    status = JobStatus.RUNNING;
                    break;
            }
            if(status!=null) {
                if(negation) {
                    states.remove(status);
                } else {
                    states.add(status);
                }
                negation = false;
            }
        }
        return states;
    }

    private int suspendJob(CommandContext c, boolean all) {
        int jobId = getJobId(c);
        EditCommandSender sender = (EditCommandSender)c.getSource();
        if(jobId==-1) {
            JobManager.suspendAllJobs(sender, all);
            if(all) {
                sender.info("Jobs of all players have been suspended.");
            } else {
                sender.info("Your jobs have been suspended.");
            }
        } else {
            if(sender.hasPermissions(Permissions.BLOCK_QUEUE_OTHER) 
                    || JobManager.getJob(jobId).getOwner().equals(sender)) {
                if(JobManager.getJob(jobId).isRunnable()) {
                    JobManager.suspendJob(sender, jobId);
                } else {
                    sender.error("This job can't be suspended.");
                }
            } else {
                sender.sendNoPermissionMessage();
            }
        }
        return 0;
    }

    private int resumeJob(CommandContext c, boolean all) {
        int jobId = getJobId(c);
        EditCommandSender sender = (EditCommandSender)c.getSource();
        if(jobId==-1) {
            JobManager.resumeAllJobs(sender, all);
            if(all) {
                sender.info("Jobs of all players have been resumed.");
            } else {
                sender.info("Your jobs have been resumed.");
            }
        } else {
            if(sender.hasPermissions(Permissions.BLOCK_QUEUE_OTHER) 
                    || JobManager.getJob(jobId).getOwner().equals(sender)) {
                if(JobManager.getJob(jobId).isRunnable()) {
                    JobManager.resumeJob(sender, jobId);
                } else {
                    sender.error("This job can't be resumed.");
                }
            } else {
                sender.sendNoPermissionMessage();
            }
        }
        return 0;
    }

    private int cancelJob(CommandContext c, boolean all) {
        int jobId = getJobId(c);
        EditCommandSender sender = (EditCommandSender)c.getSource();
        if(jobId==-1) {
            JobManager.cancelAllJobs(sender, all);
            if(all) {
                sender.info("Jobs of all players have been cancelled.");
            } else {
                sender.info("Your jobs have been cancelled.");
            }
        } else {
            if(sender.hasPermissions(Permissions.BLOCK_QUEUE_OTHER) 
                    || JobManager.getJob(jobId).getOwner().equals(sender)) {
                if(JobManager.getJob(jobId).isRunnable()) {
                    JobManager.cancelJob(sender, jobId);
                } else {
                    sender.error("This job can't be cancelled.");
                }
            } else {
                sender.sendNoPermissionMessage();
            }
        }
        return 0;
    }
    
    private int deleteJob(CommandContext c, boolean all) {
        int jobId = getJobId(c);
        EditCommandSender sender = (EditCommandSender)c.getSource();
        if(jobId==-1) {
            JobManager.dequeueAllJobs(sender, all);
            if(all) {
                sender.info("Deletable jobs of all players have been delete.");
            } else {
                sender.info("Your deletable jobs have been deleted.");
            }
        } else {
            if(sender.hasPermissions(Permissions.BLOCK_QUEUE_OTHER) 
                    || JobManager.getJob(jobId).getOwner().equals(sender)) {
                if(JobManager.getJob(jobId).isDequeueable()) {
                    JobManager.dequeueJob(jobId);
                } else {
                    sender.error("This job can't be deleted. You need to cancel it first.");
                }
            } else {
                sender.sendNoPermissionMessage();
            }
        }
        return 0;
    }

    private int getJobId(CommandContext c) {
        try {
            return (Integer) c.getArgument("<jobID>", Integer.class);
        } catch(IllegalArgumentException ex) {
            return -1;
        }
    }
}
