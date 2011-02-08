package models;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.envers.Audited;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * User: soyoung
 * Date: Dec 21, 2010
 */
@Entity
@BatchSize(size = 20)
@Audited
public class TagGroup extends Model {
    @Required
    public String name;

    public Boolean mutex;

    @OneToOne(optional = true)
    public Tag defaultTag;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    @OrderBy("sortOrder DESC") 
    @Fetch(FetchMode.SUBSELECT)
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

    public void setDefault(Tag tag) {
        this.defaultTag = tag;
        this.save();
    }

    public static List<TagGroup> getAll() {
        return TagGroup.find("order by sortOrder desc").fetch();
    }

    public static List<Tag> getDefaultTags() {
        return Tag.find("select distinct t from Tag t, TagGroup g where g.defaultTag = t").fetch();
    }
}
