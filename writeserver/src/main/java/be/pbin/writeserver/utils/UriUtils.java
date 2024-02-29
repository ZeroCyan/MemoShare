package be.pbin.writeserver.utils;

import java.net.URI;

public class UriUtils {

    /**
     * Return the last segment in the given URI.
     * E.g.: "www.website.com/api/get/lastSegment"
     */
    public static String extractLastSegment(URI uri) {
        String path = uri.getPath();
        String[] segments = path.split("/");
        return segments[segments.length - 1];
    }
}
