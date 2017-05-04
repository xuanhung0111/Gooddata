package com.gooddata.qa.graphene.entity.disc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;

import com.gooddata.qa.graphene.enums.process.Parameter;

public class Parameters {

    private Map<String, String> parameters;
    private Map<String, String> secureParameters;

    public Parameters() {
        this.parameters = new HashMap<>();
        this.secureParameters = new HashMap<>();
    }

    public static Pair<String, String> createRandomParam() {
        return Pair.of("Param-" + generateHashString(), "Value-" + generateHashString());
    }

    public Parameters addParameter(String name, String value) {
        this.parameters.put(name, value);
        return this;
    }

    public Parameters addParameter(Parameter parameter, String value) {
        return addParameter(parameter.toString(), value);
    }

    public Parameters addSecureParameter(String name, String value) {
        this.secureParameters.put(name, value);
        return this;
    }

    public Parameters addSecureParameter(Parameter parameter, String value) {
        return addSecureParameter(parameter.toString(), value);
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public Map<String, String> getSecureParameters() {
        return secureParameters;
    }

    private static String generateHashString() {
        return UUID.randomUUID().toString().substring(0, 5);
    }
}
