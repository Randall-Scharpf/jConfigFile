package com.randallscharpf.java.jconfigfile;

/**
 * Places on a file system where a configuration may be located.
 */
public enum ConfigLocation {

    /**
     * The same directory as the application.
     * 
     * For example, an app located at {@code /bin/cool_app/cool_app.jar} might
     * have its configuration located at {@code /bin/cool_app/cool_app.ini}
     * if using this type of configuration location.
     */
    SIBLING,

    /**
     * The etc folder in the same directory as the application.
     * 
     * For example, an app located at {@code /bin/cool_app/cool_app.jar} might have
     * its configuration located at {@code /bin/cool_app/etc/cool_app.ini} if
     * using this type of configuration location.
     */
    ETC,

    /**
     * An application directory in the user's documents folder.
     * 
     * For example, an app located at {@code /bin/cool_app/cool_app.jar} might have
     * its configuration located at {@code /usr/user_name/documents/cool_app/cool_app.ini}
     * if using this type of configuration location.
     */
    DOCUMENTS,

    /**
     * A dot-prefixed application directory in the user's home folder.
     * 
     * For example, an app located at {@code /bin/cool_app/cool_app.jar} might have
     * its configuration located at {@code /usr/user_name/.cool_app/cool_app.ini}
     * if using this type of configuration location.
     */
    USERPROFILE,

    /**
     * An application directory in the user's app data folder.
     * 
     * For example, an app located at {@code /bin/cool_app/cool_app.jar} might have
     * its configuration located at {@code /opt/cool_app/cool_app.ini} if using this
     * type of configuration location.
     */
    APPDATA
}
