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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;

/**
 * MainView is the main view.
 * @author Sebastian Lederer <sebastian.lederer@dass-it.de>
 */
@Route("")
@PWA(name = "Meetings-Manager", shortName = "Meetings-Manager", enableInstallPrompt = false)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class MainView extends AppLayout {
	private static final long serialVersionUID = 1L;

	private MeetingView meetingView = new MeetingView();
	private ParticipantView participantView = new ParticipantView();
	private CalendarView calendarView = new CalendarView();
	
    public MainView() {
    	super();
    	
    	meetingView.setParticipantView(participantView);
    	meetingView.setCalendarView(calendarView);
    	
    	setPrimarySection(AppLayout.Section.NAVBAR);
    	
    	Tab calendarTab = new Tab("Übersicht");
    	Tab meetingsTab = new Tab("Besprechungen");
    	Tab usersTab = new Tab("Benutzer");
    	Tabs tabs = new Tabs(calendarTab, meetingsTab, usersTab);
    	tabs.addSelectedChangeListener(event -> {
    		Component selectedPage = tabs.getSelectedTab();
    		calendarView.setVisible(selectedPage.equals(calendarTab));
    		meetingView.setVisible(selectedPage.equals(meetingsTab));
    		participantView.setVisible(selectedPage.equals(usersTab));
    	});
    	addToNavbar(tabs);
    	
    	Div pages = new Div(calendarView, meetingView, participantView);
    	pages.setSizeFull();
    	calendarView.setVisible(true);
    	meetingView.setVisible(false);
    	participantView.setVisible(false);
    	this.setContent(pages);
    }
}
