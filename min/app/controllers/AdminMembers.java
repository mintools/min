package controllers;

import models.Member;
import play.mvc.With;

/**
 * User: soyoung
 * Date: Dec 31, 2010
 */
@With(Secure.class)
@CRUD.For(Member.class)
public class AdminMembers extends CRUD {
}
