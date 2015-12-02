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
package ijfx.ui.plugin;


import ijfx.ui.UiPlugin;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.bridge.FxStatusBar;
import ijfx.bridge.FxUserInterfaceBridge;
import ijfx.ui.context.animated.Animation;
import ijfx.ui.context.animated.TransitionQueue;
import ijfx.ui.main.ImageJFX;
import javafx.animation.Transition;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiContexts;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "fx-status-bar", context = UiContexts.IMAGEJ+" "+UiContexts.PROJECT_MANAGER, localization = "bottomLeftHBox")
public class FxStatusBarWidget extends HBox implements UiPlugin {

    @Parameter
    FxUserInterfaceBridge bridge;

    ProgressBar bar = new ProgressBar();

    Label status = new Label("");

    FxStatusBar fxStatusBar;

    //LoadingIcon icon = new LoadingIcon(16);
    
    TransitionQueue queue = new TransitionQueue();
    
    Transition appearance = Animation.FADEIN.configure(this,ImageJFX.getAnimationDurationAsDouble());
    Transition disapearance = Animation.FADEOUT.configure(this,ImageJFX.getAnimationDurationAsDouble());
    
  
    
    Property<Boolean> canCancel = FxStatusBar.getInstance().canCancelProperty();
    
    Button cancelButton = GlyphsDude.createIconButton(FontAwesomeIcon.ERASER);
    
    private long lastAppearance = System.currentTimeMillis();
    
    public FxStatusBarWidget() {
        super();
        
        fxStatusBar = FxStatusBar.getInstance();
        getChildren().addAll(cancelButton,bar, status);
        setSpacing(10.0);
        
        show();
        cancelButton.visibleProperty().bind(canCancel);
        cancelButton.setOnAction(FxStatusBar.getInstance()::onCancel);
        cancelButton.getStyleClass().add(ImageJFX.CSS_SMALL_BUTTON);
        //canCancel.bind(FxStatusBar.getInstance().canCancelProperty());
        
        disapearance.onFinishedProperty().addListener((ch,oldValue,newValue)->{
            
         });
        
    }
   
    
    

    
    

    @Override
    public Node getUiElement() {
        return this;
    }

    @Override
    public UiPlugin init() {

        bar.progressProperty().bind(fxStatusBar.progressProperty());
        status.textProperty().bind(fxStatusBar.statusProperty());

        
        status.textProperty().addListener(this::onChange);
        fxStatusBar.isProgressProperty().addListener(this::onIsProgressingChanged);
        bar.progressProperty().addListener(this::onChange);
        
        disapearance.setDelay(new Duration(5000));
        return this;
    }
    
    
    public void onIsProgressingChanged(Observable obs, Boolean oldValue, Boolean newValue) {
         if(newValue) {
             queue.queue(appearance);
         }
         else {
             queue.queue(disapearance);
         }
    }
    
    
    public void onChange(Observable obs) {
        
       
        if(fxStatusBar.isProgressProperty().getValue() && getOpacity() == 0.0) {
            queue.queue(appearance);
        }
        
        if(fxStatusBar.isProgressProperty().getValue()) return;
        
         if(queue.size() == 0) {
             queue.queue(appearance);
             
         }
         else {
             queue.remove(disapearance);
         }
         
         queue.queue(disapearance);
    }
    
    
    
    boolean visible = false;

    public void hide() {

        if (visible) {
            queue.queue(disapearance);
            visible = false;
            //icon.stop();
        }

        //setVisible(false);
    }

    public void show() {
        if (!visible) {
          
            queue.queue(appearance);
            visible = true;
            ////icon.play();
        }
    }

}
