package de.jensnistler.routemap.helper;

import java.io.File;
import java.io.FilenameFilter;

public class FilenameFilterRoutes implements FilenameFilter {
    public boolean accept(File dir, String filename) {
        String path = dir.getAbsolutePath().toLowerCase() + "/" + filename.toLowerCase();

        // skip google docs cache
        CharSequence googleApps = "com.google.android.apps.docs";
        if (path.contains(googleApps)) {
            return false;
        }

        // skip my own cache
        CharSequence routeMap = "de.jensnistler.routemap";
        if (path.contains(routeMap)) {
            return false;
        }

        // check for directory
        File checkForDirectory = new File(path);
        if (checkForDirectory.isDirectory() && checkForDirectory.exists()) {
            return true;
        }

        // skip files that aren't routes
        int dotPosition = path.lastIndexOf(".");
        String fileExtension = path.substring(dotPosition + 1, path.length());
        if (!fileExtension.equals("gpx")) {
            return false;
        }

        return true;
    }
}
