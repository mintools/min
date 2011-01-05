package models;

import play.db.jpa.Model;

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
    public String name;

    @OneToMany(mappedBy = "group")
    public List<Tag> tags = new ArrayList<Tag>();
}
