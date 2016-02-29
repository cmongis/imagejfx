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
package ijfx.core.project;

import ijfx.core.project.query.Modifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import ijfx.core.metadata.GenericMetaData;
import ijfx.core.metadata.MetaData;
import ijfx.core.project.command.Command;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.core.project.modifier.ModifierPlugin;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Cyril MONGIS, 2015
 * 
 * Syntax : query( regexp , metadata_name ) = [ list of extracted metadata ]
 * Query example
 * regexp(/Well=(/\d+)__Position=(\d+)/,"File Name")=["Well","Position"]
 */
public class RegExpModifier implements ModifierPlugin{

    String request;
    
    // Regular expression verifying the syntax
    private final static Pattern syntaxVerifier = Pattern.compile("^regexp\\(/(.*)/,\"(.*)\"\\)=(\\[.*\\])$");
    
    // Json mapper transforming the a list of string into a Java String
    private final static ObjectMapper objectMapper = new ObjectMapper();
    
    // Property indicating if the Modifier parsed the syntax correctly
    private BooleanProperty validSyntaxProperty = new SimpleBooleanProperty(false);
    
    
    // list of metadata that will be added to the list
    private List<String> outputMetadataNameList;
    
    // name of the metadata that where the data will be extracted from
    private String inputMetadataName;
    
    // compiled regular expression generated from the parsing
    private Pattern regexp;
    
    final Logger logger = ImageJFX.getLogger();
    
    public RegExpModifier() {
        
    }
    
    public RegExpModifier(String nonParsedString) {
        configure(nonParsedString);
    }
   

   

    @Override
    public boolean configure(String nonParsedString) {
        this.request = nonParsedString;
        
        Matcher matcher = syntaxVerifier.matcher(nonParsedString);
            
            if(matcher != null && matcher.matches()) {
            try {
                
               
                // first parameter is the regular expression
                String regExp = matcher.group(1);
                
                // the second in the name of the metadata to extract
                inputMetadataName = matcher.group(2);
                
                // the third is the last of metadata to match
                String outputMetadataNamesAsString = matcher.group(3);
                outputMetadataNameList = objectMapper.readValue(outputMetadataNamesAsString,List.class);
                
                
                // checking if it went well
                if(outputMetadataNameList == null)  return false;
                
                
                // checking that the list of input is the same as the list of output
                if(StringUtils.countMatches(regExp, "(") < outputMetadataNameList.size()) return false;
                
                
                
                // compiling the regexp
                regexp = Pattern.compile(regExp);
               
                setValidSyntax(true);
     
            } catch (IOException ex) {
                setValidSyntax(false);
                ImageJFX.getLogger().log(Level.WARNING,null,ex);;
            }
                
                
            }
            
            else {
                setValidSyntax(false);
                
            }
        return validSyntaxProperty.getValue();
    }

    

    
    public ReadOnlyBooleanProperty validSyntaxProperty() {
        return validSyntaxProperty;
    }

   
    public void setValidSyntax(boolean valid) {
        validSyntaxProperty.set(valid);
    }
    
    /*
    public static void main(String... args) {
        
        String input = "";
        String defaultInput = "regexp(/(\\d+)([\\-\\w\\d]+)\\.DIB/,\"File name\")=[\"ID\",\"condition\"]";
      
        
       
    System.out.print("Describe the product: ");
        
        while(input.equals("quit") == false) {
              Modifier modifier = new RegExpModifier();
              Scanner scanner = new Scanner(System.in);
            input = scanner.nextLine();
            
            if(input == null || input.equals("")) {
                input = defaultInput;
            }

            modifier.parse(input);
            
        }
        
    }*/

    
    /*
    @Override
    public ModificationRequest getModificationRequest(PlaneDB plane) {
        
        // creating a modification request
        ModificationRequest modifRequest = new DefaultModificationRequest();
        
        // checking if the syntax is valid or not
        if(!validSyntaxProperty().getValue()) return modifRequest;
    
        //retrieving the targeted metadata
        MetaData metadata = plane.getMetaDataSet().get(inputMetadataName);
        
        // creating a matcher between the targeted metadata value and the regexp
        Matcher matcher = regexp.matcher(metadata.getStringValue());
        
        
        
        // checking if matching
        if(matcher.matches()) {  
            for(int i = 0;i!=outputMetadataNameList.size();i++) {
                String outputMetadataName = outputMetadataNameList.get(i);
                String value = matcher.group(i+1);
                MetaData m = new GenericMetaData(outputMetadataName, value);
                logger.fine("Adding = "+m.toString());
                modifRequest.addAddingMetaData(m);
            }
        }
        else {
            logger.warning("No match found for "+inputMetadataName);
        }
        
        return modifRequest;
    }

    @Override
    public ModificationRequest getModificationRequest() {
        return new DefaultModificationRequest();
    }*/

    @Override
    public Command getModifyingCommand(PlaneDB planeDB) {
        return null;
    }

    @Override
    public boolean wasApplied(PlaneDB planeDB) {
        return true;
    }

    @Override
    public String phraseMe() {
        return "Sorry, cannot be described right now";
    }
    
   
}
