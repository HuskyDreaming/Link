package com.huskydreaming.link.common.repositories;

/**
 * Unchecked exception thrown when a repository operation fails (e.g. SQL error).
 */
public class RepositoryException extends RuntimeException {

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryException(Throwable cause) {
        super(cause);
    }
}

