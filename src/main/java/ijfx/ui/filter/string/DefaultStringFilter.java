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
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
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
    private final ObservableList<Item> allItems = FXCollections.observableArrayList();
    private final ObservableList<Item> displayedItems = FXCollections.observableArrayList();
    //private boolean bigger = false;

    private String CSS_FILE = getClass().getResource("DefaultStringFilter.css").toExternalForm();

    // no static method !!! this is a reusable widget !
    private Property<Predicate<String>> predicate = new SimpleObjectProperty();

    private final BooleanBinding lotOfItems = Bindings.createBooleanBinding(() -> allItems.size() > 5, allItems);
    private final BooleanProperty showAll = new SimpleBooleanProperty(false);

    private final DoubleBinding prefHeight = Bindings.createDoubleBinding(this::calculateHeight, lotOfItems);
    
    public DefaultStringFilter() {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ijfx/ui/filter/string/DefaultStringFilter.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            
            // loading the FXML
            loader.load();
            
            // setting the visible property of the button so it appears only
            // only when there is a lot of items and the button option showall is not true
            moreButton.disableProperty().bind(lotOfItems.not().and(showAll.not()));

            // adding the CSS stylesheet
            getStylesheets().add(CSS_FILE);

            // change the factory to the class method
            listView.setCellFactory(this::createListCell);
            listView.setItems(displayedItems);
            
            // showing the textfield only when there is a lot of items
            textField.disableProperty().bind(lotOfItems.not());
            listView.prefHeightProperty().bind(prefHeight);

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

        allItems.clear();

        Map<String, Integer> itemCount;
        itemCount = new HashMap<>();
        list.stream().forEach((s) -> {
            if (itemCount.get(s) == null) {
                itemCount.put(s, 1);
            } else {
                itemCount.put(s, itemCount.get(s) + 1);
            }
        });
        List<Item> items = new ArrayList<>();
        itemCount.forEach((s, i) -> items.add(new ItemWrapper(s, i)));

        allItems.clear();
        allItems.addAll(items);
        updateDisplayedItems();
        predicateProperty().setValue(null);

    }

    public void updateDisplayedItems() {
        String s = textField.getText();
        List<Item> itemToShow;

        // if there is a query
        // we filter
        if (s != null && s.trim().equals("") == false) {
            itemToShow = allItems.filtered(item -> item.getName().contains(s));
        } // if everything should be shown or if there is not a lot of item, we show everything
        else if (showAll.getValue() || lotOfItems.getValue() == false) {
            itemToShow = allItems;
        } // otherwise is means only a small set of items should be shown
        else {
            itemToShow = allItems.subList(0, 5);
        }

        // clearing the displayed items
        displayedItems.clear();

        displayedItems.addAll(itemToShow);
        listView.setItems(displayedItems);

    }

    public void setBigger() {
        showAll.setValue(true);
    }

    public void setPredicate() {
        List<String> listBuffer = new ArrayList<>();

        System.out.println(displayedItems.stream().filter(e -> e.getState()).count());
        if (displayedItems.stream().filter(e -> e.getState()).count() == 0) {

            predicate.setValue(null);
            return;
        }

        listBuffer = displayedItems
                .stream()
                .filter(e -> e.getState())
                .map(e -> e.getName())
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
            //System.out.println(String.format("Testing %s against %d terms",t,strings.size()));
            return strings.contains(t);
        }

    }

    
    private Double calculateHeight() {
        if(lotOfItems.getValue()) return 200d;
        else return 40d * allItems.size();
    }
}
