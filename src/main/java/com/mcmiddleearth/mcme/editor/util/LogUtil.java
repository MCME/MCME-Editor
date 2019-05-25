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
package com.mcmiddleearth.mcme.editor.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eriol_Eandur
 */
public class LogUtil {
    
    public static void error(String message) {
        Logger.getLogger(LogUtil.class.getName()).log(Level.SEVERE,message);
    }
    
    public static void warn(String message) {
        Logger.getLogger(LogUtil.class.getName()).log(Level.WARNING,message);
    }
    
    public static void info(String message) {
        Logger.getLogger(LogUtil.class.getName()).log(Level.INFO,message);
    }
    
    public static void verbose(int level, String message) {
        Logger.getLogger(LogUtil.class.getName()).log(Level.parse(""+level),message);
    }
    
    public static void setLevel(int level) {
        Logger.getLogger(LogUtil.class.getName()).setLevel(Level.parse(""+level));
    }

    public static Level getLevel() {
        return Logger.getLogger(LogUtil.class.getName()).getLevel();
    }
}
