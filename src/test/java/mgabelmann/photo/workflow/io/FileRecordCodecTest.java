package mgabelmann.photo.workflow.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileRecordCodecTest {
    private Collection<FileRecord> records;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        BasicConfigurator.configure();
    }

    @Before
    public void setUp() throws Exception {
        records = new ArrayList<FileRecord>();
    }

    @After
    public void tearDown() throws Exception {
        records = null;
    }

    @Test
    public void testReadFile() throws IOException {
        
    }

    @Test
    public void testWriteFile() throws IOException {
        
    }

}
