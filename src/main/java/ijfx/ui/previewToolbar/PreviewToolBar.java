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

import ijfx.service.TimerService;
import ijfx.service.preview.PreviewService;
import ijfx.service.uicontext.UiContextService;
import ijfx.service.uiplugin.UiPluginService;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiPlugin;
import ijfx.ui.context.ContextualWidget;
import ijfx.ui.context.PaneContextualView;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.main.Localization;
import java.util.logging.Logger;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import mongis.utils.panecell.PaneIconCellPreview;
import org.controlsfx.control.PopOver;
import org.reactfx.util.FxTimer;
import org.reactfx.util.Timer;
import org.scijava.Context;
import org.scijava.command.CommandInfo;
import org.scijava.command.CommandService;
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
    CommandService commandService;
    @Parameter
    UiPluginService loaderService;
    @Parameter
    PluginService pluginService;
    @Parameter
    UiContextService contextService;
    
    @Parameter
    TimerService stopWatchService;
    
    
    Logger logger = ImageJFX.getLogger();
    
    private PopOver popOver;
    private JsonReader jsonReader;

    boolean created = false;
    
    public PreviewToolBar() {
        super();

    }

    @Override
    public Node getUiElement() {
        return this;
    }

    @Override
    public UiPlugin init() {
        
        if(created) return this;
        
        System.out.println("Creating toolbar");
        createToolBar();

        return this;
    }

    private void createToolBar() {
        Pane fakeToolBar;
        fakeToolBar = new HBox();
        PaneContextualView paneContextualView = new PaneContextualView(contextService, fakeToolBar, "ToolBar-Context-Dependant");
        fakeToolBar.getStyleClass().removeAll(fakeToolBar.getStyleClass());
        fakeToolBar.getStyleClass().add("imagej-top-toolbar-bar");
        //fakeToolBar.setPadding(new Insets(10, 10,10,10));
        this.setTop(fakeToolBar);
        jsonReader = new JsonReader();
        jsonReader.read("/ijfx/ui/menutoolbar/toolbarSettings.json");
        jsonReader.separate();
        popOver = new PopOver();
        popOver.setMinHeight(200);
        generateItems(jsonReader, paneContextualView);
        created = true;
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
        popOver.setMinHeight(200);
        int anchorX = (int) (this.localToScreen(this.getBoundsInParent()).getMinX() + 1);
        int anchorY = (int) (this.localToScreen(this.getTop().getBoundsInParent()).getMaxY() - 1);
        popOver.show(owner);
        popOver.setAnchorX(anchorX);
        popOver.setAnchorY(anchorY);
        popOver.getStyleClass().clear();
        popOver.getStyleClass().add("popoverToolBar");
        
    }

    /**
     * Set action when user hoovers LabelCategory
     *
     * @param labelCategory
     */
    public void setMouseAction(LabelCategory labelCategory) {
            Timer timer = FxTimer.create(java.time.Duration.ofMillis(250), () -> this.onEnter(labelCategory));
        labelCategory.addEventFilter(MouseEvent.MOUSE_ENTERED, (ee) -> {
            timer.restart();
        });
        
        labelCategory.addEventFilter(MouseEvent.MOUSE_EXITED, (e) -> {
            timer.stop();
        });
    }

    /**
     *
     * @param labelCategory
     */
    public void onEnter(LabelCategory labelCategory) {
        
        logger.info("Activating category : "+labelCategory.getText());
        
        labelCategory.getContextualView().getPane().getChildren().forEach((e) -> {
            PaneIconCellPreview paneIconCellPreview = (PaneIconCellPreview) e;
            
            //paneIconCellPreview
            
            //Has to use forceUpdateImage
            //paneIconCellPreview.updateImageAsync(paneIconCellPreview.getItem());
            paneIconCellPreview.forceImageUpdate();
            paneIconCellPreview.setSubtitleVisible(false);
        });
        if (!labelCategory.getPane().getChildren().isEmpty()) {
            popOver.setOpacity(0);
            createPopOver(labelCategory.getContextualView().getPane(), labelCategory);
        } else if (labelCategory.getPane().getChildren().isEmpty()) {
            popOver.setOpacity(0);
        }
    }

    /**
     * Generate ItemCategory and ItemWidget with DefaultFactoryPaneCell
     *
     * @param paneContextualView
     * @see ijfx.ui.previewToolbar.DefaultFactoryPaneCell
     * @param jsonReader
     */
    public void generateItems(JsonReader jsonReader, PaneContextualView paneContextualView) {
        FactoryPaneCell factoryPaneCell = new DefaultFactoryPaneCell();
        jsonReader.getCategoryList().stream().forEach((e) -> {
            LabelCategory labelCategory = factoryPaneCell.generateLabel(e, contextService);
            labelCategory.setId(labelCategory.getText());
            paneContextualView.registerNode(labelCategory, labelCategory.getContext());
            setMouseAction(labelCategory);
        });

        jsonReader.getWidgetList().stream().forEach((e) -> {
            PaneIconCellPreview<ItemWidget> paneIconCellPreview = factoryPaneCell.generate(e, previewService);
            String itemContext = ((ItemWidget) paneIconCellPreview.getItem()).getContext();
            paneIconCellPreview.setId(((ItemWidget) paneIconCellPreview.getItem()).getLabel());

            for (ContextualWidget<Node> node : paneContextualView.getWidgetList()) {
                LabelCategory labelCategory = (LabelCategory) node.getObject();

                if (itemContext.substring(itemContext.lastIndexOf("+") + 1).equals(labelCategory.getText())) {
                    labelCategory.getContextualView().registerNode(paneIconCellPreview, itemContext.substring(0, itemContext.lastIndexOf("+")));
                }
            }

            paneIconCellPreview.setOnMouseClicked(event -> {
                CommandInfo commandInfo = commandService.getCommand(paneIconCellPreview.getItem().getAction());
                commandService.run(commandInfo, true);
                System.out.println("Click Action " + paneIconCellPreview.getItem().getLabel() + paneIconCellPreview.getItem().getContext());
            });

        });
    }

}
