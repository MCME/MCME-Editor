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

import com.mcmiddleearth.mcme.editor.Permissions;
import com.mcmiddleearth.mcme.editor.command.sender.EditCommandSender;
import com.mcmiddleearth.mcme.editor.job.JobType;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;
import com.mojang.brigadier.context.CommandContext;

/**
 *
 * @author Eriol_Eandur
 */
public class CountCommand extends AbstractEditorCommand{

    @Override
    public LiteralArgumentBuilder getCommandTree() {
        LiteralArgumentBuilder builder = literal("count")
            .requires(s -> ((EditCommandSender)s).hasPermissions(Permissions.BLOCK_COUNT))
            .executes(c -> {return startJob(c, JobType.COUNT);})
            .then(argument("<options>", greedyString())
                .suggests((c,optionBuilder) -> { return addJobOptionSuggestions(optionBuilder).buildFuture();})
                .executes(c -> {return startJob(c, JobType.COUNT);}));
        /*builder.then(literal("test"));
        CommandNode node = (CommandNode) builder.getArguments().iterator().next();
        builder.then(literal("-f")
                .executes(c -> {
                    sendMissingFilename(c);
                    return 0;
                })
                .then(argument("<filename>",word())
                    .executes(c -> {
                        return startCountJob(c);
                        })
                    .redirect(node)));
                    /*})
                    .then(literal("-m")
                        .executes(c -> {
                            sendMissingFilename(c);
                            return 0;
                        })
                        .then(argument("<worldname>",word())
                            .executes(c -> {
                                return startCountJob(c);
                            })
                            .then(literal("-p")
                                .executes(c -> {
                                    sendMissingFilename(c);
                                    return 0;
                                })
                                .then(argument("<rpname>",word())
                                    .executes(c -> {
                                        return startCountJob(c);
                                    })
                        ))))));*/
        return builder;
    }
    
    @Override
    public int startJob(CommandContext c, JobType type) {
        if(((EditCommandSender)c.getSource()).getCounts().isEmpty()) {
            ((EditCommandSender)c.getSource()).error("You need to select blocks to count first.");
            return 0;
        }
        return super.startJob(c,type);
    }
    
}
