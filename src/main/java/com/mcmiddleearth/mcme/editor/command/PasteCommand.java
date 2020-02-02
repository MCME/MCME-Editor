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
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

/**
 *
 * @author Eriol_Eandur
 */
public class PasteCommand extends AbstractEditorCommand {

    @Override
    public LiteralArgumentBuilder getCommandTree() {
        return literal("paste")
            .requires(s -> (s instanceof EditPlayer) 
                        && ((EditCommandSender)s).hasPermissions(Permissions.CLIPBOARD_PASTE))
            .executes(c -> { 
                pasteClipboard((EditPlayer)c.getSource(),"");
                return 0;
            })
            .then(literal("help")
                .executes(c -> { 
                    ((EditCommandSender)c.getSource()).info("/paste [-a] [-b]: Paste your clipboard to your current location.");
                    ((EditCommandSender)c.getSource()).info("Optional argument -a omits pasting air blocks.");
                    ((EditCommandSender)c.getSource()).info("Optional argument -b omits pasting biomes.");
                    ((EditCommandSender)c.getSource()).info(MANUAL_MESSAGE);
                    return 0;
                }))
            .then(argument("<options>", greedyString())
                .executes(c -> {
                    pasteClipboard((EditPlayer)c.getSource(),c.getArgument("options",String.class));
                    return 0;
                }));
    }

    private void pasteClipboard(EditPlayer player, String options) {
        if(!player.hasClipboard()) {
            player.error("Copy something to your clipboard first with /copy.");
            return;
        }
        String[] args = options.trim().split(" ");
        boolean withAir = true;
        boolean withBiome = true;
        for(String arg: args) {
            if(arg.equals("-a")) {
                withAir = false;
            }
            if(arg.equals("-b")) {
                withBiome = false;
            }
        }
        try {
            if(ClipboardManager.pasteClipboard(player, withAir, withBiome)) {
                player.info("Your clipboard was pasted.");
            } else {
                player.error("There was an error. Your clipboard was not pasted.");
            }
        } catch (CopyPasteException ex) {
            player.error(ex.getMessage());
        }
    }

}
