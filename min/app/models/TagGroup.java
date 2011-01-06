package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * User: soyoung
 * Date: Dec 21, 2010
 */
@Entity
public class TagGroup extends Model {
    @Required
    public String name;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    public List<Tag> tags = new ArrayList<Tag>();

    public String toString() {
        return this.name;
    }
}
