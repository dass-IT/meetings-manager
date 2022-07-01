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

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * The Meeting class represents a meeting with
 * information like the title, date, list of participants etc
 * @author Sebastian Lederer <sebastian.lederer@dass-it.de>
 */

@DatabaseTable(tableName = "meetings")
public class Meeting implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;

	@DatabaseField(generatedId = true)
	private Integer id;

	@DatabaseField
	private String name = "";

	@DatabaseField
	private Date start;
	@DatabaseField
	private Date end;
	@DatabaseField(canBeNull = true)
	private String resource;
	@DatabaseField(canBeNull = true)
	private String room;
	@DatabaseField
	private String url;
	@DatabaseField(canBeNull = true)
	private String password;
	@DatabaseField(foreign = true)
	private Participant organizer;
	@DatabaseField
	private boolean notified;
	// @ForeignCollectionField(eager=true)
	private Collection<Participant> participant = new ArrayList<Participant>();

	private String randomKey;

	private final static String baseUrl = Configuration.getInstance().get("baseUrl");

	public Meeting() {
		super();
		randomKey = PasswordGenerator.password(16);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
		String key = name.replaceAll("[^A-Za-z0-9]", "");
		if (randomKey != null)
			this.url = baseUrl + key + "-" + randomKey;
	}

	public String getName() {
		return name;
	}

	private Date newTime(Date oldTime, int year, int month, int day) {
		Calendar newTime = Calendar.getInstance();
		newTime.setTime(oldTime);
		newTime.set(Calendar.YEAR, year);
		newTime.set(Calendar.MONTH, month - 1);
		newTime.set(Calendar.DAY_OF_MONTH, day);
		return newTime.getTime();
	}

	public void setDatum(LocalDate datum) {
		// set datum for start/end
		int year = datum.getYear();
		int day = datum.getDayOfMonth();
		int month = datum.getMonthValue();

		this.start = newTime(this.end, year, month, day);

		this.end = newTime(this.end, year, month, day);
	}

	public LocalDate getDatum() {
		if (start == null)
			return LocalDate.now();
		return start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	public void setStart(Date beginn) {
		this.start = beginn;
	}

	public void setStartTime(LocalTime beginn) {
		Calendar newTime = Calendar.getInstance();
		newTime.setTime(this.start);
		newTime.set(Calendar.HOUR_OF_DAY, beginn.getHour());
		newTime.set(Calendar.MINUTE, beginn.getMinute());
		newTime.set(Calendar.SECOND, 0);
		newTime.set(Calendar.MILLISECOND, 0);
		this.start = newTime.getTime();
	}

	public Date getStart() {
		return start;
	}

	public LocalTime getStartTime() {
		if (start == null)
			start = new Date();
		return LocalDateTime.ofInstant(start.toInstant(), ZoneId.systemDefault()).toLocalTime();
	}

	public void setEnd(Date ende) {
		this.end = ende;
	}

	public void setEndTime(LocalTime ende) {
		Calendar newTime = Calendar.getInstance();
		newTime.setTime(this.end);
		newTime.set(Calendar.HOUR_OF_DAY, ende.getHour());
		newTime.set(Calendar.MINUTE, ende.getMinute());
		newTime.set(Calendar.SECOND, 0);
		newTime.set(Calendar.MILLISECOND, 0);
		this.end = newTime.getTime();
	}

	public Date getEnd() {
		return end;
	}

	public LocalTime getEndTime() {
		if (end == null)
			end = new Date();
		return LocalDateTime.ofInstant(end.toInstant(), ZoneId.systemDefault()).toLocalTime();
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getResource() {
		return resource;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public boolean isNotified() {
		return notified;
	}

	public void setNotified(boolean notified) {
		this.notified = notified;
	}

	public void setParticipants(List<Participant> participant) {
		this.participant = participant;
	}

	public Collection<Participant> getParticipants() {
		return participant;
	}

	public void addOneParticipant(Participant aParticipant) {
		if (aParticipant == null) {
			Logger logger = Logger.getGlobal();
			logger.severe("Meeting.addOneParticipant aParticipant is null");
			return;
		}
		participant.add(aParticipant);
	}

	public void removeOneParticipant(Participant aParticipant) {
		participant.remove(aParticipant);
	}

	public Participant getOrganizer() {
		return organizer;
	}

	public void setOrganizer(Participant organisator) {
		this.organizer = organisator;
	}

	public void setOrganizerById(String orgId) {
		organizer = ParticipantService.getInstance().getByUid(orgId);
		if (organizer == null)
			return;
	}

	public String getOrganizerId() {
		if (organizer == null)
			return "";
		return organizer.getUid();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (this.id == null) {
			return false;
		}

		if (obj instanceof Meeting && obj.getClass().equals(getClass())) {
			return this.id.equals(((Meeting) obj).id);
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 43 * hash + (id == null ? 0 : id.hashCode());
		return hash;
	}

	@Override
	public Meeting clone() throws CloneNotSupportedException {
		Meeting c = (Meeting) super.clone();
		c.participant = new ArrayList<Participant>(c.participant);
		return c;
	}

	@Override
	public String toString() {
		return name;
	}
}
