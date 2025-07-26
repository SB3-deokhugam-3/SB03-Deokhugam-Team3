package com.sprint.deokhugam.global.scheduler;

import com.sprint.deokhugam.global.storage.S3Storage;
import java.io.File;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogFileUploadScheduler {

    private final S3Storage s3Storage;

    @Scheduled(cron = "0 0 1 * * *")
    //@Scheduled(cron = "*/10 * * * * *")
    public void uploadYesterdayLogFile() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String logFilePath = String.format("logs/app.%s.log", yesterday);
        File logFile = new File(logFilePath);

        if (!logFile.exists()) {
            log.info("[LogFileUploader] - 파일 없음: {}", logFilePath);
            return;
        }

        String key = s3Storage.uploadFile(logFile);
        log.info("[LogFileUploader] - 로그 파일 업로드 성공: {}", key);
    }
}
