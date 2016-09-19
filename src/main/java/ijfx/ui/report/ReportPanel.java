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
package ijfx.ui.report;

import ijfx.service.log.DefaultLoggingService;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import mongis.utils.CallbackTask;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import mongis.utils.FXUtilities;
import mongis.utils.TaskButtonBinding;
import org.scijava.ui.UIService;

/**
 *
 * @author Cyril MONGIS, 2015
 */

public class ReportPanel extends GridPane {

    @FXML
    Button sendButton;

    @FXML
    Button cancelButton;
    
    @FXML
    TextArea descriptionTextArea;

    @FXML
    TextField senderTextField;

    @Parameter
    DefaultLoggingService logService;

    @Parameter
    UIService uiService;

    static final Logger logger = ImageJFX.getLogger();
    
    Consumer<Boolean> onReportDone;
    
    public ReportPanel() {

        try {
            //ctx.inject(this);
            FXUtilities.injectFXML(this);

            
            new TaskButtonBinding(sendButton)
                    .setTextWhenRunning("Sending...")
                    .setTextWhenSucceed("Sent")
                    .setTaskFactory(this::generateTask);

        } catch (IOException ex) {
            ImageJFX.getLogger();
        }

    }
    
    public ReportPanel(Context context) {
        this();
        context.inject(this);
    }

    public Task generateTask(Object obj) {

        
        return new CallbackTask<Void,Boolean>()
                .run(this::sendReport)
                .then(onReportDone);
                
        
        //return task;

    }
    
    public Boolean sendReport(Object obj){
        try {
            return logService.sendMessageToDeveloper(senderTextField.getText(), descriptionTextArea.getText());
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    

    public ReportPanel setOnReportDone(Consumer<Boolean> onReportDone) {
        this.onReportDone = onReportDone;
        return this;
    }

   @FXML
   public void cancel() {
       this.onReportDone.accept(null);
   }
    
    
    

}
