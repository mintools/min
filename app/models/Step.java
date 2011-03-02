package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * User: soyoung
 * Date: Feb 28, 2011
 */
@Entity
public class Step extends Model implements Comparable {
    public Date dateCreated;
    public Date lastUpdated;

    @ManyToOne(optional = false)
    public Requirement parent;

    @ManyToOne(optional = true)
    public Requirement requirement;

    @ManyToMany()
    public Set<Requirement> extensions = null;

    public int sortOrder;

    public boolean requirementReferencesCheck() {
        return Step.find("byRequirement", requirement).fetch().size() > 1;
    }

    public Set<Requirement> getExtensions() {
        if (extensions == null) {
            extensions = new TreeSet<Requirement>();
        }
        return extensions;
    }

    public int compareTo(Object obj) {
		return sortOrder - ((Step)obj).sortOrder;
	}
}
