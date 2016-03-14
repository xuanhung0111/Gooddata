package com.gooddata.qa.utils.mail;

/**
 * This functional interface is used to avoid creating many Imap connections
 *
 * @param <T>
 */
public interface ImapClientAction<T> {
    public T doAction(ImapClient imapClient) throws Throwable;

    public static final class Utils {
        /**
         * The method is used for creating a connection to Imap to perform actions 
         * which are passed by action argument  
         * 
         * @param host
         * @param email
         * @param password
         * @param action
         * @return
         */
        public static final <T> T doActionWithImapClient(String host, String email, String password, ImapClientAction<T> action) {
            try(ImapClient imapClient = new ImapClient(host, email, password)) {
                try {
                    return action.doAction(imapClient);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            }
        }
    } 
}
