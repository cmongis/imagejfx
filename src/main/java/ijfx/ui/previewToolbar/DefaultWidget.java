/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ijfx.ui.previewToolbar;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.service.preview.PreviewService;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.utils.FontAwesomeIconUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import mongis.utils.CallbackTask;

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

    @JsonIgnore
    private Image image;
    
    @JsonIgnore
    private Logger logger = ImageJFX.getLogger();
    
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
        
        if(image != null) return image;
        
        
        if (previewService.getImageDisplayService().getActiveDataset()==null)
        {
            return null;
        }
        else if (this.getIcon().equals("preview")) {
            try {
                previewService.setParameters(-1, -1, size, size);
                return previewService.getImageDisplay(action, this.getParameters());

            } catch (Exception e) {
                logger.log(Level.WARNING, "Error when loading preview", e);
                FontAwesomeIconView fontAwesomeIconView = new FontAwesomeIconView(FontAwesomeIcon.AMBULANCE);
                return FontAwesomeIconUtils.FAItoImage(fontAwesomeIconView, size);
            }
        } 
        
        else if(getIcon().startsWith("char:")) {
            Canvas canvas = new Canvas(size,size);
            GraphicsContext graphicsContext2D = canvas.getGraphicsContext2D();
            
            graphicsContext2D.setFill(Color.WHITE);
            graphicsContext2D.setFont(javafx.scene.text.Font.font("Arial", size));
            graphicsContext2D.fillText(getIcon().substring(5), size/3, size*0.8);
            
             final SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
//            final WritableImage snapshot = canvas.snapshot(params, null);
            
            Task<WritableImage> getIcon = new CallbackTask<Canvas, WritableImage>(canvas)
                    .run(input->input.snapshot(params, null));
            
            Platform.runLater(getIcon);
            
            try {
                // Image image = new Ima

                image = getIcon.get();
                return image;
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            return null;
        }
        
        //Check if icon exist in Enumeration
        else if (Arrays.stream(FontAwesomeIcon.values()).filter(e -> e.name().equals(icon)).count() > 0) {

            FontAwesomeIconView fontAwesomeIconView = new FontAwesomeIconView(FontAwesomeIcon.valueOf(icon));
            return FontAwesomeIconUtils.FAItoImage(fontAwesomeIconView, size);
        } else {
            image = new Image(getClass().getResource(icon).toExternalForm(), size, size, true, true);
            return image;
        }
    }
}
