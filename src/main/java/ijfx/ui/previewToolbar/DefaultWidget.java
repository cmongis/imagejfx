/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ijfx.ui.previewToolbar;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.service.preview.PreviewService;
import ijfx.ui.utils.FontAwesomeIconUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import mongis.utils.panecell.PaneIconCell;

/**
 *
 * @author Tuan anh TRINH
 */
public class DefaultWidget implements ItemWidget {

    private String type;
    private String label;
    private String action;
    private String icon;
    private String context;
    private Map<String, Object> parameters;

    public DefaultWidget() {
    }

    public DefaultWidget(String type, String label, String action, String icon, String context, Map<String, Object> parameters,double order) {
        this.type = type;
        this.label = label;
        this.action = action;
        this.icon = icon;
        this.context = context;
        this.parameters = parameters;
    }

    public DefaultWidget(String type, String label, String action, String icon, String context) {
        this.type = type;
        this.label = label;
        this.action = action;
        this.icon = icon;
        this.context = context;
        this.parameters = null;

    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getAction() {
        return action;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getIcon() {
        return icon;
    }

    @Override
    public Map<String, Object> getParameters() {
        if (parameters == null) parameters = new HashMap<>();
        return parameters;
    }

    @Override
    public String getContext() {
        return context;
    }

    @Override
    public void addContext(String s) {
        context = context + s;
    }

    @Override
    public void removeSpaceContext() {
        this.context = this.context.replaceAll("\\s", "+");
    }

    @Override
    public Item getValue() {
        return this;
    }

    @Override
    public Image getImage(PreviewService previewService, int size) {

        if (this.getIcon().equals("preview")) {
            try {
                previewService.setParameters(0, 0, size, size);
                return previewService.getImageDisplay(action, this.getParameters());

            } catch (Exception e) {
                e.printStackTrace();
                FontAwesomeIconView fontAwesomeIconView = new FontAwesomeIconView(FontAwesomeIcon.AMBULANCE);
                return FontAwesomeIconUtils.FAItoImage(fontAwesomeIconView, size);
            }
        } 
        //Check if icon exist in Enumeration
        else if (Arrays.stream(FontAwesomeIcon.values()).filter(e -> e.name().equals(icon)).count() > 0) {

            FontAwesomeIconView fontAwesomeIconView = new FontAwesomeIconView(FontAwesomeIcon.valueOf(icon));
            return FontAwesomeIconUtils.FAItoImage(fontAwesomeIconView, size);
        } else {
            Image image = new Image(getClass().getResource(icon).toExternalForm(), size, size, true, true);
            return image;
        }
    }
}
