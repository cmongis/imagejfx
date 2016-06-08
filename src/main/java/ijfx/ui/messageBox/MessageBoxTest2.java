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
package ijfx.ui.messageBox;

import ijfx.ui.utils.BaseTester;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 *
 * @author cyril
 */
public class MessageBoxTest2 extends BaseTester{

    MessageBox messageBox;

    public MessageBoxTest2() {
        super();
        
        addAction("Warning",this::warningMessage);
        addAction("Success",this::successMessage);
        addAction("Empty",this::noMessage);
    }
    
    
    
    @Override
    public void initApp() {
        
        VBox vBox = new VBox();
        
        messageBox = new DefaultMessageBox();
        vBox.getChildren().addAll(messageBox.getContent(), new Label("I'm just there for proof"));
        setContent(vBox);
    }
    
    
    private void warningMessage() {
        messageBox.messageProperty().setValue(new DefaultMessage("Oh damn ! This is a long message that should be well displayed !",MessageType.WARNING));
    }
    
    private void successMessage() {
        messageBox.messageProperty().setValue(new DefaultMessage("It works. Congratulation knowing it was a quite hard work",MessageType.SUCCESS));
    }
    
    private void noMessage() {
        messageBox.messageProperty().setValue(null);
    }
    
    
    public static void main(String... args) {
        launch(args);
    }
}
