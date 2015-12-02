/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package ijfx.ui.module.input;

/**
 * Interface for a Factory that creates Skin according to the type of input
 * @author Cyril MONGIS, 2015
 */
public interface InputSkinFactory{
    
    /**
     * Creates a Skin depending of the Input type
     * @param input Object implementing the Input interface
     * @return 
     */
    public InputSkin createSkin(Input input);
    
    /**
     * Returns true if the Factory can create a Skin for the following input
     * @param input 
     * @return true if the factory can create a skin for the input
     */
    public boolean canCreateSkinFor(Input input);
   
    
    
}
