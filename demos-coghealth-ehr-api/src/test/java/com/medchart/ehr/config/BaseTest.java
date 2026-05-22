package com.medchart.ehr.config;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = com.medchart.ehr.MedchartEhrApplication.class)
@ActiveProfiles("test")
public abstract class BaseTest {
}