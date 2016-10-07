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
package ijfx.ui.correction;

import ijfx.ui.RichMessageDisplayer;
import ijfx.ui.activity.Activity;
import ijfx.ui.main.ImageJFX;
import io.scif.services.DatasetIOService;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import mongis.utils.CallableTask;
import mongis.utils.FXUtilities;
import net.imagej.Dataset;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;

/**
 *
 * @author cyril
 */
@Plugin(type = Activity.class, name = "correction-activity-2")
public class CorrectionSelector  extends BorderPane implements Activity{

    private List<PluginInfo<CorrectionUiPlugin>> pluginList;
    
    @FXML
    Accordion accordion;
    
    @FXML
    MenuButton correctionListButton;
    
    @Parameter
    Context context;
    
    @Parameter
    DatasetIOService datasetIoService;
    
    @Parameter
    PluginService pluginService;
    
    private final ObservableList<CorrectionUiPlugin> addedPlugins = FXCollections.observableArrayList();
    
    
    boolean hasInit = false;
    
    private Property<Dataset> exampleDataset = new SimpleObjectProperty();
    
    public CorrectionSelector() {
        try {
            FXUtilities.injectFXML(this,"/ijfx/ui/correction/CorrectionSelector.fxml");
        } catch (IOException ex) {
           ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }
    }
    
    public void init() {
        try {
            pluginList = pluginService.getPluginsOfType(CorrectionUiPlugin.class);
            
            correctionListButton.getItems().addAll(pluginList.stream().map(this::createMenuItem).collect(Collectors.toList()));
            
            exampleDataset.setValue(datasetIoService.open("./src/test/resources/multidim.tif"));
            
            hasInit = true;
        } catch (IOException ex) {
            Logger.getLogger(CorrectionSelector.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    
    private MenuItem createMenuItem(PluginInfo<CorrectionUiPlugin> infos) {
        MenuItem item = new MenuItem(infos.getLabel());
        item.setOnAction(event->addCorrection(infos));
        return item;
    }
    
    public void addCorrection(PluginInfo<CorrectionUiPlugin> infos) {
        
        CorrectionUiPlugin createInstance = pluginService.createInstance(infos);
        createInstance.init();
        createInstance.exampleDataset().bind(exampleDataset);
        accordion.getPanes().add(new CorrectionUiPluginWrapper(createInstance).deleteUsing(this::removeCorrection));
        
        
    }
    
    public void removeCorrection(CorrectionUiPlugin correction) {
        accordion.getPanes().remove(getWrapper(correction));
    }
    
    public CorrectionUiPluginWrapper getWrapper(CorrectionUiPlugin correctionPlugin) {
        return accordion.getPanes().stream().map(pane->(CorrectionUiPluginWrapper)pane).filter(pane->pane.getPlugin() == correctionPlugin).findFirst().orElse(null);
    }
    
    
    @Override
    public Node getContent() {
        if(!hasInit) init();
        return this;
    }

    @Override
    public Task updateOnShow() {
        
        return new CallableTask<>(this::nothing);
        
    }
    
    public Void nothing() {
        return null;
    }
    
    
    
}
