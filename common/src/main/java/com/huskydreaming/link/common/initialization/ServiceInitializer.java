package com.huskydreaming.link.common.initialization;

import com.huskydreaming.link.common.repositories.LinkRepository;
import com.huskydreaming.link.common.services.implementations.CodeServiceImpl;
import com.huskydreaming.link.common.services.implementations.LinkServiceImpl;
import com.huskydreaming.link.common.services.interfaces.CodeService;
import com.huskydreaming.link.common.services.interfaces.LinkService;

import java.util.concurrent.ExecutorService;

/**
 * Handles initialization of business logic services.
 */
public class ServiceInitializer {

    private final ExecutorService executor;

    public ServiceInitializer(ExecutorService executor) {
        this.executor = executor;
    }

    /**
     * Creates a CodeService (generates 6-char linking codes).
     */
    public CodeService initializeCodeService() {
        return new CodeServiceImpl();
    }

    /**
     * Creates a LinkService (handles Minecraft ↔ Discord linking logic).
     */
    public LinkService initializeLinkService(LinkRepository repository) {
        return new LinkServiceImpl(repository, executor);
    }
}

