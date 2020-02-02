/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.mcme.editor.command;

import com.mcmiddleearth.architect.copyPaste.CopyPasteManager;
import com.mcmiddleearth.mcme.editor.Permissions;
import com.mcmiddleearth.mcme.editor.clipboard.ClipboardManager;
import com.mcmiddleearth.mcme.editor.command.sender.EditCommandSender;
import com.mcmiddleearth.mcme.editor.command.sender.EditPlayer;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author Eriol_Eandur
 */
public class FlipCommand extends AbstractEditorCommand {

    @Override
    public LiteralArgumentBuilder getCommandTree() {
        return literal("flip")
            .requires(s -> (s instanceof EditPlayer) 
                        && ((EditCommandSender)s).hasPermissions(Permissions.CLIPBOARD_FLIP))
            .executes(c -> { 
                flip((EditPlayer)c.getSource(),null);
                return 0;
            })
            .then(argument("<axis>", word())
                .executes(c -> {
                    try { 
                        flip((EditPlayer)c.getSource(), c.getArgument("<times>",String.class));
                    }catch(IllegalArgumentException ex){
                        ((EditPlayer)c.getSource()).error("Invalid Argument, must be 'x', 'y' or 'z'.");
                    }
                    return 0;
                }))
            .then(literal("help")
                .executes(c -> { 
                    ((EditCommandSender)c.getSource()).info("/flip [axis]: Flip your clipboard along x- y- or z-axis.");
                    ((EditCommandSender)c.getSource()).info("Optional argument [axis] can be 'x', 'y' or 'z'.");
                    ((EditCommandSender)c.getSource()).info(MANUAL_MESSAGE);
                    return 0;
                }));
    }

    private void flip(EditPlayer sender, String arg) {
        Player player = sender.getPlayer();
        if(!sender.hasClipboard()) {
            sender.error("Copy something to your clipboard first with /copy.");
            return;
        }
        char axis;
        //PluginData.getMessageUtil().sendErrorMessage(cs, "Flipping is not yet supported.");
        //if(true) return true;
        if(arg!=null) {
            if(!CopyPasteManager.isAxis(arg)) {
                sender.error("Invalid argument. Possible axis are x, y and z.");
                return;
            }
            axis = arg.charAt(0);
        } else {
            Location loc = player.getLocation();
            float yaw = loc.getYaw();
            while(yaw<-180) yaw+=360;
            while(yaw>180) yaw-=360;
            if(loc.getPitch()>45 || loc.getPitch()<-45) {
                axis = 'y';
            } else if(Math.abs(yaw)<135 && Math.abs(yaw)>45) {
                axis = 'x';
            } else {
                axis = 'z';
            }
        }
        ClipboardManager.flipClipboard(sender, axis);
        sender.info("Your clipboard was flipped.");
    }
    
}
