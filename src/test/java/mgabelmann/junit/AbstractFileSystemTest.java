package mgabelmann.junit;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Before;

/**
 * 
 * @author Mike Gabelmann
 */
public abstract class AbstractFileSystemTest {
    /** File system temp dir. */
    protected static String tmpDirProperty = System.getProperty("java.io.tmpdir");
    
    /** Directory to create for testing this test. */
    protected String tstDirName = Long.toHexString(System.currentTimeMillis());
    
    /** Directory where unit tests will be performed. */
    protected File tstDir;
    
    @Before
    public void setUp() throws Exception {
        tstDir = new File(tmpDirProperty, tstDirName);
        
        //after the JVM exits this directory will be deleted.
        tstDir.deleteOnExit();
        
        if (! tstDir.mkdir()) {
            throw new IOException("unable to create test directory " + tstDir.getAbsolutePath());
        }
        
        assertTrue(tstDir.exists());
        assertTrue(tstDir.canRead());
        assertTrue(tstDir.canWrite());
    }
}
