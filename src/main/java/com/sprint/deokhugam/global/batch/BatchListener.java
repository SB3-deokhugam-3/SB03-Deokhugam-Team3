package com.sprint.deokhugam.global.batch;//package com.sprint.deokhugam.global.batchtestchunk;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BatchListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info(
            "----------- Listener: STEP [step Name:{}] START! -----------",
            stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        ExitStatus status = stepExecution.getExitStatus();
        if (status.equals(ExitStatus.COMPLETED)) {
            log.info("✅ Step '{}' 완료 - Read: {}, Write: {}",
                stepExecution.getStepName(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount());
        } else {
            log.warn("❌ Step '{}' 실패 - Status: {}",
                stepExecution.getStepName(), status.getExitCode());
        }
        return status;
    }
}
