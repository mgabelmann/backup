package mgabelmann.photo.workflow.io;

import mgabelmann.photo.workflow.HashType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;


/**
 *
 * @author mgabe
 */
public class BackupTest {
    @TempDir
    private Path tempDir;

    private Path dir1;
    private Path dir2;

    private Path file1;
    private Path file2;
    private Path file3;

    @BeforeAll
    static void beforeAll() {
        //force logging level so more statements will run
        Configurator.setLevel(LogManager.getLogger(Backup.class).getName(), Level.TRACE);
    }

    @Test
    @DisplayName("source directory is null")
    void test1_process() {
        Path dstDir = tempDir;
        HashType type = HashType.SHA256;
        boolean verify = false;

        IllegalArgumentException iae = Assertions.assertThrows(IllegalArgumentException.class, () -> new Backup(null, dstDir.toFile(), type, verify));
        Assertions.assertEquals("dirLocal cannot be null", iae.getMessage());
    }

    @Test
    @DisplayName("source directory does not exist")
    void test2_process() {
        Path srcDir = tempDir.resolve("srcDir1");
        Path dstDir = tempDir;
        HashType type = HashType.SHA256;
        boolean verify = false;

        IllegalArgumentException iae = Assertions.assertThrows(IllegalArgumentException.class, () -> new Backup(srcDir.toFile(), dstDir.toFile(), type, verify));
        Assertions.assertEquals("dirLocal does not exist", iae.getMessage());
    }

    @Test
    @DisplayName("destination directory is null")
    void test3_process() {
        Path srcDir = tempDir;
        HashType type = HashType.SHA256;
        boolean verify = false;

        IllegalArgumentException iae = Assertions.assertThrows(IllegalArgumentException.class, () -> new Backup(srcDir.toFile(), null, type, verify));
        Assertions.assertEquals("dirRemote cannot be null", iae.getMessage());
    }

    @Test
    @DisplayName("source directory does not exist")
    void test4_process() {
        Path srcDir = tempDir;
        Path dstDir = tempDir.resolve("dstDir1");
        HashType type = HashType.SHA256;
        boolean verify = false;

        IllegalArgumentException iae = Assertions.assertThrows(IllegalArgumentException.class, () -> new Backup(srcDir.toFile(), dstDir.toFile(), type, verify));
        Assertions.assertEquals("dirRemote does not exist", iae.getMessage());
    }

    @Test
    @DisplayName("type cannot be null")
    void test5_process() {
        Path srcDir = tempDir;
        Path dstDir = tempDir;
        HashType type = null;
        boolean verify = false;

        IllegalArgumentException iae = Assertions.assertThrows(IllegalArgumentException.class, () -> new Backup(srcDir.toFile(), dstDir.toFile(), type, verify));
        Assertions.assertEquals("type cannot be null", iae.getMessage());
    }

    //TODO: srcDir not readable

    @Test
    @DisplayName("destination directory is not writable")
    void test7_process() throws Exception {
        //Windows directories are always readable, so we skip the running of this test
        Assumptions.assumeFalse(System.getProperty("os.name").startsWith("Windows"));

        Path srcDir = this.createDirectory(tempDir, "srcDir");
        Path dstDir = this.createDirectory(tempDir, "dstDir");
        HashType type = HashType.SHA256;
        boolean verify = false;

        boolean success = dstDir.toFile().setWritable(false);

        if (!success) {
            Assertions.fail("could not set to read only");
        }

        IllegalArgumentException iae = Assertions.assertThrows(IllegalArgumentException.class, () -> new Backup(srcDir.toFile(), dstDir.toFile(), type, verify));
        Assertions.assertEquals("dirRemote is not writable", iae.getMessage());

        dstDir.toFile().setWritable(true);
    }


    @Test
    @DisplayName("create destination directory")
    void test10_process() throws Exception {
        Path srcDir = this.createDirectory(tempDir, "srcDir");
        this.createDirectory(srcDir, "srcDir2");

        Path dstDir = this.createDirectory(tempDir, "dstDir");

        Backup b = new Backup(srcDir.toFile(), dstDir.toFile(), HashType.SHA256, false);
        b.process();

        Path dstDir2 = Paths.get(dstDir + File.separator + "srcDir2");

        Assertions.assertTrue(Files.exists(dstDir2));
        Assertions.assertTrue(Files.isDirectory(dstDir2));
    }

    @Test
    @DisplayName("create new destination file")
    void test11_process() throws Exception {
        Path srcDir = this.createDirectory(tempDir, "srcDir");
        Path dstDir = this.createDirectory(tempDir, "dstDir");

        Path srcFile1 = this.createFile(srcDir, "srcFile1.jpg");

        Backup b = new Backup(srcDir.toFile(), dstDir.toFile(), HashType.SHA256, false);
        b.process();

        Path dstFile1 = Paths.get(dstDir + File.separator + "srcFile1.jpg");

        Assertions.assertTrue(Files.exists(dstFile1));
    }

