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

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.jewelsea.willow.util.LruCache;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/* Helper class for handling favicons for sites */
public class FavIconHandler {
    /**
     * prefix used for the favicon fetching thread.
     */
    public static final String FAVICON_FETCH_THREAD_PREFIX = "favicon-fetcher-";
    /**
     * max number of threads we will use to simultaneously fetch favicons.
     */
    private static final int N_FETCH_THREADS = 4;
    private static FavIconHandler instance;
    /**
     * a threadpool for fetching favicons.
     */
    private final ExecutorService threadpool;
    /**
     * least recently used cache of favicons
     */
    private final Map<String, ImageView> faviconCache =
            new ConcurrentHashMap<>(
                    new LruCache<>(200)
            );

    /**
     * constructor.
     */
    public FavIconHandler() {
        // initialize the favicon threadpool to the specified number of threads.
        // the name of the threads are customized so they are easy to recognize.
        // the status of the threads are set to daemon, so that the application can
        // exit even if a favicon fetch is in progress or stalled.
        threadpool = Executors.newFixedThreadPool(N_FETCH_THREADS, new ThreadFactory() {
            ThreadFactory defaultFactory = Executors.defaultThreadFactory();

            @Override
            public Thread newThread(Runnable r) {
                Thread newThread = defaultFactory.newThread(r);
                newThread.setName(FAVICON_FETCH_THREAD_PREFIX + newThread.getName());
                newThread.setDaemon(true);
                return newThread;
            }
        });
    }

    /**
     * @return singleton instance
     */
    public static FavIconHandler getInstance() {
        if (instance == null) instance = new FavIconHandler();
        return instance;
    }

    /**
     * Fetch a favicon for a given location.
     *
     * @param browserLoc the location of a browser for which a favicon is to be fetched.
     * @return the favicon for the browser location or null if no such favicon could be determined.
     */
    public ImageView fetchFavIcon(String browserLoc) {
        // fetch the favicon from cache if it is there.
        String serverRoot = findRootLoc(browserLoc);
        
        
        ImageView cachedFavicon = faviconCache.get(serverRoot);
        if (cachedFavicon != null) return cachedFavicon;

        // ok, it wasn't in the cache, create a placeholder, to be used if the site doesn't have a favicon.
        ImageView favicon = new ImageView();

        // if the serverRoot of the location cannot be determined, just return the placeholder.
        if (serverRoot == null) return favicon;

        // store the new favicon placeholder in the cache.
        faviconCache.put(serverRoot, favicon);

        // lazily fetch the real favicon.
        Task<Image> task = new Task<Image>() {
            @SuppressWarnings("HardcodedFileSeparator")
            @Override
            protected Image call() throws Exception {
                // fetch the favicon from the server if we can.
                URL url = new URL(serverRoot + "/favicon.ico");

                // decode the favicon into an awt image.
                //List<BufferedImage> imgs = ICODecoder.read(url.openStream());

                // if the decoding was successful convert to a JavaFX image and return it.
                /*if (imgs.size() > 0) {
                    return ResourceUtil.bufferedImageToFXImage(imgs.get(0), 0, 16, true, true);
                } else */
                return null;
            }
        };

        // replace the placeholder in a favicon whenever the lazy fetch completes.
        task.valueProperty().addListener((observableValue, oldImage, newImage) -> {
            if (newImage != null) {
                favicon.setImage(newImage);
            }
        });

        threadpool.execute(task);

        return favicon;
    }

    /**
     * Determines the root location for a server.
     * For example http://www.yahoo.com/games => http://www.yahoo.com
     *
     * @param browserLoc the location string of a browser window.
     * @return the computed server root url or null if the browser location does not represent a server.
     */
    @SuppressWarnings("HardcodedFileSeparator")
    private String findRootLoc(String browserLoc) {
        int protocolSepLoc = browserLoc.indexOf("://");
        if (protocolSepLoc > 0) {
            // workout the location of the favicon.
            int pathSepLoc = browserLoc.indexOf('/', protocolSepLoc + 3);
            return (pathSepLoc > 0) ? browserLoc.substring(0, pathSepLoc) : browserLoc;
        }

        return "about:";
    }
}

// todo think about other favicon types such as pngs, and jpgs and how they may be processed as well as favicons which could be specified inside html.