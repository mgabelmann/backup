package mgabelmann.photo.workflow.io;

import mgabelmann.photo.workflow.HashType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


/**
 *
 * @author mgabe
 */
public class BackupTest {
    private static final Logger LOG = LogManager.getLogger(ArchiveTest.class);

    @TempDir
    private Path tempDir;

    private Path dir1;
    private Path dir2;

    private Path file1;
    private Path file2;
    private Path file3;


    @Test
    @DisplayName("source directory does not exist")
    void test1_process() {
        Path srcDir = Paths.get(tempDir.toString() + File.separator + "srcDir");
        Path dstDir = Paths.get(tempDir.toString() + File.separator + "dstDir");

        Backup b = new Backup(srcDir.toFile(), dstDir.toFile(), HashType.SHA256, false);
        IOException ie = Assertions.assertThrows(IOException.class, b::process);

        Assertions.assertEquals(srcDir + " does not exist", ie.getMessage());
    }

    @Test
    @DisplayName("destination directory does not exist")
    void test3_process() throws Exception {
        Path srcDir = this.createDirectory(tempDir, "srcDir");
        Path dstDir = Paths.get(tempDir.toString() + File.separator + "dstDir");

        Backup b = new Backup(srcDir.toFile(), dstDir.toFile(), HashType.SHA256, false);
        IOException ie = Assertions.assertThrows(IOException.class, b::process);

        Assertions.assertEquals(dstDir + " does not exist", ie.getMessage());
    }

    @Test
    @DisplayName("destination directory exists")
    void test4_process() throws Exception {
        Path srcDir = this.createDirectory(tempDir, "srcDir");
        Path dstDir = this.createDirectory(tempDir, "dstDir");

        Backup b = new Backup(srcDir.toFile(), dstDir.toFile(), HashType.SHA256, false);
        b.process();
    }

    @Test
    @DisplayName("create destination directory")
    void test5_process() throws Exception {
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
    void test6_process() throws Exception {
        Path srcDir = this.createDirectory(tempDir, "srcDir");
        Path dstDir = this.createDirectory(tempDir, "dstDir");

        Path srcFile1 = this.createFile(srcDir, "srcFile1.jpg");

        Backup b = new Backup(srcDir.toFile(), dstDir.toFile(), HashType.SHA256, false);
        b.process();

        Path dstFile1 = Paths.get(dstDir + File.separator + "srcFile1.jpg");

        Assertions.assertTrue(Files.exists(dstFile1));
        Assertions.assertFalse(Files.isDirectory(dstFile1));
    }

    /*
    private static URI srcURI;
    
    private File sourceDir;
    
    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        srcURI = new URI(BackupTest.class.getResource("BackupTest.class").toString());
    }

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        
        sourceDir = new File(new File(srcURI).getParentFile(), "01_src");
        Assertions.assertTrue(sourceDir.exists(), "sourceDir does not exist");
    }

    @AfterEach
    public void tearDown() throws Exception {
        sourceDir = null;
    }

    @Test
    public void process() throws IOException, InterruptedException {
        Backup backup = new Backup(sourceDir, tstDir, HashType.SHA256, true);
        backup.process();
        
        File[] files = tstDir.listFiles();

        Assertions.assertEquals(2, files.length);
        
        //manifest file is not written for this test, probably should be
        //assertTrue("file not found " + FileRecordCodec.FILENAME, new File(tstDir, FileRecordCodec.FILENAME).exists());
        
        File f2009 = new File(tstDir, "2009");
        Assertions.assertTrue(f2009.exists(), "directory not found " + f2009.getAbsolutePath());
        Assertions.assertEquals(3, f2009.listFiles().length);
        
        File f20090215 = new File(f2009, "2009-02-15");
        Assertions.assertTrue(f20090215.exists(), "directory not found " + f20090215.getAbsolutePath());
        Assertions.assertEquals(12, f20090215.listFiles().length);
        
        File f2010 = new File(tstDir, "2010");
        Assertions.assertTrue(f2010.exists(), "directory not found " + f2010.getAbsolutePath());
        Assertions.assertEquals(1, f2010.listFiles().length);
        
        File f20100202 = new File(f2010, "2010-02-02");
        Assertions.assertTrue(f20100202.exists(), "directory not found " + f20100202.getAbsolutePath());
        Assertions.assertEquals(1, f20100202.listFiles().length);
    }
     */

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
        Path file = dir.resolve(filename);
        Random r = new Random();
        List<String> data = Arrays.asList("" + r.nextInt(), "" + r.nextBoolean(), "" + r.nextFloat());
        Files.write(file, data);

        return file;
    }

}
