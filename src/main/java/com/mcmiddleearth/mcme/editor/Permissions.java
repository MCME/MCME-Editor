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
package com.mcmiddleearth.mcme.editor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author Eriol_Eandur
 */
public enum Permissions {
    
    BLOCK               ("mcmeeditor.command.block",            PermissionDefault.OP),
    BLOCK_FILES         ("mcmeeditor.command.block.files",      PermissionDefault.OP, BLOCK),
    BLOCK_QUEUE         ("mcmeeditor.command.block.queue",      PermissionDefault.OP, BLOCK),
    BLOCK_QUEUE_OTHER   ("mcmeeditor.command.block.queue.other",      PermissionDefault.OP, BLOCK_QUEUE),
    BLOCK_COUNT         ("mcmeeditor.command.block.count",      PermissionDefault.OP, BLOCK),
    BLOCK_REPLACE       ("mcmeeditor.command.block.replace",    PermissionDefault.OP, BLOCK);
    
    @Getter
    private final String permissionNode;
    
    @Getter
    private final Permissions[] children;
    
    @Getter
    private final PermissionDefault defaultPerm;

    private Permissions(String permissionNode, PermissionDefault defaultPerm, Permissions... children) {
        this.permissionNode = permissionNode;
        this.children = children;
        this.defaultPerm = defaultPerm;
    }
    
    public Permissions[] getWithChildren() {
        Permissions[] result = Arrays.copyOf(children, children.length+1);
        result[children.length] = this;
        return result;
    }
    
    public static void register() {
        for(Permissions editorPermission: Permissions.values()) {
            Map<String, Boolean> children = new HashMap<>();
            for(Permissions child: editorPermission.getChildren()) {
                children.put(child.getPermissionNode(), Boolean.TRUE);
            }
            Permission bukkitPerm = new Permission(editorPermission.getPermissionNode(), 
                                                   editorPermission.getDefaultPerm(),
                                                   children);
            Bukkit.getServer().getPluginManager().addPermission(bukkitPerm);
//Logger.getGlobal().info("register: "+bukkitPerm.getName());
        }
    }
}