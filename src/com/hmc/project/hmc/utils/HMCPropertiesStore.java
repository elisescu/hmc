package com.hmc.project.hmc.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.bouncycastle2.util.encoders.Base64;

import net.java.otr4j.OtrKeyManagerStore;

class HMCPropertiesStore implements OtrKeyManagerStore {
    private final Properties properties = new Properties();
    private String filepath;

    public HMCPropertiesStore(String filepath) throws IOException {
        if (filepath == null || filepath.length() < 1)
            throw new IllegalArgumentException();
        this.filepath = filepath;
        properties.clear();

        InputStream in = new BufferedInputStream(new FileInputStream(getConfigurationFile()));
        try {
            properties.load(in);
        } finally {
            in.close();
        }
    }

    private File getConfigurationFile() throws IOException {
        File configFile = new File(filepath);
        if (!configFile.exists())
            configFile.createNewFile();
        return configFile;
    }

    public void setProperty(String id, boolean value) {
        properties.setProperty(id, "true");
        try {
            this.store();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void store() throws FileNotFoundException, IOException {
        OutputStream out = new FileOutputStream(getConfigurationFile());
        properties.store(out, null);
        out.close();
    }

    public void setProperty(String id, byte[] value) {
        properties.setProperty(id, new String(Base64.encode(value)));
        try {
            this.store();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeProperty(String id) {
        properties.remove(id);

    }

    public byte[] getPropertyBytes(String id) {
        String value = properties.getProperty(id);
        if (value == null)
            return null;
        return Base64.decode(value);
    }

    public boolean getPropertyBoolean(String id, boolean defaultValue) {
        try {
            return Boolean.valueOf(properties.get(id).toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