    @Test
    @DisplayName("destination file exists, different length")
    void test12_process() throws Exception {
        Path srcDir = this.createDirectory(tempDir, "srcDir");
        Path dstDir = this.createDirectory(tempDir, "dstDir");

        Path srcFile1 = this.createFileWithData(srcDir, "srcFile1.jpg", "updated data");
        Path dstFile1 = this.createFileWithData(dstDir, "srcFile1.jpg", "old data");

        Backup b = new Backup(srcDir.toFile(), dstDir.toFile(), HashType.SHA256, false);
        b.process();

        Assertions.assertEquals(12, dstFile1.toFile().length());
    }

    @Test
    @DisplayName("destination file exists, same length, different modification date/time")
    void test13_process() throws Exception {
        Path srcDir = this.createDirectory(tempDir, "srcDir");
        Path dstDir = this.createDirectory(tempDir, "dstDir");

        Path srcFile1 = this.createFileWithData(srcDir, "srcFile1.jpg", "updated data");
        Path dstFile1 = this.createFileWithData(dstDir, "srcFile1.jpg", "updated data");
        dstFile1.toFile().setLastModified(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());

        Backup b = new Backup(srcDir.toFile(), dstDir.toFile(), HashType.SHA256, false);
        b.process();

        Assertions.assertEquals(srcFile1.toFile().lastModified(), dstFile1.toFile().lastModified());
    }

    @Test
    @DisplayName("destination file exists and is identical")
    void test14_process() throws Exception {
        Path srcDir = this.createDirectory(tempDir, "srcDir");
        Path dstDir = this.createDirectory(tempDir, "dstDir");

        Path srcFile1 = this.createFileWithData(srcDir, "srcFile1.jpg", "updated data");
        Path dstFile1 = this.createFileWithData(dstDir, "srcFile1.jpg", "updated data");
        dstFile1.toFile().setLastModified(srcFile1.toFile().lastModified());

        Backup b = new Backup(srcDir.toFile(), dstDir.toFile(), HashType.SHA256, false);
        b.process();

        Assertions.assertEquals(srcFile1.toFile().lastModified(), dstFile1.toFile().lastModified());
    }

    @Disabled("requires refactoring")
    @Test
    @DisplayName("can't create destination directory")
    void test15_process() throws Exception {
        //Windows directories are always readable, so we skip the running of this test
        Assumptions.assumeFalse(System.getProperty("os.name").startsWith("Windows"));

        Path srcDir = this.createDirectory(tempDir, "srcDir");
        Path dstDir = this.createDirectory(tempDir, "dstDir");
        boolean success = dstDir.toFile().setWritable(false);

        if (!success) {
            Assertions.fail("could not set to read only");
        }

        Path srcFile1 = this.createFileWithData(srcDir, "srcFile1.jpg", "updated data");

        Backup b = new Backup(srcDir.toFile(), dstDir.toFile(), HashType.SHA256, false);

        IOException ie = Assertions.assertThrows(IOException.class, b::process);
        Assertions.assertEquals("unable to create directory " + dstDir.toAbsolutePath(), ie.getMessage());

        dstDir.toFile().setWritable(true);
    }



    /**
     * Create a known directory/file structure.
     * <pre>
     *     tempDir/
     *         srcFile1.jpg
     *         srcDir1/
     *             srcFile2.jpg
     *             srcDir2/
     *                 srcFile3.jpg
     * </pre>
     * @throws IOException error
     */
    private void createFiles() throws IOException {
        this.dir1 = this.createDirectory(tempDir, "srcDir1");
        this.dir2 = this.createDirectory(dir1, "srcDir2");

        this.file1 = this.createFile(tempDir, "srcfile1.jpg");
        this.file2 = this.createFile(dir1, "srcfile2.jpg");
        this.file3 = this.createFile(dir2, "srcfile3.jpg");
    }

    /**
     * Create directory.
     * @param parentDirectory parent directory
     * @param directory directory name
     * @return created directory
     * @throws IOException error
     */
    private Path createDirectory(Path parentDirectory, String directory) throws IOException {
        Assertions.assertTrue(Files.exists(parentDirectory));
        Assertions.assertNotNull(directory);
        Assertions.assertNotEquals("", directory);

        return Files.createDirectory(Paths.get(parentDirectory.toString(), directory));
    }

    /**
     * Create file.
     * @param dir parent directory
     * @param filename file name
     * @return file created file
     * @throws IOException error
     */
    private Path createFile(Path dir, String filename) throws IOException {
        Random r = new Random();
        byte[] b = new byte[1024];
        r.nextBytes(b);

        return this.createFileWithData(dir, filename, new String(b));
    }

    private Path createFileWithData(Path dir, String filename, String data) throws IOException {
        Assertions.assertTrue(Files.exists(dir));
        Assertions.assertNotNull(filename);
        Assertions.assertNotEquals("", filename);
        Assertions.assertNotNull(data);

        Path file = dir.resolve(filename);
        Files.writeString(file, data);

        return file;
    }

}
