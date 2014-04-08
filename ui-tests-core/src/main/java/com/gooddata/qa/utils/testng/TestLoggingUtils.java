package com.gooddata.qa.utils.testng;

import java.util.Date;

import org.testng.ITestResult;

import static com.gooddata.qa.utils.testng.TestInfo.STATUSES;
import static com.gooddata.qa.utils.testng.TestInfo.getPackageClassMethodName;

public final class TestLoggingUtils {

    private TestLoggingUtils() {
    }

    public static String getTestDescription(ITestResult result) {
        final String methodName = getPackageClassMethodName(result);
        final String status = STATUSES.get(result.getStatus());

        // parameters
        StringBuilder parameters = new StringBuilder("(");
        if (result.getParameters() != null
                && result.getParameters().length != 0) {
            for (int i = 0; i < result.getParameters().length; i++) {
                parameters.append("\"");
                parameters.append(result.getParameters()[i]);
                parameters.append(i == result.getParameters().length - 1 ? "\"" : "\", ");
            }
        }
        parameters.append(")");

        // result
        String message = String.format("[%tT] %s: %s%s", new Date(),
                status.toUpperCase(), methodName, parameters.toString());
        return message;
    }
}
