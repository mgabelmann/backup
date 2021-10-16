package mgabelmann.photo.workflow.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import mgabelmann.junit.AbstractFileSystemTest;
import mgabelmann.photo.workflow.HashType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;


public final class ArchiveTest extends AbstractFileSystemTest {
    private static final Logger LOG = LogManager.getLogger(ArchiveTest.class);
    
    private static URI srcURI;
    
    private File sourceDir;
    
    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        srcURI = new URI(ArchiveTest.class.getResource("ArchiveTest.class").toString());
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
    public void testProcess() throws IOException {
        Archive archive = new Archive(sourceDir, tstDir, HashType.SHA256, true);
        archive.process();
        
        File[] files = tstDir.listFiles();
        
        //there should be 3 directories and 1 file
        Assertions.assertEquals(4, tstDir.listFiles().length);
        Assertions.assertTrue(new File(tstDir, FileRecordCodec.FILENAME).exists(), "file not found " + FileRecordCodec.FILENAME);
        
        File f1 = new File(tstDir, "0f2");
        Assertions.assertTrue(f1.exists(), "directory not found " + f1.getPath());
        
        File f2 = new File(tstDir, "2e9");
        Assertions.assertTrue(f2.exists(), "directory not found " + f2.getPath());
        
        File f3 = new File(tstDir, "b05");
        Assertions.assertTrue(f3.exists(), "directory not found " + f3.getPath());

        Assertions.assertEquals(1, f1.listFiles().length, "1 file must exist");
        Assertions.assertEquals(12, f2.listFiles().length, "12 files must exist");
        Assertions.assertEquals(2, f3.listFiles().length, "2 files must exist");
    }

    @Test
    public void testVerify() throws IOException {
        testProcess();
        
        Archive archive = new Archive(sourceDir, tstDir, HashType.SHA256, true);
        archive.validate();
    }
}
