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

After you finish your application and pay the fee, you must upload the PDF file and ZIP file.


## Backup
Copies all files from the SRC directory to the DST directory. Only new or changed files
are copied. The last modified date is used if the file exists in both locations.

    java mgabelmann.photo.workflow.io.Backup <SRC> <DST>

    SRC - source directory
    DST - destination directory

