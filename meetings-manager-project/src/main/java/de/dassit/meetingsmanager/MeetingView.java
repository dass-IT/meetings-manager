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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

/**
 * MeetingView shows a list of existing meetings with a search
 * filter and buttons to edit meetings and create new ones.
 * @author Sebastian Lederer <sebastian.lederer@dass-it.de>
 */

public class MeetingView extends VerticalLayout {
	private static final long serialVersionUID = 1L;
	private MeetingService service = MeetingService.getInstance();
    private Grid<Meeting> grid = new Grid<>(Meeting.class);
    private TextField filterText = new TextField();
    private MeetingForm meetingForm = new MeetingForm(this);

    public MeetingView() {
    	super();
    	
    	HorizontalLayout toolbar = new HorizontalLayout();
    	
        filterText.setPlaceholder("Suche nach Name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.EAGER);
        filterText.addValueChangeListener( e -> updateList());

        Button addButton = new Button("Neues Meeting");
        addButton.addClickListener( e -> {
        	grid.asSingleSelect().clear();
        	meetingForm.setMeeting(new Meeting());
        	meetingForm.enableFields(true);
        	meetingForm.focus();
        });
        
        toolbar.add(filterText, addButton);
        
        grid.setColumns("name", "datum", "organizerId", "room", "resource");
        grid.getColumnByKey("name").setResizable(true);
        grid.getColumnByKey("organizerId").setResizable(true);
        grid.getColumnByKey("room").setHeader("Raum");
        grid.asSingleSelect().addValueChangeListener(event ->
            meetingForm.showMeeting(grid.asSingleSelect().getValue()));
        grid.setSizeFull();
    

        HorizontalLayout mainContent = new HorizontalLayout(grid, meetingForm);
        mainContent.setSizeFull();

        add(toolbar, mainContent);

        setSizeFull();

        updateList();
    }

    public void updateList() {
        grid.setItems(service.findAll(filterText.getValue()));
    }
    
    public void setParticipantView(ParticipantView v) {
    	meetingForm.setParticipantView(v);
    }
    
    public void setCalendarView(CalendarView v) {
    	meetingForm.setCalendarView(v);
    }
}
