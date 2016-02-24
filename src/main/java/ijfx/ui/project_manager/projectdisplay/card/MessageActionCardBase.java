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
package ijfx.ui.project_manager.projectdisplay.card;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.core.project.Project;
import javafx.beans.property.Property;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import mongis.utils.AsyncCallback;
import mongis.utils.TaskButtonBinding;

/**
 *
 * @author cyril
 */
public abstract class MessageActionCardBase extends BorderPane implements ProjectCard {

    private final Label label = new Label();
    private final Button button = new Button();
    private final TaskButtonBinding taskButtonBinding = new TaskButtonBinding(button);
    private final FontAwesomeIcon icon;
    private final String NAME;
    private final VBox buttonBox = new VBox();
    
    private Project project;
    
    public MessageActionCardBase(String name, String message, FontAwesomeIcon icon) {
        this.icon = icon;
        label.setText(message);
        NAME = name;
        configureBinding(taskButtonBinding);
        taskButtonBinding.runTaskOnClick(this::onClick);
        setCenter(label);
        setBottom(buttonBox);
        buttonBox.getChildren().add(button);
    }

    protected abstract void configureBinding(TaskButtonBinding binding);
    protected abstract Task onClick(TaskButtonBinding binding);
    protected abstract Boolean shouldDisplay(Project project);
    @Override
    public Node getContent() {
        return this;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public FontAwesomeIcon getIcon() {
        return icon;
    }

    DismissableCardDecorator<Project> decorator = new DismissableCardDecorator<>(this);

    @Override
    public Property<Boolean> dismissable() {
        return decorator.dismissable();

    }

    @Override
    public Property<Boolean> dismissed() {
        return decorator.dismissed();
    }
    
    @Override
    public Task<Boolean> update(Project project) {
        this.project = project;
        
        return new AsyncCallback<Project,Boolean>()
               .setInput(project)
                .run(this::shouldDisplay)
                .then(result->decorator.dismissed().setValue(!result))
                ;
    }
    
    public void addButton(String text, FontAwesomeIcon icon,Runnable runnable, String... cssClasses) {
        Button button = new Button(text,new FontAwesomeIconView(icon));
        button.setOnAction(event->runnable.run());
        button.getStyleClass().addAll(cssClasses);
        buttonBox.getChildren().add(button);
    }

}
