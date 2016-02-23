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

import ijfx.core.project.AnnotationRuleImpl;
import ijfx.core.project.ProjectManagerService;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.core.project.query.QueryService;
import ijfx.core.project.query.Selector;
import ijfx.service.uicontext.UiContextService;
import ijfx.ui.RichMessageDisplayer;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiPlugin;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.main.Localization;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import mongis.utils.AsyncCallback;
import mongis.utils.FXUtilities;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "rule-editor", localization = Localization.CENTER, context = "project-rule-edition")
public class RuleEditor extends BorderPane implements UiPlugin {

    @Parameter
    Context context;

    @Parameter
    UiContextService uiContextService;

    @Parameter
    QueryService queryService;

    @Parameter
    RuleEditorService ruleEditorService;

    @Parameter
    ProjectManagerService projectManagerService;

    @FXML
    Group selectorGroup;
    @FXML
    BorderPane borderPane;

    @FXML
    TextArea selectorTextArea;

    @FXML
    WebView selectorWebView;

    @FXML
    Label planeCountLabel;

    @FXML
    GridPane gridPane;

    ModifierSelector<Node> modifierEditor = new DefaultModifierEditor();

    RichMessageDisplayer richMessageDisplayer;

    int affectedPlaneCount = 0;

    private int WEB_VIEW_WIDTH = 400;

    private final String WEB_VIEW_ORIGINAL_TEXT = "Write here a *query*. All the *planes corresponding to the query* will be affected by the modification.<br><br>e.g. channel = 0 *and*&nbsp;file name contains stack";

    private String selectorPhrase = "";

    private Selector selector;

    public RuleEditor() {

        try {

            FXUtilities.injectFXML(this);

            Platform.runLater(this::initWebView);
            selectorTextArea.textProperty().addListener((obs, oldValue, newValue) -> {
                new AsyncCallback<String, Void>()
                        .setInput(newValue)
                        .run(this::updateSelector)
                        .then(this::updateUi)
                        .start();
            });

            updateUi(null);
            init();
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }

    }

    public void initWebView() {

        selectorWebView = new WebView();
        richMessageDisplayer = new RichMessageDisplayer(selectorWebView)
                // put a back to line betweens the words "and" and "or" --> better display of rules
                .addStringProcessor(RichMessageDisplayer.BACKLINE_ANDS_AND_ORS)
                // colors important words in orange
                .addStringProcessor(RichMessageDisplayer.COLOR_IMPORTANT_WORDS);

        selectorWebView.setPrefWidth(WEB_VIEW_WIDTH);
        selectorWebView.setPrefHeight(100);
        selectorWebView.setMaxWidth(Double.POSITIVE_INFINITY);
        selectorWebView.setMaxHeight(Double.POSITIVE_INFINITY);
        gridPane.add(selectorWebView, 1, 1);
        richMessageDisplayer.setMessage(WEB_VIEW_ORIGINAL_TEXT);

    }

    @Override
    public Node getUiElement() {
        return this;
    }

    @Override
    public UiPlugin init() {
        if (context != null) {
            context.inject(modifierEditor);
            modifierEditor.afterContextInjection();
            borderPane.setCenter(modifierEditor.getUiElement());
        }
        return this;
    }

    @FXML
    private void save() {
        System.out.println(modifierEditor.getEditedModifier());
        if (selector != null && modifierEditor.getEditedModifier() != null) {
            projectManagerService.getCurrentProject().addAnnotationRule(new AnnotationRuleImpl(selector, modifierEditor.getEditedModifier()));
        }
        cancel();
    }

    @FXML
    private void cancel() {
        uiContextService.leave("project-rule-edition");
        uiContextService.enter("project-rule-list");
        uiContextService.update();
    }

    // first we update the selectors.
    protected Void updateSelector(String query) {
        selector = queryService.getSelector(query);
        List<PlaneDB> planeList = queryService.query(projectManagerService.getCurrentProject(), selector, true);
        affectedPlaneCount = planeList.size();
        selectorPhrase = selector.phraseMe();
        
        return null;
    }

    // then on the java fx, we update the ui with the calculated value
    protected void updateUi(Void v) {
        if (richMessageDisplayer != null) {
            if (selectorTextArea.getText().trim().equals("")) {
                richMessageDisplayer.setMessage(WEB_VIEW_ORIGINAL_TEXT);
            } else {
                richMessageDisplayer.setMessage(selectorPhrase);
            }
        }
        planeCountLabel.setText("" + affectedPlaneCount);
    }
}
