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

import ijfx.service.workflow.Workflow;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.Node;
import net.imagej.Dataset;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

/**
 *
 * @author cyril
 */
public interface CorrectionUiPlugin extends SciJavaPlugin{
    
    public void init();
    
    /**
     * Property explanining what the plugin will do
     * @return 
     */
    public ReadOnlyStringProperty explanationProperty();
    
    /**
     * Property should be left unbind by the plugin. It will be updated with example dataset
     * to help for configuration
     * @return 
     */
    public Property<Dataset> exampleDataset();
    /**
     * Property checked to see if the workflow is well configured.
     * If the property is null, the plugin is considered false
     * 
     * @return 
     */
    public ReadOnlyObjectProperty<Workflow> workflowProperty();
    
    public Node getContent();
    
    public default String getName() {
        return getClass().getAnnotation(Plugin.class).label();
    }
    
}
