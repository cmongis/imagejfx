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
import ijfx.ui.arcmenu.ArcItem;
import ijfx.ui.arcmenu.ArcItemType;
import ijfx.ui.main.ImageJFX;

import java.util.logging.Logger;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.ImageDisplay;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class AxisArcItem extends ArcItem<Double> {

    CalibratedAxis axis;
    int id;
    ImageDisplay display;

    
    Logger logger = ImageJFX.getLogger();
    
    
    
    public AxisArcItem(ImageDisplay display, int id) {
        super(display.axis(id).type().getLabel(), FontAwesomeIcon.QUESTION);
        setup(display,id);
        
        if(axisCompatible(axis, AxisPreConfiguration.TIME)) {
            configure(AxisPreConfiguration.TIME);
        }
        else if(axisCompatible(axis, AxisPreConfiguration.Z))
            configure(AxisPreConfiguration.Z);
        else if (axisCompatible(axis, AxisPreConfiguration.CHANNEL))
            configure(AxisPreConfiguration.CHANNEL);
        else {
            configure(new AxisPreConfiguration("?", FontAwesomeIcon.QUESTION, ArcItemType.SLIDE, 180+90));
        }

    }

    
    public void setup(ImageDisplay display, int id) {
        this.display = display;
        axis = display.axis(id);
        this.id = id;
    }
            
            
    public AxisArcItem(ImageDisplay display, int id,AxisPreConfiguration configuration) {
        super(display.axis(id).type().getLabel(), FontAwesomeIcon.QUESTION);
       setup(display,id);
        
        configure(configuration);
        
        
    }
    
    public void configure(AxisPreConfiguration configuration) {
        
        setIcon(configuration.getIcon());
        setType(configuration.getType());
        if(configuration.getType() == ArcItemType.CHOICE) {
             long len = display.max(id)+1;
             
             logger.info("Channel's max : "+len);
            Double[] choices = new Double[(int)len];
            
            
            sliderWidth.set(50*choices.length);
            
            
            for (int i = 0; i != len; i++) {
                choices[i] = new Double(i);
            }
            setChoices(choices);
        }
        
        if(configuration.getType() == ArcItemType.SLIDE) {
            setSliderMinValue(display.min(id));
            setSliderMaxValue(display.max(id));
            setSliderTick(1.0);
            setValue(0.0);
        }
        
         sliderValue.addListener((event, old, newValue) -> updateAxis(newValue.doubleValue()));
        
    }
    
    

    protected String getAxisLabel(CalibratedAxis axis) {
        return axis.type().getLabel().toLowerCase();
    }

    protected boolean axisLabelContains(CalibratedAxis axis, String string) {
        return getAxisLabel(axis).contains(string.toLowerCase());
    }

    protected boolean axisCompatible(CalibratedAxis axis, AxisPreConfiguration conf) {
        return axisLabelContains(axis, conf.getLabel());
    }

    public FontAwesomeIcon getAxisIcon(CalibratedAxis axis) {
        if (axis.type().getLabel().toLowerCase().contains("z")) {
            return FontAwesomeIcon.ARROWS_V;
        }
        if (axis.type().getLabel().toLowerCase().contains("channel")) {
            return FontAwesomeIcon.TINT;
        }
        return FontAwesomeIcon.QUESTION;
    }

    public void updateAxis(double newValue) {
       // logger.info(String.format("Updating axis %s (%d - %d) : %.3f",axis.type().toString(),display.min(id),display.max(id),newValue));
        display.setPosition(Math.round(newValue), axis.type());
    }

}
