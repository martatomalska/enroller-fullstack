package com.company.enroller.persistence;

import com.company.enroller.model.Meeting;
import com.company.enroller.model.Participant;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.criterion.*;
import org.springframework.stereotype.Component;
import java.util.Collection;

@Component("meetingService")
public class MeetingService {

    DatabaseConnector connector;


    public MeetingService() {
        connector = DatabaseConnector.getInstance();
    }

    public Collection<Meeting> getAll() {
        String hql = "FROM Meeting";
        Query query = connector.getSession().createQuery(hql);
        return query.list();
    }

    public Collection<Meeting> getAllSortedByTitle(String sort, String order) {
        Criteria criteria = connector.getSession().createCriteria(Meeting.class);
        if (order.equals("desc")) {
            criteria.addOrder(Order.desc(sort));
        } else {
            criteria.addOrder(Order.asc(sort));
        }
        return criteria.list();
    }

    public Meeting findById(long id) {
        return (Meeting) connector.getSession().get(Meeting.class, id);
    }

    public Meeting add(Meeting meeting) {
        Transaction transaction = this.connector.getSession().beginTransaction();
        connector.getSession().save(meeting);
        transaction.commit();
        return meeting;
    }

    public Meeting update(Meeting meeting) {
        Transaction transaction = this.connector.getSession().beginTransaction();
        connector.getSession().merge(meeting);
        transaction.commit();
        return meeting;
    }

    public void delete(Meeting meeting) {
        Transaction transaction = this.connector.getSession().beginTransaction();
        connector.getSession().delete(meeting);
        transaction.commit();
    }

    public Collection<Participant> getAllParticipants(long id) {
        return ((Meeting) connector.getSession().get(Meeting.class, id)).getParticipants();
    }

    public void addParticipantToMeeting(Meeting meeting) {
        Transaction transaction = connector.getSession().beginTransaction();
        connector.getSession().save(meeting);
        transaction.commit();
    }

    public void deleteParticipantFromMeeting(Meeting meeting) {
        Transaction transaction = connector.getSession().beginTransaction();
        connector.getSession().save(meeting);
        transaction.commit();
    }

    public Collection getMeetingsWithParticipant(String query) {
        Criteria crit = connector.getSession().createCriteria(Meeting.class);
        crit.createAlias("participants", "participantsAlias");
        crit.add(Restrictions.eq("participantsAlias.login", query));
        return crit.list();
    }

    public Participant findByLoginInMeeting(long id, String login) {
        Collection<Participant> participants = ((Meeting) connector.getSession().get(Meeting.class, id))
                .getParticipants();
        for (Participant participant : participants) {
            if (participant.getLogin().equals(login)) {
                return participant;
            }
        }
        connector.getSession().get(Meeting.class, id);
        return null;
    }

    public boolean isParticipantExist(String login) {
        return !(connector.getSession().get(Participant.class, login) != null);
    }

    public Collection<Meeting> getMeetingsWithSubstring(String query) {
        Criteria crit = connector.getSession().createCriteria(Meeting.class);
        Criterion findInTitle = Restrictions.like("title", query, MatchMode.ANYWHERE);
        Criterion findInDescription = Restrictions.like("description", query, MatchMode.ANYWHERE);
        LogicalExpression orExp = Restrictions.or(findInTitle, findInDescription);
        crit.add(orExp);
        return crit.list();
    }

}
