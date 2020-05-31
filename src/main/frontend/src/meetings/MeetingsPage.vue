<template>
  <div>
    <new-meeting-form @added="addNewMeeting($event)"></new-meeting-form>

    <span v-if="meetings.length === 0">
               Brak zaplanowanych spotkań.
           </span>
    <h3 v-else>
      Zaplanowane zajęcia ({{ meetings.length }})
    </h3>

    <meetings-list :meetings="meetings"
                   :username="username"
                   @attend="addMeetingParticipant($event)"
                   @unattend="removeMeetingParticipant($event)"
                   @delete="deleteMeeting($event)"></meetings-list>
  </div>
</template>

<script>
    import NewMeetingForm from "./NewMeetingForm";
    import MeetingsList from "./MeetingsList";

    export default {
        components: {NewMeetingForm, MeetingsList},
        props: ['username'],
        data() {
            return {
                meetings: []
            };
        },
        mounted() {
            this.$nextTick(this.getMeetings());
        },
        methods: {
            getMeetings() {
                this.$http.get('meetings').then(response => {this.meetings = response.body;})
            },
            addNewMeeting(meeting) {
                this.$http.post('meetings', meeting).then(response => {this.meetings.push(response.body);});
                // this.meetings.push(meeting);
                this.getMeetings();

            },
            addMeetingParticipant(meeting) {
                meeting.participants.push(this.username);
                this.$http.post('meetings/' + meeting.id + '/participants', {login:this.username})
                    .then(response => {console.log("zapisano do spotkania");
                    this.getMeetings();
                    })
                    .catch(response => {console.log("nie udało się zapisać");
                });
            },
            removeMeetingParticipant(meeting) {
                meeting.participants.splice(meeting.participants.indexOf(this.username), 1);
                this.$http.delete('meetings/'+ meeting.id + '/participants/' + this.username)
                    .then(response => {console.log("wypisano ze spotkania");
                    this.getMeetings();
                    })
                    .catch(response => {console.log("nie udało się wypisać");
                    });
            },
            deleteMeeting(meeting) {
                this.meetings.splice(this.meetings.indexOf(meeting), 1);
                this.getMeetings();
                this.$http.delete('meetings/' + meeting.id.toString())
                    .then(response => {console.log("usunięto spotkanie");
                    })
                    .catch(response => {console.log("nie udało się usunąć");
                    });
            }
        },
    }
</script>
