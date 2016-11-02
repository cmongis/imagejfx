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
package ijfx.ui.correction;

import ijfx.core.imagedb.ImageLoaderService;
import ijfx.service.IjfxService;
import ijfx.service.ui.LoadingScreenService;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.widgets.FileExplorableWrapper;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import mongis.utils.CallbackTask;
import mongis.utils.ProgressHandler;
import net.imagej.Dataset;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = Service.class)
public class CorrectionUiService extends AbstractService implements IjfxService {

    @Parameter
    ImageLoaderService imageLoaderService;

    @Parameter
    LoadingScreenService loadingScreenService;


    private final ObjectProperty<List<FileExplorableWrapper>> fileListProperty = new SimpleObjectProperty();

    private final ObjectProperty<Dataset> exampleDataset = new SimpleObjectProperty<Dataset>();

    private final ObjectProperty<File> sourceFolder = new SimpleObjectProperty<>();

    private final ObjectProperty<File> destinationDirectory = new SimpleObjectProperty<>();
    
    @Override
    public void initialize() {
        sourceFolder.addListener(this::onSourceFolderChanged);
    }

    private void onSourceFolderChanged(Observable obs, File oldFile, File newFile) {

        new CallbackTask<File, List<FileExplorableWrapper>>()
                .setInput(newFile)
                .run(this::checkFolder)
                .then(this::onFoldedChecked)
                .submit(loadingScreenService)
                .start();
    }

    protected List<FileExplorableWrapper> checkFolder(ProgressHandler handler, File input) {

        handler.setProgress(1, 3);
        handler.setStatus(String.format("Checking folder %s", input.getName()));

        handler.setProgress(2, 3);

        return imageLoaderService
                .getAllImagesFromDirectory(input)
                .stream()
                .map(FileExplorableWrapper::new)
                .map(exp->{
                    getContext().inject(exp);
                    return exp;
                
                })
                .collect(Collectors.toList());

    }

    protected void onFoldedChecked(List<FileExplorableWrapper> list) {
        fileListProperty.setValue(list);
    }

    
    public List<? extends Explorable> getSelectedObjects() {
        return fileListProperty().getValue().stream()
                .filter(exp->exp.selectedProperty().getValue())
                .collect(Collectors.toList());
    }
    
    public List<File> getSelectedFiles() {
       return fileListProperty
               .getValue()
               .stream()
               .filter(f->f.selectedProperty().getValue())
               .map(wrapper->wrapper.getFile())
               .collect(Collectors.toList());
   }
    
    public Property<Dataset> exampleDatasetProperty() {
        return exampleDataset;
    }

    ObjectProperty<File> sourceFolderProperty() {
        return sourceFolder;
    }

    ObjectProperty<List<FileExplorableWrapper>> fileListProperty() {
        return fileListProperty;
    }

    
}
