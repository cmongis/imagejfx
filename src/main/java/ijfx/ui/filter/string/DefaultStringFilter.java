/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ijfx.ui.filter.string;


import ijfx.ui.filter.StringFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

/**
 * FXML Controller class
 *
 * @author tuananh
 */
public class DefaultStringFilter extends BorderPane implements Initializable, StringFilter {

    /**
     * Initializes the controller class.
     */
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TextField textField;
    @FXML
    private Button moreButton;
    @FXML
    private ListView<Item> listView;
    private static List<Item> listValues;
    private ObservableList<Item> observableList;
    private boolean bigger = false;
    
    private String CSS_FILE = getClass().getResource("DefaultStringFilter.css").toExternalForm();
    
    // no static method !!! this is a reusable widget !
    private Property<Predicate<String>> predicate = new SimpleObjectProperty();
    
    
    public DefaultStringFilter() throws IOException {
        listValues = new ArrayList<>();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ijfx/ui/filter/string/DefaultStringFilter.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        loader.load();
        try {
            observableList = FXCollections.observableArrayList(listValues.subList(0, 5));
            getStylesheets().add(CSS_FILE);
        } catch (Exception e) {
        } finally {
            if (observableList == null) {
                observableList = FXCollections.observableArrayList(listValues);
            }
        }
        // change the factory to the class method
        listView.setCellFactory(this::createListCell);
        listView.setItems(observableList);

    }
    
    public ListCell<Item> createListCell(ListView<Item> listView) {
        
        // I give a pointer to a runnable that will be executed each time
        // the something is ticked.
        // It makes the code more reuable
        ListCellCheckbox listCellCheckBox = new ListCellCheckbox(this::setPredicate);
        
        return listCellCheckBox;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        textField.onKeyPressedProperty();
        //Generate Array
        List<String> t = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            t.add(generateString(new Random(), "az", 5));
        }
        setAllPossibleValues(t);
        this.setPrefSize(listView.getPrefWidth(), listView.getPrefHeight() + 100);

    }

    public static String generateString(Random rng, String characters, int length) {
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }

    @Override
    public void setAllPossibleValues(Collection<String> list) {
        Map<String, Integer> items;
        items = new HashMap<>();
        list.stream().forEach((s) -> {
            if (items.get(s) == null) {
                items.put(s, 1);
            } else {
                items.put(s, items.get(s) + 1);
            }
        });
        items.forEach((s, i) -> listValues.add(new ItemWrapper(s, i)));

    }

    public void filterQuery() {
        String s = textField.getText();
        List<Item> listValuesTmp = new ArrayList<>();
        if (bigger) {
            listValues.stream().filter(e -> e.getName().contains(s)).forEach(e -> listValuesTmp.add(e));
        } else {
            try {
                listValues.subList(0, 5).stream().filter(e -> e.getName().contains(s)).forEach(e -> listValuesTmp.add(e));

            } catch (Exception e) {
            } finally {
                if (observableList == null) {
                    observableList = FXCollections.observableArrayList(listValues);

                }
            }

        }
        observableList = FXCollections.observableArrayList(listValuesTmp);
        listView.setItems(observableList);

    }

    public void setBigger() {
        if (!bigger) {
            bigger = true;
            filterQuery();
            moreButton.setVisible(false);
        }
        
    }

    public void setPredicate() {
        List<String> listBuffer = new ArrayList<>();
        listValues.stream().filter(e -> e.getState()).forEach(e -> listBuffer.add(e.getName()));
        predicate.setValue(p -> listBuffer.contains(p));
        getStylesheets().remove(CSS_FILE);
        getStylesheets().add(CSS_FILE);
      
    }
    
    public Property<Predicate<String>> predicateProperty() {
        return predicate;
    }

    @Override
    public StringProperty keyWordProperty() {
        return textField.textProperty();
    }

   
    

}
