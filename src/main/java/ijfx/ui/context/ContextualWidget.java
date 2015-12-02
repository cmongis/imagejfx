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
package ijfx.ui.context;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public interface ContextualWidget {

    /**
     *
     * @return
     */
    public String getName();

    /**
     *
     * @return
     */
    public boolean isHidden();

    /**
     *
     * @param show
     */
    public default void toggle(boolean show) {

        if (show) {

            if (isHidden()) {
                show();
            }
        } else {
            if (isHidden() == false) {
                hide();
            }
        }
    }

    /**
     *
     */
    public default void toggle() {
        if (isHidden()) {
            show();
        } else {
            hide();
        }
    }

    /**
     *
     * @param enabled
     */
    public default void enable(boolean enabled) {
        if (enabled) {
            enable();
        } else {
            disable();
        }
    }

    /**
     *
     */
    public void enable();

    /**
     *
     * @return
     */
    public boolean isEnabled();

    /**
     *
     */
    public void disable();

    /**
     *
     */
    public void show();

    /**
     *
     */
    public void hide();

}
