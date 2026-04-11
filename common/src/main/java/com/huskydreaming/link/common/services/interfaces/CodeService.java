package com.huskydreaming.link.common.services.interfaces;

import java.util.Optional;
import java.util.UUID;

public interface CodeService {
    boolean hasCode(UUID uuid);

    String generate(UUID uuid);
    Optional<UUID> consume(String code);
}
