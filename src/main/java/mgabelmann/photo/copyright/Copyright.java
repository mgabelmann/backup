package mgabelmann.photo.copyright;

import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import mgabelmann.photo.workflow.exception.WorkflowException;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.common.GenericImageMetadata;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegPhotoshopMetadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    private static final int TABLE_FONT_SIZE = 8;
    private static final int TABLE_PADDING = 3;
    private static final int BODY_FONT_SIZE = 10;

    public static final String FIELD_DELIMITER = ",";
    public static final String FIELD_SEPARATOR = FIELD_DELIMITER + " ";

    public static final String DATE_FORMAT_YEARMONTH = "yyyy-MM";

    /** Directory to process. */
    private Path directory;

    /** Copyright case number. */
    private String caseNumber;

    /** Is group published or unpublished. */
    private boolean published;

    /** Results of scanning files. */
    private final List<FileInfo> fileInfos;

    /**
     * Constructor.
     * @param path directory
     * @param caseNumber case number
     * @param published published or unpublished
     */
    public Copyright(
            final Path path,
            final String caseNumber,
            final boolean published) {

        this.setDirectory(path);
        this.setCaseNumber(caseNumber);
        this.setPublished(published);

        this.fileInfos = new ArrayList<>();
    }

    /**
     * Entry point for using this utility.
     * @param args arguments
     */
    public static void main(final String[] args) {
        if (args.length != 3) {
            System.err.println("invalid number of arguments");
            System.out.println("Usage: <directory> <caseNumber> <published>");
            System.exit(1);
        }

        Path directory = Paths.get(args[0]);
        String caseNumber = args[1];
        boolean published = args[2].equalsIgnoreCase("p");

        Copyright copyright = new Copyright(directory, caseNumber, published);

        try {
            copyright.collect();
            copyright.process();

        } catch (WorkflowException we) {
            LOGGER.error(we.getMessage());
        }
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(final String caseNumber) {
        if (caseNumber == null || caseNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("case number is required");
        }

        this.caseNumber = caseNumber;
    }

    public Path getDirectory() {
        return directory;
    }

    public void setDirectory(final Path directory) {
        if (directory == null) {
            throw new IllegalArgumentException("directory is required");

        } else if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("directory is not a directory");
        }

        this.directory = directory;
    }

    public List<FileInfo> getFileInfos() {
        return fileInfos;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(final boolean published) {
        this.published = published;
    }

    /**
     * Call this method once all files are collected.
     * @throws WorkflowException
     */
    public void process() throws WorkflowException {
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

            String dateStr = DateTimeFormatter.ofPattern(DATE_FORMAT_YEARMONTH).format(date);

            if (titleRecords.containsKey(dateStr)) {
                titleRecords.get(dateStr).add(fileInfo);

            } else {
                ArrayList<FileInfo> infos = new ArrayList<>();
                infos.add(fileInfo);
                titleRecords.put(dateStr, infos);
            }
        }

        try {
            Path manifestFilename = this.getManifestFilename(directory, directory.getFileName().toString());

            {
                //generate manifest file
                String manifest = this.getManifest(dateRecords);
                this.writeTextFile(manifest, manifestFilename);
            }

            {
                //generate titles file
                List<String> titles = this.getAllTitles(titleRecords);
                String titlesStr = titles.stream().collect(Collectors.joining(System.lineSeparator() + System.lineSeparator()));

                Path titlesFilename = this.getTitlesFilename(directory);
                this.writeTextFile(titlesStr, titlesFilename);
            }

            {
                //create PDF manifest file
                Path pdfFilename = this.getPDFFilename(directory, directory.getFileName().toString());
                this.writePdfFile(dateRecords, pdfFilename);
            }

            {
                //create ZIP file of ALL files processed/created except titles
                Path zipFilename = this.getZipFilename(directory, directory.getFileName().toString());
                this.writeZipFile(dateRecords, manifestFilename, zipFilename);
            }

        } catch (IOException ie) {
            throw new WorkflowException(ie);
        }

        LOGGER.info("copyright - finished");
    }

    /**
     * Add records, but only if they do not already exist.
     * @param records
     */
    public void add(final List<FileInfo> records) {
        List<FileInfo> recordsToAdd = records.stream().filter(a->!fileInfos.contains(a)).toList();
        fileInfos.addAll(recordsToAdd);
    }

    /**
     * Call this to do work. Used by command line tool, only call once.
     * @throws WorkflowException error
     */
    public void collect() throws WorkflowException {
        LOGGER.info("copyright - processing directory '{}', for case number '{}', published={}", directory, caseNumber, published);

        List<FileInfo> records = this.collect(directory);
        this.fileInfos.addAll(records);
    }

    /**
     * Process a single directory and all its contents.
     * @param dir starting directory
     * @return list of matching files
     * @throws WorkflowException error
     */
    public List<FileInfo> collect(final Path dir) throws WorkflowException {
        List<FileInfo> records = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(dir, 10).distinct()) {
            for (Path p : (Iterable<Path>) paths::iterator) {
                if (Files.isDirectory(p)) {
                    this.collectDirectory(p);

                } else if (Files.isReadable(p) && p.getFileName().toString().toLowerCase().matches(".*\\.jpe?g")) {
                    FileInfo fi = this.collectFile(p);
                    records.add(fi);

                    LOGGER.debug("found file '{}'", fi.getFilename());

                } else {
                    LOGGER.debug("skipping file {}, is not a JPG", p.getFileName());
                }
            }

        } catch (IOException ie) {
            throw new WorkflowException(ie);
        }

        return records;
    }

    /**
     * Process a directory.
     * @param dir directory
     * @throws WorkflowException error
     */
    public void collectDirectory(final Path dir) throws WorkflowException {
        LOGGER.debug("directory={}", dir.toString());
    }

    /**
     * Process a single file.
     * @param file file to process
     * @return record
     * @throws WorkflowException error
     */
    public FileInfo collectFile(final Path file) throws WorkflowException {
        try {
            final String fileName = file.getFileName().toString();
            final LocalDateTime dateTime = this.getFileDateTime(file, false);
            String title;

            try {
                title = this.getTitle(file);

            } catch (ImagingException ie) {
                title = null;

                printStackTrace(Level.DEBUG, ie);
                LOGGER.warn("unable to get title from file {}, error is {}", fileName, ie.getMessage());
            }

            if (title == null || title.isEmpty()) {
                final int i = fileName.lastIndexOf('.');
                title = i == -1 ? fileName : fileName.substring(0, i);
            }

            return new FileInfo(file, title, dateTime);

        } catch (IOException ie) {
            printStackTrace(Level.DEBUG, ie);
            throw new WorkflowException("error processing " + file.getFileName().toString() + ", " + ie.getMessage(), ie);
        }
    }

    /**
     * Get manifest file name.
     * @return file name
     */
    Path getManifestFilename(final Path base, final String fileBase) {
        final String filePrefix = published ? "p" : "u";
        final String fileName = (filePrefix + fileBase + "_" + caseNumber + ".csv").toLowerCase();

        return Paths.get(base.toString(), fileName);
    }

    /**
     * Get PDF file name.
     * @return file name
     */
    Path getPDFFilename(final Path base, final String fileBase) {
        final String fileName = (fileBase + ".pdf").toLowerCase();

        return Paths.get(base.toString(), fileName);
    }

    /**
     * Get ZIP file name.
     * @return file name
     */
    Path getZipFilename(final Path base, final String fileBase) {
        final String fileName = (fileBase + ".zip").toLowerCase();

        return Paths.get(base.toString(), fileName);
    }

    /**
     * Get titles file name.
     * @return file name
     */
    Path getTitlesFilename(final Path base) {
        final String fileName = "_titles.txt";

        return Paths.get(base.toString(), fileName);
    }

    /**
     * Get contents for manifest file.
     * @param dateRecords sorted records
     * @return manifest contents
     */
    String getManifest(final Map<LocalDate, List<FileInfo>> dateRecords) {
        StringBuilder manifest = new StringBuilder();

        int imageNumber = 0;

        String publicationType = published ? "Published" : "Unpublished";

        manifest.append("Group Registration of ").append(publicationType).append(" Photographs").append(System.lineSeparator());;
        manifest.append("This is a complete list of photographs for case number: ").append(caseNumber).append(System.lineSeparator());

        final List<String> titles = new ArrayList<>(Arrays.asList("Photograph #", "Filename of Photograph", "Title of Photograph"));
        if (published) {
            titles.add("Date of Publication");
        }

        manifest.append(String.join(FIELD_SEPARATOR, titles)).append(System.lineSeparator());

        for (Map.Entry<LocalDate, List<FileInfo>> entry : dateRecords.entrySet()) {
            List<FileInfo> values = entry.getValue();

            //sort value collections
            values.sort(new AlphanumComparator());

            for (FileInfo fileInfo : values) {
                manifest.append(++imageNumber).append(FIELD_SEPARATOR);
                manifest.append(fileInfo.getFilename()).append(FIELD_SEPARATOR);
                manifest.append(fileInfo.getName());

                if (published) {
                    manifest.append(FIELD_SEPARATOR);
                    manifest.append(fileInfo.getDate());
                }

                manifest.append(System.lineSeparator());
            }
        }

        return manifest.toString();
    }

    /**
     * Group all image titles into smaller groups with a maximum size.
     * @param titleRecords sorted records
     * @return list of titles
     */
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
    void writeTextFile(final String sb, final Path path) throws IOException {
        //create or replace existing
        Files.write(path, sb.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        LOGGER.info("created: {}", path);
    }

    /**
     * Create a zip file of all required files.
     * @param dateRecords sorted records
     * @param manifestPath location of manifest file
     * @param zipPath path to zip file
     * @throws IOException error
     */
    void writeZipFile(final Map<LocalDate, List<FileInfo>> dateRecords, final Path manifestPath, final Path zipPath) throws IOException {
        List<Path> zipPaths = new ArrayList<>();
        zipPaths.add(manifestPath);

        for (Map.Entry<LocalDate, List<FileInfo>> items : dateRecords.entrySet()) {
            for (FileInfo item : items.getValue()) {
                zipPaths.add(item.getPath());
            }
        }

        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
            for (Path filePath : zipPaths) {
                Path newPath = Paths.get(filePath.getFileName().toString());
                zipOut.putNextEntry(new ZipEntry(newPath.toString()));
                Files.copy(filePath, zipOut);
            }
        }

        LOGGER.info("created: {}", zipPath);
    }

    /**
     * Create PDF of all records which is very similar to CSV version.
     * @param dateRecords sorted records
     * @throws WorkflowException error
     */
    void writePdfFile(final Map<LocalDate, List<FileInfo>> dateRecords, final Path pdfPath) throws WorkflowException {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_YEARMONTH);

        try {
            PdfWriter writer = new PdfWriter(pdfPath.toFile());
            PdfDocument pdfDoc = new PdfDocument(writer);

            Document doc = new Document(pdfDoc, PageSize.A4, true);
            doc.setMargins(20,20,30,20);
            doc.setFontSize(TABLE_FONT_SIZE);

            FooterEventHandler footerHandler = new FooterEventHandler();
            pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, footerHandler);

            String publicationType = published ? "Published" : "Unpublished";
            int columns = published ? 4 : 3;

            Paragraph p1 = new Paragraph("Group Registration of " + publicationType + " Photographs" + System.lineSeparator());
            p1.add("This is a complete list of photographs for case number: " + caseNumber + System.lineSeparator());
            p1.setFontSize(BODY_FONT_SIZE);
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
                    table.addCell(this.getTableCell(item.getPath().getFileName().toString()));
                    table.addCell(this.getTableCell(item.getName()));

                    if (published) {
                        String dateStr = dateFormatter.format(item.getDate());
                        table.addCell(this.getTableCell(dateStr));
                    }
                }
            }

            doc.add(table);

            //update the footer with the total number of pages
            footerHandler.writeTotal(pdfDoc);

            pdfDoc.close();

            LOGGER.info("created: {}", pdfPath);

        } catch (Exception e) {
            throw new WorkflowException(e);
        }
    }

    /**
     * Print stack trace to the log.
     * @param level log level
     * @param ex stacktrace
     */
    void printStackTrace(final Level level, final Throwable ex) {
        try (Writer buf = new StringWriter(); PrintWriter pw = new PrintWriter(buf)) {
            ex.printStackTrace(pw);
            LOGGER.atLevel(level).log(buf.toString());

        } catch (IOException ie) {
            LOGGER.warn("error printing stacktrace. msg={}", ie.getMessage());
        }
    }

    /**
     * Get title of image from file metadata.
     * @param file file to process
     * @return title or empty string
     * @throws IOException error
     */
    String getTitle(final Path file) throws IOException {
        ImageMetadata metadata = Imaging.getMetadata(file.toFile());

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

        LOGGER.trace("{}, no title found", file.getFileName());

        return "";
    }

    /**
     * Get file name.
     * @param file file to process
     * @return file name
     */
    String getFilename(final Path file) {
        return file.getFileName().toString();
    }

    /**
     *
     * @param file
     * @return
     */
    String getParent(final Path file) {
        return file.getParent().toString();
    }

    /**
     * Get date and time for file. Uses last modified.
     * @param file file to process
     * @param created use created vs last modified time
     * @return date and time
     * @throws IOException error
     */
    LocalDateTime getFileDateTime(final Path file, final boolean created) throws IOException {
        BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
        FileTime dateTime = created ? attr.creationTime() : attr.lastModifiedTime();

        return LocalDateTime.ofInstant(dateTime.toInstant(), ZoneId.systemDefault());
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
        cell.setPadding(TABLE_PADDING);
        cell.setFontSize(TABLE_FONT_SIZE);
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
        cell.setPadding(TABLE_PADDING);
        cell.setFontSize(TABLE_FONT_SIZE);

        return cell;
    }

}
