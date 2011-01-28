package models;

import java.util.*;
import javax.persistence.*;

import play.data.validation.Required;
import play.db.jpa.*;

@Entity
public class Tag extends Model implements Comparable<Tag> {

    @Required
    public String name;

    @ManyToOne(optional = true)
    public TagGroup group;

    public Integer sortOrder;

    public String toString() {
        return name;
    }

    public int compareTo(Tag otherTag) {
        return name.compareTo(otherTag.name);
    }

    /**
     * @return true if the tag is the default of its group
     */
    public boolean isDefault() {
       return group != null && this.equals(group.defaultTag); 
    }

    public static Tag findOrCreateByName(String name) {
        Tag tag = Tag.find("from Tag t where upper(t.name) = ?", name.toUpperCase()).first();
        if (tag == null) {
            tag = new Tag();
            tag.name = name;
        }
        tag.save();
        return tag;
    }

    public static List<Tag> ungrouped() {
        return Tag.find("select t from models.Tag as t where t.group = null").fetch();
    }

    public static List<Map> getCloud() {
        List<Map> result = Tag.find(
            "select new map(t.name as tag, count(p.id) as pound) from Post p join p.tags as t group by t.name order by t.name"
        ).fetch();
        return result;
    }
}