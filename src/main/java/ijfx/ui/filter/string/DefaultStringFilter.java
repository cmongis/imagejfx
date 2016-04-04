/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ijfx.ui.filter.string;

import ijfx.ui.filter.StringFilter;
import ijfx.ui.filter.StringFilterWrapper;
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
import java.util.stream.Collectors;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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

    public DefaultStringFilter() {
        listValues = new ArrayList<>();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ijfx/ui/filter/string/DefaultStringFilter.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {

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
        } catch (IOException e) {
            e.printStackTrace();
        }
//        predicateProperty().addListener();
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

       
        this.setPrefSize(listView.getPrefWidth(), 200);

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
        
        listValues.clear();
        
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

        observableList.clear();
        observableList.addAll(listValues);
        
        predicateProperty().setValue(null);
        
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
        
        
        System.out.println(observableList.stream().filter(e->e.getState()).count());
        if(observableList.stream().filter(e->e.getState()).count() == 0) {
            
            predicate.setValue(null);
            return;
        }
        
        
        listBuffer = observableList
                .stream()
                .filter(e -> e.getState())
                .map(e->e.getName())
                .collect(Collectors.toList());
        predicate.setValue(new ContainStringPredicate(listBuffer));
        //getStylesheets().remove(CSS_FILE);
        //getStylesheets().add(CSS_FILE);
    }

    @Override
    public Property<Predicate<String>> predicateProperty() {
        return predicate;
    }

    @Override
    public StringProperty keyWordProperty() {
        return textField.textProperty();
    }

    @Override
    public Node getContent() {
        return this;
    }
    
    private class ContainStringPredicate implements Predicate<String> {
        
       private final List<String> strings;

        public ContainStringPredicate(List<String> strings) {
            this.strings = strings;
        }

        @Override
        public boolean test(String t) {
            System.out.println(String.format("Testing %s against %d terms",t,strings.size()));
            return strings.contains(t);
        }
       
       
        
    }
    
}
