/*
 * Copyright (C) 2014 me
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nars.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import static nars.web.HTTPServer.HTTP_FORBIDDEN;
import static nars.web.HTTPServer.HTTP_INTERNALERROR;
import static nars.web.HTTPServer.HTTP_NOTFOUND;
import static nars.web.HTTPServer.HTTP_OK;
import static nars.web.HTTPServer.HTTP_REDIRECT;
import static nars.web.HTTPServer.MIME_DEFAULT_BINARY;
import static nars.web.HTTPServer.MIME_HTML;
import static nars.web.HTTPServer.MIME_PLAINTEXT;

/**
 *
 * @author me
 */
public class HTTPServeFiles extends HTTPServer {

    File staticFilePath;
    
    public HTTPServeFiles(int port, File staticFilePath) throws IOException {
        super(port);
        this.staticFilePath = staticFilePath;
    }

    
    @Override
    public Response serve(String uri, String method, Properties header, Properties parms) {
         return serveFile(uri, header, staticFilePath, true);
    }
    
  // 
    // File server code
    // 
    /**
     * Serves file from homeDir and its' subdirectories (only). Uses only URI,
     * ignores all headers and HTTP parameters.
     */
    public Response serveFile(String uri, Properties header, File homeDir,
            boolean allowDirectoryListing) {
        // Make sure we won't die of an exception later
        if (!homeDir.isDirectory()) {
            return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT,
                    "INTERNAL ERRROR: serveFile(): given homeDir is not a directory.");
        }

        // Remove URL arguments
        uri = uri.trim().replace(File.separatorChar, '/');
        if (uri.indexOf('?') >= 0) {
            uri = uri.substring(0, uri.indexOf('?'));
        }

        // Prohibit getting out of current directory
        if (uri.startsWith("..") || uri.endsWith("..") || uri.contains("../")) {
            return new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT,
                    "FORBIDDEN: Won't serve ../ for security reasons.");
        }

        File f = new File(homeDir, uri);
        if (!f.exists()) {
            return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "Error 404, file not found.");
        }

        // List the directory, if necessary
        if (f.isDirectory()) {
      // Browsers get confused without '/' after the
            // directory, send a redirect.
            if (!uri.endsWith("/")) {
                uri += "/";
                Response r = new Response(HTTP_REDIRECT, MIME_HTML, "<html><body>Redirected: <a href=\""
                        + uri + "\">" + uri + "</a></body></html>");
                r.addHeader("Location", uri);
                return r;
            }

            // First try index.html and index.htm
            if (new File(f, "index.html").exists()) {
                f = new File(homeDir, uri + "/index.html");
            } else if (new File(f, "index.htm").exists()) {
                f = new File(homeDir, uri + "/index.htm");
            } // No index file, list the directory
            else if (allowDirectoryListing) {
                String[] files = f.list();
                String msg = "<html><body><h1>Directory " + uri + "</h1><br/>";

                if (uri.length() > 1) {
                    String u = uri.substring(0, uri.length() - 1);
                    int slash = u.lastIndexOf('/');
                    if (slash >= 0 && slash < u.length()) {
                        msg += "<b><a href=\"" + uri.substring(0, slash + 1) + "\">..</a></b><br/>";
                    }
                }

                for (int i = 0; i < files.length; ++i) {
                    File curFile = new File(f, files[i]);
                    boolean dir = curFile.isDirectory();
                    if (dir) {
                        msg += "<b>";
                        files[i] += "/";
                    }

                    try {
                        msg += "<a href=\"" + encodeUri(uri + files[i]) + "\">" + files[i] + "</a>";
                    } catch (UnsupportedEncodingException ex) {
                        msg += "<a>" + files[i] + "</a>";
                    }

                    // Show file size
                    if (curFile.isFile()) {
                        long len = curFile.length();
                        msg += " &nbsp;<font size=2>(";
                        if (len < 1024) {
                            msg += curFile.length() + " bytes";
                        } else if (len < 1024 * 1024) {
                            msg += curFile.length() / 1024 + "." + (curFile.length() % 1024 / 10 % 100) + " KB";
                        } else {
                            msg += curFile.length() / (1024 * 1024) + "." + curFile.length() % (1024 * 1024) / 10
                                    % 100 + " MB";
                        }

                        msg += ")</font>";
                    }
                    msg += "<br/>";
                    if (dir) {
                        msg += "</b>";
                    }
                }
                return new Response(HTTP_OK, MIME_HTML, msg);
            } else {
                return new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT, "FORBIDDEN: No directory listing.");
            }
        }

        try {
            // Get MIME type from file name extension, if possible
            String mime = null;
            int dot = f.getCanonicalPath().lastIndexOf('.');
            if (dot >= 0) {
                mime = (String) theMimeTypes.get(f.getCanonicalPath().substring(dot + 1).toLowerCase());
            }
            if (mime == null) {
                mime = MIME_DEFAULT_BINARY;
            }

            // Support (simple) skipping:
            long startFrom = 0;
            String range = header.getProperty("Range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    if (minus > 0) {
                        range = range.substring(0, minus);
                    }
                    try {
                        startFrom = Long.parseLong(range);
                    } catch (NumberFormatException nfe) {
                    }
                }
            }

            FileInputStream fis = new FileInputStream(f);
            fis.skip(startFrom);
            Response r = new Response(HTTP_OK, mime, fis);
            r.addHeader("Content-length", "" + (f.length() - startFrom));
            r.addHeader("Content-range", "" + startFrom + "-" + (f.length() - 1) + "/" + f.length());
            return r;
        } catch (IOException ioe) {
            return new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
        }
    }
    
    
    
}
