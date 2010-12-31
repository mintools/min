package models;

import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.*;

/**
 * User: soyoung
 * Date: Dec 21, 2010
 */
@Entity
public class Task extends Model {
    public Date createdDate;
    public Member owner;

    public Boolean isActive;
    public Integer sortOrder;

    @ManyToMany(cascade = CascadeType.PERSIST)
    public Set<Tag> tags = new HashSet<Tag>();

    @Required
    public String title;

    @MaxSize(value = 4000)
    @Column(length = 4000)
    public String content;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<Attachment> attachments = new ArrayList<Attachment>();


    public Task tagItWith(String name) {
        tags.add(Tag.findOrCreateByName(name));
        return this;
    }

    public Task activate() {
        this.isActive = true;
        return this.save();
    }

    public Task deactivate() {
        this.isActive = false;
        return this.save();
    }

    public static List<Task> findTaggedWith(String tag) {
        return Task.find("select distinct t from Task t join t.tags as g where t.isActive = true and g.name = ?", tag).fetch();
    }

    public static boolean deleteById(Long id) {
        return delete("id = ?", id) > 0;
    }

    public static List<Task> findTaggedWith(String[] tags) {
        return Task.find(
                "select distinct t from Task t join t.tags as tg where tg.name in (:tags) group by t.id having count(g.id) = :size"
        ).bind("tags", tags).bind("size", tags.length).fetch();
    }
}
