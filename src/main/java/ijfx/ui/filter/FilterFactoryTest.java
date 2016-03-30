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
package ijfx.ui.filter;

import ijfx.core.metadata.GenericMetaData;
import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataOwner;
import ijfx.core.metadata.MetaDataSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.math3.random.RandomDataGenerator;

/**
 *
 * @author Pierre BONNEAU
 */
public class FilterFactoryTest extends Application{
    
    public static void main(String[] args) {
        launch(args);
    }
    
    
    @Override
    public void start(Stage primaryStage) throws Exception{

        Collection<MetaDataOwner> ownerList = new ArrayList<>();
        String[] keyName= {"Name", "Date", "Amount"};
        
        for (int i = 0; i != 200; i++) {
            ownerList.add(new OwnerTest());
        }
        
        MetaDataFilterFactory factory = new DefaultMetaDataFilterFactory();
        
        RandomDataGenerator generator = new RandomDataGenerator();
        int index = generator.nextInt(0, 2);
        
        MetaDataOwnerFilter filter = factory.generateFilter(ownerList, keyName[1]);
        
        Scene scene = new Scene((Parent)filter.getContent());
        primaryStage.setScene(scene);
        
        primaryStage.show();
    }
    
    
    public final class OwnerTest implements MetaDataOwner{
    
        private MetaDataSet metaDataSet;
        
        public OwnerTest(){
            this.metaDataSet = this.generateRandomMetaDataSet();
        }
    
        
        @Override
        public MetaDataSet getMetaDataSet(){
            return this.metaDataSet;
        }
        
        
        public void setMetaDataSet(MetaDataSet metaDataSet){
            this.metaDataSet = metaDataSet;
        }
        
        
        public MetaDataSet generateRandomMetaDataSet(){
            
            String[] keys = {"Name", "Date", "Amount"};
            
            MetaData name = new GenericMetaData(keys[0], generateRandomName());
            MetaData date = new GenericMetaData(keys[1], generateDate());
            MetaData amount = new GenericMetaData(keys[2], generateRandomNumber());
            
            MetaDataSet metaDataSet = new MetaDataSet();
            metaDataSet.put(name);
            metaDataSet.put(date);
            metaDataSet.put(amount);
            
            return metaDataSet;
        }
        
        
        public String generateRandomName(){
            
            RandomDataGenerator generator = new RandomDataGenerator();
            
            int length = generator.nextInt(3, 20);
            String characters = "abcdefghijklmnopqrstuvwxyz";
            
            Random rng = new Random();
            
            char[] name = new char[length];
            
            for (int i = 0; i < length; i++){
                name[i] = characters.charAt(rng.nextInt(characters.length()));
            }
            
            return new String(name);
        }
        
        
        public String generateDate(){
            LocalDateTime date = LocalDateTime.now();
            return date.toString();
        }
        
        
        public Number generateRandomNumber(){
            RandomDataGenerator generator = new RandomDataGenerator();
            double number = generator.nextUniform(10, 1000);
            return number;
        }
    }
}