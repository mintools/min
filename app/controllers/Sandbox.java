package controllers;

import models.Task;
import org.hibernate.Session;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import play.db.jpa.JPA;
import play.mvc.Controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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

    public static void testEnvers(Long taskId) throws Exception {
        Session session = (Session) JPA.em().getDelegate();


//        AuditReader reader = AuditReaderFactory.get(session);
//        Task t = (Task) reader.find(Task.class, new Long(1), 1);



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
}