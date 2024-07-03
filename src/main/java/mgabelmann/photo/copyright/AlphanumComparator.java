package mgabelmann.photo.copyright;

import java.util.Comparator;

/**
 * Copyright © 2021 Dave Koelle www.davekoelle.com/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * NOTE: updated to use my custom object FileInfo instead of String. This class will
 *       sort strings properly instead of charactar point (Java default).
 *
 *       eg: 10, 215, 0, 1, 2, 3
 *           becomes:    0, 1, 2, 3, 10, 215
 *           instead of: 0, 1, 10, 2, 215, 3
 */
public class AlphanumComparator implements Comparator<FileInfo> {
    private boolean isDigit(char ch) {
        return ((ch >= 48) && (ch <= 57));
    }

    /**
     * Length of string is passed in for improved efficiency (only need to calculate it once)
     **/
    private String getChunk(String s, int slength, int marker) {
        StringBuilder chunk = new StringBuilder();
        char c = s.charAt(marker);
        chunk.append(c);
        marker++;
        if (isDigit(c)) {
            while (marker < slength) {
                c = s.charAt(marker);
                if (!isDigit(c)) {
                    break;
                }
                chunk.append(c);
                marker++;
            }
        } else {
            while (marker < slength) {
                c = s.charAt(marker);
                if (isDigit(c)) {
                    break;
                }
                chunk.append(c);
                marker++;
            }
        }
        return chunk.toString();
    }

    /**
     * Compare two objects.
     * @param f1 the first object to be compared.
     * @param f2 the second object to be compared.
     * @return value
     */
    public int compare(FileInfo f1, FileInfo f2) {
        String s1 = f1.getName();
        String s2 = f2.getName();

        if ((s1 == null) || (s2 == null)) {
            return 0;
        }

        int thisMarker = 0;
        int thatMarker = 0;
        int s1Length = s1.length();
        int s2Length = s2.length();

        while (thisMarker < s1Length && thatMarker < s2Length) {
            String thisChunk = getChunk(s1, s1Length, thisMarker);
            thisMarker += thisChunk.length();

            String thatChunk = getChunk(s2, s2Length, thatMarker);
            thatMarker += thatChunk.length();

            // If both chunks contain numeric characters, sort them numerically
            int result = 0;
            if (isDigit(thisChunk.charAt(0)) && isDigit(thatChunk.charAt(0))) {
                // Simple chunk comparison by length.
                int thisChunkLength = thisChunk.length();
                result = thisChunkLength - thatChunk.length();
                // If equal, the first different number counts
                if (result == 0) {
                    for (int i = 0; i < thisChunkLength; i++) {
                        result = thisChunk.charAt(i) - thatChunk.charAt(i);
                        if (result != 0) {
                            return result;
                        }
                    }
                }
            } else {
                result = thisChunk.compareTo(thatChunk);
            }

            if (result != 0) {
                return result;
            }
        }

        return s1Length - s2Length;
    }
}
