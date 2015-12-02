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
package ijfx.ui.project_manager;

import ijfx.core.project.AnnotationRule;
import ijfx.ui.project_manager.other.RuleEditor;
import ijfx.service.uicontext.UiContextService;
import java.util.function.Predicate;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;

/**
 *
 * @author Cyril Quinton
 */
public class EditingQueryCell extends TableCell<AnnotationRule, String> {

    private final UiContextService contextService;
    private final RuleEditor editor;
    private final Predicate<String> editCommitChecker;

    public EditingQueryCell(UiContextService contextService, RuleEditor editor, Predicate<String> editCommitChecker) {
        this.contextService = contextService;
        this.editor = editor;
        this.editCommitChecker = editCommitChecker;

    }

    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            initEditor();
            setText(null);
            setGraphic(editor);

        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getItem());
        setGraphic(null);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                editor.getCodeArea().replaceText(item);
                setText(null);
                setGraphic(editor);
            } else {
                setText(item);
                setGraphic(null);
            }

        }
    }

    private void handleFocusedChange(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (!newValue) {
            commitChange();
        }
    }

    private void commitChange() {
        String editText = editor.getText();
        if (editCommitChecker.test(editText)) {
            commitEdit(editText);
        } else {
            showText();
        }
    }

    private void initEditor() {
       showText();
        editor.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        editor.getCodeArea().focusedProperty().addListener(this::handleFocusedChange);

    }
    private void showText() {
         editor.getCodeArea().replaceText(getItem());
    }
}
