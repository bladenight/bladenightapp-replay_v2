package app.bladenight.replay.log.local.templatedata;

import app.bladenight.common.events.Event;


public class EventProxy {
    public EventProxy(Event event) {
        this.event = event;
    }
    public String getDateIso() {
        return event.getStartDateAsString("yyyy-MM-dd");
    }
    public String getDateUserString() {
        return event.getStartDateAsString("yyyy-MM-dd");
    }
    public String getHref() {
        return getDateIso() + "/index.html";
    }
    public String getRouteName() {
        return event.getRouteName();
    }
    public String getParticipants() {
        return Integer.toString(event.getParticipants());
    }
    private Event event;
}
