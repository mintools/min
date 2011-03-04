package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;

import play.db.jpa.Model;

/**
 * User: soyoung Date: Jan 10, 2011
 */
@Entity
@Audited
public class WorkingOn extends Model {
	@ManyToOne(optional = false)
	public Member member;

	public Date dateAssigned;

	@ManyToOne(optional = false)
	public Task task;

	public WorkingOn(Member member, Task task) {
		this.member = member;
		this.task = task;
		this.dateAssigned = new Date();
	}

	public static WorkingOn findWorkingOn(Member member, Task task) {
		return WorkingOn.find("from WorkingOn as t where t.member = ? and t.task = ?", member, task).first();
	}

	
	public static void removeWorkingOn(Member member, Task task) {
		WorkingOn.delete("from WorkingOn as t where t.member = ? and t.task = ?", member, task);
	}

	public String toString() {
		return member.username;
	}
}
