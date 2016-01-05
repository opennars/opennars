/*
 * Copyright 2013 John Smith
 *
 * This file is part of Willow.
 *
 * Willow is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Willow is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Willow. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact details: http://jewelsea.wordpress.com
 */

package org.jewelsea.willow.helpers;

import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URL;

import static org.jewelsea.willow.util.ResourceUtil.getString;

public enum LocationHandler {
    ;

    @SuppressWarnings("HardcodedFileSeparator")
    public static void handleLocation(WebView view, String location) {
        // todo try the JavaFX based jpedalfx viewer instead...
        /*if (location.endsWith(".pdf")) {
            SwingUtilities.invokeLater(() -> {
                try {
                    final PDFViewer pdfViewer = new PDFViewer(false);
                    pdfViewer.openFile(new URL(location));
                } catch (Exception ex) {
                    // just fail to open a bad pdf url silently - no action required.
                }
            });
        }*/

        // todo I wonder how to find out from WebView which documents it could not process so that I could trigger a save as for them?
        String downloadableExtension = null;
        String[] downloadableExtensions = {".doc", ".xls", ".zip", ".tgz", ".jar"};
        for (String ext : downloadableExtensions) {
            if (location.endsWith(ext)) {
                downloadableExtension = ext;
                break;
            }
        }

        if (downloadableExtension != null) {
            // create a file save option for performing a download.
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save " + location);
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            getString("download-filechooser.title"),
                            downloadableExtension
                    )
            );

            int filenameIdx = location.lastIndexOf('/') + 1;
            if (filenameIdx != 0) {
                File saveFile = chooser.showSaveDialog(view.getScene().getWindow());

                if (saveFile != null) {
                    try (BufferedInputStream is = new BufferedInputStream(new URL(location).openStream());
                         BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(saveFile))) {
                        int b = is.read();
                        while (b != -1) {
                            os.write(b);
                            b = is.read();
                        }
                    } catch (IOException e) {
                        System.out.println("Unable to save file: " + e);
                    }
                }

                // todo shell the download out to a task, provide feedback on the save function and provide a download list and download list lookup.
            }
        }
    }
}
