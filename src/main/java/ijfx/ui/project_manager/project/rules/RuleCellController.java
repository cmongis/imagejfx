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
package ijfx.ui.project_manager.project.rules;

import ijfx.core.project.AnnotationRule;
import ijfx.core.project.Project;
import ijfx.core.project.ProjectManagerService;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.core.project.query.QueryService;
import ijfx.ui.RichMessageDisplayer;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import mongis.utils.AsyncCallback;
import mongis.utils.FXUtilities;
import mongis.utils.FunctionnalTask;
import mongis.utils.ListCellController;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class RuleCellController extends BorderPane implements ListCellController<AnnotationRule> {

    /*
        Services
     */
    @Parameter
    ProjectManagerService projectManagerService;

    @Parameter
    QueryService queryService;

    /* 
        Attributes
     */
    AnnotationRule rule;

    List<PlaneDB> selectedPlanes;

    long selectedPlaneCount;
    long modifiablePlaneCount;

    /*
        FXML
    
    @FXML
    Label selectorLabel;
    @FXML
    Label modifierLabel;
     */
    @FXML
    Label countLabel;

    @FXML
    Button applyButton;

    @FXML
    WebView ruleWebView;

    RichMessageDisplayer richMessageDisplayer;

    public static final String APPLY_RULE_TO = "Apply (%d planes)";
    public static final String APPLY_RULE = "Apply rule";
    public static final String COUNT = "%d planes are concerned, %d are left to be modified";

    Consumer<AnnotationRule> deleteHandler;

    Consumer<AnnotationRule> applyHandler;

    public RuleCellController() {
        try {
            FXUtilities.injectFXML(this);
            richMessageDisplayer = new RichMessageDisplayer(ruleWebView)
                    .addStringProcessor(RichMessageDisplayer.COLOR_IMPORTANT_WORDS)
                    .addStringProcessor(RichMessageDisplayer.BIGGER);
            ruleWebView.setPrefHeight(70);
            ruleWebView.prefWidthProperty().bind(Bindings.subtract(widthProperty(), 200));
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setItem(AnnotationRule t) {
        rule = t;
        selectedPlanes = queryService.query(getCurrentProject(), rule.getSelector(), false);
        update();

    }

    @Override
    public AnnotationRule getItem() {
        return rule;
    }

    public void updateCounts() {
        selectedPlaneCount = calculateSelectedPlaneCount();
        modifiablePlaneCount = calculateModifiablePlaneCount();
    }

    public void updateUi() {
        applyButton.setDisable(modifiablePlaneCount <= 0);
        applyButton.setText(modifiablePlaneCount > 0 ? String.format(APPLY_RULE_TO, modifiablePlaneCount) : APPLY_RULE);

        countLabel.setText(String.format(COUNT, selectedPlaneCount, modifiablePlaneCount));
    }

    private void update() {
        if (rule == null) {
            return;
        }

        //selectorLabel.setText(rule.getSelector().phraseMe());
        //modifierLabel.setText(rule.getModifier().phraseMe());
        richMessageDisplayer.setMessage(rule.getModifier().phraseMe() + " to all the planes where " + rule.getSelector().phraseMe());

        new AsyncCallback<Void, Void>()
                .run(this::updateCounts)
                .then(v -> updateUi())
                .start();

    }

    public int calculateSelectedPlaneCount() {
        return selectedPlanes.size();
    }

    public long calculateModifiablePlaneCount() {
        return selectedPlanes.parallelStream().filter(plane -> !rule.getModifier().wasApplied(plane)).count();
    }

    private Project getCurrentProject() {
        return projectManagerService.getCurrentProject();
    }

    @FXML
    private void applyRule() {
        applyHandler.accept(rule);
    }

    @FXML
    private void delete() {
        deleteHandler.accept(rule);
    }

    public void setDeleteHandler(Consumer<AnnotationRule> deleteHandler) {
        this.deleteHandler = deleteHandler;
    }

    public void setApplyHandler(Consumer<AnnotationRule> applyHandler) {
        this.applyHandler = applyHandler;
        update();
    }

}
