package com.hmc.project.hmc.utils;

// TODO: Auto-generated Javadoc
/**
 * The Class UniqueId.
 */
public class UniqueId {

    /** The m unique inc id. */
    static int mUniqueIncId = 0;

    /**
     * Gets the unique id.
     *
     * @return the unique id
     */
    public static int getUniqueId() {
        mUniqueIncId++;
        return mUniqueIncId;
    }

}
