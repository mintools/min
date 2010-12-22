package models;

import play.db.jpa.Model;

import javax.persistence.Entity;

/**
 * User: soyoung
 * Date: Dec 21, 2010
 */
@Entity
public class Member extends Model {
    public String username;
    public String password;
}
