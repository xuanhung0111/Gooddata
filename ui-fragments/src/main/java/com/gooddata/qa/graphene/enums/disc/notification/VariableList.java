package com.gooddata.qa.graphene.enums.disc.notification;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.Arrays.asList;

import java.util.Collection;

public enum VariableList {

    SUCCESS {
        @Override
        public String buildMessage() {
            return buildMessage(getVariables());
        }

        @Override
        public Collection<Variable> getVariables() {
            return getAllVariables().stream()
                    .filter(v -> v != Variable.SCHEDULE_TIME && v != Variable.ERROR_MESSAGE)
                    .collect(toList());
        }
    },
    FAILURE {
        @Override
        public String buildMessage() {
            return buildMessage(getVariables());
        }

        @Override
        public Collection<Variable> getVariables() {
            return getAllVariables().stream()
                    .filter(v -> v != Variable.SCHEDULE_TIME)
                    .collect(toList());
        }
    },
    PROCESS_SCHEDULED {
        @Override
        public String buildMessage() {
            return buildMessage(getVariables());
        }

        @Override
        public Collection<Variable> getVariables() {
            return asList(Variable.PROJECT, Variable.USER, Variable.USER_EMAIL, Variable.PROCESS_URI, Variable.PROCESS_ID,
                    Variable.EXECUTABLE, Variable.SCHEDULE_ID, Variable.SCHEDULE_NAME, Variable.SCHEDULE_TIME);
        }
    },
    PROCESS_STARTED {
        @Override
        public String buildMessage() {
            return buildMessage(getVariables());
        }

        @Override
        public Collection<Variable> getVariables() {
            return getAllVariables().stream()
                    .filter(v -> v != Variable.SCHEDULE_TIME && v != Variable.FINISH_TIME && v != Variable.ERROR_MESSAGE)
                    .collect(toList());
        }
    };

    public abstract Collection<Variable> getVariables();

    public abstract String buildMessage();

    public String buildMessage(Variable... variables) {
        return buildMessage(asList(variables));
    }

    Collection<Variable> getAllVariables() {
        return asList(Variable.PROJECT, Variable.USER, Variable.USER_EMAIL, Variable.PROCESS_URI,
                Variable.PROCESS_ID, Variable.PROCESS_NAME, Variable.EXECUTABLE, Variable.SCHEDULE_ID,
                Variable.SCHEDULE_NAME, Variable.SCHEDULE_TIME, Variable.LOG, Variable.START_TIME,
                Variable.FINISH_TIME, Variable.ERROR_MESSAGE);
    }

    String buildMessage(Collection<Variable> variables) {
        return variables.stream()
                .map(v -> v.getName() + "==" + v.getValue())
                .collect(joining(" | "));
        
    }
}
