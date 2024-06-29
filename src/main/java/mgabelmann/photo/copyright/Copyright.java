package mgabelmann.photo.copyright;

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


public class Copyright {
    private static final Logger LOGGER = LoggerFactory.getLogger(Copyright.class);

    /** Maximum number of characters for a titles group before breaking into a new group. */
    public static final int TITLES_GROUP_MAX_CHARACTERS = 1950;

    private final File directory;
    private final String caseNumber;
    private boolean published;

    private final ExecutorService service;

    private List<FileInfo> fileInfos;

    /**
     * Entry point for using this utility.
     * @param args
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

        File directory = new File("P:\\Mike\\catalog1\\05_output\\copyright\\to_submit\\2024_Q2");
        String caseNumber = "1-xxxx";
        boolean published = true;

        Copyright copyright = new Copyright(directory, caseNumber, published);

        try {
            copyright.process();

        } catch (WorkflowException we) {
            LOGGER.error(we.getMessage());
        }
    }

    /**
     * Constructor.
     * @param directory
     * @param caseNumber
     * @param published
     */
    public Copyright(
            final File directory,
            final String caseNumber,
            final boolean published) {

        if (directory == null) {
            throw new IllegalArgumentException("directory is required");

        } else if (!directory.isDirectory()) {
            throw new IllegalArgumentException("directory is not a directory");

        } else if (caseNumber == null || caseNumber.trim().length() == 0) {
            throw new IllegalArgumentException("case number is required");
        }

        this.directory = directory;
        this.caseNumber = caseNumber;
        this.published = published;
        this.fileInfos = new ArrayList<>();
        this.service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    public void process() throws WorkflowException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("copyright - processing directory {}, for case number {}", directory, caseNumber);
        }

        try {
            this.processDirectory(this.directory);

            service.shutdown();

            boolean timeout = service.awaitTermination(15, TimeUnit.SECONDS);

            //group photos by date, so we can sort by date
            Map<LocalDate, List<FileInfo>> dateRecords = new TreeMap<>();
            Map<String, List<FileInfo>> titleRecords = new TreeMap<>();

            for (FileInfo fileInfo : fileInfos) {
                if (dateRecords.containsKey(fileInfo.getDate())) {
                    dateRecords.get(fileInfo.getDate()).add(fileInfo);

                } else {
                    ArrayList<FileInfo> infos = new ArrayList<>();
                    infos.add(fileInfo);
                    dateRecords.put(fileInfo.getDate(), infos);
                }

                String dateStr = DateTimeFormatter.ofPattern("yyyy-MM").format(fileInfo.getDate());

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
            String filePrefix = published ? "p" : "u";
            String fileBase = directory.getName();
            String manifestFilename = (filePrefix + fileBase + "_" + caseNumber + ".csv").toLowerCase();
            String manifestPath = directory.getAbsolutePath() + File.separator + manifestFilename;

            this.writeTextFile(sb1.toString(), manifestPath);

            //generate titles file
            List<String> titles = this.getAllTitles(titleRecords);
            String titlesStr = titles.stream().collect(Collectors.joining(System.lineSeparator() + System.lineSeparator()));
            String titlesPath = directory.getAbsolutePath() + File.separator + "_titles.txt";

            this.writeTextFile(titlesStr, titlesPath);

            //create ZIP file of ALL files processed/created except titles
            this.writeZipFile(dateRecords, manifestPath);

            if (!timeout) {
                LOGGER.warn("service timed out");
            }

        } catch (IOException | InterruptedException e) {
            throw new WorkflowException(e);
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("copyright - finished\n");
        }
    }

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

    public void processFile(final File file) throws WorkflowException {
        try {
            final String fileName = this.getFilename(file);
            final LocalDateTime dateTime = this.getFileDateTime(file);
            final String title = this.getTitle(file);

            fileInfos.add(new FileInfo(fileName, title, dateTime));
            LOGGER.debug("added: {}", file.getName());

        } catch (IOException ie) {
            throw new WorkflowException(ie);
        }
    }

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

    List<String> getAllTitles(Map<String, List<FileInfo>> titleRecords) {
        List<String> titles = new ArrayList<>();

        for (Map.Entry<String, List<FileInfo>> entry : titleRecords.entrySet()) {
            String key = entry.getKey();
            List<FileInfo> values = entry.getValue();

            //sort value collections
            values.sort(new AlphanumComparator());

            StringBuilder sb = new StringBuilder();

            for (FileInfo value : values) {
                String name = value.getName();

                if (sb.length() + name.length() > TITLES_GROUP_MAX_CHARACTERS) {
                    sb.insert(0, key + ": ");
                    sb.delete(sb.length() - 2, sb.length());

                    titles.add(sb.toString());
                    sb.setLength(0);
                }

                sb.append(name).append(", ");
            }

            sb.insert(0, key + ": ");
            sb.delete(sb.length() - 2, sb.length());

            titles.add(sb.toString());
            sb.setLength(0);
        }

        return titles;
    }

    void writeTextFile(final String sb, String path) throws IOException {
        //create or replace existing
        Files.write(Paths.get(path), sb.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    void writeZipFile(final Map<LocalDate, List<FileInfo>> dateRecords, final String manifestPath) throws IOException {
        String fileBase = directory.getName();
        String zipPath = directory.getAbsolutePath() + File.separator + (fileBase + ".zip").toLowerCase();

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
    }

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

                    if (propertyName.equals("Object Name")) {
                        return propertyValue;
                    }
                }
            }
        }

        return "";
    }

    String getFilename(final File file) {
        return file.getName();
    }

    LocalDateTime getFileDateTime(final File file) throws IOException {
        if (published) {
            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            return LocalDateTime.ofInstant(attr.lastModifiedTime().toInstant(), ZoneId.systemDefault());

        } else {
            return LocalDateTime.now();
        }
    }

}
