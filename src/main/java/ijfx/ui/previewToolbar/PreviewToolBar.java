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
package ijfx.ui.previewToolbar;

import ijfx.service.preview.PreviewService;
import ijfx.service.uicontext.UiContextService;
import ijfx.service.uiplugin.UiPluginService;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiPlugin;
import ijfx.ui.context.PaneContextualView;
import ijfx.ui.main.Localization;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import mongis.utils.panecell.PaneIconCell;
import mongis.utils.panecell.PaneLabelCell;
import org.controlsfx.control.PopOver;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "imagej-top-toolbar", context = "imagej+image-open", order = 13.0, localization = Localization.TOP_TOOLBAR)
public class PreviewToolBar extends BorderPane implements UiPlugin {

    @Parameter
    PreviewService previewService;
    @Parameter
    Context context;

    @Parameter
    UiPluginService loaderService;
    @Parameter
    PluginService pluginService;
    @Parameter
    UiContextService contextService;
    private HBox fakeToolBar;
    private PopOver popOver;
    private JsonReader jsonReader;

    public PreviewToolBar() {
        super();
        fakeToolBar = new HBox();
        this.setTop(fakeToolBar);

    }

    @Override
    public Node getUiElement() {
        return this;
    }

    @Override
    public UiPlugin init() {
//                context.inject(this);

        createToolBar(fakeToolBar);

        return this;
    }

    private void createToolBar(HBox hbox) {
        jsonReader = new JsonReader();
        jsonReader.read("./src/main/resources/ijfx/ui/menutoolbar/toolbarSettings.json");
        jsonReader.separate();
        popOver = new PopOver();
        generateItems(jsonReader, hbox);
        System.out.println("ijfx.examples.context.AlgorithmsToolBar.createToolBar()");
    }

    /**
     *
     * @param pane
     * @param owner
     */
    private void createPopOver(Pane pane, Node owner) {

        if (popOver == null) {
            popOver = new PopOver(pane);
            setPopOver(pane, owner);

        } else if (!popOver.isShowing() || popOver.getOpacity() == 0.0) {
            popOver.setContentNode(pane);
            setPopOver(pane, owner);
        }

    }

    /**
     * Set the PopOver properties
     *
     * @param pane
     * @param owner
     */
    private void setPopOver(Pane pane, Node owner) {
        popOver.setCornerRadius(0);
        popOver.minWidthProperty().bind(this.getScene().widthProperty());
        popOver.setWidth(this.getScene().widthProperty().getValue());
        popOver.maxWidthProperty().bind(this.getScene().widthProperty());
        pane.setMinWidth(popOver.minWidthProperty().getValue());
        pane.setMaxWidth(popOver.minWidthProperty().getValue());
        popOver.setDetached(false);
        popOver.setDetachable(false);
        popOver.setHideOnEscape(true);
        popOver.setAutoFix(true);
        popOver.setAutoHide(true);
        popOver.setOpacity(1.0);
        popOver.setArrowSize(0);
        popOver.show(owner);
        popOver.setAnchorX(this.localToScreen(this.getBoundsInParent()).getMinX() + 1);
        popOver.setAnchorY(this.localToScreen(this.getTop().getBoundsInParent()).getMaxY() - 1);
        popOver.getStyleClass().clear();
        popOver.getStyleClass().add("popoverToolBar");

    }

    /**
     * Set action when user hoovers PaneIconCell
     *
     * @param labelCategory
     */
    public void setMouseAction(LabelCategory labelCategory) {


        labelCategory.addEventFilter(MouseEvent.MOUSE_ENTERED, (ee) -> {

            labelCategory.getContextualView().getPane().getChildren().forEach((e) -> {
                PaneIconCell paneIconCell = (PaneIconCell) e;
                ItemWidget itemWidget = (ItemWidget) paneIconCell.getItem();
                if (itemWidget.getIcon().equals("preview")) {
                    String action = itemWidget.getAction();
                    previewService.setParameters(0, 0, 120, 120);
                    paneIconCell.setImage(previewService.getImageDisplay(action, itemWidget.getParameters()));

                }
                paneIconCell.setSubtitleVisible(false);
            });
            System.out.println(labelCategory.getText());
            if (!labelCategory.getPane().getChildren().isEmpty()) {
                popOver.setOpacity(0);
                createPopOver(labelCategory.getContextualView().getPane(), labelCategory);
            } else if (labelCategory.getPane().getChildren().isEmpty()) {
                popOver.setOpacity(0);
            }
        });

    }

    /**
     * Generate ItemCategory and ItemWidget with FactoryPaneCell
     *
     * @see ijfx.ui.previewToolbar.FactoryPaneCell
     * @param jsonReader
     * @param fakeToolBar
     */
    public void generateItems(JsonReader jsonReader, HBox fakeToolBar) {
        jsonReader.getCategoryList().stream().forEach((e) -> {
            LabelCategory labelCategory = FactoryPaneCell.generateLabel(e, contextService);

            fakeToolBar.getChildren().add(labelCategory);
            setMouseAction(labelCategory);
        });

        jsonReader.getWidgetList().stream().forEach((e) -> {
            PaneIconCell<ItemWidget> paneIconCell = FactoryPaneCell.generate(e, previewService);
            String itemContext = ((ItemWidget) paneIconCell.getItem()).getContext();
            paneIconCell.setId(((ItemWidget) paneIconCell.getItem()).getLabel());

            for (Node node : fakeToolBar.getChildren()) {
                LabelCategory labelCategory = (LabelCategory) node;
                if (itemContext.substring(itemContext.lastIndexOf("+") + 1).equals(labelCategory.getText()))
                {
                    labelCategory.getContextualView().registerNode(paneIconCell, itemContext.substring(0,itemContext.lastIndexOf("+")));

                    System.out.println(labelCategory.getText());
                    System.out.println(itemContext);
                }
            }

            paneIconCell.setOnMouseClicked(event -> {
                System.out.println("Click Action " + paneIconCell.getItem().getLabel() + paneIconCell.getItem().getContext());

            });

        });
    }

}
