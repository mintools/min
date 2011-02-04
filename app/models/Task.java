package models;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
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

    @ManyToOne(optional = false)
    public Member owner;

    @ManyToOne(optional = true)
    public Member assignedTo;

    public Boolean isActive;
    public Long sortOrder;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @Fetch(FetchMode.SUBSELECT)
    public Set<Tag> tags = new HashSet<Tag>();

    @Required
    public String title;

    @MaxSize(value = 4000)
    @Column(length = 4000)
    public String content;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval=true)
    @Fetch(FetchMode.SUBSELECT)
    public List<Attachment> attachments = new ArrayList<Attachment>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval=true)
    @BatchSize(size = 20)
    public List<WorkingOn> workingOn = new ArrayList<WorkingOn>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval=true)
    @OrderBy("createdDate DESC")
    @Fetch(FetchMode.SUBSELECT)
    public List<Comment> comments = new ArrayList<Comment>();

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

    public Comment addComment(Member member, String commentStr) {
        Comment comment = new Comment(this, member, commentStr);
        this.comments.add(comment);
        this.save();

        return comment;
    }

    public List<Member> getWorkers() {
        // can only call this method after the Task has been persisted
        if (this.id == null) {
            return null;
        }
        return Member.find("select m from Member m join m.workingOn t where t.task = ?", this).fetch();
    }

    public void setWorkers(List<Member> members) {
        workingOn.clear();
        for (Iterator<Member> iterator = members.iterator(); iterator.hasNext();) {
            Member member = iterator.next();
            workingOn.add(new WorkingOn(member, this));
        }
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

    public static List<Task> findActive() {
        return Task.find("from Task t where t.isActive = true order by sortOrder desc").fetch();
    }

    public static List<Task> findInactive() {
        return Task.find("from Task t where t.isActive = false order by sortOrder desc").fetch();
    }
}
