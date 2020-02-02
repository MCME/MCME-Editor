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
import com.mcmiddleearth.pluginutil.WEUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.entity.Player;

/**
 *
 * @author Eriol_Eandur
 */
public class CopyCommand extends AbstractEditorCommand {

    @Override
    public LiteralArgumentBuilder getCommandTree() {
        return literal("copy")
            .requires(s -> (s instanceof EditPlayer) 
                        && ((EditCommandSender)s).hasPermissions(Permissions.CLIPBOARD_COPY))
            .executes(c -> { 
                copyClipboard((EditPlayer)c.getSource());
                return 0;
            })
            .then(literal("help")
                .executes(c -> { 
                    ((EditCommandSender)c.getSource()).info("/copy: Copy your cuboid WE selection to your clipboard.");
                    ((EditCommandSender)c.getSource()).info(MANUAL_MESSAGE);
                    return 0;
                }));
    }

    private void copyClipboard(EditPlayer sender) {
        Player player = sender.getPlayer();
        Region weRegion = WEUtil.getSelection(player);
        if(weRegion==null) {
            sender.error("Please make a WE selection first.");
            return;
        }
        if(!(weRegion instanceof CuboidRegion)) {
            sender.error("Only cuboid selections are supported.");
            return;
        }
        int area = weRegion.getArea();
        int maxArea = ClipboardManager.maxAllowedSize(sender);
        if(area > maxArea && !(player.hasPermission(Permissions.CLIPBOARD_UNLIMITED.getPermissionNode()))) {
            sender.error("Your selections is too large: "+area+" blocks."
                                                             +"You are allowed to copy "+maxArea+" block only.");
            return;
        }
        try{
//Logger.getGlobal().info("1");
            if(ClipboardManager.copyToClipboard(sender, (CuboidRegion) weRegion)) {
                sender.info("Your selection was copied to your clipboard.");
            } else {
                sender.error("There was an error. Your selection was not copied.");
            }
        } catch (CopyPasteException ex) {
            sender.error(ex.getMessage());
        }
    }
    
}
