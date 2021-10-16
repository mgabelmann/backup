package mgabelmann.util;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

public class FileUtilTest {
    
    
    @BeforeEach
    public void setUp() throws Exception {

    }

    @AfterEach
    public void tearDown() throws Exception {

    }

    @Test
    public void testGetFileExtensionFile1() {
        File file = new File("FileUtilTest.class");
        Assertions.assertEquals("class", FileUtil.getFileExtension(file));
    }
    
    @Test
    public void testGetFileExtensionFile2() {
        File file = null;
        Assertions.assertEquals("", FileUtil.getFileExtension(file));
    }
    
    @Test
    public void testGetFileExtensionFile3() {
        File file = new File("C:/pathtofile/Filewithnoextension");
        Assertions.assertEquals("", FileUtil.getFileExtension(file));
    }
    

    @Test
    public void testGetFileExtensionString1() {
        Assertions.assertEquals("class", FileUtil.getFileExtension("FileUtilTest.class"));
    }
    
    @Test
    public void testGetFileExtensionString2() {
        Assertions.assertEquals("", FileUtil.getFileExtension(""));
    }
    
    @Test
    public void testGetFileExtensionString3() {
        Assertions.assertEquals("", FileUtil.getFileExtension("C:/pathtofile/Filewithnoextension"));
    }
    
    @Test
    public void testGetFileExtensionString4() {
        String s = null;
        Assertions.assertEquals("", FileUtil.getFileExtension(s));
    }

}
