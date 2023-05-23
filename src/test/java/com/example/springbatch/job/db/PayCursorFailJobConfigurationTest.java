package com.example.springbatch.job.db;

import com.example.springbatch.entity.Pay;
import com.example.springbatch.job.reader.db.pay.PayCursorJobConfiguration;
import com.example.springbatch.repository.PayRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {"job.name=" + PayCursorJobConfiguration.JOB_NAME})
class PayCursorFailJobConfigurationTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private PayRepository payRepository;

    @Test
    void 같은조건으로_업데이트() throws Exception {
        for (long i = 0; i < 50; i++) {
            payRepository.save(new Pay(i, false));
        }

        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(payRepository.findAllSuccess().size()).isEqualTo(50);
    }
}