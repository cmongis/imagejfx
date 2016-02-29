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

import ijfx.core.project.query.QueryService;
import ijfx.core.hash.HashService;
import ijfx.core.project.command.AddPlaneCommand;
import ijfx.core.project.command.Command;
import ijfx.core.project.command.Invoker;
import ijfx.core.project.imageDBService.ImageReferenceImpl;
import ijfx.core.project.imageDBService.PlaneDB;
import static ijfx.core.project.imageDBService.PlaneDB.METADATASET_STRING;
import ijfx.core.project.imageDBService.PlaneDBInMemory;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import ijfx.core.metadata.extraction.ImagePlane;
import ijfx.core.metadata.extraction.MetaDataExtractorService;
import ijfx.core.metadata.extraction.PlaneList;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril Quinton
 */
public class ImportImageFileTask extends Task<Command> {

    public static int MESSAGE_SLEEP_TIME = 200;
    public static int WARNING_SLEEP_TIME = 2000;
    private final List<File> imageFiles;
    private final Project project;
    private final ResourceBundle rb;
    
    @Parameter
    private HashService hashGenerator;
    
    @Parameter
    private ImageLoaderService imageLoaderService;
    
    @Parameter
    private QueryService queryService;
    
    @Parameter
    private MetaDataExtractorService extractor;
    
    
    private int nbFile;
    private int nbLoaded;

    Logger logger;
    
    
    
    public ImportImageFileTask(List<File> imageFiles, Project project, Context context) {
        this.imageFiles = imageFiles;
        this.project = project;
        nbFile = imageFiles.size();
        nbLoaded = 0;
        rb = ProjectManagerService.rb;
        
        context.inject(this);
        
        /*
        extractor = context.getService(BioFormatExtractor.class);
        hashGenerator = context.getService(HashService.class);
        imageLoaderService = context.getService(DefaultImageLoaderService.class);
        queryService = context.getService(DefaultQueryService.class);
        */

    }

    
    
    
    @Override
    protected void succeeded() {
        super.succeeded();
        if(this.getValue() != null) {
             project.getInvoker().executeCommand(this.getValue());
        }
        
    }
    
    @Override
    protected Command call() throws Exception {
        
        List<PlaneDB> totalPlaneList = loadImageFile();
        int nbExtractedPlanes = totalPlaneList.size();
        String planeWord = nbExtractedPlanes > 1 ? rb.getString("planes") : rb.getString("plane");
        String message = nbExtractedPlanes + " " + planeWord + " " + rb.getString("extracted");
        updateMessage(message);
        if (nbExtractedPlanes == 0) {
            return null;
        }
        Command addPlaneCmd = new AddPlaneCommand(project, totalPlaneList,true);
        //ResourceBundle rb = ProjectManagerService.rb;
        String cmdName = imageFiles.size() > 1 ? rb.getString("addImagesFromFile")
                : rb.getString("addImageFromFile");
        addPlaneCmd.setName(cmdName);
        List<Command> annotationCmd = getAnnotationCmd(totalPlaneList);

        annotationCmd.add(0, addPlaneCmd);

        return Invoker.getWrappedCommand(annotationCmd, cmdName);
    }

    private List<PlaneDB> loadImageFile() {
        List<PlaneDB> totalPlaneList = new ArrayList<>();
        int total = totalPlaneList.size();
        int count = 0;
        for (File imageFile : imageFiles) {
            if (isCancelled()) {
                break;
            }
            
            count++;
            
            
            try {
                
                String message = rb.getString("importing") + " : " + imageFile.getName();
                String speMessage = message + " : " + rb.getString("generatingID");
                updateMessage(speMessage);
                updateProgress(nbLoaded, nbFile);
                String id = calculateHash(imageFile);

                if (fileAlreadyInDB(id)) {
                    warnFileNotLoaded(imageFile, rb.getString("fileAlreadyInProject"));
                    continue;
                } else {
                    speMessage = message + " : " + rb.getString("extractingPlanes");
                    updateMessage(speMessage);
                    ijfx.core.project.imageDBService.ImageReference imageReference
                            = new ImageReferenceImpl(id, imageFile.getAbsolutePath());
                    try {
                        PlaneList planeList = getPlaneList(imageFile);
                        if (planeList.isEmpty()) {
                            warnFileNotLoaded(imageFile, rb.getString("badFormat"));
                            continue;
                        } else {
                            for (ImagePlane plane : planeList) {
                                PlaneDB planeDB = new PlaneDBInMemory();
                                planeDB.setImageReference(imageReference);
                                planeDB.addMetaDataSet(plane.getMetaDataSet(), METADATASET_STRING);

                                planeDB.resetModifiedMetaDataSet();

                                long planeIndex = plane.getPlaneIndex();
                                planeDB.setPlaneIndex(planeIndex);
                                totalPlaneList.add(planeDB);

                            }
                        }
                    } catch (Exception ex) {
                        
                        warnFileNotLoaded(imageFile, ex.getMessage());
                        logger().log(Level.WARNING,String.format("Could'nt load file '%s'.",imageFile.getAbsolutePath()),ex);
                        
                    }

                    nbLoaded++;

                }
            } catch (IOException ex) {
                warnFileNotLoaded(imageFile, ex.getMessage());
            }
        }
        
      
        return totalPlaneList;
    }

    private boolean fileAlreadyInDB(String id) {
        return imageLoaderService.fileAlreadyInDB(id, project);
    }

    private String calculateHash(File imageFile) throws IOException {
        return hashGenerator.getHash(imageFile);
    }

    private PlaneList getPlaneList(File imageFile) {
        return extractor.extract(imageFile);
    }

    private void warnFileNotLoaded(File file, String message) {
        ImageJFX.getLogger().warning("File no imported for some reason : "+file.getAbsolutePath());
        String warning = rb.getString("fileNotImported") + " : " + file.getName()
                + ".\t" + rb.getString("reason") + " : " + message;
        updateMessage(warning);
        //sleep(MESSAGE_SLEEP_TIME);
        nbFile--;
    }

    private List<Command> getAnnotationCmd(List<PlaneDB> totalPlaneList) {
        updateMessage(rb.getString("preparingAutomaticAnnotation"));
        List<Command> cmds = queryService.applyAnnotationRulesCommand(project, totalPlaneList);
        int nbPlaneToBeModified = queryService.getNbOfAnnotatedPlane();
        updateMessage(nbPlaneToBeModified + " " + rb.getString("planesWillBeAnnotated"));
        return cmds;
    }

    @Override
    protected void updateMessage(String message) {
        super.updateMessage(message);
        //ImageJFX.getLogger();
       // sleep(MESSAGE_SLEEP_TIME);
    }

    
    
    public Logger logger() {
        if(logger == null) {
            logger = ImageJFX.getLogger();
        }
        return logger();
    }

}
