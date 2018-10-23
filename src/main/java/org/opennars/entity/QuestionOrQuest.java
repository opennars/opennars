package org.opennars.entity;

import java.io.Serializable;

public class QuestionOrQuest implements Serializable {
    public Task task;

    public QuestionOrQuest(final Task task) {
        this.task = task;
    }
}
