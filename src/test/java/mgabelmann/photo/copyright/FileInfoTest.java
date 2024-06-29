package mgabelmann.photo.copyright;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;


class FileInfoTest {
    private FileInfo fileInfo1;
    private FileInfo fileInfo2;
    private FileInfo fileInfo3;
    private FileInfo fileInfo4;
    private FileInfo fileInfo5;
    private FileInfo fileInfo6;

    @BeforeEach
    void setUp() {
        fileInfo1 = new FileInfo("1.jpg", "f", LocalDateTime.of(2024, Month.JANUARY, 15, 13, 50, 0));
        fileInfo2 = new FileInfo("2.jpg", "e", LocalDateTime.of(2024, Month.JANUARY, 15, 13, 49, 0));
        fileInfo3 = new FileInfo("3.jpg", "d", LocalDateTime.of(2024, Month.JANUARY, 15, 13, 48, 0));
        fileInfo4 = new FileInfo("4.jpg", "c", LocalDateTime.of(2024, Month.JANUARY, 14, 13, 47, 0));
        fileInfo5 = new FileInfo("5.jpg", "b", LocalDateTime.of(2024, Month.JANUARY, 14, 13, 46, 0));
        fileInfo6 = new FileInfo("6.jpg", "a", LocalDateTime.of(2024, Month.JANUARY, 14, 13, 45, 0));
    }

    @Test
    void getFileName() {
    }

    @Test
    void getTitle() {
    }

    @Test
    void getDateTime() {
    }

    @Test
    void getDate() {
    }

    @Test
    void testToString() {
    }

    @Test
    void test1_compareTo() {
        FileInfo[] fileInfos = new FileInfo[] {fileInfo1, fileInfo2, fileInfo3};
        Arrays.sort(fileInfos);

        Assertions.assertEquals(fileInfo3, fileInfos[0]);
        Assertions.assertEquals(fileInfo2, fileInfos[1]);
        Assertions.assertEquals(fileInfo1, fileInfos[2]);
    }

    @Test
    void test2_compareTo() {
        FileInfo[] fileInfos = new FileInfo[] {fileInfo1, fileInfo2, fileInfo3, fileInfo4, fileInfo5, fileInfo6};
        Arrays.sort(fileInfos);

        Assertions.assertEquals(fileInfo6, fileInfos[0]);
        Assertions.assertEquals(fileInfo5, fileInfos[1]);
        Assertions.assertEquals(fileInfo4, fileInfos[2]);
        Assertions.assertEquals(fileInfo3, fileInfos[3]);
        Assertions.assertEquals(fileInfo2, fileInfos[4]);
        Assertions.assertEquals(fileInfo1, fileInfos[5]);
    }

    @Test
    @DisplayName("no title, sort by date and filename")
    void test3_compareTo() {
        FileInfo fileInfo10 = new FileInfo("3.jpg", "", LocalDateTime.of(2024, Month.JANUARY, 15, 13, 50, 0));
        FileInfo fileInfo11 = new FileInfo("1.jpg", "", LocalDateTime.of(2024, Month.JANUARY, 15, 13, 50, 0));
        FileInfo fileInfo12 = new FileInfo("2.jpg", "", LocalDateTime.of(2024, Month.JANUARY, 15, 13, 50, 0));

        FileInfo[] fileInfos = new FileInfo[] {fileInfo10, fileInfo11, fileInfo12};
        Arrays.sort(fileInfos);

        Assertions.assertEquals(fileInfo10, fileInfos[2]);
        Assertions.assertEquals(fileInfo11, fileInfos[0]);
        Assertions.assertEquals(fileInfo12, fileInfos[1]);
    }

    @Test
    @DisplayName("mixed use of title and name")
    void test4_compareTo() {
        FileInfo fileInfo10 = new FileInfo("3.jpg", "a", LocalDateTime.of(2024, Month.JANUARY, 15, 13, 50, 0));
        FileInfo fileInfo11 = new FileInfo("1.jpg", "", LocalDateTime.of(2024, Month.JANUARY, 15, 13, 50, 0));
        FileInfo fileInfo12 = new FileInfo("2.jpg", "d", LocalDateTime.of(2024, Month.JANUARY, 15, 13, 50, 0));

        FileInfo[] fileInfos = new FileInfo[] {fileInfo10, fileInfo11, fileInfo12};
        Arrays.sort(fileInfos);

        Assertions.assertEquals(fileInfo10, fileInfos[1]);
        Assertions.assertEquals(fileInfo11, fileInfos[0]);
        Assertions.assertEquals(fileInfo12, fileInfos[2]);
    }

}