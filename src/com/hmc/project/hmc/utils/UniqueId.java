package com.hmc.project.hmc.utils;

public class UniqueId {
    static int mUniqueIncId = 0;

    public static int getUniqueId() {
        mUniqueIncId++;
        return mUniqueIncId;
    }

}
