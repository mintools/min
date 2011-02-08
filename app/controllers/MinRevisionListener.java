package controllers;

import controllers.Security;
import models.MinRevisionEntity;
import org.hibernate.envers.RevisionListener;
import play.mvc.Controller;

/**
 * User: soyoung
 * Date: Feb 7, 2011
 */
public class MinRevisionListener implements RevisionListener {
    public void newRevision(Object revisionEntity) {
        MinRevisionEntity minRevisionEntity = (MinRevisionEntity) revisionEntity;

        minRevisionEntity.setUsername(Security.connected());
    }
}
