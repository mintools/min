package controllers;

import models.Tag;
import models.Task;
import org.hibernate.Session;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import play.db.DB;
import play.db.jpa.JPA;
import play.mvc.Controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

        List auditResult = query.addOrder(AuditEntity.revisionNumber().desc()).add(AuditEntity.id().eq(taskId)).getResultList();

        for (Iterator iterator = auditResult.iterator(); iterator.hasNext();) {
            Object[] auditItem = (Object[]) iterator.next();
            Task task = (Task) auditItem[0];
            Object tags = task.tags;

            System.out.println(tags);
        }

        render(auditResult);
    }

    public static void getChanges(Integer revision, Long taskId) throws Exception {
        Session session = (Session) JPA.em().getDelegate();

        List changeList = new ArrayList();

        AuditReader reader = AuditReaderFactory.get(session);
        AuditQuery query = reader.createQuery().forEntitiesAtRevision(Task.class, revision).add(AuditEntity.id().eq(taskId));
        Task currentTaskRevision = (Task) query.getSingleResult();

        query = reader.createQuery().forEntitiesAtRevision(Task.class, revision - 1);
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