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

import ijfx.ui.main.ImageJFX;
import javafx.event.Event;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.StageStyle;
import org.scijava.Context;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class ReportDialog extends Dialog<Boolean>{
    
    DialogPane pane;
    
    public ReportDialog(Context context) {
        super();
        
        initStyle(StageStyle.DECORATED);
        
        pane = new DialogPane();
       
        
        setDialogPane(pane);
        pane.getStylesheets().add(ImageJFX.STYLESHEET_ADDR);
        pane.setContent(new ReportPanel(context).setOnReportDone(this::onSendingDone));
       
       setResultConverter(this::onResultFired);
        
    }
    
    
    
    public Void onSendingDone(Boolean result) {
          
            if(result == null) {
                setResult(false);
                return null;
            }
            
        
            if(result) {
                new Alert(Alert.AlertType.CONFIRMATION, "Report sent ! Thank you for your contribution.", ButtonType.CLOSE).show();
            }
            else {
                new Alert(Alert.AlertType.ERROR, "Error when sending report. Perhaps our server is down\nor you are not connected to the internet.", ButtonType.CLOSE).show();
            }
            setResult(true);
            

            return null;
    }
    
    
    public Boolean onResultFired(ButtonType buttonType) {
        
        if(buttonType == ButtonType.OK) {
            return true;
        }
        else {
            return false;
        }
    } 
    
}
