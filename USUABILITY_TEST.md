# ImageJFX UI Test Checklist

This is a list of the features that should be tested before releasing any new version of the ImageJFX Beta

## Single images

### Opening

- Open an image from the Menu
- Open image from drag and drop
- Open image from button

### Format compatibility
- ND2
- DV file
- Multidimensional TIFF files

### Visualization
- Adjust min/max value for each channel
- Auto contrast
- Changes the min/max values in the LUT Panel on current image change

### ImageJFX Menu
- Content changes depending on the type of image
- Content changes for Tables/MetaDataDisplay/ObjectDisplay
- Check all operations

### Multi dimensional image operations


- Crop
- Resize
- go through dimensions
- Isolate/delete  channels and slices


## Segmentation
- Check SimpleThreshold slider -> expect change of the mask
- Check auto threshold -> expect change of the threshold slider and change of the mask
- 		 













