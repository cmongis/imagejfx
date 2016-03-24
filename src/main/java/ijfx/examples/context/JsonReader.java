/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ijfx.examples.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mongis.utils.panecell.PaneIconCell;

/**
 *
 * @author tuananh
 */
public class JsonReader {

    private ArrayList<WidgetGroup> widgetGroupList;
    private ArrayList<ItemCategory> categoryList;
    private ArrayList<ItemWidget> widgetList;

    public  ArrayList<ItemCategory> getCategoryList(){
        return categoryList;
    }
    public ArrayList<ItemWidget> getWidgetList()
    {
        return widgetList;
    }
    public JsonReader()
    {
        categoryList = new ArrayList<>();
        widgetList = new ArrayList<>();
    }
    public void read() {
 
        ObjectMapper mapper = new ObjectMapper();


        try {
            //JSON from file to Object
            TypeFactory typeFactory = TypeFactory.defaultInstance();

            widgetGroupList = mapper.readValue(new File("./src/main/resources/ijfx/ui/menutoolbar/myJson.json"), typeFactory.constructCollectionType(ArrayList.class, DefaultWidgetGroup.class));
        } catch (IOException ex) {
            Logger.getLogger(JsonReader.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public void separate()
    {
        for (WidgetGroup widgetGroup : widgetGroupList)
        {
            ItemCategory itemCategory = new DefaultCategory(widgetGroup.getName(), widgetGroup.getContext());
            categoryList.add(itemCategory);
            for (ItemWidget itemwidget: widgetGroup.getItems())
            {
                widgetList.add(itemwidget);
                PaneIconCell p = new PaneIconCell();
            }
            
        }
    }
}
