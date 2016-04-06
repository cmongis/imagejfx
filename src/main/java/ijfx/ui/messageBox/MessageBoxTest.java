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

import java.io.IOException;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import static javafx.application.Application.launch;
import static javafx.application.Application.launch;
import static javafx.application.Application.launch;

/**
 *
 * @author Pierre BONNEAU
 */
public class MessageBoxTest extends Application{
    
    public static void main(String[] args) {
        launch(args);
    }
    
    
    @Override
    public void start(Stage primaryStage) throws IOException{
        
        MBoxTest mbox = new MBoxTest();
        
        Scene scene = new Scene((Parent) mbox);
        
        primaryStage.setScene(scene);
//        primaryStage.resizableProperty().setValue(Boolean.FALSE);
        primaryStage.show();
    }
    
    
    public class MBoxTest extends VBox{
        
        @FXML
        private TextArea textArea;
        
        @FXML
        private RadioButton successBtn;
        
        @FXML
        private RadioButton warningBtn;
        
        @FXML
        private RadioButton dangerBtn;
        
        @FXML
        private Button submitBtn;
        
        @FXML
        private Button clearBtn;
        
        
        private ToggleGroup toggleGroup;
        
        private MessageBox messageBox;
        
        
        public MBoxTest() throws IOException{
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MessageBoxTest.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
            
            ToggleGroup toggleGroup = new ToggleGroup();
            
            
        
            successBtn.setToggleGroup(toggleGroup);
            warningBtn.setToggleGroup(toggleGroup);
            dangerBtn.setToggleGroup(toggleGroup);
            
            messageBox = new DefaultMessageBox();

            submitBtn.onMouseClickedProperty().setValue((e)->{
                
                RadioButton btn = (RadioButton)toggleGroup.getSelectedToggle();                
                String label = btn.getText();

                MessageType newType = MessageType.NULL;
                
                if(label.equals("SUCCESS"))
                    newType = MessageType.SUCCESS;
                else if(label.equals("WARNING"))
                    newType = MessageType.WARNING;
                else if(label.equals("DANGER"))
                    newType = MessageType.DANGER;
                
                messageBox.typeProperty().setValue(newType);
                messageBox.messageProperty().setValue(textArea.getText());
            });
            
            clearBtn.onMouseClickedProperty().setValue((e) -> {
                messageBox.messageProperty().setValue(null);
            });
            
            this.getChildren().add(messageBox.getContent());
        }
    }
}
