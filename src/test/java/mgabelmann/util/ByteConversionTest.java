package mgabelmann.util;

import org.junit.jupiter.api.*;


public class ByteConversionTest {

    @Test
    public void testCalculateBytes() {
        Assertions.assertEquals(0.0, ByteConversion.BYTES.calculate(0), 0.1);
        Assertions.assertEquals(1.0, ByteConversion.BYTES.calculate(ByteConversion.BYTES.getSize()), 0);
        Assertions.assertEquals(1024.0, ByteConversion.BYTES.calculate(ByteConversion.KILOBYTES.getSize()), 0);
        Assertions.assertEquals(1048576.0, ByteConversion.BYTES.calculate(ByteConversion.MEGABYTES.getSize()), 0);
        Assertions.assertEquals(1073741824.0, ByteConversion.BYTES.calculate(ByteConversion.GIGABYTES.getSize()), 0);
    }
    
    @Test
    public void testCalculateKB() {
        Assertions.assertEquals(0.00097, ByteConversion.KILOBYTES.calculate(ByteConversion.BYTES.getSize()), 0.00097);
        Assertions.assertEquals(1.0, ByteConversion.KILOBYTES.calculate(ByteConversion.KILOBYTES.getSize()), 0);
        Assertions.assertEquals(1024.0, ByteConversion.KILOBYTES.calculate(ByteConversion.MEGABYTES.getSize()), 0);
        Assertions.assertEquals(1048576.0, ByteConversion.KILOBYTES.calculate(ByteConversion.GIGABYTES.getSize()), 0);
        Assertions.assertEquals(1073741824.0, ByteConversion.KILOBYTES.calculate(ByteConversion.TERABYTES.getSize()), 0);
    }
    
    @Test
    public void testCalculateMB() {
        Assertions.assertEquals(0.00000095, ByteConversion.MEGABYTES.calculate(ByteConversion.BYTES.getSize()), 0.00000095);
        Assertions.assertEquals(0.00097, ByteConversion.MEGABYTES.calculate(ByteConversion.KILOBYTES.getSize()), 0.00097);
        Assertions.assertEquals(1.0, ByteConversion.MEGABYTES.calculate(ByteConversion.MEGABYTES.getSize()), 0);
        Assertions.assertEquals(1024.0, ByteConversion.MEGABYTES.calculate(ByteConversion.GIGABYTES.getSize()), 0);
        Assertions.assertEquals(1048576, ByteConversion.MEGABYTES.calculate(ByteConversion.TERABYTES.getSize()), 0);
    }

    @Disabled("implement me")
    @Test
    public void testCalculateGB() {

    }

    @Disabled("implement me")
    @Test
    public void testCalculateTB() {

    }

    @Disabled("implement me")
    @Test
    public void testCalculatePB() {

    }

    @Test
    public void testFormatShortName() {
        Assertions.assertEquals("1.0b", ByteConversion.BYTES.formatShortName(ByteConversion.BYTES.getSize()));
        Assertions.assertEquals("1.0kB", ByteConversion.KILOBYTES.formatShortName(ByteConversion.KILOBYTES.getSize()));
        Assertions.assertEquals("1.0MB", ByteConversion.MEGABYTES.formatShortName(ByteConversion.MEGABYTES.getSize()));
        Assertions.assertEquals("1.0GB", ByteConversion.GIGABYTES.formatShortName(ByteConversion.GIGABYTES.getSize()));
        Assertions.assertEquals("1.0TB", ByteConversion.TERABYTES.formatShortName(ByteConversion.TERABYTES.getSize()));
        Assertions.assertEquals("1.0PB", ByteConversion.PETABYTES.formatShortName(ByteConversion.PETABYTES.getSize()));
    }

    @Test
    public void testFormatLongName() {
        Assertions.assertEquals("1.0 Bytes", ByteConversion.BYTES.formatLongName(ByteConversion.BYTES.getSize()));
        Assertions.assertEquals("1.0 Kilobytes", ByteConversion.KILOBYTES.formatLongName(ByteConversion.KILOBYTES.getSize()));
        Assertions.assertEquals("1.0 Megabytes", ByteConversion.MEGABYTES.formatLongName(ByteConversion.MEGABYTES.getSize()));
        Assertions.assertEquals("1.0 Gigabytes", ByteConversion.GIGABYTES.formatLongName(ByteConversion.GIGABYTES.getSize()));
        Assertions.assertEquals("1.0 Terabytes", ByteConversion.TERABYTES.formatLongName(ByteConversion.TERABYTES.getSize()));
        Assertions.assertEquals("1.0 Petabytes", ByteConversion.PETABYTES.formatLongName(ByteConversion.PETABYTES.getSize()));
    }

    @Test
    public void testFormat1() {
        Assertions.assertEquals("1.0b", ByteConversion.format(1));
        Assertions.assertEquals("1.0kB", ByteConversion.format(1024));
        Assertions.assertEquals("1.0MB", ByteConversion.format(1024 * 1024));
        Assertions.assertEquals("1.0GB", ByteConversion.format(1024 * 1024 * 1024));
    }
    
    @Test
    public void testFormat2() {
        Assertions.assertEquals("8.1MB", ByteConversion.format(8493466));
        Assertions.assertEquals("1.1GB", ByteConversion.format(1181116006));
    }
    
    @Test
    public void testFormat3() {
        Assertions.assertEquals("8.1MB", ByteConversion.MEGABYTES.formatShortName(8493466));
    }
}
