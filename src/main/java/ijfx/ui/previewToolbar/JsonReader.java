/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ijfx.ui.previewToolbar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tuan anh TRINH
 */
public class JsonReader {

    private ArrayList<WidgetGroup> widgetGroupList;
    private final ArrayList<ItemCategory> categoryList;
    private final ArrayList<ItemWidget> widgetList;

    public ArrayList<ItemCategory> getCategoryList() {
        return categoryList;
    }

    public ArrayList<ItemWidget> getWidgetList() {
        return widgetList;
    }

    public JsonReader() {
        categoryList = new ArrayList<>();
        widgetList = new ArrayList<>();
    }

    /**
     * Parse Json File and create list of {@link WidgetGroup}.
     * @param path 
     */
    public void read(String path) {

        ObjectMapper mapper = new ObjectMapper();

        try {
            //JSON from file to Object
            TypeFactory typeFactory = TypeFactory.defaultInstance();

            widgetGroupList = mapper.readValue(new File(path), typeFactory.constructCollectionType(ArrayList.class, DefaultWidgetGroup.class));
        } catch (IOException ex) {
            Logger.getLogger(JsonReader.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Create a list of {@link ItemCategory} and a list of {@link ItemWidget}
     * Add name of {@link ItemCategory} to the context to link itemWiget with itemCategory
     */
    public void separate() {
        widgetGroupList.stream().forEach((widgetGroup) -> {
            ItemCategory itemCategory = new DefaultCategory(widgetGroup.getName(), widgetGroup.getContext(), widgetGroup.getIcon());
            categoryList.add(itemCategory);
            for (ItemWidget itemwidget : widgetGroup.getItems()) {
                itemwidget.addContext("+" + itemCategory.getName());
                itemwidget.removeSpaceContext();
                widgetList.add(itemwidget);
            }
        });
    }
}
