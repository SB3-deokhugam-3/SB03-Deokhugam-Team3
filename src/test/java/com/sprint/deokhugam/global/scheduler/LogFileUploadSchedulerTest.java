package com.sprint.deokhugam.global.scheduler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sprint.deokhugam.global.storage.S3Storage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogFileUploadSchedulerTest {

    @InjectMocks
    private LogFileUploadScheduler scheduler;

    @Mock
    private S3Storage s3Storage;

    @Test
    void 로그_파일이_존재하면_S3에_업로드_한다(@TempDir Path tempDir) throws Exception {

        // given
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String fileName = String.format("app.%s.log", yesterday);
        Path logFilePath = tempDir.resolve(fileName);
        File logFile = logFilePath.toFile();

        Files.writeString(logFile.toPath(), "테스트 로그 내용");

        String expectedKey = "logs/" + fileName;
        given(s3Storage.uploadFile(logFile)).willReturn(expectedKey);

        // when
        scheduler.uploadYesterdayLogFile(tempDir.toString());

        // then
        verify(s3Storage).uploadFile(logFile);
    }

    @Test
    void 로그_파일이_없으면_업로드하지_않는다(@TempDir Path tempDir) {

        // given
        Path notExistDir = tempDir.resolve("testLogs");
        notExistDir.toFile().mkdirs();

        // when
        scheduler.uploadYesterdayLogFile(notExistDir.toString());

        // then
        verify(s3Storage, never()).uploadFile(any());
    }
}