package app.bladenight.replay.log.local;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import app.bladenight.common.events.Event;
import app.bladenight.common.procession.Procession;
import app.bladenight.common.procession.Statistics;
import app.bladenight.common.procession.Statistics.Segment;
import app.bladenight.replay.log.local.templatedata.TemplateProxy;

public class UsersByTime extends ProcessionStatisticsWriter {

    UsersByTime(File basePath, Procession procession, Event event) throws IOException {
        super(basePath, procession, event);
    }

    @Override
    public void checkpoint(DateTime dateTime) {
        Statistics statistics = procession.getStatistics();
        int n = 0;
        if (statistics != null ) {
            for (Segment segment : statistics.segments ) {
                n += segment.nParticipants;
            }
        }
        entries.add(new OutputEntry(dateTime.toString(), n));
    }

    @Override
    public void finish() {
        TemplateProxy templateProxy = new TemplateProxy("users-by-time/users-by-time.ftl.json");
        templateProxy.putData("entries", entries);
        String targetFileName = "users-by-time.json";
        File targetFile = new File(basePath, targetFileName);
        templateProxy.generate(targetFile);
    }

    static public class OutputEntry {
        public OutputEntry(String dateTimeStr, int users) {
            this.dateTimeStr = dateTimeStr;
            this.users = users;;
        }
        public String dateTimeStr;
        public int users;
        public String getDateTimeStr() {
            return dateTimeStr;
        }
        public int getUsers() {
            return users;
        }
    };

    private List<OutputEntry> entries = new ArrayList<OutputEntry>();


}
