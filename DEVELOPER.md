# Developer Manual

## Getting started
ImageJ FX was origanally developed using Netbeans. However, it's a regular Maven/Git project so you should be able to edit it with any IDE.

### With Netbeans

TODO: Coming soon


## Understanding ImageJ FX

### Boot sequence

ImageJFX relies heavily on the SciJava library like ImageJ. However, the launch process is a bit different. Instead of being launch as User Interface by ImageJ, ImageJ FX launch its main window, create an ImageJ instance and inject the context to itself.

### Context based interface

ImageJFX doesn't have an interface with fixed widgets. The Main Window is split into different regions which load widgets inside them depending on the situation (or UiContext).

#### What's a UiContext ?
A UiContext is just a String representing a situation for the user. Several UiContext can happen at the same time and widgets are linked to one or more UiContext. UiContexts are managed by the UiContextService

~~~java
@Parameter
UiContextService uiContextService;

public void onClick() {
	uiContextService.enter("image-open");
	uiContextService.enter("8-bit-image");
	uiContextService.update();
}

~~~
The previous code will trigger the display of all widgets linked to the *image-open* and *8-bit-image* UiContext. As you can see, eases the creation of button that should always appear when a 8-bits image is edited. The programmer doesn't have to care about hiding or showing.

#### Create a Widget

A Widget returns a JavaFX node and is caracterized by 4 attributes :

* **id** : important, allows the Context Manager to find the widget
* **localization** : tells the User interface where the 
* **context** : list of contexts is associated to
* **order** : order inside its localization [Optional]

Here is a boiler plate widget

~~~java

@Plugin(type = FxWidgetPlugin.class)
@Widget(id = "context-switch-button", localization = Localization.BOTTOM_RIGHT, context="always")
public class ContextSwitchButton {
	
	// the services will be injected *AFTER* instanciation
	@Parameter
	UiContextService uiContextService;
	
	Button button;
	
	// constructor
	public ContextSwitchButton() {
		// creating the button
		button = new Button("Switch to my context");
		// setting the click handler
		button.setOnAction(this::onClick);
	}
	
	
	// method ran after injection of the service
	public FxWidgetPlugin init() {
		// you can now uses the services
		
		return this;
	}
	
	// should return the node displayed in the interface
	public Node getWidget() {
		return button;
	}
	
	// click event handler for the button
	public void onClick(ActionEvent event) {
	
		// when clicking the button, we enter the context
		uiContextService.enter("my-context");
		
		// if you don't run the update method, the widgets
		// won't be hidden or updated.
		uiContextService.update();
	}

}

~~~

As you can see, a UiContext doesn't need to be pre-existent. You can create as many UiContext as you want but it's also your responsability to provide a way to the user to come back to a previous contexts.

In this way, one could imagine creating an set of widgets and plugins linked to "Super-resolution" processes.

However, Widgets should only be used to provide an nice UI elements. The logic of your UiContext should be inside a SciJava Service and the image process should operate through ImageJ Modules.

## ImageJ FX Programming Guide lines

If you plan to extend ImageJ FX with a new context, logic and UI Elements, you should follow the following guide line :

### Logic inside SciJava Service

ImageJ2 provides a really powerfull API in order to deal with services and dependancy injection. You can create a Service that contains the whole logic and inject it inside your widgets. The Widgets should only be a link between the user and the service.

As an example, let's create a Service that manages favorites file for the user. The usual behaviour is to create an java interface representing the service and implements it later with an concrete classes. This allows easier switching of implementation when necessary.

~~~java

public interface FavoriteFileService extends ImageJService {
	
	public List<File> getFavoriteFileList();
	
	public void addFile(File file);
	
	public void deleteFile(File file);
	
}
~~~

Now we may want to communicate with other elements of the software that files has been added or deleted. For this, we can use the Event system provided by SciJava and create specific events.

We must create new classes that inherit from SciJavaEvent;

FavoriteFileAddedEvent.java

~~~java

public class FavoriteFileAddedEvent extends SciJavaEvent {
	File file;
	
	public FavoriteFileAddedEvent(File f) {
		this.file = f;
	}
	
	public File getFile() {
		return file;
	}
}

~~~

FavoriteFileDeletedEvent.java

~~~java

public class FavoriteFileDeletedEvent extends SciJavaEvent {
	File file;
	
	public FavoriteFileDeletedEvent(File f) {
		this.file = f;
	}
	
	public File getFile() {
		return file;
	}
}

~~~


Then let's create an implementation of our service :

~~~java
public class DefaultFavoriteFileService extends AbstractService implements FavoriteFileService {


	private List<File> fileList = new ArrayList<>();
	
	/*
		ImageJ2 Services
	*/
	
	@Parameter
	EventService eventService; // for propagating events
	
	
	public List<File> getFavoriteFileList() {
		return fileList;
	}
	
	public void addFile(File f) {
		
		fileList.add(f);
		
		// publishing the event so that listeners
		// are notified of the new added file
		eventService.publish(new FavoriteFileAddedEvent(f));
		
	}
	
	public void deleteFile(File f) {
	
		fileList.remove(f);
		
		// publishing the event so that listeners
		// are notified of the new added file
		eventService.publish(new FavoriteFileDeletedEvent(f));
		
	}
	
}
~~~

Now if we want to listen for such events in our Widget or in (any other Context injected object), you can just add the following method to your class :

~~~java

ObservableList<File> favoriteFiles;

@EventHandler
public void handleEventXZY(FavoriteFileAddedEvent event) {
	
	// adding the added file to the View Model
	favoriteFiles.add(event.getFile()); 
	
}

~~~


This is a very naive example but it illustrates what can be done with the different part of the API.

#### Note concerning JavaFX Observable API

JavaFX provides an API to listen easily to the model. An alternative to the previous design would be to propose a service that only returns an ObservableList<File>. Since this type of object already deals with event propagation, SciJavaEvent classes would become useless. However, this implementation would enforce the use of JavaFX API and perhaps close the possibility of linking your service with other types of interface.


## Userful ImageJFX Classes

### AbstractContextButton

This class allows you to create easily a button that will appear in a certain context to execute a single action.

~~~java
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.ui.main.Localization;
import org.scijava.plugin.Plugin;
import ijfx.service.widget.Widget;
import javafx.event.ActionEvent;
import net.imagej.plugins.commands.typechange.TypeChanger;
import net.imagej.types.DataTypeService;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;

@Plugin(type = FxWidgetPlugin.class)
@Widget(id = "float-image-button", context = "image-open", localization = Localization.LEFT)
public class FloatTheImage extends AbstractContextButton {

    @Parameter
    CommandService commandService;

    @Parameter
    DataTypeService dataTypeService;

    public FloatTheImage() {
        // defines an icon of the button
        super(FontAwesomeIcon.LEAF);
    }

    @Override
    public void onAction(ActionEvent event) {
		
        final Future future = commandService.run(TypeChanger.class, true,
                // setting command parameters
                "typeName", dataTypeService.getTypeByAttributes(32, true, false, true, true).longName(), "combineChannels", false);
        
        submitFuture(future);

    }    
} // end of class
~~~

