package com.alecgorge.minecraft.jsonapi.util;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Used for accessing and creating .[properties] files, reads them as utf-8, saves as utf-8.
 * Internationalization is key importance especially for character codes.
 *
 * @author Nijikokun
 * @version 1.0.4, %G%
 */
public final class PropertiesFile {

    private static final Logger log = Logger.getLogger("Minecraft");
    private String fileName;
    private List<String> lines = new ArrayList<String>();
    private Map<String, String> props = new HashMap<String, String>();

    /**
     * Creates or opens a properties file using specified filename
     *
     * @param fileName
     */
    public PropertiesFile(String fileName) {
        this.fileName = fileName;
        File file = new File(fileName);

        if (file.exists()) {
            try {
                load();
            } catch (IOException ex) {
                log.severe("[PropertiesFile] Unable to load " + fileName + "!");
            }
        } else {
            save();
        }
    }

    /**
     * The loader for property files, it reads the file as UTF8 or converts the string into UTF8.
     * Used for simple runthrough's, loading, or reloading of the file.
     *
     * @throws IOException
     */
    public void load() throws IOException {
        BufferedReader reader;
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.fileName), "UTF8"));
        String line;

        // Clear the file & unwritten properties
        lines.clear();
        props.clear();

        // Begin reading the file.
        while ((line = reader.readLine()) != null) {
            line = new String(line.getBytes(), "UTF-8");
            char c = 0;
            int pos = 0;

            while (pos < line.length() && Character.isWhitespace(c = line.charAt(pos))) {
                pos++;
            }

            if ((line.length() - pos) == 0 || line.charAt(pos) == '#' || line.charAt(pos) == '!') {
                lines.add(line);
                continue;
            }

            int start = pos;
            boolean needsEscape = line.indexOf('\\', pos) != -1;
            StringBuffer key = needsEscape ? new StringBuffer() : null;

            while (pos < line.length() && !Character.isWhitespace(c = line.charAt(pos++)) && c != '=' && c != ':') {
                if (needsEscape && c == '\\') {
                    if (pos == line.length()) {
                        line = reader.readLine();

                        if (line == null) {
                            line = "";
                        }

                        pos = 0;

                        while (pos < line.length() && Character.isWhitespace(c = line.charAt(pos))) {
                            pos++;
                        }
                    } else {
                        c = line.charAt(pos++);

                        switch (c) {
                            case 'n':
                                key.append('\n');
                                break;
                            case 't':
                                key.append('\t');
                                break;
                            case 'r':
                                key.append('\r');
                                break;
                            case 'u':
                                if (pos + 4 <= line.length()) {
                                    char uni = (char) Integer.parseInt(line.substring(pos, pos + 4), 16);
                                    key.append(uni);
                                    pos += 4;
                                }

                                break;
                            default:
                                key.append(c);
                                break;
                        }
                    }
                } else if (needsEscape) {
                    key.append(c);
                }
            }

            boolean isDelim = (c == ':' || c == '=');
            String keyString;

            if (needsEscape) {
                keyString = key.toString();
            } else if (isDelim || Character.isWhitespace(c)) {
                keyString = line.substring(start, pos - 1);
            } else {
                keyString = line.substring(start, pos);
            }

            while (pos < line.length() && Character.isWhitespace(c = line.charAt(pos))) {
                pos++;
            }

            if (!isDelim && (c == ':' || c == '=')) {
                pos++;

                while (pos < line.length() && Character.isWhitespace(c = line.charAt(pos))) {
                    pos++;
                }
            }

            // Short-circuit if no escape chars found.
            if (!needsEscape) {
                lines.add(line);
                continue;
            }

            // Escape char found so iterate through the rest of the line.
            StringBuilder element = new StringBuilder(line.length() - pos);
            while (pos < line.length()) {
                c = line.charAt(pos++);
                if (c == '\\') {
                    if (pos == line.length()) {
                        line = reader.readLine();

                        if (line == null) {
                            break;
                        }

                        pos = 0;
                        while (pos < line.length() && Character.isWhitespace(c = line.charAt(pos))) {
                            pos++;
                        }
                        element.ensureCapacity(line.length() - pos + element.length());
                    } else {
                        c = line.charAt(pos++);
                        switch (c) {
                            case 'n':
                                element.append('\n');
                                break;
                            case 't':
                                element.append('\t');
                                break;
                            case 'r':
                                element.append('\r');
                                break;
                            case 'u':
                                if (pos + 4 <= line.length()) {
                                    char uni = (char) Integer.parseInt(line.substring(pos, pos + 4), 16);
                                    element.append(uni);
                                    pos += 4;
                                }
                                break;
                            default:
                                element.append(c);
                                break;
                        }
                    }
                } else {
                    element.append(c);
                }
            }
            lines.add(keyString + "=" + element.toString());
        }

        reader.close();
    }

    /**
     * Writes out the <code>key=value</code> properties that were changed into
     * a .[properties] file in UTF8.
     *
     * @see #load()
     */
    public void save() {
        OutputStream os = null;

        try {
            os = new FileOutputStream(this.fileName);
        } catch (FileNotFoundException ex) {
            log.severe("[PropertiesFile] Unable to open " + fileName + "!");
        }

        PrintStream ps = null;
        try {
            ps = new PrintStream(os, true, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            log.severe("[PropertiesFile] Unable to write to " + fileName + "!");
        }

        // Keep track of properties that were set
        List<String> usedProps = new ArrayList<String>();

        for (String line : this.lines) {
            if (line.trim().length() == 0) {
                ps.println(line);
                continue;
            }

            if (line.charAt(0) == '#') {
                ps.println(line);
                continue;
            }

            if (line.contains("=")) {
                int delimPosition = line.indexOf('=');
                String key = line.substring(0, delimPosition).trim();

                if (this.props.containsKey(key)) {
                    String value = this.props.get(key);
                    ps.println(key + "=" + value);
                    usedProps.add(key);
                } else {
                    ps.println(line);
                }
            } else {
                ps.println(line);
            }
        }

        // Add any new properties
        for (Map.Entry<String, String> entry : this.props.entrySet()) {
            if (!usedProps.contains(entry.getKey())) {
                ps.println(entry.getKey() + "=" + entry.getValue());
            }
        }

        // Exit that stream
        ps.close();

        // Reload
        try {
            props.clear();
            lines.clear();
            this.load();
        } catch (IOException ex) {
            log.severe("[PropertiesFile] Unable to load " + fileName + "!");
        }
    }

    /**
     * Returns a Map of all <code>key=value</code> properties in the file as <code>&lt;key (java.lang.String), value (java.lang.String)></code>
     * <br /><br />
     * Example:
     * <blockquote><pre>
     * PropertiesFile settings = new PropertiesFile("settings.properties");
     * Map<String, String> mappedSettings;
     * 
     * try {
     * 	 mappedSettings = settings.returnMap();
     * } catch (Exception ex) {
     * 	 log.info("Failed mapping settings.properties");
     * }
     * </pre></blockquote>
     *
     * @return <code>map</code> - Simple Map HashMap of the entire <code>key=value</code> as <code>&lt;key (java.lang.String), value (java.lang.String)></code>
     * @throws Exception If the properties file doesn't exist.
     */
    public Map<String, String> returnMap() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        BufferedReader reader = new BufferedReader(new FileReader(this.fileName));
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.trim().length() == 0) {
                continue;
            }

            if (line.charAt(0) == '#') {
                continue;
            }

            if (line.contains("=")) {
                int delimPosition = line.indexOf('=');
                String key = line.substring(0, delimPosition).trim();
                String value = line.substring(delimPosition + 1).trim();
                map.put(key, value);
            } else {
                continue;
            }
        }

        reader.close();
        return map;
    }

    /**
     * Checks to see if the .[properties] file contains the given <code>key</code>.
     *
     * @param var The key we are going to be checking the existance of.
     * @return <code>Boolean</code> - True if the <code>key</code> exists, false if it cannot be found.
     */
    public boolean containsKey(String var) {
        for (String line : this.lines) {
            if (line.trim().length() == 0) {
                continue;
            }

            if (line.charAt(0) == '#') {
                continue;
            }

            if (line.contains("=")) {
                int delimPosition = line.indexOf('=');
                String key = line.substring(0, delimPosition);

                if (key.equals(var)) {
                    return true;
                }
            } else {
                continue;
            }
        }

        return false;
    }

    /**
     * Checks to see if this <code>key</code> exists in the .[properties] file.
     *
     * @param var The key we are grabbing the value of.
     * @return <code>java.lang.String</code> - True if the <code>key</code> exists, false if it cannot be found.
     */
    public String getProperty(String var) {
        for (String line : this.lines) {
            if (line.trim().length() == 0) {
                continue;
            }
            if (line.charAt(0) == '#') {
                continue;
            }

            if (line.contains("=")) {
                int delimPosition = line.indexOf('=');
                String key = line.substring(0, delimPosition).trim();
                String value = line.substring(delimPosition + 1);

                if (key.equals(var)) {
                    return value;
                }
            } else {
                continue;
            }
        }

        return "";
    }

    /**
     * Remove a key from the file if it exists.
     * This will save() which will invoke a load() on the file.
     *
     * @see #save()
     * @param var The <code>key</code> that will be removed from the file
     */
    public void removeKey(String var) {
        Boolean changed = false;

        if (this.props.containsKey(var)) {
            this.props.remove(var);
            changed = true;
        }

        try {
            for (int i = 0; i < this.lines.size(); i++) {
                String line = this.lines.get(i);

                if (line.trim().length() == 0) {
                    continue;
                }

                if (line.charAt(0) == '#') {
                    continue;
                }

                if (line.contains("=")) {
                    int delimPosition = line.indexOf('=');
                    String key = line.substring(0, delimPosition).trim();

                    if (key.equals(var)) {
                        this.lines.remove(i);
                        changed = true;
                    }
                } else {
                    continue;
                }
            }
        } catch (ConcurrentModificationException concEx) {
            removeKey(var);
            return;
        }

        // Save on change
        if (changed) {
            save();
        }
    }

    /**
     * Checks the existance of a <code>key</code>.
     *
     * @see #containsKey(java.lang.String)
     * @param key The <code>key</code> in question of existance.
     * @return <code>Boolean</code> - True for existance, false for <code>key</code> found.
     */
    public boolean keyExists(String key) {
        try {
            return (this.containsKey(key)) ? true : false;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Returns the value of the <code>key</code> given as a <code>String</code>,
     * however we do not set a string if no <code>key</code> is found.
     *
     * @see #getProperty(java.lang.String)
     * @param key The <code>key</code> we will retrieve the property from, if no <code>key</code> is found default to "" or empty.
     */
    public String getString(String key) {
        if (this.containsKey(key)) {
            return this.getProperty(key);
        }

        return "";
    }

    /**
     * Returns the value of the <code>key</code> given as a <code>String</code>.
     * If it is not found, it will invoke saving the default <code>value</code> to the properties file.
     *
     * @see #setString(java.lang.String, java.lang.String)
     * @see #getProperty(java.lang.String)
     * @param key The key that we will be grabbing the value from, if no value is found set and return <code>value</code>
     * @param value The default value that we will be setting if no prior <code>key</code> is found.
     * @return java.lang.String Either we will return the default value or a prior existing value depending on existance.
     */
    public String getString(String key, String value) {
        if (this.containsKey(key)) {
            return this.getProperty(key);
        }

        setString(key, value);
        return value;
    }

    /**
     * Save the value given as a <code>String</code> on the specified key.
     *
     * @see #save()
     * @param key The <code>key</code> that we will be addressing the <code>value</code> to.
     * @param value The <code>value</code> we will be setting inside the <code>.[properties]</code> file.
     */
    public void setString(String key, String value) {
        props.put(key, value);

        save();
    }

    /**
     * Returns the value of the <code>key</code> given in a Integer,
     * however we do not set a string if no <code>key</code> is found.
     *
     * @see #getProperty(String var)
     * @param key The <code>key</code> we will retrieve the property from, if no <code>key</code> is found default to 0
     */
    public int getInt(String key) {
        if (this.containsKey(key)) {
            return Integer.parseInt(this.getProperty(key));
        }

        return 0;
    }

    /**
     * Returns the int value of a key
     *
     * @see #setInt(String key, int value)
     * @param key The key that we will be grabbing the value from, if no value is found set and return <code>value</code>
     * @param value The default value that we will be setting if no prior <code>key</code> is found.
     * @return <code>Integer</code> - Either we will return the default value or a prior existing value depending on existance.
     */
    public int getInt(String key, int value) {
        if (this.containsKey(key)) {
            return Integer.parseInt(this.getProperty(key));
        }

        setInt(key, value);
        return value;

    }

    /**
     * Save the value given as a <code>int</code> on the specified key.
     *
     * @see #save()
     * @param key The <code>key</code> that we will be addressing the <code>value</code> to.
     * @param value The <code>value</code> we will be setting inside the <code>.[properties]</code> file.
     */
    public void setInt(String key, int value) {
        props.put(key, String.valueOf(value));

        save();
    }

    /**
     * Returns the value of the <code>key</code> given in a Double,
     * however we do not set a string if no <code>key</code> is found.
     *
     * @see #getProperty(String var)
     * @param key The <code>key</code> we will retrieve the property from, if no <code>key</code> is found default to 0.0
     */
    public double getDouble(String key) {
        if (this.containsKey(key)) {
            return Double.parseDouble(this.getProperty(key));
        }

        return 0;
    }

    /**
     * Returns the double value of a key
     *
     * @see #setDouble(String key, double value)
     * @param key The key that we will be grabbing the value from, if no value is found set and return <code>value</code>
     * @param value The default value that we will be setting if no prior <code>key</code> is found.
     * @return <code>Double</code> - Either we will return the default value or a prior existing value depending on existance.
     */
    public double getDouble(String key, double value) {
        if (this.containsKey(key)) {
            return Double.parseDouble(this.getProperty(key));
        }

        setDouble(key, value);
        return value;
    }

    /**
     * Save the value given as a <code>double</code> on the specified key.
     *
     * @see #save()
     * @param key The <code>key</code> that we will be addressing the <code>value</code> to.
     * @param value The <code>value</code> we will be setting inside the <code>.[properties]</code> file.
     */
    public void setDouble(String key, double value) {
        props.put(key, String.valueOf(value));

        save();
    }

    /**
     * Returns the value of the <code>key</code> given in a Long,
     * however we do not set a string if no <code>key</code> is found.
     *
     * @see #getProperty(String var)
     * @param key The <code>key</code> we will retrieve the property from, if no <code>key</code> is found default to 0L
     */
    public long getLong(String key) {
        if (this.containsKey(key)) {
            return Long.parseLong(this.getProperty(key));
        }

        return 0;
    }

    /**
     * Returns the long value of a key
     *
     * @see #setLong(String key, long value)
     * @param key The key that we will be grabbing the value from, if no value is found set and return <code>value</code>
     * @param value The default value that we will be setting if no prior <code>key</code> is found.
     * @return <code>Long</code> - Either we will return the default value or a prior existing value depending on existance.
     */
    public long getLong(String key, long value) {
        if (this.containsKey(key)) {
            return Long.parseLong(this.getProperty(key));
        }

        setLong(key, value);
        return value;
    }

    /**
     * Save the value given as a <code>long</code> on the specified key.
     *
     * @see #save()
     * @param key The <code>key</code> that we will be addressing the <code>value</code> to.
     * @param value The <code>value</code> we will be setting inside the <code>.[properties]</code> file.
     */
    public void setLong(String key, long value) {
        props.put(key, String.valueOf(value));

        save();
    }

    /**
     * Returns the value of the <code>key</code> given in a Boolean,
     * however we do not set a string if no <code>key</code> is found.
     *
     * @see #getProperty(String var)
     * @param key The <code>key</code> we will retrieve the property from, if no <code>key</code> is found default to false
     */
    public boolean getBoolean(String key) {
        if (this.containsKey(key)) {
            return Boolean.parseBoolean(this.getProperty(key));
        }

        return false;
    }

    /**
     * Returns the boolean value of a key
     *
     * @see #setBoolean(String key, boolean value)
     * @param key The key that we will be grabbing the value from, if no value is found set and return <code>value</code>
     * @param value The default value that we will be setting if no prior <code>key</code> is found.
     * @return <code>Boolean</code> - Either we will return the default value or a prior existing value depending on existance.
     */
    public boolean getBoolean(String key, boolean value) {
        if (this.containsKey(key)) {
            return Boolean.parseBoolean(this.getProperty(key));
        }

        setBoolean(key, value);
        return value;
    }

    /**
     * Save the value given as a <code>boolean</code> on the specified key.
     *
     * @see #save()
     * @param key The <code>key</code> that we will be addressing the <code>value</code> to.
     * @param value The <code>value</code> we will be setting inside the <code>.[properties]</code> file.
     */
    public void setBoolean(String key, boolean value) {
        props.put(key, String.valueOf(value));

        save();
    }
}
