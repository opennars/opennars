/**
 * Copyright (C) 2009 Future Invent Informationsmanagement GmbH. All rights
 * reserved. <http://www.fuin.org/>
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package objenome.util.bytecode;

import org.junit.Assert;

import java.io.*;


/**
 * Some assertion helper methods.
 */
public   enum SgAssert {
    ;

    private static void writeToFile(File file, String src) {
        try {
            // Write to file
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(src);
            }
//            // Format with Jalopy
//            final Jalopy jalopy = new Jalopy();
//            jalopy.setInput(file);
//            jalopy.setOutput(file);
//            jalopy.format();

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String readFromFile(File file) {
        try {
            try (LineNumberReader lnr = new LineNumberReader(new FileReader(file))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = lnr.readLine()) != null) {
                    sb.append(line);
                    sb.append('\n');
                }
                return sb.toString();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Assert the content of the two java source files is equal.
     * 
     * @param actualFile
     *            Actual file.
     * @param expectedFile
     *            Excpected file.
     */
    public static void assertSrcFilesEqual(File actualFile, File expectedFile) {
        String actualSrc = readFromFile(actualFile);
        String expectedSrc = readFromFile(expectedFile);
        Assert.assertEquals(actualSrc, expectedSrc);
    }

    /**
     * Assert the content of <code>clasz.toString()</code> is equal to the
     * content stored in a file. The name of the expected file is the full
     * qualified class name with the extension ".java". The name of the actual
     * file is the full qualified class name with the extension ".tmp"
     * 
     * @param baseDir
     *            Base directory where the expected source files are stored.
     * @param clasz
     *            Class to test.
     */
    public static void assertEqualToFile(File baseDir, SgClass clasz) {
        File expectedFile = new File(baseDir, clasz.getName() + ".java");
        File actualFile = new File(baseDir, clasz.getName() + ".tmp");
        writeToFile(actualFile, clasz.toString());
        assertSrcFilesEqual(actualFile, expectedFile);
    }

}
