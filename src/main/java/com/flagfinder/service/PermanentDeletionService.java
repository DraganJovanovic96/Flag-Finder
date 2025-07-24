package com.mss.service;

/**
 * PermanentDeletionService interface for managing softly deleted resources.
 * The PermanentDeletionService interface contains methods that will be implemented is PermanentDeletionServiceImpl.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
public interface PermanentDeletionService {
    /**
     * Permanently deletes all resources that have been softly deleted except users.
     * <p>
     * This method is scheduled to run as a cron job every day at midnight (UTC/GMT+2).
     * It performs the cleanup by permanently removing all resources marked as softly deleted.
     * </p>
     */
    void permanentlyDeleteResources();
}
