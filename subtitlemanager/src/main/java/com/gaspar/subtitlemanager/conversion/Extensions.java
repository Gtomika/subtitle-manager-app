package com.gaspar.subtitlemanager.conversion;

/**
 * Stores the supported extensions.
 */
public class Extensions {

    public enum Extension {
        SRT("SRT (SubRip)", "srt"),
        VTT("VTT (WebVTT)", "vtt");
        //add more supported extension here

        /**
         * Used when interacting with the user.
         */
        private String name;

        /**
         * Used when navigating in the file system.
         */
        private String shortName;

        Extension(String name, String shortName) {
            this.shortName = shortName;
            this.name = name;
        }

        public String extensionName() {
            return name;
        }

        public String shortExtensionName() {
            return shortName;
        }
    }

    private static final String[] SUPPORTED_TYPES = createExtensionStrings();

    private static String[] createExtensionStrings() {
        String[] array = new String[Extension.values().length];
        int i=0;
        for(Extension e: Extension.values()) {
            array[i++] = e.extensionName();
        }
        return array;
    }

    /**
     * @return All supported file types.
     */
    public static String[] getSupportedExtensionNames() {
        return SUPPORTED_TYPES;
    }

    /**
     * Matches an extensions full name to its short name that can be used to navigate in the file system.
     */
    public static String findShortNameFor(String fullName) throws IllegalArgumentException {
        for(Extension extension: Extension.values()) {
            if(extension.extensionName().equals(fullName)) return extension.shortExtensionName();
        }
        throw new IllegalArgumentException();
    }

    /**
     * Matches a short extension name to the full one.
     */
    public static String findFullNameFor(String shortName) throws IllegalArgumentException {
        for(Extension extension: Extension.values()) {
            if(extension.shortExtensionName().equals(shortName)) return extension.extensionName();
        }
        throw new IllegalArgumentException();
    }
}
