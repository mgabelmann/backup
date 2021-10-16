package mgabelmann.photo.workflow.io;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class FileRecordCodecTest {
    private Collection<FileRecord> records;
    
    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.INFO);
    }

    @BeforeEach
    public void setUp() throws Exception {
        records = new ArrayList<>();
    }

    @AfterEach
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
