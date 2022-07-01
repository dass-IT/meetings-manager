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

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * DAO class for meeting participants
 * @author Sebastian Lederer <sebastian.lederer@dass-it.de>
 */
@DatabaseTable(tableName="meeting_participant")
public class MeetingParticipant {
	@DatabaseField(generatedId=true)
	int id;
	@DatabaseField(foreign=true)
	Meeting meeting;
	@DatabaseField(foreign=true)
	Participant participant;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Meeting getMeeting() {
		return meeting;
	}
	public void setMeeting(Meeting meeting) {
		this.meeting = meeting;
	}
	public Participant getParticipant() {
		return participant;
	}
	public void setParticipant(Participant participant) {
		this.participant = participant;
	}
}
