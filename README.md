# Photo Utilities
Useful tools for backing up and performing other Photography related processes.


## Copyright
Handy utility to generate files for US copyright registration for bulk registration
of photographs. Up to 750 photographs can be registered in one application.

This tool generates everything required to register:

1. ZIP file (all images scanned, CSV manifest)
2. PDF manifest file
3. titles file (divided by month and/or maximum character count)

The titles are extracted from the JPG files in the DIR used above. Adobe Photoshop
or Lightroom can be used to add this value. The JPG images in the DIR must be 
smaller (eg: 640 pixels in the longest dimension). 

If a JPG file does not contain a title then the filename is used. While you don't need 
to add titles for each of your images it is recommended.

    java mgabelmann.photo.workflow.io.Backup <DIR> <CASENUMBER> <PUBLISHED> 

    DIR        - directory to scan
    CASENUMBER - US Copyright application number
    PUBLISHED  - p (published) or u (unpublished)

After you finish your application and pay the fee, you must upload the PDF file and ZIP 
file.


## Copyright GUI

Alternatively you can use a Graphical User Interface to construct a set of images and 
edit the titles as needed. The output artifacts are the same.

    java mgabelmann.photo.copyright.gui.CopyrightGUI

* File
  * Exit - exit application

* Edit
  * Add File - select a single JPG image
  * Add Directory - add a single directory and all its contents. recursive addition of files
  * Remove File(s) - if one or more files are selected they can be removed when this is selected

From the main UI you can change the output directory using the 'Browse' button. This is
the location where the files are output. No images are added from this directory.

You can also change the case number and whether the case is published or unpublished.

A contextual menu is available from the table by using a right-click. This
menu has the same options as the Edit menu.

When you are finished select 'Process' to start processing and create output files. All
outputs are placed in the directory at the top of the screen added by using 'Browse' button.


## Backup
Copies all files from the SRC directory to the DST directory. Only new or changed files
are copied. The last modified date is used if the file exists in both locations.

    java mgabelmann.photo.workflow.io.Backup <SRC> <DST>

    SRC - source directory
    DST - destination directory


## New Features / Issues
* need to resize images that are too large to be zipped
* give user feedback in the UI when errors occur
