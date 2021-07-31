package mgabelmann.photo.workflow.io;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import mgabelmann.junit.AbstractFileSystemTest;
import mgabelmann.photo.workflow.HashType;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public final class ArchiveTest extends AbstractFileSystemTest {
    private static final Logger LOG = Logger.getLogger(ArchiveTest.class);
    
    private static URI srcURI;
    
    private File sourceDir;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        BasicConfigurator.configure();
        srcURI = new URI(ArchiveTest.class.getResource("ArchiveTest.class").toString());
    }
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        sourceDir = new File(new File(srcURI).getParentFile(), "01_src");
        assertTrue("sourceDir does not exist", sourceDir.exists());
    }

    @After
    public void tearDown() throws Exception {
        sourceDir = null;
    }

    @Test
    public void testProcess() throws IOException {
        Archive archive = new Archive(sourceDir, tstDir, HashType.SHA256, true);
        archive.process();
        
        File[] files = tstDir.listFiles();
        
        //there should be 3 directories and 1 file
        assertEquals(4, tstDir.listFiles().length);
        assertTrue("file not found " + FileRecordCodec.FILENAME, new File(tstDir, FileRecordCodec.FILENAME).exists());
        
        File f1 = new File(tstDir, "0f2");
        assertTrue("directory not found " + f1.getPath(), f1.exists());
        
        File f2 = new File(tstDir, "2e9");
        assertTrue("directory not found " + f2.getPath(), f2.exists());
        
        File f3 = new File(tstDir, "b05");
        assertTrue("directory not found " + f3.getPath(), f3.exists());
        
        assertEquals("1 file must exist", 1, f1.listFiles().length);
        assertEquals("12 files must exist", 12, f2.listFiles().length);
        assertEquals("2 files must exist", 2, f3.listFiles().length);
    }

    @Test
    public void testVerify() throws IOException {
        testProcess();
        
        Archive archive = new Archive(sourceDir, tstDir, HashType.SHA256, true);
        archive.validate();
    }
}
