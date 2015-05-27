/*
 *
 *
 * Copyright (C) 2015 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */
package org.sipfoundry.sipxcallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.sipfoundry.commons.freeswitch.FreeSwitchConfigurationInterface;
import org.sipfoundry.commons.log4j.SipFoundryLayout;

/**
 * Holds the configuration data needed for sipXcallback.
 *
 */
public class CallbackConfiguration implements FreeSwitchConfigurationInterface {

    private static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxcallback");
    private String m_logFile; // The file to log into
    private int m_eventSocketPort; // The Event Socket Listen port
    private String m_dataDirectory; // File path to the media server data directory
    private String m_promptsDirectory; // File path to the AA prompts
    private String m_docDirectory; // File path to DOC Directory (usually /usr/share/www/doc)
    private String m_sipxchangeDomainName; // The domain name of this system
    private String m_realm;


    private static CallbackConfiguration s_current;
    private static File s_propertiesFile;
    private static long s_lastModified;

    private CallbackConfiguration() {
    }

    public static CallbackConfiguration get() {
        return update(true);
    }

    public static CallbackConfiguration getTest() {
        return update(false);
    }

    /**
     * Load new Configuration object if the underlying properties files have changed since the last
     * time
     *
     * @return
     */
    private static CallbackConfiguration update(boolean load) {
        if (s_current == null || s_propertiesFile != null && s_propertiesFile.lastModified() != s_lastModified) {
            s_current = new CallbackConfiguration();
            if (load) {
                s_current.properties();
            }
        }
        return s_current;
    }

    void properties() {
        // Configure log4j
        String path = System.getProperty("conf.dir");
        PropertyConfigurator.configureAndWatch(path+"/sipxcallback/log4j.properties",
                SipFoundryLayout.LOG4J_MONITOR_FILE_DELAY);

        if (path == null) {
            System.err.println("Cannot get System Property conf.dir!  Check jvm argument -Dconf.dir=");
            System.exit(1);
        }

        // Setup SSL properties so we can talk to HTTPS servers
        String keyStore = System.getProperty("javax.net.ssl.keyStore");
        if (keyStore == null) {
            // Take an educated guess as to where it should be
            keyStore = path+"/ssl/ssl.keystore";
            System.setProperty("javax.net.ssl.keyStore", keyStore);
            System.setProperty("javax.net.ssl.keyStorePassword", "changeit"); // Real security!
        }
        String trustStore = System.getProperty("javax.net.ssl.trustStore");
        if (trustStore == null) {
            // Take an educated guess as to where it should be
            trustStore = path+"/ssl/authorities.jks";
            System.setProperty("javax.net.ssl.trustStore", trustStore);
            System.setProperty("javax.net.ssl.trustStoreType", "JKS");
            System.setProperty("javax.net.ssl.trustStorePassword", "changeit"); // Real security!
        }

        String name = "sipxcallback.properties";
        FileInputStream inStream;
        Properties props = null;
        try {
            s_propertiesFile = new File(path + "/" + name);
            s_lastModified = s_propertiesFile.lastModified();
            inStream = new FileInputStream(s_propertiesFile);
            props = new Properties();
            props.load(inStream);
            inStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace(System.err);
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }

        String prop = null;
        try {
            m_logFile = props.getProperty(prop = "log.file");
            m_eventSocketPort = Integer.parseInt(props
                    .getProperty(prop = "freeswitch.eventSocketPort"));
            m_dataDirectory = props.getProperty(prop = "callback.dataDirectory");

        } catch (Exception e) {
            System.err.println("Problem understanding property " + prop);
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    public String getLogLevel() {
        return SipFoundryLayout.getSipFoundryLogLevel(this.getClass()).toString();
    }

    public String getLogFile() {
        return m_logFile;
    }

    public String getConfigFile() {
        return s_propertiesFile.getPath();
    }

    public int getEventSocketPort() {
        return m_eventSocketPort;
    }

    public void setEventSocketPort(int port) {
        m_eventSocketPort = port;
    }

    public String getDataDirectory() {
        return m_dataDirectory;
    }

    public String getPromptsDirectory() {
        return m_promptsDirectory;
    }

    public String getDocDirectory() {
        return m_docDirectory;
    }

    public String getSipxchangeDomainName() {
        return m_sipxchangeDomainName;
    }

    public String getRealm() {
        return m_realm;
    }

    public void setRealm(String realm) {
        m_realm = realm;
    }

	@Override
	public Logger getLogger() {
		return LOG;
	}
}
