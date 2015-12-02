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
package ijfx.ui.project_manager.other;

import ijfx.ui.project_manager.project.CodeHighlighterService;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;
import org.fxmisc.richtext.CodeArea;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril Quinton
 */
public abstract class RuleEditor extends Pane {
private static int BAR_WIDTH = 200;
    private static int BAR_HEIGHT = 40;
    protected final CodeArea codeArea;
    
    @Parameter
    protected CodeHighlighterService highlighter;

    public RuleEditor(Context context) {
        context.inject(this);
        //highlighter = contextService.getContext().getService(CodeHighlighterService.class);
        codeArea = new CodeArea();
        
        //codeArea.setPrefWidth(BAR_WIDTH);
        //codeArea.setPrefHeight(BAR_HEIGHT);
        //codeArea.setMaxHeight(Double.MAX_VALUE);
        //codeArea.setMaxWidth(Double.MAX_VALUE);
        codeArea.getStyleClass().add("text-field");
        codeArea.getStyleClass().add("query-bar");
        
        codeArea.getStylesheets().add(getClass().getResource("query-code.css").toExternalForm());
         codeArea.textProperty().addListener(this::handleCodeChange);
         String styleSheet = getClass().getResource("query-code.css").toExternalForm();
        ObservableList<String> list = this.getStylesheets();
        list.add(styleSheet);
        this.getChildren().add(codeArea);
    }
    public CodeArea getCodeArea() {
        return codeArea;
    }
    public String getText() {
        return codeArea.getText();
    }
    protected abstract void handleCodeChange(ObservableValue<? extends String> observable, String oldValue, String newValue);
}
