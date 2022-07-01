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
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

/**
 * This dialog is used to add participants to a Meeting.
 *  @author Sebastian Lederer <sebastian.lederer@dass-it.de>
 */

public class AddParticipantDialog extends Dialog {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ParticipantService service = ParticipantService.getInstance();
    private Grid<Participant> grid = new Grid<>(Participant.class);
    private TextField filterText = new TextField();
    private EmailField externalParticipant = new EmailField(); 
    private Meeting meeting;
    private MeetingForm meetingForm;
    
    public AddParticipantDialog(MeetingForm mf) {
    	super();
    	meetingForm = mf;
    	
    	HorizontalLayout buttonBar = new HorizontalLayout();

        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.EAGER);
        filterText.addValueChangeListener( e -> updateList());

        grid.setColumns("uid", "displayName", "email", "external");
        grid.setHeight("23em");
        
        Button addButton = new Button("Hinzufügen");
        Button doneButton = new Button("Fertig");
        
        addButton.addClickListener( e -> {
        	// grid.asSingleSelect().clear();
        	Participant selected = grid.asSingleSelect().getValue();
        	
        	Notification n = new Notification("Teilnehmer hinzugefügt. Speichern nicht vergessen!",2000);
        	n.open();
        	
        	meetingForm.addOneParticipant(selected);
        });
        
        doneButton.addClickListener( e-> {
        	this.close();
        });
        
        ParticipantService participantService = ParticipantService.getInstance();

        externalParticipant.setPlaceholder("E-Mail-Adresse");
        Button externalButton = new Button("externen Teilnehmer hinzufügen");
        externalButton.addClickListener(e -> {
        	Participant t = new Participant();
        	t.setEmail(externalParticipant.getValue());
        	t.setActive(true);
        	t.setExternal(true);
        	Integer tId = participantService.save(t);
        	t.setId(tId);
        	
        	Notification n = new Notification("Teilnehmer hinzugefügt. Speichern nicht vergessen!",2000);
        	n.open();
        	
        	meetingForm.addOneParticipant(t);
        	externalParticipant.clear();
        });
        buttonBar.add(filterText, addButton, externalParticipant, externalButton);

        add(new Label("Teilnehmer hinzufügen"));
        
        add(grid, buttonBar);

        add(doneButton);
        
        setSizeFull();

        updateList();
    }

    public void updateList() {
        grid.setItems(service.findAll(filterText.getValue()));
    }

	public Meeting getMeeting() {
		return meeting;
	}

	public void setMeeting(Meeting meeting) {
		this.meeting = meeting;
	}
}
