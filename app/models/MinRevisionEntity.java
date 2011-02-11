package models;

import controllers.MinRevisionListener;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import javax.persistence.Entity;

/**
 * User: soyoung
 * Date: Feb 7, 2011
 */
@Entity
@RevisionEntity(MinRevisionListener.class)
public class MinRevisionEntity extends DefaultRevisionEntity implements Comparable {
	private String username;

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

    public int compareTo(Object o) {
        return ((MinRevisionEntity) o).getId() - this.getId();
    }
}
