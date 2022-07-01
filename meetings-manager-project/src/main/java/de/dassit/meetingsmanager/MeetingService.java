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
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * This class provides a service to handle persistence of meeting objects.
 * {@link MeetingService#getInstance()}.
 * @author Sebastian Lederer <sebastian.lederer@dass-it.de>
 */
public class MeetingService {
	private static MeetingService instance;
	private static final Logger LOGGER = Logger.getLogger(MeetingService.class.getName());
	private static final int PASSWORD_LENGTH = 8;

	private HashMap<Integer, Meeting> meetings = new HashMap<>();

	private Dao<Meeting, Integer> dao;
	private Dao<MeetingParticipant, Integer> participantListDao;
	private Dao<Participant, Integer> participantDao;

	private MeetingService() {
		try {
			ConnectionSource connectionSource = DbConnection.getConnectionSource();

			dao = DaoManager.createDao(connectionSource, Meeting.class);
			TableUtils.createTableIfNotExists(connectionSource, Meeting.class);

			participantListDao = DaoManager.createDao(connectionSource, MeetingParticipant.class);
			TableUtils.createTableIfNotExists(connectionSource, MeetingParticipant.class);

			participantDao = DaoManager.createDao(connectionSource, Participant.class);
			TableUtils.createTableIfNotExists(connectionSource, Participant.class);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			dao = null;
		}
	}

	/**
	 * @return a reference to a facade for Meeting objects.
	 */
	public static MeetingService getInstance() {
		if (instance == null) {
			instance = new MeetingService();
		}
		return instance;
	}

	/**
	 * @return all available Meeting objects.
	 */
	public synchronized List<Meeting> findAll() {
		return findAll(null);
	}

	public void loadFromDb() {
		List<Meeting> result = null;

		try {
			result = dao.queryForAll();
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return;
		}

		meetings = new HashMap<Integer, Meeting>();

		for (Meeting m : result) {
			try {
				ParticipantService.getInstance().refresh(m.getOrganizer());
				loadParticipantList(m);
			} catch (SQLException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
			meetings.put(m.getId(), m);
		}
	}

	private void loadParticipantList(Meeting m) throws SQLException {
		List<MeetingParticipant> result;
		List<Participant> participantList = new ArrayList<Participant>();

		result = participantListDao.queryForEq("meeting_id", m.getId());

		for (MeetingParticipant mt : result) {
			dao.refresh(mt.getMeeting());
			participantDao.refresh(mt.getParticipant());

			participantList.add(mt.getParticipant());
		}

		m.setParticipants(participantList);
	}

	private void saveParticipantList(Meeting m) throws SQLException {
		deleteParticipantList(m);

		for (Participant t : m.getParticipants()) {
			MeetingParticipant mt = new MeetingParticipant();
			mt.setMeeting(m);
			mt.setParticipant(t);
			participantListDao.createIfNotExists(mt);
		}
	}

	private void deleteParticipantList(Meeting m) throws SQLException {
		if (m == null || m.getId() == null)
			return;
		
		List<MeetingParticipant> result = participantListDao.queryForEq("meeting_id", m.getId());

		participantListDao.delete(result);
	}

	/**
	 * Finds all Meetings that match given filter.
	 *
	 * @param stringFilter filter that returned objects should match or null/empty
	 *                     string if all objects should be returned.
	 * @return list a Meeting objects
	 */
	public synchronized List<Meeting> findAll(String stringFilter) {
		if (stringFilter == null || stringFilter.equals(""))
			loadFromDb();
		ArrayList<Meeting> arrayList = new ArrayList<>();
		for (Meeting contact : meetings.values()) {
			try {
				boolean passesFilter = (stringFilter == null || stringFilter.isEmpty())
						|| contact.toString().toLowerCase().contains(stringFilter.toLowerCase());
				if (passesFilter) {
					arrayList.add(contact.clone());
				}
			} catch (CloneNotSupportedException ex) {
				Logger.getLogger(MeetingService.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		Collections.sort(arrayList, new Comparator<Meeting>() {

			@Override
			public int compare(Meeting o1, Meeting o2) {
				return (int) (o2.getId() - o1.getId());
			}
		});
		return arrayList;
	}

	/**
	 * Finds all Meetings that match given filter and limits the resultset.
	 *
	 * @param stringFilter filter that returned objects should match or null/empty
	 *                     string if all objects should be returned.
	 * @param start        the index of first result
	 * @param maxresults   maximum result count
	 * @return list a Meeting objects
	 */
	public synchronized List<Meeting> findAll(String stringFilter, int start, int maxresults) {
		ArrayList<Meeting> arrayList = new ArrayList<>();
		for (Meeting contact : meetings.values()) {
			try {
				boolean passesFilter = (stringFilter == null || stringFilter.isEmpty())
						|| contact.toString().toLowerCase().contains(stringFilter.toLowerCase());
				if (passesFilter) {
					arrayList.add(contact.clone());
				}
			} catch (CloneNotSupportedException ex) {
				Logger.getLogger(MeetingService.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		Collections.sort(arrayList, new Comparator<Meeting>() {

			@Override
			public int compare(Meeting o1, Meeting o2) {
				return (int) (o2.getId() - o1.getId());
			}
		});
		int end = start + maxresults;
		if (end > arrayList.size()) {
			end = arrayList.size();
		}
		return arrayList.subList(start, end);
	}

	/**
	 * @return the amount of participants
	 */
	public synchronized long count() {
		return meetings.size();
	}

	/**
	 * Deletes a meeting, updates the database
	 *
	 * @param value the Meeting to be deleted
	 */
	public synchronized void delete(Meeting value) {
		try {
			deleteParticipantList(value);
		} catch (SQLException e1) {
			LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
		}
		try {
			dao.delete(value);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		meetings.remove(value.getId());
	}

	/**
	 * Persists or updates a meeting. Also assigns an identifier for
	 * new Meeting instances.
	 *
	 * @param entry
	 */
	public synchronized void save(Meeting entry) {
		if (entry == null) {
			LOGGER.log(Level.SEVERE, "Meeting is null.");
			return;
		}
		try {
			entry = (Meeting) entry.clone();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		try {
			dao.createOrUpdate(entry);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}

		try {
			saveParticipantList(entry);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}

		Participant organisator = entry.getOrganizer();
		if(organisator != null && !organisator.isActive()) {
			organisator.setActive(true);
			organisator.setPassword(PasswordGenerator.password(PASSWORD_LENGTH));
			ParticipantService.getInstance().save(organisator);
		}
		meetings.put(entry.getId(), entry);
	}
}
