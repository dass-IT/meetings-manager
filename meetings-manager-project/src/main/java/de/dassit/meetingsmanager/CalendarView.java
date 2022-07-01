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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.vaadin.stefan.fullcalendar.BusinessHours;
import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;
import org.vaadin.stefan.fullcalendar.Timezone;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;

public class CalendarView extends VerticalLayout {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ZoneId zid;
	private ZoneId utc = ZoneId.of("UTC");
	private FullCalendar calendar;

	private Button buttonDatePicker;
	private HorizontalLayout toolbar;

	private Map<String, String> roomColors = new HashMap<String, String>();

        /**
        *  This view shows a calendar (month overview) with all scheduled meetings. 
        *  @author Sebastian Lederer <sebastian.lederer@dass-it.de>
        */
	public CalendarView() {
		calendar = FullCalendarBuilder.create().build();
		zid = ZoneId.of("Europe/Berlin");
		Timezone tz = new Timezone(zid);
		calendar.setTimezone(tz);
		calendar.setLocale(new Locale("de"));
		calendar.setFirstDay(DayOfWeek.MONDAY);
		calendar.setNowIndicatorShown(true);
		calendar.setBusinessHours(
				new BusinessHours(LocalTime.of(8, 0), LocalTime.of(17, 0), BusinessHours.DEFAULT_BUSINESS_WEEK));
		calendar.setOption("weekends", false);
		calendar.setOption("allDaySlot",false);
		
		createToolbar();

		this.add(toolbar);
		this.add(calendar);
		this.setFlexGrow(1, calendar);

		this.setHeight("86%");

		List<String> rooms = Configuration.getInstance().getRooms();
		List<String> colors = Configuration.getInstance().getMultiValue("roomColors");

		Iterator<String> cols = colors.iterator();

		for (String room : rooms) {
			roomColors.put(room, cols.next());
		}

		load();
	}

	public void update() {
		calendar.removeAllEntries();
		load();
	}

	public void load() {
		List<Meeting> meetings = MeetingService.getInstance().findAll();
		addMeetings(meetings);
	}

	public void addMeetings(List<Meeting> meetings) {
		for (Meeting m : meetings) {
			Entry entry = new Entry();

			entry.setTitle(makeTitle(m));
			entry.setStart(dateToLocal(m.getStart()));
			entry.setEnd(dateToLocal(m.getEnd()));

			String room = m.getRoom();
			if (room != null && roomColors.containsKey(room)) {
				entry.setColor(roomColors.get(m.getRoom()));
			}

			calendar.addEntry(entry);
		}
	}

	private static String makeTitle(Meeting m) {
		String resource = m.getResource();
		String room = m.getRoom();
                String name = m.getName();
                Participant organisator = m.getOrganizer();
                String orgUid;

                if(name == null) name = " ";
		if(room == null) room = "-";
		if(resource == null) resource = "-";
	        if(organisator != null)
                    orgUid = organisator.getUid();
                else
                    orgUid = "";
                    
		return room + "/" + resource + ": " + name + " - " + orgUid;
	}

	private LocalDateTime dateToLocal(Date d) {
		return d.toInstant().atZone(utc).toLocalDateTime();
	}

	private void createToolbar() {
		Button buttonToday = new Button("Heute", VaadinIcon.HOME.create(), e -> calendar.today());
		Button buttonPrevious = new Button("", VaadinIcon.ANGLE_LEFT.create(), e -> calendar.previous());
		Button buttonNext = new Button("", VaadinIcon.ANGLE_RIGHT.create(), e -> calendar.next());
		buttonNext.setIconAfterText(true);

		DatePicker gotoDate = new DatePicker();
		gotoDate.addValueChangeListener(event1 -> calendar.gotoDate(event1.getValue()));
		gotoDate.getElement().getStyle().set("visibility", "hidden");
		gotoDate.getElement().getStyle().set("position", "fixed");
		gotoDate.setWidth("0px");
		gotoDate.setHeight("0px");
		gotoDate.setWeekNumbersVisible(true);
		buttonDatePicker = new Button(VaadinIcon.CALENDAR.create());
		buttonDatePicker.getElement().appendChild(gotoDate.getElement());
		buttonDatePicker.addClickListener(event -> gotoDate.open());

		Select<String> switchView = new Select<String>("Monatsansicht", "Wochenansicht");
		switchView.setValue("Monatsansicht");
		switchView.addValueChangeListener(event -> {
			String value = event.getValue();
			if (value.equals("Wochenansicht")) {
				calendar.changeView(CalendarViewImpl.TIME_GRID_WEEK);
			} else {
				calendar.changeView(CalendarViewImpl.DAY_GRID_MONTH);
			}
		});

		calendar.addDatesRenderedListener(event -> updateDateLabel(event.getIntervalStart()));
		
		toolbar = new HorizontalLayout(switchView, buttonToday, buttonPrevious, buttonNext, buttonDatePicker, gotoDate);

		add(toolbar);
	}

	private void updateDateLabel(LocalDate start) {
		Locale locale = calendar.getLocale();
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale);
		String text = start.format(dateFormatter);
		buttonDatePicker.setText(text);
	}
}
