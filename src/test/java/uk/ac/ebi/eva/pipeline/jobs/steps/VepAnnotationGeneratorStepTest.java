/*
 * Copyright 2016 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.pipeline.jobs.steps;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.eva.pipeline.configuration.AnnotationConfiguration;
import uk.ac.ebi.eva.pipeline.jobs.AnnotationJob;
import uk.ac.ebi.eva.pipeline.jobs.flows.AnnotationFlow;
import uk.ac.ebi.eva.pipeline.parameters.JobOptions;
import uk.ac.ebi.eva.test.rules.PipelineTemporaryFolderRule;
import uk.ac.ebi.eva.test.utils.JobTestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.*;
import static uk.ac.ebi.eva.test.utils.TestFileUtils.getResource;

/**
 * Test for {@link VepAnnotationGeneratorStep}
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("variant-annotation-mongo")
@ContextConfiguration(classes = {JobOptions.class, AnnotationJob.class, AnnotationConfiguration.class, JobLauncherTestUtils.class})
public class VepAnnotationGeneratorStepTest {

    private static final String VEP_INPUT_CONTENT = "20\t60343\t60343\tG/A\t+";
    private static final String MOCKVEP = "/mockvep.pl";
    @Rule
    public PipelineTemporaryFolderRule temporaryFolderRule = new PipelineTemporaryFolderRule();

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private JobOptions jobOptions;

    @Before
    public void setUp() throws Exception {
        jobOptions.loadArgs();
        jobOptions.setAppVepPath(getResource(MOCKVEP));
    }

    @Test
    public void shouldGenerateVepAnnotations() throws Exception {
        jobOptions.setVepInputFile(temporaryFolderRule.newGzipFile(VEP_INPUT_CONTENT).getAbsolutePath());
        File vepOutputFile = temporaryFolderRule.newFile();
        jobOptions.setVepOutput(vepOutputFile.getAbsolutePath());

        // When the execute method in variantsAnnotCreate is executed
        JobExecution jobExecution = jobLauncherTestUtils.launchStep(AnnotationFlow.GENERATE_VEP_ANNOTATION);

        //Then variantsAnnotCreate step should complete correctly
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

        // And VEP output should exist and annotations should be in the file
        assertTrue(vepOutputFile.exists());
        Assert.assertEquals(537, JobTestUtils.getLines(new GZIPInputStream(new FileInputStream(vepOutputFile))));
    }

}