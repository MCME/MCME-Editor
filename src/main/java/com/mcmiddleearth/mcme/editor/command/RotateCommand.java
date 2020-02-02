/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.mcme.editor.command;

import com.mcmiddleearth.mcme.editor.Permissions;
import com.mcmiddleearth.mcme.editor.clipboard.ClipboardManager;
import com.mcmiddleearth.mcme.editor.command.sender.EditCommandSender;
import com.mcmiddleearth.mcme.editor.command.sender.EditPlayer;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;
import org.bukkit.entity.Player;

/**
 *
 * @author Eriol_Eandur
 */
public class RotateCommand extends AbstractEditorCommand {

    @Override
    public LiteralArgumentBuilder getCommandTree() {
        return literal("rot")
            .requires(s -> (s instanceof EditPlayer) 
                        && ((EditCommandSender)s).hasPermissions(Permissions.CLIPBOARD_ROTATE))
            .executes(c -> { 
                rotate((EditPlayer)c.getSource(),90);
                return 0;
            })
            .then(argument("<degree>", integer())
                .executes(c -> {
                    try { 
                        rotate((EditPlayer)c.getSource(), c.getArgument("<degree>",Integer.class));
                    }catch(IllegalArgumentException ex){
                        ((EditPlayer)c.getSource()).error("Invalid Argument, must be an integer.");
                    }
                    return 0;
                }))
            .then(literal("help")
                .executes(c -> { 
                    ((EditCommandSender)c.getSource()).info("/rot [degree]: Rotate your clipboard in steps of 90 degree.");
                    ((EditCommandSender)c.getSource()).info(MANUAL_MESSAGE);
                    return 0;
                }));
    }

    private void rotate(EditPlayer sender, int degree) {
        Player player = sender.getPlayer();
        if(!sender.hasClipboard()) {
            sender.error("Copy something to your clipboard first with /copy.");
            return;
        }
        ClipboardManager.rotateClipboard(sender, degree);
        sender.info("Your clipboard was rotated.");
    }
    
}
