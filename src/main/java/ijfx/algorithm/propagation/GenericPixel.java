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
package ijfx.algorithm.propagation;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class GenericPixel<T> implements Pixel<T>{
    
    int x;
    int y;
    
    T value;
    
    boolean isValid = true;
 

    public GenericPixel(int x, int y, T value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    
    
    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public void setValid(boolean valid) {
        this.isValid = valid;
    }
    
    
    
    
    @FunctionalInterface
    public interface DoubleValueGetter {
        public Double getPixel();
    }
    
}
