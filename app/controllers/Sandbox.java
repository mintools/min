package controllers;

import models.MinRevisionEntity;
import models.Tag;
import models.Task;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.Session;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import play.db.DB;
import play.db.jpa.JPA;
import play.mvc.Controller;

import javax.persistence.NoResultException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.util.*;

/**
 * User: soyoung
 * Date: Feb 4, 2011
 */
public class Sandbox extends Controller {
    public static void dragndrop() throws Exception {
        render();
    }

    public static void upload() throws Exception {
        BufferedReader r = new BufferedReader(new InputStreamReader(request.body)); 
        System.out.println("Uploaded: " + r.readLine());

        renderText("DONE");
    }

    public static void getRevisions(Long taskId) throws Exception {
        Session session = (Session) JPA.em().getDelegate();

        AuditReader reader = AuditReaderFactory.get(session);
        AuditQuery query = reader.createQuery().forRevisionsOfEntity(Task.class, false, true);

        List auditResult = query.addOrder(AuditEntity.revisionNumber().asc()).add(AuditEntity.id().eq(taskId)).getResultList();

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

                changeMap.put(revisionEntity, deltas);
            }

            previousTask = task;
        }

        render(changeMap);
    }
//
//    protected List<Revision> fetchRevisions(final AuditQuery query, final AuditReader reader) {
//        try {
//            final Object resultList = query.getResultList();
//            final List<Object[]> queryResult = (List<Object[]>) resultList;
//            final List<Revision<T, REVINFO>> result = new ArrayList<Revision<T, REVINFO>>(queryResult.size());
//            for (final Object[] array : queryResult) {
//                result.add(new RevisionImpl<T, REVINFO>((T) array[0], (REVINFO) array[1], (RevisionType) array[2]));
//            }
//            return result;
//        } catch (final RevisionDoesNotExistException ex) {
//            return null;
//        } catch (final NoResultException ex) {
//            return null;
//        }
//    }

    public static void getChanges(Integer revision, Long taskId) throws Exception {
        Session session = (Session) JPA.em().getDelegate();

        List changeList = new ArrayList();

        AuditReader reader = AuditReaderFactory.get(session);
        AuditQuery query = reader.createQuery().forEntitiesAtRevision(Task.class, revision).add(AuditEntity.id().eq(taskId));
        Task currentTaskRevision = (Task) query.getSingleResult();

        query = reader.createQuery().forEntitiesAtRevision(Task.class, revision - 1).add(AuditEntity.id().eq(taskId));
        Task previousTaskRevision = (Task) query.getSingleResult();

        if (!currentTaskRevision.title.equals(previousTaskRevision.title)) {
            changeList.add(new Delta("Task", 1, "title"));
        }

        if (!currentTaskRevision.content.equals(previousTaskRevision.content)) {
            changeList.add(new Delta("Task", 1, "content"));
        }


        if (currentTaskRevision.assignedTo == null && previousTaskRevision.assignedTo == null) {
            // no change
        }
        else if (currentTaskRevision.assignedTo == null && previousTaskRevision.assignedTo != null) {
            changeList.add(new Delta("assignedTo", 2, previousTaskRevision.assignedTo.username));
        }
        else if (currentTaskRevision.assignedTo != null && previousTaskRevision.assignedTo == null) {
            changeList.add(new Delta("assignedTo", 0, currentTaskRevision.assignedTo.username));
        }
        else if (!currentTaskRevision.assignedTo.equals(previousTaskRevision.assignedTo)) {
            changeList.add(new Delta("assignedTo", 1, "from " + previousTaskRevision.assignedTo.username + " to " + currentTaskRevision.assignedTo.username));
        }
        
        ResultSet rs = DB.executeQuery("select * from Task_Tag_AUD where REV = " + revision + " and Task_id = " + taskId);

        while (rs.next()) {
            changeList.add(new Delta("Tag", rs.getInt("REVTYPE"), rs.getString("tags_id")));
        }

        rs = DB.executeQuery("select * from WorkingOn_AUD where REV = " + revision + " and Task_id = " + taskId);

        while (rs.next()) {
            changeList.add(new Delta("Workers", rs.getInt("REVTYPE"), rs.getString("member_id")));
        }

        rs = DB.executeQuery("select * from Comment_AUD where REV = " + revision + " and Task_id = " + taskId);

        while (rs.next()) {
            changeList.add(new Delta("Comment", rs.getInt("REVTYPE"), rs.getString("content")));
        }

        rs = DB.executeQuery("select * from Attachment_AUD where REV = " + revision + " and Task_id = " + taskId);

        while (rs.next()) {
            changeList.add(new Delta("Attachment", rs.getInt("REVTYPE"), rs.getString("title")));
        }

        render(currentTaskRevision, previousTaskRevision, changeList);
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