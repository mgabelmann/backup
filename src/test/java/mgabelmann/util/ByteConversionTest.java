package mgabelmann.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ByteConversionTest {
    
    
    @Before
    public void setUp() throws Exception {
        
    }

    @After
    public void tearDown() throws Exception {
        
    }

    @Test
    public void testCalculateBytes() {
        assertEquals(0.0, ByteConversion.BYTES.calculate(0), 0.1);
        assertEquals(1.0, ByteConversion.BYTES.calculate(ByteConversion.BYTES.getSize()), 0);
        assertEquals(1024.0, ByteConversion.BYTES.calculate(ByteConversion.KILOBYTES.getSize()), 0);
        assertEquals(1048576.0, ByteConversion.BYTES.calculate(ByteConversion.MEGABYTES.getSize()), 0);
        assertEquals(1073741824.0, ByteConversion.BYTES.calculate(ByteConversion.GIGABYTES.getSize()), 0);
    }
    
    @Test
    public void testCalculateKB() {
        assertEquals(0.00097, ByteConversion.KILOBYTES.calculate(ByteConversion.BYTES.getSize()), 0.00097);
        assertEquals(1.0, ByteConversion.KILOBYTES.calculate(ByteConversion.KILOBYTES.getSize()), 0);
        assertEquals(1024.0, ByteConversion.KILOBYTES.calculate(ByteConversion.MEGABYTES.getSize()), 0);
        assertEquals(1048576.0, ByteConversion.KILOBYTES.calculate(ByteConversion.GIGABYTES.getSize()), 0);
        assertEquals(1073741824.0, ByteConversion.KILOBYTES.calculate(ByteConversion.TERABYTES.getSize()), 0);
    }
    
    @Test
    public void testCalculateMB() {
        assertEquals(0.00000095, ByteConversion.MEGABYTES.calculate(ByteConversion.BYTES.getSize()), 0.00000095);
        assertEquals(0.00097, ByteConversion.MEGABYTES.calculate(ByteConversion.KILOBYTES.getSize()), 0.00097);
        assertEquals(1.0, ByteConversion.MEGABYTES.calculate(ByteConversion.MEGABYTES.getSize()), 0);
        assertEquals(1024.0, ByteConversion.MEGABYTES.calculate(ByteConversion.GIGABYTES.getSize()), 0);
        assertEquals(1048576, ByteConversion.MEGABYTES.calculate(ByteConversion.TERABYTES.getSize()), 0);
    }
    
    @Test
    public void testCalculateGB() {
        fail("todo");
    }
    
    @Test
    public void testCalculateTB() {
        fail("todo");
    }
    
    @Test
    public void testCalculatePB() {
        fail("todo");
    }

    @Test
    public void testFormatShortName() {
        assertEquals("1.0b", ByteConversion.BYTES.formatShortName(ByteConversion.BYTES.getSize()));
        assertEquals("1.0kB", ByteConversion.KILOBYTES.formatShortName(ByteConversion.KILOBYTES.getSize()));
        assertEquals("1.0MB", ByteConversion.MEGABYTES.formatShortName(ByteConversion.MEGABYTES.getSize()));
        assertEquals("1.0GB", ByteConversion.GIGABYTES.formatShortName(ByteConversion.GIGABYTES.getSize()));
        assertEquals("1.0TB", ByteConversion.TERABYTES.formatShortName(ByteConversion.TERABYTES.getSize()));
        assertEquals("1.0PB", ByteConversion.PETABYTES.formatShortName(ByteConversion.PETABYTES.getSize()));
    }

    @Test
    public void testFormatLongName() {
        assertEquals("1.0 Bytes", ByteConversion.BYTES.formatLongName(ByteConversion.BYTES.getSize()));
        assertEquals("1.0 Kilobytes", ByteConversion.KILOBYTES.formatLongName(ByteConversion.KILOBYTES.getSize()));
        assertEquals("1.0 Megabytes", ByteConversion.MEGABYTES.formatLongName(ByteConversion.MEGABYTES.getSize()));
        assertEquals("1.0 Gigabytes", ByteConversion.GIGABYTES.formatLongName(ByteConversion.GIGABYTES.getSize()));
        assertEquals("1.0 Terabytes", ByteConversion.TERABYTES.formatLongName(ByteConversion.TERABYTES.getSize()));
        assertEquals("1.0 Petabytes", ByteConversion.PETABYTES.formatLongName(ByteConversion.PETABYTES.getSize()));
    }

    @Test
    public void testFormat1() {
        assertEquals("1.0b", ByteConversion.format(1));
        assertEquals("1.0kB", ByteConversion.format(1024));
        assertEquals("1.0MB", ByteConversion.format(1048576));
        assertEquals("1.0GB", ByteConversion.format(1073741824));
    }
    
    @Test
    public void testFormat2() {
        assertEquals("8.1MB", ByteConversion.format(8493466));
        assertEquals("1.1GB", ByteConversion.format(1181116006));
    }
    
    @Test
    public void testFormat3() {
        assertEquals("8.1MB", ByteConversion.MEGABYTES.formatShortName(8493466));
    }
}
