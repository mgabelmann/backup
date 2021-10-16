package mgabelmann.junit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;

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
    
    @BeforeEach
    public void setUp() throws Exception {
        tstDir = new File(tmpDirProperty, tstDirName);
        
        //after the JVM exits this directory will be deleted.
        tstDir.deleteOnExit();
        
        if (! tstDir.mkdir()) {
            throw new IOException("unable to create test directory " + tstDir.getAbsolutePath());
        }
        
        Assertions.assertTrue(tstDir.exists());
        Assertions.assertTrue(tstDir.canRead());
        Assertions.assertTrue(tstDir.canWrite());
    }
}
