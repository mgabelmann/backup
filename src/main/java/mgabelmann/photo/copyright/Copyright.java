package mgabelmann.photo.copyright;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import mgabelmann.photo.workflow.exception.WorkflowException;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.GenericImageMetadata;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegPhotoshopMetadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 *
 *
 * @author mgabelmann
 */
public class Copyright {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Copyright.class);

    /** Maximum number of characters for a titles group before breaking into a new group. */
    public static final int TITLES_GROUP_MAX_CHARACTERS = 1950;

    public static final int IMAGES_PER_GROUP_REGISTRATION_MAX = 750;

    private static final int FONT_SIZE_BODY = 8;
    private static final int FONT_SIZE_HEADER = 10;

    /** Directory to process. */
    private final File directory;

    /** Copyright case number. */
    private final String caseNumber;

    /** Is group published or unpublished. */
    private boolean published;

    /** Service for scanning files. */
    private final ExecutorService service;

    /** Results of scanning files. */
    private List<FileInfo> fileInfos;


    /**
     * Constructor.
     * @param directory directory
     * @param caseNumber case number
     * @param published published or unpublished
     */
    public Copyright(
            final File directory,
            final String caseNumber,
            final boolean published) {

        if (directory == null) {
            throw new IllegalArgumentException("directory is required");

        } else if (!directory.isDirectory()) {
            throw new IllegalArgumentException("directory is not a directory");

        } else if (caseNumber == null || caseNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("case number is required");
        }

        this.directory = directory;
        this.caseNumber = caseNumber;
        this.published = published;
        this.fileInfos = new ArrayList<>();
        this.service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    /**
     * Entry point for using this utility.
     * @param args arguments
     */
    public static void main(final String[] args) {
        /*
        if (args.length != 3) {
            System.err.println("invalid number of arguments");
            System.exit(1);
        }

        File directory = new File(args[0]);
        String caseNumber = args[1];
        boolean published = args[2].equalsIgnoreCase("p");
        */

        File directory = new File("P:\\Mike\\catalog1\\05_output\\copyright\\to_submit\\test");
        String caseNumber = "casenumber";
        boolean published = true;

        Copyright copyright = new Copyright(directory, caseNumber, published);

        try {
            copyright.process();

        } catch (WorkflowException we) {
            LOGGER.error(we.getMessage());
        }
    }

    /**
     * Call this to do work.
     * @throws WorkflowException error
     */
    public void process() throws WorkflowException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("copyright - processing directory {}, for case number {}", directory, caseNumber);
        }

        try {
            this.processDirectory(this.directory);

            service.shutdown();

            boolean timeout = service.awaitTermination(15, TimeUnit.SECONDS);

            if (fileInfos.size() > IMAGES_PER_GROUP_REGISTRATION_MAX) {
                throw new WorkflowException("Too many images, max image count for bulk registration is: " + IMAGES_PER_GROUP_REGISTRATION_MAX);
            } else {
                LOGGER.info("found {} images to process", fileInfos.size());
            }

            //group photos by date, so we can sort by date
            Map<LocalDate, List<FileInfo>> dateRecords = new TreeMap<>();
            Map<String, List<FileInfo>> titleRecords = new TreeMap<>();

            for (FileInfo fileInfo : fileInfos) {
                LocalDate date = published ? fileInfo.getDate() : LocalDate.now();

                if (dateRecords.containsKey(date)) {
                    dateRecords.get(date).add(fileInfo);

                } else {
                    ArrayList<FileInfo> infos = new ArrayList<>();
                    infos.add(fileInfo);
                    dateRecords.put(date, infos);
                }

                String dateStr = DateTimeFormatter.ofPattern("yyyy-MM").format(date);

                if (titleRecords.containsKey(dateStr)) {
                    titleRecords.get(dateStr).add(fileInfo);

                } else {
                    ArrayList<FileInfo> infos = new ArrayList<>();
                    infos.add(fileInfo);
                    titleRecords.put(dateStr, infos);
                }
            }

            //generate manifest file
            StringBuilder sb1 = this.getManifest(dateRecords);
            String manifestFilename = this.getManifestFilename();

            this.writeTextFile(sb1.toString(), manifestFilename);

            //generate titles file
            List<String> titles = this.getAllTitles(titleRecords);
            String titlesStr = titles.stream().collect(Collectors.joining(System.lineSeparator() + System.lineSeparator()));
            String titlesPath = this.getTitlesFilename();

            this.writeTextFile(titlesStr, titlesPath);

            //TODO: do we want to make a PDF of the manifest file?
            this.writePDF(dateRecords);

            //create ZIP file of ALL files processed/created except titles
            this.writeZip(dateRecords, manifestFilename);

            if (!timeout) {
                LOGGER.warn("service timed out");
            }

        } catch (IOException | InterruptedException e) {
            throw new WorkflowException(e);
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("copyright - finished");
        }
    }

    /**
     * Process a single directory, it will recurse into subdirectories by default. Files are added to list if they
     * meet criteria.
     * @param dir directory
     * @throws WorkflowException error
     */
    public void processDirectory(final File dir) throws WorkflowException {
        final File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    this.processDirectory(file);

                } else if (file.getName().toLowerCase().matches(".*\\.jpe?g")) {
                    this.processFile(file);

                } else {
                    LOGGER.debug("skipping file {}, is not a JPG", file.getName());
                }
            }
        }
    }

    /**
     * Process a single file.
     * @param file file to process
     * @throws WorkflowException error
     */
    public void processFile(final File file) throws WorkflowException {
        try {
            final String fileName = this.getFilename(file);
            final LocalDateTime dateTime = this.getFileDateTime(file, false);
            final String title = this.getTitle(file);

            fileInfos.add(new FileInfo(fileName, title, dateTime));
            LOGGER.debug("added: {}", file.getName());

        } catch (IOException ie) {
            throw new WorkflowException(ie);
        }
    }

    /**
     * Get manifest file name.
     * @return file name
     */
    String getManifestFilename() {
        final String filePrefix = published ? "p" : "u";
        final String fileBase = directory.getName();

        return directory.getAbsolutePath() + File.separator + (filePrefix + fileBase + "_" + caseNumber + ".csv").toLowerCase();
    }

    /**
     * Get PDF file name.
     * @return file name
     */
    String getPDFFilename() {
        final String fileBase = directory.getName();
        return directory.getAbsolutePath() + File.separator + (fileBase + ".pdf").toLowerCase();
    }

    /**
     * Get ZIP file name.
     * @return file name
     */
    String getZipFilename() {
        final String fileBase = directory.getName();
        return directory.getAbsolutePath() + File.separator + (fileBase + ".zip").toLowerCase();
    }

    /**
     * Get titles file name.
     * @return file name
     */
    String getTitlesFilename() {
        return directory.getAbsolutePath() + File.separator + "_titles.txt";
    }

    /**
     * Get contents for manifest file.
     * @param dateRecords sorted records
     * @return manifest contents
     */
    StringBuilder getManifest(final Map<LocalDate, List<FileInfo>> dateRecords) {
        StringBuilder manifest = new StringBuilder();

        int imageNumber = 0;

        String publicationType = published ? "Published" : "Unpublished";

        manifest.append("Group Registration of ").append(publicationType).append(" Photographs").append(System.lineSeparator());;
        manifest.append("This is a complete list of photographs for case number: ").append(caseNumber).append(System.lineSeparator());

        if (published) {
            manifest.append("Photograph #, Filename of Photograph, Title of Photograph, Date of Publication").append(System.lineSeparator());

        } else {
            manifest.append("Photograph #, Filename of Photograph, Title of Photograph").append(System.lineSeparator());;
        }

        for (Map.Entry<LocalDate, List<FileInfo>> entry : dateRecords.entrySet()) {
            List<FileInfo> values = entry.getValue();

            //sort value collections
            values.sort(new AlphanumComparator());

            for (FileInfo fileInfo : values) {
                manifest.append(++imageNumber).append(", ");
                manifest.append(fileInfo.getFileName()).append(", ");
                manifest.append(fileInfo.getName());

                if (published) {
                    manifest.append(", ");
                    manifest.append(fileInfo.getDate());
                }

                manifest.append(System.lineSeparator());
            }
        }

        return manifest;
    }

    /**
     * Group all image titles into smaller groups with a maximum size.
     * @param titleRecords sorted records
     * @return list of titles
     */
    //FIXME: need to do this for published or unpublished
    List<String> getAllTitles(final Map<String, List<FileInfo>> titleRecords) {
        List<String> titles = new ArrayList<>();
        int counter = 0;

        for (Map.Entry<String, List<FileInfo>> entry : titleRecords.entrySet()) {
            String key = entry.getKey();
            List<FileInfo> values = entry.getValue();

            //sort value collections
            values.sort(new AlphanumComparator());

            StringBuilder sb = new StringBuilder();

            for (FileInfo value : values) {
                ++counter;
                String name = value.getName();

                if (sb.length() + name.length() > TITLES_GROUP_MAX_CHARACTERS) {
                    sb.insert(0, key + "(" + counter +"): ");
                    sb.delete(sb.length() - 2, sb.length());

                    titles.add(sb.toString());
                    sb.setLength(0);
                    counter = 0;
                }

                sb.append(name).append(", ");
            }

            sb.insert(0, key + " (" + counter +"): ");
            sb.delete(sb.length() - 2, sb.length());

            titles.add(sb.toString());
            sb.setLength(0);
            counter = 0;
        }

        return titles;
    }

    /**
     * Create a text file.
     * @param sb file contents
     * @param path path and file to create
     * @throws IOException error
     */
    void writeTextFile(final String sb, final String path) throws IOException {
        //create or replace existing
        Files.write(Paths.get(path), sb.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        LOGGER.info("created: {}", path);
    }

    /**
     * Create a zip file of all required files.
     * @param dateRecords sorted records
     * @param manifestPath location of manifest file
     * @throws IOException error
     */
    //FIXME: use dateRecords or fileInfos?
    void writeZip(final Map<LocalDate, List<FileInfo>> dateRecords, final String manifestPath) throws IOException {
        String zipPath = this.getZipFilename();

        List<String> zipPaths = new ArrayList<>();
        zipPaths.add(manifestPath);

        for (Map.Entry<LocalDate, List<FileInfo>> items : dateRecords.entrySet()) {
            for (FileInfo item : items.getValue()) {
                zipPaths.add(directory.getAbsolutePath() + File.separator + item.getFileName());
            }
        }

        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipPath))) {
            for (String filePath : zipPaths) {
                File fileToZip = new File(filePath);
                zipOut.putNextEntry(new ZipEntry(fileToZip.getName()));
                Files.copy(fileToZip.toPath(), zipOut);
            }
        }

        LOGGER.info("created: {}", manifestPath);
    }

    /**
     * Create PDF of all records which is very similar to CSV version.
     * @param dateRecords sorted records
     * @throws WorkflowException error
     */
    void writePDF(final Map<LocalDate, List<FileInfo>> dateRecords) throws WorkflowException {
        String pdfPath = this.getPDFFilename();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

        try {
            PdfWriter writer = new PdfWriter(pdfPath);
            //writer.setPageEvent(new FooterPdfPageEventHelper());

            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(PageSize.A4);

            Document doc = new Document(pdfDoc);
            doc.setMargins(15,15,15,15);
            doc.setFontSize(FONT_SIZE_BODY);

            String publicationType = published ? "Published" : "Unpublished";
            int columns = published ? 4 : 3;

            Paragraph p1 = new Paragraph("Group Registration of " + publicationType + " Photographs" + System.lineSeparator());
            p1.add("This is a complete list of photographs for case number: " + caseNumber + System.lineSeparator());
            p1.setFontSize(FONT_SIZE_HEADER);
            p1.setBold();
            doc.add(p1);

            Table table = new Table(columns);
            table.addCell(this.getTableHeaderCell(60, "Photograph #"));
            table.addCell(this.getTableHeaderCell(180, "Filename of Photograph"));
            table.addCell(this.getTableHeaderCell(180, "Title of Photograph"));

            if (published) {
                table.addCell(this.getTableHeaderCell(75, "Date of Publication"));
            }

            int number = 0;
            for (Map.Entry<LocalDate, List<FileInfo>> items : dateRecords.entrySet()) {
                for (FileInfo item : items.getValue()) {
                    table.addCell(this.getTableCell("" + (++number)));
                    table.addCell(this.getTableCell(item.getFileName()));
                    table.addCell(this.getTableCell(item.getName()));

                    if (published) {
                        String dateStr = dateFormatter.format(item.getDate());
                        table.addCell(this.getTableCell(dateStr));
                    }
                }
            }

            doc.add(table);
            doc.close();

            LOGGER.info("created: {}", pdfPath);

        } catch (Exception e) {
            throw new WorkflowException(e);
        }
    }

    /**
     * Get table header cell.
     * @param width width of cell
     * @param text text for cell
     * @return table cell
     */
    private Cell getTableHeaderCell(final int width, final String text) {
        Cell cell = new Cell(1, 1).add(new Paragraph(text));
        cell.setWidth(width);
        cell.setPadding(3);
        cell.setFontSize(FONT_SIZE_BODY);
        cell.setBold();

        return cell;
    }

    /**
     * Get table cell.
     * @param text text for cell
     * @return table cell
     */
    private Cell getTableCell(final String text) {
        Cell cell = new Cell(1, 1).add(new Paragraph(text));
        cell.setPadding(3);
        cell.setFontSize(FONT_SIZE_BODY);

        return cell;
    }

    /**
     * Get title of image from file metadata.
     * @param file file to process
     * @return title or empty string
     * @throws IOException error
     */
    String getTitle(final File file) throws IOException {
        ImageMetadata metadata = Imaging.getMetadata(file);

        if (metadata instanceof JpegImageMetadata jpegMetadata) {
            JpegPhotoshopMetadata photoshopMetadata = jpegMetadata.getPhotoshop();

            if (photoshopMetadata != null) {
                @SuppressWarnings("unchecked")
                final List<GenericImageMetadata.GenericImageMetadataItem> photoshopMetadataItems = (List<GenericImageMetadata.GenericImageMetadataItem>) photoshopMetadata.getItems();

                for (final GenericImageMetadata.GenericImageMetadataItem photoshopMetadataItem : photoshopMetadataItems) {
                    final String propertyName = photoshopMetadataItem.getKeyword();
                    final String propertyValue = photoshopMetadataItem.getText();

                    //Object Name is the title as output by Adobe Photoshop and Lightroom
                    if (propertyName.equals("Object Name")) {
                        return propertyValue;
                    }
                }
            }
        }

        return "";
    }

    /**
     * Get file name.
     * @param file file to process
     * @return file name
     */
    String getFilename(final File file) {
        return file.getName();
    }

    /**
     * Get date and time for file. Uses last modified.
     * @param file file to process
     * @return date and time
     * @throws IOException error
     */
    LocalDateTime getFileDateTime(final File file, final boolean created) throws IOException {
        BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        FileTime dateTime = created ? attr.creationTime() : attr.lastModifiedTime();

        return LocalDateTime.ofInstant(dateTime.toInstant(), ZoneId.systemDefault());
    }

}
