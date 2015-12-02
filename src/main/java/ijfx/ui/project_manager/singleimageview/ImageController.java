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
package ijfx.ui.project_manager.singleimageview;

/**
 *
 * @author Cyril Quinton
 */
public abstract class ImageController {
    /*
     protected final ProjectManager projectManager;
     protected PlaneDB image;
     protected SimpleStringProperty nameProperty = new SimpleStringProperty();
     protected SimpleBooleanProperty selectedProperty = new SimpleBooleanProperty();

     public ImageController(PlaneDB image,ProjectManager projectManager) {
     this.image = image;
     this.projectManager = projectManager;
     nameProperty.set(image.getName());
     selectedProperty.set(image.isSelected());
     }

     public void updateName() {
     nameProperty.set(image.getName());
     }

     ;
     public void updateMetaData() {
     }

     ;
     public void updateTags() {
     }

     ;
     public void updateSelected() {
     selectedProperty.set(image.isSelected());
     }

     @Override
     public void update(String identifier) {
     switch (identifier) {
     case PlaneDB.FILE_NAME_STRING:
     updateName();
     break;
     case PlaneDB.METADATASET_STRING:
     updateMetaData();
     break;
     case PlaneDB.TAG_STRING:
     updateTags();
     break;
     case PlaneDB.SELECTED_STRING:
     updateSelected();
     break;
     case PlaneDB.DESTRUCTED_STRING:
     listenerService.stopListening();
     }
     }

     public void removeTag(String tag) {
     projectManager.getCurrentProject().removeTagUndoable(image, tag);
     }

     public void modifyTag(String oldValue, String newValue) {
     projectManager.getCurrentProject().replaceTagUndoable(image, oldValue, newValue);
     }
     */
}
