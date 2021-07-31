package mgabelmann.photo.workflow.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

public class BackupTest extends AbstractFileSystemTest {
    private static final Logger LOG = Logger.getLogger(ArchiveTest.class);
    
    private static URI srcURI;
    
    private File sourceDir;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        BasicConfigurator.configure();
        srcURI = new URI(BackupTest.class.getResource("BackupTest.class").toString());
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
    public void process() throws IOException, InterruptedException {
        Backup backup = new Backup(sourceDir, tstDir, HashType.SHA256, true);
        backup.process();
        
        File[] files = tstDir.listFiles();
        
        assertEquals(2, files.length);
        
        //manifest file is not written for this test, probably should be
        //assertTrue("file not found " + FileRecordCodec.FILENAME, new File(tstDir, FileRecordCodec.FILENAME).exists());
        
        File f2009 = new File(tstDir, "2009");
        assertTrue("directory not found " + f2009.getAbsolutePath(), f2009.exists());
        assertEquals(3, f2009.listFiles().length);
        
        File f20090215 = new File(f2009, "2009-02-15");
        assertTrue("directory not found " + f20090215.getAbsolutePath(), f20090215.exists());
        assertEquals(12, f20090215.listFiles().length);
        
        File f2010 = new File(tstDir, "2010");
        assertTrue("directory not found " + f2010.getAbsolutePath(), f2010.exists());
        assertEquals(1, f2010.listFiles().length);
        
        File f20100202 = new File(f2010, "2010-02-02");
        assertTrue("directory not found " + f20100202.getAbsolutePath(), f20100202.exists());
        assertEquals(1, f20100202.listFiles().length);
    }

}
