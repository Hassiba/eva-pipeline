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

<<<<<<< HEAD
import junit.framework.TestCase;
=======
import embl.ebi.variation.eva.VariantJobsArgs;
import junit.framework.TestCase;
import uk.ac.ebi.eva.pipeline.jobs.VariantStatsConfigurationTest;
import uk.ac.ebi.eva.test.utils.CommonUtils;
import uk.ac.ebi.eva.test.utils.JobTestUtils;
import uk.ac.ebi.eva.pipeline.configuration.AnnotationConfig;
import embl.ebi.variation.eva.pipeline.steps.VariantsAnnotGenerateInput;
import embl.ebi.variation.eva.pipeline.jobs.VariantAnnotConfiguration;
>>>>>>> 15d9dcd2d437c46bd24fc5e16ea5058ff22648b6
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
<<<<<<< HEAD
import uk.ac.ebi.eva.pipeline.configuration.AnnotationConfig;
import uk.ac.ebi.eva.pipeline.configuration.VariantJobsArgs;
import uk.ac.ebi.eva.pipeline.jobs.JobTestUtils;
import uk.ac.ebi.eva.pipeline.jobs.VariantAnnotConfiguration;
import uk.ac.ebi.eva.pipeline.jobs.VariantStatsConfigurationTest;
import uk.ac.ebi.eva.test.utils.CommonUtils;

import java.io.File;

import static junit.framework.TestCase.assertEquals;
=======

import java.io.*;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
>>>>>>> 15d9dcd2d437c46bd24fc5e16ea5058ff22648b6
import static junit.framework.TestCase.assertTrue;

/**
 * @author Diego Poggioli
 *
 * Test {@link VariantsAnnotGenerateInput}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { VariantAnnotConfiguration.class, AnnotationConfig.class, JobLauncherTestUtils.class})
public class VariantsAnnotGenerateInputTest {

    private static final String VARIANTS_ANNOT_GENERATE_VEP_INPUT_DB_NAME = "VariantStatsConfigurationTest_vl";

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private VariantJobsArgs variantJobsArgs;

    @Before
    public void setUp() throws Exception {
        variantJobsArgs.loadArgs();
    }

    @Test
    public void variantsAnnotGenerateInputStepShouldGenerateVepInput() throws Exception {
        String dump = VariantStatsConfigurationTest.class.getResource("/dump/").getFile();
        JobTestUtils.restoreMongoDbFromDump(dump);
        File vepInputFile = new File(variantJobsArgs.getVepInput());

        if(vepInputFile.exists())
            vepInputFile.delete();

        Assert.assertFalse(vepInputFile.exists());

        JobExecution jobExecution = jobLauncherTestUtils.launchStep(VariantsAnnotGenerateInput.FIND_VARIANTS_TO_ANNOTATE);

        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

        assertTrue(vepInputFile.exists());
        TestCase.assertEquals("20\t60343\t60343\tG/A\t+", CommonUtils.readFirstLine(vepInputFile));
        JobTestUtils.cleanDBs(VARIANTS_ANNOT_GENERATE_VEP_INPUT_DB_NAME);
    }
<<<<<<< HEAD
=======

>>>>>>> 15d9dcd2d437c46bd24fc5e16ea5058ff22648b6
}
