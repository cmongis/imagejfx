/*
    This file is part of ImageJ FX.

    ImageJ FX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ImageJ FX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
    
     Copyright 2015,2016 Cyril MONGIS, Michael Knop
	
 */
package ijfx.ui.explorer.view.chartview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javafx.scene.paint.Color;

/**
 *
 * @author Tuan anh TRINH
 */
public class ColorGenerator {

    List<Color> colorList;

    int numColor;

    public ColorGenerator(List<Color> colorList, int n) {
        numColor = n;
        this.colorList = colorList;
    }

    public Color generateColor(Color x, Color y) {

        double blending = 1.0 / numColor;//set by you

        double oppose_blending = 1 - blending;
        double red = x.getRed() * blending + y.getRed() * oppose_blending;
        double green = x.getGreen() * blending + y.getGreen() * oppose_blending;
        double blue = x.getBlue() * blending + y.getBlue() * oppose_blending;

        double max = 255;
        Color blended = new Color(red / max, green / max, blue / max, 1.0);
        return blended;
    }

    public static List<Color> generateColor(List<Color> colors, int numColor){
        colors.stream().forEach(e -> {
            Color[] tmpColors = new Color[colors.size()/numColor];
            Arrays.fill(tmpColors, e);
        });
    }
    public static List<Color> generateInterpolatedColor(List<Color> colorList, int numColor) {
        List<Color> result = new ArrayList<>();

        for (int i = 0; i < colorList.size() - 1; i++) {
            result.addAll(RgbLinearInterpolate(colorList.get(i), colorList.get(i + 1), ((Integer)numColor).doubleValue()/(colorList.size()-1)));
        }
        return result;
    }

    public static List<Color> RgbLinearInterpolate(Color start, Color end, double colorCount) {
        List<Color> colors = new ArrayList<>();
        colorCount = Math.round(colorCount);
        // linear interpolation lerp (r,a,b) = (1-r)*a + r*b = (1-r)*(ax,ay,az) + r*(bx,by,bz)
        for (int n = 0; n < colorCount; n++) {
            double r = (double) n / (double) (colorCount - 1);
            double nr = 1.0 - r;
            double alpha = (nr * start.getOpacity()) + (r * end.getOpacity());
            double red = (nr * start.getRed()) + (r * end.getRed());
            double green = (nr * start.getGreen()) + (r * end.getGreen());
            double blue = (nr * start.getBlue()) + (r * end.getBlue());

            colors.add(new Color(red, green, blue, alpha));
        }

        return colors;
    }

    public List<Color> getColorList() {
        return colorList;
    }

    public int getNumColor() {
        return numColor;
    }

}
