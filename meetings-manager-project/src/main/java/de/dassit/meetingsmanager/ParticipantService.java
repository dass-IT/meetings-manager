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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.table.TableUtils;

/**
 * ParticipantService is a singleton class used to get a list
 * of all possible meeting participants
 * (NOT participants of a specific meeting).
 * @author Sebastian Lederer <sebastian.lederer@dass-it.de>
 */
public class ParticipantService {

	private static ParticipantService instance;
	private static final Logger LOGGER = Logger.getLogger(ParticipantService.class.getName());

	private HashMap<Integer, Participant> contacts = new HashMap<>();

	private Dao<Participant, Integer> dao;

	private ParticipantService() {
		try {
			dao = DaoManager.createDao(DbConnection.getConnectionSource(), Participant.class);
			TableUtils.createTableIfNotExists(dao.getConnectionSource(), Participant.class);
		} catch (SQLException e) {
			e.printStackTrace();
			dao = null;
		}
	}

	public static ParticipantService getInstance() {
		if (instance == null) {
			instance = new ParticipantService();
		}
		return instance;
	}

	public void loadFromDb() {
		List<Participant> result = null;

		try {
			// result = dao.queryForEq("external", false);
			result = dao.queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}

		contacts = new HashMap<Integer, Participant>();

		for (Participant t : result) {
			contacts.put(t.getId(), t);
		}
	}

	/**
	 * @return all available Participant objects.
	 */
	public synchronized List<Participant> findAll() {
		return findAll(null);
	}

	public List<String> findAllUids() {
		List<String> result = new ArrayList<String>();

		for (Participant t : findAll()) {
			if (!t.isExternal()) {
				result.add(t.getUid());
			}
		}
		return result;
	}

	public void removeInactive(List<Participant> participant) {
		participant.removeIf(t -> !t.isActive() && !t.isExternal());
	}
	
	public synchronized Participant getByUid(String uid) {
		loadFromDb();
		for (Participant t : contacts.values()) {
			if (t.getUid().equals(uid)) {
				return t;
			}
		}
		return null;
	}

	/**
	 * Finds all participants that match the given filter.
	 *
	 * @param stringFilter filter that returned objects should match or null/empty
	 *                     string if all objects should be returned.
	 * @return list a Participant objects
	 */
	public synchronized List<Participant> findAll(String stringFilter) {
		if (stringFilter == null || stringFilter.equals(""))
			loadFromDb();
		ArrayList<Participant> arrayList = new ArrayList<>();
		for (Participant contact : contacts.values()) {
			try {
				boolean passesFilter = (stringFilter == null || stringFilter.isEmpty())
						|| contact.toString().toLowerCase().contains(stringFilter.toLowerCase());
				if (passesFilter) {
					arrayList.add(contact.clone());
				}
			} catch (CloneNotSupportedException ex) {
				Logger.getLogger(ParticipantService.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		Collections.sort(arrayList, new Comparator<Participant>() {

			@Override
			public int compare(Participant o1, Participant o2) {
				return (int) (o2.getId() - o1.getId());
			}
		});
		return arrayList;
	}

	/**
	 * Finds all Participant's that match given filter and limits the resultset.
	 *
	 * @param stringFilter filter that returned objects should match or null/empty
	 *                     string if all objects should be returned.
	 * @param start        the index of first result
	 * @param maxresults   maximum result count
	 * @return list a Participant objects
	 */
	public synchronized List<Participant> findAll(String stringFilter, int start, int maxresults) {
		ArrayList<Participant> arrayList = new ArrayList<>();
		for (Participant contact : contacts.values()) {
			try {
				boolean passesFilter = (stringFilter == null || stringFilter.isEmpty())
						|| contact.toString().toLowerCase().contains(stringFilter.toLowerCase());
				if (passesFilter) {
					arrayList.add(contact.clone());
				}
			} catch (CloneNotSupportedException ex) {
				Logger.getLogger(ParticipantService.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		Collections.sort(arrayList, new Comparator<Participant>() {
			@Override
			public int compare(Participant o1, Participant o2) {
				return o1.getUid().compareTo(o2.getUid());
			}
		});
		int end = start + maxresults;
		if (end > arrayList.size()) {
			end = arrayList.size();
		}
		return arrayList.subList(start, end);
	}

	/**
	 * @return the amount of all customers in the system
	 */
	public synchronized long count() {
		return contacts.size();
	}

	/**
	 * Deletes a customer from a system
	 *
	 * @param value the Participant to be deleted
	 */
	public synchronized void delete(Participant value) {
		try {
			dao.delete(value);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		contacts.remove(value.getId());
	}

	/**
	 * Persists or updates customer in the system. Also assigns an identifier for
	 * new Participant instances.
	 *
	 * @param entry
	 */
	public synchronized Integer save(Participant entry) {
		if (entry == null) {
			LOGGER.log(Level.SEVERE, "Participant is null.");
			return null;
		}
		try {
			entry = (Participant) entry.clone();
			dao.createOrUpdate(entry);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		contacts.put(entry.getId(), entry);
		return entry.getId();
	}

	public void refresh(Participant entry) throws SQLException {
		dao.refresh(entry);
	}

	public Dao<Participant, Integer> getDao() {
		return dao;
	}
}
