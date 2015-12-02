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
import ijfx.ui.main.ImageJFX;
import ijfx.ui.main.Localization;

import java.util.HashMap;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.scijava.command.CommandService;
import org.scijava.menu.MenuService;
import org.scijava.menu.ShadowMenu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.tool.ToolService;
import ijfx.ui.UiConfiguration;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "module-search", context = "imagej", localization = Localization.TOP_RIGHT,order=0.5)
public class ModuleSearchField extends HBox implements UiPlugin {

    TextField textfield = new TextField();

    @Parameter
    CommandService commandService;

    @Parameter
    MenuService menuService;

    HashMap<String, Runnable> modules = new HashMap<>();

    AutoCompletionBinding<String> bindAutoCompletion;

    Transition focusingTransition;
    Transition defocusingTransition;

    @Parameter
    ToolService toolService;

    public ModuleSearchField() {
        super();

        getChildren().add(textfield);
        textfield.setId("module-search-textfield");
        textfield.setPromptText("Search a module...");
    }

    public UiPlugin init() {

        registerAction(menuService.getMenu());

        bindAutoCompletion = TextFields.bindAutoCompletion(textfield, modules.keySet());

        textfield.addEventHandler(KeyEvent.KEY_RELEASED, event -> handleKeyPress(event));

        textfield.focusedProperty().addListener(this::onFocus);
        double xtr = -200;
        double ytr = 50;
        TranslateTransition onFocus = new TranslateTransition(ImageJFX.ANIMATION_DURATION, textfield);
        TranslateTransition onDefocus = new TranslateTransition(ImageJFX.ANIMATION_DURATION, textfield);
        ScaleTransition bigger = new ScaleTransition(ImageJFX.ANIMATION_DURATION, textfield);
        ScaleTransition smaller = new ScaleTransition(ImageJFX.ANIMATION_DURATION, textfield);
        bigger.setByX(2);
        smaller.setByX(-2);
        onFocus.setByX(xtr);
        onFocus.setByY(ytr);
        onDefocus.setByX(-xtr);
        onDefocus.setByY(-ytr);

        focusingTransition = new ParallelTransition(onFocus);
        defocusingTransition = new ParallelTransition(onDefocus);

        return this;

    }

    public void onFocus(ObservableValue<? extends Boolean> property, Boolean oldValue, Boolean newValue) {

        
        if (true) {
            return;
        }
        if (newValue) {
            focusingTransition.play();
        } else {
            defocusingTransition.play();
        }
    }

    public void handleKeyPress(KeyEvent event) {
        
        if (event.getCode() == KeyCode.ENTER) {

            try {

                modules.get(textfield.getText()).run();
                textfield.setText("");
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

    }

    public void registerAction(ShadowMenu menu) {
        if (menu.isLeaf()) {
            modules.put(menu.getName(), () -> menu.run());
        } else {
            menu.getChildren().forEach(child -> registerAction(child));
        }
    }

    @Override
    public Node getUiElement() {
        return this;
    }

    private abstract class Runner {

        String name;
        Runnable run;

        public Runner(String name, Runnable run) {
            this.name = name;
            this.run = run;
        }

    }

}
