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
package com.mcmiddleearth.mcme.editor.job.action;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;

/**
 *
 * @author Eriol_Eandur
 */
public class ReplaceAction extends CountAction {
    
    @Getter
    private final BlockData replaceData;
    
    public ReplaceAction(int id, BlockData search, BlockData replace) {
        super(id, search);
        replaceData = replace;
    }
 
    @Override
    public BlockData apply() {
        applicationCount++;
        return replaceData;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String,Object> map = new HashMap<>();
        map.put("id",getId());
        map.put("search", getSearchData().getAsString(true));
        map.put("replace", replaceData.getAsString(true));
        return map;
    }
    
    public static ReplaceAction deserialize(Map<String,Object> map) {
        return new ReplaceAction((int)map.get("id"),
                                 Bukkit.createBlockData((String)map.get("search")),
                                 Bukkit.createBlockData((String)map.get("replace")));
    }
}
