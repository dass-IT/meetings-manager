/*
 * Copyright 2022 dass IT GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.dassit.meetingsmanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * The Configuration class provides a singleton for reading
 * the main configuration file (in Java properties format).
 * 
 * @author Sebastian Lederer <sebastian.lederer@dass-it.de>
 *
 */
public class Configuration {
	private final static Configuration instance = new Configuration();
	private final static String configDir = "/var/lib/meetingsmanager";
	private final static String configFile = "config.properties";

	private Properties props = null;

	private List<String> resources = null;
	private List<String> rooms = null;

	public static Configuration getInstance() {
		return instance;
	}

	public Configuration() {
		readConfig();
	}

	private void readConfig() {
		File f = new File(configDir, configFile);
		FileReader r = null;
                Logger logger = Logger.getGlobal();

		try {
			r = new FileReader(f);
			props = new Properties();
			props.load(r);
		} catch (FileNotFoundException e) {
			logger.warning(e.getMessage());
		} catch (IOException e) {
			logger.warning(e.getMessage());
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (IOException e) {
					logger.warning(e.getMessage());
				}
			}
		}

		resources = getMultiValue("resources");
		rooms = getMultiValue("rooms");
	}

	public List<String> getResources() {
		return resources;
	}

	public List<String> getRooms() {
		return rooms;
	}

	public String getConfigDir() {
		return configDir;
	}

	public String get(String name) {
		return props.getProperty(name);
	}

	public List<String> getMultiValue(String name) {
		ArrayList<String> result = new ArrayList<String>();

		String value = props.getProperty(name);
		if (value != null) {
			String[] parts = value.split(",");

			for (String s : parts) {
				result.add(s.trim());
			}
		}
		return result;
	}

}
