package mgabelmann.photo.workflow.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import mgabelmann.junit.AbstractFileSystemTest;
import mgabelmann.photo.workflow.HashType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;


public class BackupTest extends AbstractFileSystemTest {
    private static final Logger LOG = LogManager.getLogger(ArchiveTest.class);
    
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

}
