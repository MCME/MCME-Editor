package com.mcmiddleearth.mcme.editor.data.block;

import org.bukkit.block.data.BlockData;

public class InventoryClearData implements EditBlockData {

    public InventoryClearData() {
    }

    @Override
    public BlockData getBlockData() {
        return null;
    }

    @Override
    public String getAsString(boolean hideUnspecified) {
        return null;
    }

    @Override
    public String getAsString() {
        return null;
    }

    @Override
    public EditBlockData clone() {
        try {
            EditBlockData clone = (EditBlockData) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return new InventoryClearData();
    }

    @Override
    public double getProbability() {
        return 1;
    }
}
