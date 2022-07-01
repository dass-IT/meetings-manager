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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;

/**
 * This form allows creating and editing meetings.
 * @author Sebastian Lederer <sebastian.lederer@dass-it.de>
 *
 */
public class MeetingForm extends FormLayout {
	private static final long serialVersionUID = 1L;
	private TextField name = new TextField("Name");
	private ComboBox<String> resourceMenu = new ComboBox<String>("Resource");
	private ComboBox<String> roomsMenu = new ComboBox<String>("Raum");
	private TextField url = new TextField("URL");
	private DatePicker datum = new DatePicker("Datum");
	private TimePicker start = new TimePicker("Beginn");
	private TimePicker end = new TimePicker("Ende");
	private ComboBox<String> organizer = new ComboBox<String>("Organisator");
	private Label listenLabel = new Label("Teilnehmer:");
	private ListBox<String> participantList = new ListBox<String>();
	private Button participantAddButton = new Button("Teilnehmer hinzufügen...");
	private Button participantRemoveButton = new Button("Teilnehmer entfernen");
	private Button save = new Button("Speichern");
	private Button delete = new Button("Löschen");

	private AddParticipantDialog addParticipantDialog;

	private Binder<Meeting> binder = new Binder<>(Meeting.class);

	private MeetingView meetingView;
	private ParticipantView participantView;
	private CalendarView calendarView;
	private MeetingService meetingService = MeetingService.getInstance();
	private ParticipantService participantService = ParticipantService.getInstance();

	private Meeting currentMeeting;

	public MeetingForm(MeetingView m) {
		this.meetingView = m;
		addParticipantDialog = new AddParticipantDialog(this);

		resourceMenu.setItems(Configuration.getInstance().getResources());
		roomsMenu.setItems(Configuration.getInstance().getRooms());
		List<String> organisatorItems = participantService.findAllUids();
		organisatorItems.sort(null);
		organizer.setItems(organisatorItems);

		participantList.setHeight("10em");

		HorizontalLayout buttons = new HorizontalLayout(save, delete);

		participantList.setWidth("32em");
		participantList.addValueChangeListener(e -> {
			String val = participantList.getValue();
			participantRemoveButton.setEnabled(!(val == null || val.contentEquals("")));
		});

		VerticalLayout participantBox = new VerticalLayout(listenLabel,
				new HorizontalLayout(participantList, new VerticalLayout(participantAddButton, participantRemoveButton)));
		participantAddButton.addClickListener(e -> {
			addParticipantDialog.open();
		});

		participantRemoveButton.addClickListener(e -> {
			removeParticipantByName(participantList.getValue());
		});
		participantRemoveButton.setEnabled(false);

		save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		save.addClickListener(event -> save());

		delete.addClickListener(event -> delete());

		add(name, url, resourceMenu, roomsMenu, datum, start, organizer, end, participantBox, buttons);
		setColspan(participantBox, 2);

		binder.forField(name).bind("name");
		binder.forField(url).bind("url");
		binder.forField(resourceMenu).bind(meeting -> meeting.getResource(),
				(meeting, value) -> meeting.setResource(value));
		binder.forField(roomsMenu).bind(meeting -> meeting.getRoom(), (meeting, value) -> meeting.setRoom(value));
		binder.forField(datum).bind(meeting -> meeting.getDatum(), (meeting, value) -> meeting.setDatum(value));
		binder.forField(start).bind(meeting -> meeting.getStartTime(),
				(meeting, value) -> meeting.setStartTime(value));
		binder.forField(end).bind(meeting -> meeting.getEndTime(), (meeting, value) -> meeting.setEndTime(value));
		binder.forField(organizer).bind(meeting -> meeting.getOrganizerId(),
				(meeting, value) -> meeting.setOrganizerById(value));
		addParticipantDialog.setHeight("32em");
		addParticipantDialog.setWidth("60em");

		enableFields(false);
	}

	public void setMeeting(Meeting m) {
		currentMeeting = m;

		addParticipantDialog.setMeeting(m);

		binder.setBean(m);

		if (m != null) {
			name.focus();
		}
	}

	public ParticipantView getParticipantView() {
		return participantView;
	}

	public void setParticipantView(ParticipantView participantView) {
		this.participantView = participantView;
	}

	public void clearFields() {
		name.clear();
		url.clear();
		resourceMenu.clear();
		roomsMenu.clear();
		datum.clear();
		start.clear();
		end.clear();
		organizer.clear();
		participantList.clear();
		participantList.setItems();
	}

	public void enableFields(boolean enable) {
		name.setEnabled(enable);
		url.setEnabled(enable);
		resourceMenu.setEnabled(enable);
		roomsMenu.setEnabled(enable);
		datum.setEnabled(enable);
		start.setEnabled(enable);
		end.setEnabled(enable);
		organizer.setEnabled(enable);
		participantAddButton.setEnabled(enable);
		participantList.setEnabled(enable);
		save.setEnabled(enable);
		delete.setEnabled(enable);
	}

	public void showMeeting(Meeting m) {
		if (m == null) {
			setMeeting(m);
			clearFields();
			enableFields(false);
		} else {
			try {
				setMeeting(m.clone());
			} catch (CloneNotSupportedException e) {
				Logger logger = Logger.getGlobal();
				logger.log(Level.WARNING, e.getMessage(), e);
			}
			makeParticipantList();
			enableFields(true);
		}
	}

	private void makeParticipantList() {
		ArrayList<String> items = new ArrayList<String>();

		for (Participant t : currentMeeting.getParticipants()) {
			String uid = t.getUid();
			if (uid == null || uid.equals("")) {
				items.add(t.getEmail());
			} else {
				items.add(uid);
			}
		}
		items.sort(null);
		participantList.setItems(items);
	}

	private void save() {
		Meeting t = binder.getBean();
		meetingService.save(t);
		meetingView.updateList();
		participantView.updateList();
		calendarView.update();

		setMeeting(null);
		clearFields();
	}

	private void delete() {
		Meeting t = binder.getBean();
		meetingService.delete(t);
		meetingView.updateList();
		participantView.updateList();
		calendarView.update();

		setMeeting(null);
	}

	public void focus() {
		name.focus();
	}

	public void addOneParticipant(Participant t) {
		currentMeeting.addOneParticipant(t);
		makeParticipantList();
	}

	public void removeParticipantByName(String s) {
		Participant found = null;

		if (s == null)
			return;

		for (Participant t : currentMeeting.getParticipants()) {
			if (t.getEmail().equals(s) || (t.getUid() != null && t.getUid().contentEquals(s))) {
				found = t;
				break;
			}
		}

		if (found != null) {
			ArrayList<Participant> newList = new ArrayList<Participant>(currentMeeting.getParticipants());
			newList.remove(found);
			if (found.isExternal()) {
				participantService.delete(found);
			}
			currentMeeting.setParticipants(newList);
		}

		makeParticipantList();
	}

	public CalendarView getCalendarView() {
		return calendarView;
	}

	public void setCalendarView(CalendarView calendarView) {
		this.calendarView = calendarView;
	}
}
