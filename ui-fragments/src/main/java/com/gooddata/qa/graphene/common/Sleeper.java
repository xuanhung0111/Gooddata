package com.gooddata.qa.graphene.common;

/**
 * Primitives for sleeping
 */
public class Sleeper {

    /**
     * Sleeps without explicitly throwing an InterruptedException
     *
     * @param timeoutInSeconds Sleep time in seconds.
     * @throws RuntimeException wrapping an InterruptedException if one gets thrown
     */
    public static void sleepTightInSeconds(long timeoutInSeconds) {
        sleepTight(timeoutInSeconds * 1000);
    }

    /**
     * Sleeps without explicitly throwing an InterruptedException
     *
     * @param timeout the amout of time to sleep
     * @throws RuntimeException wrapping an InterruptedException if one gets thrown
     */
    public static void sleepTight(long timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
