package com.gooddata.qa.utils.java;

import com.gooddata.qa.graphene.utils.Sleeper;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.logging.Logger;

public class RetryCommand {
    protected static final Logger log = Logger.getLogger(RetryCommand.class.getName());
    private int retryCounter;
    private int maxRetries;

    private RetryCommand() {
    }

    public RetryCommand(final int maxRetries) {
        if (maxRetries < 1) {
            throw new IllegalStateException("max retry must > 1");
        }
        this.maxRetries = maxRetries;
    }

    /**
     * Execute function in the supplier param, if the run throw an Exception as exception in parameters, sleep 2 second
     * then rerun the function unless the counter > maxRetries
     *
     * @param exception clazz
     * @param function  function plan to execute
     * @param <T>
     * @param <E>
     * @return result of function execution
     */
    public <T, E extends Throwable> T retryOnException(@Nonnull final Class<E> exception,
                                                       @Nonnull final ThrowingSupplier<T> function) {
        Objects.requireNonNull(exception);
        Objects.requireNonNull(function);
        retryCounter = 0;
        do {
            try {
                return function.get();
            } catch (Throwable t) {
                if (isExpectedExceptionThrown(exception, t)) {
                    log.info(String.format("Catch %s: retry count = %d", t.getMessage(), retryCounter));
                    Sleeper.sleepTightInSeconds(2);
                } else {
                    throw new RuntimeException(t);
                }
            } finally {
                retryCounter++;
            }
        }
        while (retryCounter <= maxRetries);

        throw new RuntimeException("Command execution failed after " + maxRetries + " retries");
    }

    /**
     * Execute function in the supplier param, if the run throw an Exception as exception in parameters, sleep 2 second
     * then rerun the function unless the counter > maxRetries
     *
     * @param exception clazz
     * @param function  function plan to execute
     * @param <E>
     * @return result of function execution
     */
    public <E extends Throwable> void retryOnException(@Nonnull final Class<E> exception,
                                                       @Nonnull final ThrowingConsumer function) {
        Objects.requireNonNull(exception);
        Objects.requireNonNull(function);
        retryCounter = 0;
        do {
            try {
                function.apply();
                return;
            } catch (Throwable t) {
                if (isExpectedExceptionThrown(exception, t)) {
                    log.info(String.format("Catch %s: retry count = %d", t.getMessage(), retryCounter));
                    Sleeper.sleepTightInSeconds(2);
                } else {
                    throw new RuntimeException(t);
                }
            } finally {
                retryCounter++;
            }
        }
        while (retryCounter <= maxRetries);

        throw new RuntimeException("Command execution failed after " + maxRetries + " retries");
    }

    private boolean isExpectedExceptionThrown(final Class expectedExceptionClazz, final Throwable currentThrow) {
        return currentThrow.getClass().equals(expectedExceptionClazz) ||
                (currentThrow.getCause() != null && currentThrow.getCause().getClass().equals(expectedExceptionClazz));
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingConsumer {
        void apply() throws Exception;
    }
}
