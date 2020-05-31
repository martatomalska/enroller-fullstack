package com.company.enroller.controllers;

import com.company.enroller.model.Meeting;
import com.company.enroller.model.Participant;
import com.company.enroller.persistence.MeetingService;
import com.company.enroller.persistence.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.concurrent.Callable;

@RestController
@RequestMapping("/api/meetings")
public class MeetingRestController {

    @Autowired
    MeetingService meetingService;

    @Autowired
    ParticipantService participantService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getMeetings(@RequestParam(defaultValue = "id") String sort, @RequestParam(defaultValue = "asc") String order) {
        Collection<Meeting> meetings = meetingService.getAllSortedByTitle(sort, order);
        return new ResponseEntity<>(meetings, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getMeeting(@PathVariable("id") long id) {
        Meeting meeting = meetingService.findById(id);
        if (meeting == null) {
            return new ResponseEntity("Meeting doesn't exist", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Meeting>(meeting, HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<?> createMeeting(@RequestBody Meeting meeting) {
        Meeting foundMeeting = meetingService.findById(meeting.getId());
        if (foundMeeting != null) {
            return new ResponseEntity(
                    "Unable to create. A meeting with an id " + meeting.getId() + " already exists.", HttpStatus.CONFLICT);
        }
        meetingService.add(meeting);
        return new ResponseEntity<Meeting>(meeting, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{id}/participants", method = RequestMethod.GET)
    public ResponseEntity<?> getParticipants(@PathVariable("id") long id) {
        Meeting meeting = meetingService.findById(id);
        if (meeting == null) {
            return new ResponseEntity("Meeting doesn't exist", HttpStatus.NOT_FOUND);
        }
        Collection<Participant> participants = meetingService.getAllParticipants(id);
        return  new ResponseEntity<Collection<Participant>>(participants, HttpStatus.OK);
    }

    @RequestMapping(value ="/{id}/participants/", method = RequestMethod.POST)
    public ResponseEntity<?> addParticipantToMeeting(@PathVariable("id") long id, @RequestBody Participant participant) {
        Meeting foundMeeting = meetingService.findById(id);
        if (foundMeeting == null) {
            return new ResponseEntity("Meeting doesn't exist", HttpStatus.NOT_FOUND);
        }
        if (meetingService.isParticipantExist(participant.getLogin())) {
            return new ResponseEntity<>("Participant with login " + participant.getLogin() + " does not exist.",
                    HttpStatus.NOT_FOUND);
        }
        if (meetingService.findByLoginInMeeting(id, participant.getLogin()) != null) {
            return new ResponseEntity("Unable to add. Participant with login " + participant.getLogin()
                    + " already added to the Meeting.", HttpStatus.CONFLICT);
        }
        foundMeeting.addParticipant(participant);
        meetingService.addParticipantToMeeting(foundMeeting);
        return new ResponseEntity("Participant " + participant + " added to meeting with an id " + foundMeeting, HttpStatus.OK);

    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteMeeting(@PathVariable("id") long id) {
        Meeting foundMeeting = meetingService.findById(id);
        if (foundMeeting == null) {
            return new ResponseEntity("Meeting doesn't exist", HttpStatus.NOT_FOUND);
        }
        meetingService.delete(foundMeeting);
        return new ResponseEntity<Meeting>(foundMeeting, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> updateMeeting(@PathVariable("id") long id, @RequestBody Meeting updatedMeeting) {
        Meeting foundMeeting = meetingService.findById(id);
        if (foundMeeting == null) {
            return new ResponseEntity("Meeting doesn't exist", HttpStatus.NOT_FOUND);
        }
        foundMeeting.setTitle(updatedMeeting.getTitle());
        foundMeeting.setDescription(updatedMeeting.getDescription());
        foundMeeting.setDate(updatedMeeting.getDate());
        meetingService.update(foundMeeting);
        return new ResponseEntity<Meeting>(foundMeeting, HttpStatus.OK);
    }

    @RequestMapping(value ="/{id}/participants/{login}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeParticipantFromMeeting(@PathVariable("id") long id, @PathVariable("login") String login) {
        Meeting foundMeeting = meetingService.findById(id);
        if (foundMeeting == null) {
            return new ResponseEntity("Meeting doesn't exist", HttpStatus.NOT_FOUND);
        }
        Participant foundParticipant = meetingService.findByLoginInMeeting(id, login);
        if (foundParticipant == null) {
            return new ResponseEntity("Participant not found", HttpStatus.NOT_FOUND);
        }
        foundMeeting.removeParticipant(foundParticipant);
        meetingService.deleteParticipantFromMeeting(foundMeeting);
        return new ResponseEntity("Participant " + foundParticipant + " removed from meeting with an id " + foundMeeting, HttpStatus.OK);

    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public ResponseEntity<?> searchMeetings(@RequestParam(defaultValue = "") String type,
                                            @RequestParam(defaultValue = "") String query) {
        if (query.equals("")) {
            return getMeetings("", "");
        }
        if (type.equals("participant")) {
            Collection<Meeting> meetings = meetingService.getMeetingsWithParticipant(query);
            if (meetings.size() > 0) {
                return new ResponseEntity<Collection<Meeting>>(meetings, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Meeting with participant " + query + " does not exist.",
                        HttpStatus.NOT_FOUND);
            }
        }

        Collection<Meeting> meetings = meetingService.getMeetingsWithSubstring(query);
        if (meetings.size() > 0) {
            return new ResponseEntity<Collection<Meeting>>(meetings, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Meeting with given title or description does not exist.",
                    HttpStatus.NOT_FOUND);
        }

    }


}
