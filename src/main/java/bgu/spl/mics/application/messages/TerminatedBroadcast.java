package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MicroService;

public class TerminatedBroadcast implements Broadcast {
    private MicroService sourceService;
    private String serviceName;

    public TerminatedBroadcast(MicroService sourceService, String serviceName) {
        this.sourceService = sourceService;
        this.serviceName = serviceName;
    }


    public Class getServiceClass() {
        return sourceService.getClass();
    }
}
