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

import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

/**
 * ParticipantView shows the list of participants
 */

public class ParticipantView extends VerticalLayout {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ParticipantService service = ParticipantService.getInstance();
    private Grid<Participant> grid = new Grid<>(Participant.class);
    private TextField filterText = new TextField();
    private ParticipantForm participantForm = new ParticipantForm(this);

    public ParticipantView() {
    	super();
    	
    	HorizontalLayout toolbar = new HorizontalLayout();

        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.EAGER);
        filterText.addValueChangeListener(e -> updateList());

        grid.setColumns("uid", "displayName", "email", "permanent", "external");
        grid.asSingleSelect().addValueChangeListener(event ->
            participantForm.showParticipant(grid.asSingleSelect().getValue()));
        grid.setSizeFull();
        
        Button addButton = new Button("Neuer externer Benutzer");
        addButton.addClickListener( e -> {
        	grid.asSingleSelect().clear();
                Participant t = new Participant();
                t.setExternal(true);
        	participantForm.setParticipant(t);
        	participantForm.enableFields(true);
        	participantForm.focus();
        });
        
        toolbar.add(filterText, addButton);

        HorizontalLayout mainContent = new HorizontalLayout(grid, participantForm);
        mainContent.setSizeFull();

        add(toolbar, mainContent);

        setSizeFull();

        updateList();
    }

    public void updateList() {
    	List<Participant> activeParticipants = service.findAll(filterText.getValue());
    	service.removeInactive(activeParticipants);
        grid.setItems(activeParticipants);
    }
}
