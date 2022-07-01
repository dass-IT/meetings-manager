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

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * A entity object for meeting participants.
 * @author Sebastian Lederer <sebastian.lederer@dass-it.de>
 */
@DatabaseTable(tableName="participant")
public class Participant implements Serializable, Cloneable {
	private static final long serialVersionUID = 7522720582673510219L;

	@DatabaseField(generatedId=true)
	private Integer id;

	@DatabaseField
    private String displayName = "";

	@DatabaseField
    private boolean external;

	@DatabaseField
    private boolean permanent;

	@DatabaseField
    private boolean active;

	@DatabaseField(canBeNull=true)
    private String uid;
    
	@DatabaseField(canBeNull=true)
    private String email = "";
	
	@DatabaseField(canBeNull=true)
    private String password = "";

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setUid(String newUid) {
    	uid = newUid;
    }
    
    public String getUid() {
    	return uid;
    }
    
    /**
     * Get the value of email
     *
     * @return the value of email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the value of email
     *
     * @param email new value of email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get the value of status
     *
     * @return the value of status
     */
    public boolean isExternal() {
        return external;
    }

    /**
     * Set the value of status
     *
     * @param status new value of status
     */
    public void setExternal(boolean aBool) {
        this.external = aBool;
    }

    /**
     * Get the value of status
     *
     * @return the value of status
     */
    public boolean isPermanent() {
        return permanent;
    }

    /**
     * Set the value of status
     *
     * @param status new value of status
     */
    public void setActive(boolean aBool) {
        this.active = aBool;
    }

    /**
     * Get the value of status
     *
     * @return the value of status
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Set the value of status
     *
     * @param status new value of status
     */
    public void setPermanent(boolean aBool) {
        this.permanent = aBool;
    }

    /**
     * Get the value of displayName
     *
     * @return the value of displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Set the value of displayName
     *
     * @param displayName new value of displayName
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isPersisted() {
        return id != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (this.id == null) {
            return false;
        }

        if (obj instanceof Participant && obj.getClass().equals(getClass())) {
            return this.id.equals(((Participant) obj).id);
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
    public Participant clone() throws CloneNotSupportedException {
        return (Participant) super.clone();
    }

    @Override
    public String toString() {
        return displayName;
    }
}
