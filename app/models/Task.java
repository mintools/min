package models;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.envers.Audited;
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
@Audited
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

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    public List<Attachment> attachments = new ArrayList<Attachment>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    public List<WorkingOn> workingOn = new ArrayList<WorkingOn>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
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

    public void setTags(List<Long> tagIds) {
        // add tags (it would be easier to remove all tags then add the new ones again, but this screws up the auditing in Envers

        // first, remove tags that have been deselected
        List removedTags = new ArrayList();

        for (Iterator<Tag> i = this.tags.iterator(); i.hasNext();) {
            Tag tag = i.next();

            boolean found = false;
            for (int j = 0; j < tagIds.size(); j++) {
                Long selectedTagId = tagIds.get(j);
                if (tag.id.equals(selectedTagId)) {
                    found = true;
                    tagIds.remove(j);
                    break;
                }
            }
            if (!found) {
                // delete it
                i.remove();
                this.tags.remove(tag);
            }
        }

        // add the left over ones in selectTagIds
        for (Iterator<Long> iterator = tagIds.iterator(); iterator.hasNext();) {
            Long tagId = iterator.next();
            Tag tag = Tag.findById(tagId);

            this.tags.add(tag);
        }
    }

    public void setWorkers(List<Long> memberIds) {
         // add workers (it would be easier to remove all workers then add the new ones again, but this screws up the auditing in Envers

        // first, remove workers that have been deselected
        List removedWorkers = new ArrayList();

        for (Iterator<WorkingOn> i = this.workingOn.iterator(); i.hasNext();) {
            WorkingOn worker = i.next();

            boolean found = false;
            for (int j = 0; j < memberIds.size(); j++) {
                Long selectedMemberId = memberIds.get(j);
                if (worker.member.id.equals(selectedMemberId)) {
                    found = true;
                    memberIds.remove(j);
                    break;
                }
            }
            if (!found) {
                // delete it
                i.remove();
                this.workingOn.remove(worker);
            }
        }

        // add the left over ones in selectTagIds
        for (Iterator<Long> iterator = memberIds.iterator(); iterator.hasNext();) {
            Long memberId = iterator.next();
            WorkingOn newWorker = new WorkingOn(Member.<Member>findById(memberId), this);

            this.workingOn.add(newWorker);
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
