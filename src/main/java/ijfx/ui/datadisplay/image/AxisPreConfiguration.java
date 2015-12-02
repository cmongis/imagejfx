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
package ijfx.ui.datadisplay.image;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.ui.arcmenu.ArcItemType;

/**
 *
 * @author Cyril MONGIS, 2015
 */
class AxisPreConfiguration {

    String label;
    FontAwesomeIcon icon;
    ArcItemType type;
    double angle;

    public AxisPreConfiguration(String label, FontAwesomeIcon icon, ArcItemType type, double angle) {
        this.label = label;
        this.icon = icon;
        this.type = type;
        this.angle = angle;
    }

    public String getLabel() {
        return label;
    }

    public AxisPreConfiguration setLabel(String label) {
        this.label = label;
        return this;
    }

    public FontAwesomeIcon getIcon() {
        return icon;
    }

    public AxisPreConfiguration setIcon(FontAwesomeIcon icon) {
        this.icon = icon;
        return this;
    }

    public ArcItemType getType() {
        return type;
    }

    public AxisPreConfiguration setType(ArcItemType type) {
        this.type = type;
        return this;
    }

    public double getAngle() {
        return angle;
    }

    public AxisPreConfiguration setAngle(double angle) {
        this.angle = angle;
        return this;
    }

    public static AxisPreConfiguration CHANNEL = new AxisPreConfiguration("channel", FontAwesomeIcon.TINT, ArcItemType.CHOICE, 90);
    public static AxisPreConfiguration TIME = new AxisPreConfiguration("time", FontAwesomeIcon.PLAY, ArcItemType.SLIDE, 180);
    public static AxisPreConfiguration Z = new AxisPreConfiguration("z", FontAwesomeIcon.STACK_OVERFLOW, ArcItemType.SLIDE, 0);

}
