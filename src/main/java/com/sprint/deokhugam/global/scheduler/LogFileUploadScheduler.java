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
    public void uploadSchedule() {
        uploadYesterdayLogFile("logs");
    }

    public void uploadYesterdayLogFile(String baseDir) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String fileName = String.format("app.%s.log", yesterday);
        File logFile = new File(baseDir, fileName);

        if (!logFile.exists()) {
            log.info("[LogFileUploader] - 파일 없음: {}", logFile);
            return;
        }

        try {
            String key = s3Storage.uploadFile(logFile);
            log.info("[LogFileUploader] - 로그 파일 업로드 성공: {}", key);
        } catch (Exception e) {
            log.error("[LogFileUploader] - 로그 파일 업로드 실패: {}", logFile.getName(), e);
        }
    }
}
