package controllers;

import models.Member;
import play.mvc.Controller;

/**
 * User: soyoung
 * Date: Feb 1, 2011
 */
public class BaseController extends Controller {
    protected static Member getLoggedInMember() {
        Member member = null;

        if (Security.connected() != null) {
            member = Member.find("byUsername", Security.connected()).first();
        }

        return member;
    }
}
