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

import java.sql.SQLException;
import java.util.logging.Logger;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

/**
* This class holds the JDBC connection for ORMlite.
* The JDBC URL is taken from the {@link Configuration} class.
* @author Sebastian Lederer <sebastian.lederer@dass-it.de>
*/

public class DbConnection {
	private static ConnectionSource connectionSource;

	public synchronized static ConnectionSource getConnectionSource() {
		if (connectionSource == null) {
			if (connectionSource == null) {
				try {
					String jdbcUrl = Configuration.getInstance().get("jdbcUrl");
					connectionSource = new JdbcConnectionSource(jdbcUrl);
				} catch (SQLException e) {
					Logger logger = Logger.getGlobal();
					logger.severe(e.getMessage());
					return null;
				}
			}
		}
		return connectionSource;
	}
}
