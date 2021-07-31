package mgabelmann.util;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileUtilTest {
    
    
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetFileExtensionFile1() {
        File file = new File("FileUtilTest.class");
        assertEquals("class", FileUtil.getFileExtension(file));
    }
    
    @Test
    public void testGetFileExtensionFile2() {
        File file = null;
        assertEquals("", FileUtil.getFileExtension(file));
    }
    
    @Test
    public void testGetFileExtensionFile3() {
        File file = new File("C:/pathtofile/Filewithnoextension");
        assertEquals("", FileUtil.getFileExtension(file));
    }
    

    @Test
    public void testGetFileExtensionString1() {
        assertEquals("class", FileUtil.getFileExtension("FileUtilTest.class"));
    }
    
    @Test
    public void testGetFileExtensionString2() {
        assertEquals("", FileUtil.getFileExtension(""));
    }
    
    @Test
    public void testGetFileExtensionString3() {
        assertEquals("", FileUtil.getFileExtension("C:/pathtofile/Filewithnoextension"));
    }
    
    @Test
    public void testGetFileExtensionString4() {
        String s = null;
        assertEquals("", FileUtil.getFileExtension(s));
    }

}
