package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * User: soyoung
 * Date: Feb 28, 2011
 */
@Entity
public class Rule extends Model {

    @ManyToOne(optional = false)
    public Requirement requirement;

    public Date dateCreated;
    public Date lastUpdated;

	public String reference;

    @Required
	public String type;

    @Required
	public String label;

	public String description;
}
