package com.demandtracker.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DemandaEncerradaEvent extends ApplicationEvent {
    private final Long demandaId;

    public DemandaEncerradaEvent(Object source, Long demandaId) {
        super(source);
        this.demandaId = demandaId;
    }
}
