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
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.data.binder.Binder;


/**
 * ParticipantForm is a view that allows editing a meeting participant. 
 * @author Sebastian Lederer <sebastian.lederer@dass-it.de>
 *
 */
public class ParticipantForm extends FormLayout {
	private static final long serialVersionUID = 1L;
	private TextField uid = new TextField("Benutzername");
	private TextField displayName = new TextField("Angezeigter Name");
    private TextField email = new TextField("E-Mail");
    // private Checkbox external = new Checkbox("Extern");
    private Checkbox permanent = new Checkbox("Dauerhaft");
    private TextField password = new TextField("Passwort");
    private Button save = new Button("Speichern");
    private Button delete = new Button("Löschen");

    private Binder<Participant> binder = new Binder<>(Participant.class);

    private ParticipantView mainView;
    private ParticipantService participantService = ParticipantService.getInstance();


    public ParticipantForm(ParticipantView m) {
        this.mainView = m;

        HorizontalLayout buttons = new HorizontalLayout(save, delete);

        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(event -> save());

        delete.addClickListener(event -> delete());

        add(uid, displayName, email, password, permanent, buttons);

        binder.bindInstanceFields(this);
        enableFields(false);
    }

    public void setParticipant(Participant t) {
        binder.setBean(t);
    }
    
    public void clearFields() {
    	 displayName.clear();
         email.clear();
         uid.clear();
         password.clear();
         permanent.clear();
    }
    
    public void enableFields(boolean enable) {
    	uid.setEnabled(enable);
    	displayName.setEnabled(enable);
    	email.setEnabled(enable);
    	password.setEnabled(enable);
    	permanent.setEnabled(enable);
    	save.setEnabled(enable);
    	delete.setEnabled(enable);
    }

    private void save() {
        Participant t = binder.getBean();
        participantService.save(t);
        mainView.updateList();
        setParticipant(null);
    }

    private void delete() {
        Participant t = binder.getBean();
        participantService.delete(t);
        mainView.updateList();
        setParticipant(null);
    }

	public Object showParticipant(Participant value) {
		if(value==null) {
			setParticipant(value);
			clearFields();
			enableFields(false);
		}
		else {
			try {
				setParticipant(value.clone());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			enableFields(true);
		}
		return this;
	}

	public void focus() {
		uid.focus();
	}
}