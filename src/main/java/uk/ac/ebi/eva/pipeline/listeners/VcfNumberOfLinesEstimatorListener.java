/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.pipeline.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import uk.ac.ebi.eva.pipeline.parameters.JobParametersNames;
import uk.ac.ebi.eva.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

/**
 * Estimate the number of lines in a VCF file.
 * <p>
 * Given that the VCF file could be VERY big then we estimate the number of lines using the following steps:
 * 1) retrieve the size in bytes of the whole zipped VCF (vcfFileSize)
 * 2) retrieve the size in bytes of the head of the VCF in a zipped file (vcfHeadFileSize)
 * 3) retrieve the size in bytes of the first 100 lines of the VCF in a zipped file and divide the value by 100 to
 * obtain the size of a single line (singleVcfLineSize)
 * 4) calculate the (vcfFileSize - vcfHeadFileSize) / singleVcfLineSize to estimate the total number of line in the VCF.
 * <p>
 * Why 100 in point 3?
 * Tested on a VCF with 157049 lines, 100 is the best and minimum number of lines to compress. This should generate an
 * estimated total number of lines similar to the real one.
 * <p>
 * In case of small VCF the NUMBER_OF_LINES will be 0 and no % will be printed in {@link StepProgressListener#afterChunk}
 */
public class VcfNumberOfLinesEstimatorListener implements StepExecutionListener {
    private static final Logger logger = LoggerFactory.getLogger(VcfNumberOfLinesEstimatorListener.class);

    private static final int NUMBER_OF_LINES = 100;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        logger.debug("Estimating the number of lines in the VCF file");
        int estimatedTotalNumberOfLines = 0;

        String vcfFilePath = stepExecution.getJobExecution().getJobParameters().getString(JobParametersNames.INPUT_VCF);

        String vcfHead;
        String vcfSection;

        try {
            vcfHead = retrieveVcfHead(vcfFilePath);
            vcfSection = retrieveVcfSection(vcfFilePath);
        } catch (IOException e) {
            throw new RuntimeException("Error reading VCF " + vcfFilePath, e);
        }

        if (!vcfSection.isEmpty()) {
            File vcfHeadFile;
            File vcfSectionFile;

            try {
                vcfHeadFile = FileUtils.newGzipFile(vcfHead, "vcfHeadFile");
                vcfSectionFile = FileUtils.newGzipFile(vcfSection, "vcfSectionFile");
            } catch (IOException e) {
                throw new RuntimeException("Error while creating zip file", e);
            }

            double vcfFileSize = new File(vcfFilePath).length();
            double singleVcfLineSize = (vcfSectionFile != null ? vcfSectionFile.length() : 0) / NUMBER_OF_LINES;
            double vcfHeadFileSize = vcfHeadFile != null ? vcfHeadFile.length() : 0;

            estimatedTotalNumberOfLines = (int) ((vcfFileSize - vcfHeadFileSize) / singleVcfLineSize);
        }

        logger.debug("Estimated number of lines in VCF file: {}", estimatedTotalNumberOfLines);
        stepExecution.getExecutionContext().put(JobParametersNames.NUMBER_OF_LINES, estimatedTotalNumberOfLines);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }

    /**
     * @param vcfFilePath location of the VCF to parse
     * @return the head of the VCF
     * @throws IOException
     */
    private String retrieveVcfHead(String vcfFilePath) throws IOException {
        String vcfHead = "";

        Scanner scanner = new Scanner(new GZIPInputStream(new FileInputStream(vcfFilePath)));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("#")) {
                vcfHead += line + "\n";
            }
        }
        scanner.close();

        return vcfHead;
    }

    /**
     * @param vcfFilePath location of the VCF to parse
     * @return first NUMBER_OF_LINES of VCF, empty sting in case of VCF smaller than NUMBER_OF_LINES
     * @throws IOException
     */
    private String retrieveVcfSection(String vcfFilePath) throws IOException {
        String vcfSection = "";

        int lineCnt = NUMBER_OF_LINES;
        Scanner scanner = new Scanner(new GZIPInputStream(new FileInputStream(vcfFilePath)));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!line.startsWith("#")) {
                lineCnt--;
                vcfSection += line + "\n";
                if (lineCnt == 0) {
                    break;
                }
            }
        }
        scanner.close();

        //in case of small VCF
        if (lineCnt > 0) {
            return "";
        }

        return vcfSection;
    }

}
