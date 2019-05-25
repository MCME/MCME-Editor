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
package com.mcmiddleearth.mcme.editor.listener;

import com.mcmiddleearth.mcme.editor.Permissions;
import com.mcmiddleearth.mcme.editor.command.BlockCommand;
import com.mcmiddleearth.mcme.editor.command.sender.EditPlayer;
import com.mcmiddleearth.mcme.editor.data.PluginData;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Eriol_Eandur
 */
public class BlockSelectionListener implements Listener {
    
    //Select blockData by clicking with tool item
    @EventHandler
    public void onSelectBlock(PlayerInteractEvent event) {
        EditPlayer player = PluginData.getOrCreateEditPlayer(event.getPlayer());
        ItemStack item = event.getItem();
        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) 
                && item !=null && item.getType().equals(Material.PRISMARINE_SHARD)) {
            if(player.hasPermissions(Permissions.BLOCK)) {
                if(player.hasPermissions(Permissions.BLOCK_REPLACE)
                        || player.getBlockSelectionMode().equals(EditPlayer.BlockSelectionMode.COUNT)
                            && player.hasPermissions(Permissions.BLOCK_COUNT)) {
                    event.setCancelled(true);
                    switch(player.getBlockSelectionMode()) {
                        case COUNT:
                            player.addCount(event.getClickedBlock().getBlockData());
                            BlockCommand.sendSelectedBlocksMessage(player);
                            break;
                        case REPLACE:
                            if(player.getIncompleteBlockSelection()==null) {
                                player.setIncompleteBlockSelection(event.getClickedBlock().getBlockData());
                                player.info("Block data to be replaced saved, next click at a block to place instead.");
                            } else {
                                player.addReplace(player.getIncompleteBlockSelection(),
                                            event.getClickedBlock().getBlockData());
                                player.setIncompleteBlockSelection(null);
                                BlockCommand.sendSelectedBlocksMessage(player);//"Block data selected for replacing (hover for more info).");
                            }
                            break;
                        case SWITCH:
                            if(player.getIncompleteBlockSelection()==null) {
                                player.setIncompleteBlockSelection(event.getClickedBlock().getBlockData());
                                player.info("Block data to be switched saved, next click at a block to switch with.");
                            } else {
                                player.addSwitch(player.getIncompleteBlockSelection(),
                                            event.getClickedBlock().getBlockData());
                                player.setIncompleteBlockSelection(null);
                                BlockCommand.sendSelectedBlocksMessage(player);
                            }
                            break;
                    }
                }
            }
        }
    }
}