package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * User: soyoung
 * Date: Dec 21, 2010
 */
@Entity
public class TagGroup extends Model {
    @Required
    public String name;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    @OrderBy("sortOrder DESC") 
    public List<Tag> tags = new ArrayList<Tag>();

    public Integer sortOrder;

    public String toString() {
        return this.name;
    }

    /**
     * Returns true if any tag in the specified list belongs to the current group
     */
    public boolean hasTags(Set<Tag> tags) {
        if (tags != null) {
            for (Iterator<Tag> iterator = tags.iterator(); iterator.hasNext();) {
                Tag tag = iterator.next();
                if (this.tags.contains(tag)) return true;
            }
        }

        return false;
    }

    public static List<TagGroup> getAll() {
        return TagGroup.find("order by sortOrder desc").fetch(); 
    }
}
