package com.gooddata.qa.graphene.enums.disc.notification;

import static java.util.stream.Collectors.toList;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import com.gooddata.qa.graphene.entity.disc.NotificationRule;

public enum VariableList {

    SUCCESS {
        @Override
        public Collection<Variable> getVariables() {
            return Stream.concat(PROCESS_STARTED.getVariables().stream(), Stream.of(Variable.FINISH_TIME))
                    .collect(toList());
        }
    },
    FAILURE {
        @Override
        public Collection<Variable> getVariables() {
            Collection<Variable> result = new ArrayList<>(SUCCESS.getVariables());
            result.add(Variable.CONSECUTIVE_FAILURES);
            result.add(Variable.ERROR_MESSAGE);
            return result;
        }
    },
    PROCESS_SCHEDULED {
        @Override
        public Collection<Variable> getVariables() {
            return asList(Variable.PROJECT, Variable.USER, Variable.USER_EMAIL, Variable.PROCESS_URI, Variable.PROCESS_ID,
                    Variable.EXECUTABLE, Variable.SCHEDULE_ID, Variable.SCHEDULE_NAME, Variable.SCHEDULE_TIME);
        }
    },
    PROCESS_STARTED {
        @Override
        public Collection<Variable> getVariables() {
            return asList(Variable.PROJECT, Variable.USER, Variable.USER_EMAIL, Variable.PROCESS_URI, Variable.PROCESS_ID,
                    Variable.PROCESS_NAME, Variable.EXECUTABLE, Variable.SCHEDULE_ID, Variable.SCHEDULE_NAME,
                    Variable.LOG, Variable.START_TIME);
        }
    };

    public abstract Collection<Variable> getVariables();

    public String buildMessage() {
        return NotificationRule.buildMessage(getVariables().stream().toArray(Variable[]::new));
    };
}
