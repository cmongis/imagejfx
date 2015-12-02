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
package ijfx.ui.project_manager.project;

import ijfx.core.listenableSystem.Listening;
import ijfx.core.project.Project;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 *
 * @author Cyril Quinton
 */
public class HierarchyValueDisplay extends VBox implements Listening {

    private final ChangeListener<ObservableList<String>> hierarchyListener;
    private final Project project;

    public HierarchyValueDisplay(Project project) {
        this.project = project;
        hierarchyListener = (ObservableValue<? extends ObservableList<String>> observable, ObservableList<String> oldValue, ObservableList<String> newValue) -> {
            createView(newValue);
        };
        project.getHierarchy().addListener(hierarchyListener);
        createView(project.getHierarchy());
    }

    private void createView(List<String> hierarchy) {
        this.getChildren().clear();
        for (String key : hierarchy) {
            this.getChildren().add(new Label(key));
        }

    }

    @Override
    public void stopListening() {
        project.getHierarchy().removeListener(hierarchyListener);
    }
}
