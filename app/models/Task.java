package models;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Session;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.Audited;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.JPA;
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
    @BatchSize(size = 20)
    public List<Interest> interests = new ArrayList<Interest>();

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

    public List<Member> getInteresteds() {
        // can only call this method after the Task has been persisted
        if (this.id == null) {
            return null;
        }
        return Member.find("select m from Member m join m.interests t where t.task = ?", this).fetch();
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

    public void setInteresteds(List<Long> memberIds) {
         // add interests (it would be easier to remove all workers then add the new ones again, but this screws up the auditing in Envers

        // first, remove workers that have been deselected
        List removedInterests = new ArrayList();

        for (Iterator<Interest> i = this.interests.iterator(); i.hasNext();) {
            Interest interest = i.next();

            boolean found = false;
            for (int j = 0; j < memberIds.size(); j++) {
                Long selectedMemberId = memberIds.get(j);
                if (interest.member.id.equals(selectedMemberId)) {
                    found = true;
                    memberIds.remove(j);
                    break;
                }
            }
            if (!found) {
                // delete it
                i.remove();
                this.interests.remove(interest);
            }
        }

        // add the left over ones in selectTagIds
        for (Iterator<Long> iterator = memberIds.iterator(); iterator.hasNext();) {
            Long memberId = iterator.next();
            Interest newInterest = new Interest(Member.<Member>findById(memberId), this);

            this.interests.add(newInterest);
        }
    }

    public Map retrieveRevisions() throws Exception {
        Session session = (Session) JPA.em().getDelegate();

        AuditReader reader = AuditReaderFactory.get(session);
        AuditQuery query = reader.createQuery().forRevisionsOfEntity(Task.class, false, true);

        List auditResult = query.addOrder(AuditEntity.revisionNumber().asc()).add(AuditEntity.id().eq(this.id)).getResultList();

        Map<MinRevisionEntity, List<Delta>> changeMap = new TreeMap<MinRevisionEntity, List<Delta>>();

        Task previousTask = null;

        for (Iterator iterator = auditResult.iterator(); iterator.hasNext();) {
            Object[] auditItem = (Object[]) iterator.next();
            Task task = (Task) auditItem[0];
//            Task task = reader.find(Task.class, taskId, revision);

            MinRevisionEntity revisionEntity = (MinRevisionEntity) auditItem[1];

//            RevisionType type = (RevisionType) auditItem[2];
//            int revision = revisionEntity.getId();

            // compare properties
            if (previousTask != null) {

                Map properties = BeanUtils.describe(task);

                List<Delta> deltas = new ArrayList<Delta>();

                for (Iterator i = properties.keySet().iterator(); i.hasNext();) {
                    String key = (String) i.next();
                    Object oldValue = PropertyUtils.getProperty(previousTask, key);
                    Object newValue = PropertyUtils.getProperty(task, key);

                    if (oldValue instanceof Collection) {
                        Collection oldCollection = (Collection) oldValue;
                        Collection newCollection = (Collection) newValue;

                        Collection newEntries = CollectionUtils.subtract(newCollection, oldCollection);
                        Collection deletedEntries = CollectionUtils.subtract(oldCollection, newCollection);

                        if (!newEntries.isEmpty()) {
                            deltas.add(new Delta(key, 0, newEntries.toString()));
                        }
                        if (!deletedEntries.isEmpty()) {
                            deltas.add(new Delta(key, 2, deletedEntries.toString()));
                        }
                    }
                    else {
                        if (oldValue == null && newValue == null) {
                            // do nothing
                        }
                        else if (oldValue == null && newValue != null) {
                            deltas.add(new Delta(key, 0, newValue.toString()));
                        }
                        else if (oldValue != null && newValue == null) {
                            deltas.add(new Delta(key, 2, oldValue.toString()));
                        }
                        else if (!oldValue.equals(newValue)) {
                            deltas.add(new Delta(key, 1, oldValue.toString() + " to " + newValue.toString()));
                        }
                    }
                }

                changeMap.put(revisionEntity, deltas);
            }

            previousTask = task;
        }

        return changeMap;
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

    public static class Delta {
        private String entity;
        private String changeType;
        private String attribute;

        public Delta(String entity, Integer changeType, String attribute) {
            this.entity = entity;

            switch (changeType) {
                case 0:
                    this.changeType = "Added";
                    break;
                case 1:
                    this.changeType = "Modified";
                    break;
                case 2:
                    this.changeType = "Deleted";
                    break;
            }

            this.attribute = attribute;
        }

        public String getEntity() {
            return entity;
        }

        public String getChangeType() {
            return changeType;
        }

        public String getAttribute() {
            return attribute;
        }
    }
}
