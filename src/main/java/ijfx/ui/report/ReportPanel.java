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

import ijfx.service.log.LogService;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import mongis.utils.FXUtilities;
import mongis.utils.TaskButtonBinding;

/**
 *
 * @author Cyril MONGIS, 2015
 */

public class ReportPanel extends GridPane {

    @FXML
    Button sendButton;

    @FXML
    TextArea descriptionTextArea;

    @FXML
    TextField senderTextField;

    @Parameter
    LogService logService;


    static final Logger logger = ImageJFX.getLogger();
    
    Callback<Boolean,Void> onReportDone;
    
    public ReportPanel() {

        try {
            //ctx.inject(this);
            FXUtilities.injectFXML(this);

            
            new TaskButtonBinding(sendButton)
                    .setTextWhenRunning("Sending...")
                    .setTextWhenSucceed("Sent")
                    .setWhenSucceed(() -> {
                        onReportDone.call(true);
                    })
                    .runTaskOnClick(this::generateTask);

        } catch (IOException ex) {
            ImageJFX.getLogger();
        }

    }
    
    public ReportPanel(Context context) {
        this();
        context.inject(this);
    }

    public Task generateTask(Object obj) {

        Task<Boolean> task;
        task = new Task<Boolean>() {
            @Override
            protected Boolean call()  {
               
                try {
                    return logService.sendMessageToDeveloper(senderTextField.getText(), descriptionTextArea.getText());
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    return Boolean.FALSE;
                }
               
                
            }
            ;
        };
        
        return task;

    }

    public Callback<Boolean, Void> getOnReportDone() {
        return onReportDone;
    }

    public ReportPanel setOnReportDone(Callback<Boolean, Void> onReportDone) {
        this.onReportDone = onReportDone;
        return this;
    }

  
    
    
    

}
