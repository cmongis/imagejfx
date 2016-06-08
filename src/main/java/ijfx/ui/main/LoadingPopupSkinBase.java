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
package ijfx.ui.main;

import ijfx.ui.IjfxCss;
import ijfx.ui.context.animated.Animations;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Skin;
import javafx.scene.layout.StackPane;

/**
 *
 * @author cyril
 */
public class LoadingPopupSkinBase extends StackPane implements Skin<LoadingPopup> {

    private LoadingPopup skinnable;
    
    
    private final Label label = new Label();
    
    private final LoadingIcon icon = new LoadingIcon(80);
    
    private final ProgressBar progressBar = new ProgressBar();
    
    private final Button cancelButton = new Button("Cancel");
    
    private final Button finishButton = new Button("Finished !");
    
    private int margin = 20;
    
    private int baseY = -20;
    
    private final DoubleProperty labelWidth = new SimpleDoubleProperty(150);
    
    public LoadingPopupSkinBase(LoadingPopup skinnable) {
        this.skinnable = skinnable;
        
        
        getChildren().addAll(label,icon,progressBar,finishButton,cancelButton);
        
        bindY(label,this::getLabelY);
        bindY(icon,this::getIconY);
        bindY(cancelButton,this::getButtonY);
        bindY(finishButton, this::getButtonY);
        bindY(progressBar,this::getProgressY);
        
        prefWidthProperty().bind(skinnable.prefWidthProperty());
        prefHeightProperty().bind(skinnable.prefHeightProperty());
        
        label.textProperty().bind(skinnable.messageProperty());
        cancelButton.visibleProperty().bind(skinnable.canCancelProperty().and(skinnable.taskRunningProperty()));
        
        
        cancelButton.prefWidthProperty().bind(labelWidth);
        finishButton.prefWidthProperty().bind(labelWidth);
        
        
        cancelButton.getStyleClass().add(IjfxCss.WARNING);
        
        finishButton.visibleProperty().bind(skinnable.showCloseButtonProperty());
        
        finishButton.translateXProperty().bind(Bindings.createDoubleBinding(this::getFinishButtonX, widthProperty(),skinnable.showingProperty()));
        
        progressBar.progressProperty().bind(skinnable.progressProperty());
        
        progressBar.visibleProperty().bind(Bindings.createBooleanBinding(this::shouldDisplayProgressBar , skinnable.progressProperty()));
        
        getStyleClass().add("dark-background");
        
        finishButton.setOnAction(this::onFinishedButtonClicked);
        
        skinnable.showingProperty().addListener(this::onShowing);
        
    }
    
    
    @Override
    public LoadingPopup getSkinnable() {
        return skinnable;
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public void dispose() {
        skinnable = null;
    }
    
    
   
    
    private void bindY(Node node, Callable<Double> callable) {
        node.translateYProperty().bind(Bindings.createDoubleBinding(callable, skinnable.heightProperty(),skinnable.showingProperty()));
    }
    
    private double getIconY() {
        return  baseY-(icon.getPrefHeight() / 2) - (margin);
    }
    
    private double half(Node node) {
        return node.layoutBoundsProperty().get().getHeight() / 2;
    }
    
    private double getProgressY() {
        return baseY + margin*2 + progressBar.getHeight() / 2;
    }
    
    private double getLabelY() {
        return getProgressY() + (progressBar.getHeight()/2) +  (margin) + (label.getHeight() / 2);
    }
    
    private double getButtonY() {
        return getLabelY() + ( label.getHeight() / 2 ) + margin + (cancelButton.getHeight() / 2) ;
    }
    
    private double getFinishButtonX() {
        System.out.println(" ??? ");
        return 0;
    }
    
    private void onFinishedButtonClicked(ActionEvent event) {
        skinnable.hide();
    }
    
    private void onShowing(Observable obs, Boolean oldValue, Boolean showing) {
        if(showing) {
            Animations.FADEIN.configure(this, ImageJFX.getAnimationDurationAsDouble()).play();
           icon.play();
        }
        else {
            icon.stop();
            Animations.FADEOUT.configure(this,ImageJFX.getAnimationDurationAsDouble()).play();
        }
    }
    
    private Boolean shouldDisplayProgressBar() {
        return skinnable.progressProperty().getValue() > 0;
    }
    
}
