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
package ijfx.ui.widgets;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.service.ui.RichTextDialog;
import ijfx.ui.RichMessageDisplayer;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import mongis.utils.FXUtilities;

/**
 *
 * @author cyril
 */
public class FXRichTextDialog extends Dialog<RichTextDialog.Answer> implements RichTextDialog {

    private BorderPane borderPane = new BorderPane();

    @FXML
    private Label titleLabel;


    private WebView webView;

    private RichMessageDisplayer displayer = new RichMessageDisplayer();

    boolean wereButtonsAdded = false;
    
    List<AnswerType> buttonTypes = new ArrayList<>();
    
    public FXRichTextDialog() throws IOException {

        FXMLLoader loader = new FXMLLoader();

        loader.setLocation(this.getClass().getResource("/ijfx/ui/widgets/FXRichTextDialog.fxml"));
        loader.setController(this);
        loader.setRoot(borderPane);

        loader.load();
        getDialogPane().getStylesheets().add(ImageJFX.STYLESHEET_ADDR);
        
        this.getDialogPane().setContent(borderPane);
          getDialogPane().setPadding(new Insets(15));
        FXUtilities.createWebView().then(this::onWebViewCreated);
        
        this.setResultConverter(this::convertAnswer);
        
    }

    private void onWebViewCreated(WebView view) {
        webView = view;
         
        displayer.addCss("ul li { list-style-type:round;text-indent:0px;margin-left:30px;margin-top:7px;margin-bottom:7px;}; h4 { margin-top:10px; } body {font-size:14px;}");
        displayer.setWebView(view);
        
        borderPane.setCenter(view);
     

    }
    

    @Override
    public RichTextDialog setDialogTitle(String title) {

        this.titleLabel.setText(title);

        return this;
    }

    @Override
    public RichTextDialog setDialogContent(String context) {
       displayer.setMessage(context);
       return this;
    }

    @Override
    public RichTextDialog setContentType(ContentType contentType) {

        switch (contentType) {
            case MARKDOWN:
                displayer.addStringProcessor(com.github.rjeschke.txtmark.Processor::process);
        }

        return this;
    }

    @Override
    public RichTextDialog loadContent(Class<?> clazz,String path) {
        try {
            displayer.setContent(clazz, path);
        } catch (IOException ex) {
            displayer.setMessage("Couldn't load content");
            Logger.getLogger(FXRichTextDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        return this;
    }

    @Override
    public RichTextDialog addAnswerButton(AnswerType buttonType, String text) {

        switch (buttonType) {
            case VALIDATE:
                ButtonType type = new ButtonType(text, ButtonData.OK_DONE);
                
                this.getDialogPane().getButtonTypes().add(type);
                
                break;
            case CANCEL:
                this.getDialogPane().getButtonTypes().add(new javafx.scene.control.ButtonType(text, ButtonBar.ButtonData.CANCEL_CLOSE));
        }
        buttonTypes.add(buttonType);
        wereButtonsAdded = true;
        
        return this;

    }

    
    
    
    @Override
    public Answer showDialog() {
        
        if(!wereButtonsAdded) {
            addAnswerButton(AnswerType.VALIDATE, "OK");
        }
        System.out.println("Styling buttons !");
            
        ButtonBar bar = (ButtonBar) getDialogPane().lookup(".button-bar");
        
        for(int i = 0; i != bar.getButtons().size(); i++) {
            
            
            Button button = (Button)bar.getButtons().get(i);
            
            AnswerType answerType = buttonTypes.get(i);
            
            styleButton(button,answerType);
            
        }
        
        return showAndWait().orElse(new Answer(AnswerType.CANCEL,"NULL"));
    }

    
    private Answer convertAnswer(ButtonType type) {
        return new Answer(type.getButtonData() == ButtonData.OK_DONE ? AnswerType.VALIDATE : AnswerType.CANCEL,type.getText());
    }
    
    private void styleButton(Button button,AnswerType answerType) {
        if(answerType == AnswerType.VALIDATE) {
            button.getStyleClass().add("success");
            button.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.CHECK));
        }
        else {
            button.getStyleClass().add("danger");
            button.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.REMOVE));
        }
        
    }
}
