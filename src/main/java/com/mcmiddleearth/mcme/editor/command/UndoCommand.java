/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.mcme.editor.command;

import com.mcmiddleearth.mcme.editor.Permissions;
import com.mcmiddleearth.mcme.editor.clipboard.ClipboardManager;
import com.mcmiddleearth.mcme.editor.clipboard.CopyPasteException;
import com.mcmiddleearth.mcme.editor.command.sender.EditCommandSender;
import com.mcmiddleearth.mcme.editor.command.sender.EditPlayer;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

/**
 *
 * @author Eriol_Eandur
 */
public class UndoCommand extends AbstractEditorCommand {

    @Override
    public LiteralArgumentBuilder getCommandTree() {
        return literal("undo")
            .requires(s -> (s instanceof EditPlayer) 
                        && ((EditCommandSender)s).hasPermissions(Permissions.CLIPBOARD_UNDO))
            .executes(c -> { 
                undo((EditPlayer)c.getSource(),1);
                return 0;
            })
            .then(argument("<times>", integer())
                .executes(c -> {
                    try { 
                        undo((EditPlayer)c.getSource(), c.getArgument("<times>",Integer.class));
                    }catch(IllegalArgumentException ex){
                        ((EditPlayer)c.getSource()).error("Invalid Argument, must be an integer.");
                    }
                    return 0;
                }))
            .then(literal("help")
                .executes(c -> { 
                    ((EditCommandSender)c.getSource()).info("/undo [#n]: Undo up to #n previous edits.");
                    ((EditCommandSender)c.getSource()).info(MANUAL_MESSAGE);
                    return 0;
                }));
    }

    private void undo(EditPlayer sender, int undos) {
        if(!sender.hasUndos()) {
            sender.error("Nothing to undo.");
            return;
        }
        int undid;
        try {
            undid = ClipboardManager.undoEdits(sender, undos);
            sender.info("Successuly reverted "+undid+" edit"+(undid!=1?"s":"")+".");
        } catch (CopyPasteException ex) {
            sender.error(ex.getMessage());
        }
    }
    
}
