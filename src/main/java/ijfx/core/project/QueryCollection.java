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
package ijfx.core.project;
import ijfx.core.project.imageDBService.PlaneDB;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 *
 * @author Cyril Quinton
 */
public interface QueryCollection <T> {
     public void query(Predicate<T> p, Consumer<T> c);
     public static<E> void query(Predicate<E> p, Consumer<E> c, Collection<E> collection) {
          for (E obj: collection) {
            if (p.test(obj)) {
                c.accept(obj);
            }
        }
     }
}
