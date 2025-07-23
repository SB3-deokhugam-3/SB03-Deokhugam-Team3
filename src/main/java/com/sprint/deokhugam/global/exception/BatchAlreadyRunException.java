package com.sprint.deokhugam.global.exception;

import java.util.Map;

public class BatchAlreadyRunException extends DomainException {

    // 1. 도메인 있을 때
    public BatchAlreadyRunException(String domain,
        Map<String, Object> details) {
        super(domain, ErrorCode.BATCH_ALREADY_RUN, details);
    }

    // 2. 없을 때
    public BatchAlreadyRunException(Map<String, Object> details) {
        super(ErrorCode.BATCH_ALREADY_RUN, details);
    }
}
