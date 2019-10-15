/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.prep.reporting.builder;

import org.openmrs.module.kenyacore.report.ReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyacore.report.builder.AbstractReportBuilder;
import org.openmrs.module.kenyacore.report.builder.Builds;
import org.openmrs.module.kenyaemr.reporting.ColumnParameters;
import org.openmrs.module.kenyaemr.reporting.EmrReportingUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.prep.reporting.library.ETLReports.MOH731B.ETLMoh731BIndicatorLibrary;
import org.openmrs.module.prep.reporting.library.shared.common.CommonPrEPDimensionLibrary;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Report builder for ETL MOH 731 for Green Card
 */
@Component
@Builds({ "kenyaemr.prep.common.report.moh731B" })
public class ETLMOH731BReportBuilder extends AbstractReportBuilder {
	
	@Autowired
	private CommonPrEPDimensionLibrary commonDimensions;
	
	@Autowired
	private ETLMoh731BIndicatorLibrary moh731BIndicators;
	
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	
	ColumnParameters m_15_to_19 = new ColumnParameters(null, "15-19, Male", "gender=M|age=15-19");
	
	ColumnParameters f_15_to_19 = new ColumnParameters(null, "15-19, Female", "gender=F|age=15-19");
	
	ColumnParameters m_20_to_24 = new ColumnParameters(null, "20-24, Male", "gender=M|age=20-24");
	
	ColumnParameters f_20_to_24 = new ColumnParameters(null, "20-24, Female", "gender=F|age=20-24");
	
	ColumnParameters m_25_to_30 = new ColumnParameters(null, "25-30, Male", "gender=M|age=25-30");
	
	ColumnParameters f_25_to_30 = new ColumnParameters(null, "25-30, Female", "gender=F|age=25-30");
	
	ColumnParameters m_30_and_above = new ColumnParameters(null, "30+, Male", "gender=M|age=30+");
	
	ColumnParameters f_30_and_above = new ColumnParameters(null, "30+, Female", "gender=F|age=30+");
	
	ColumnParameters colTotal = new ColumnParameters(null, "Total", "");
	
	List<ColumnParameters> standardDisaggregationAgeAndSex = Arrays.asList(m_15_to_19, f_15_to_19, m_20_to_24, f_20_to_24,
	    m_25_to_30, f_25_to_30, m_30_and_above, f_30_and_above);
	
	@Override
	protected List<Parameter> getParameters(ReportDescriptor reportDescriptor) {
		return Arrays.asList(new Parameter("startDate", "Start Date", Date.class), new Parameter("endDate", "End Date",
		        Date.class), new Parameter("dateBasedReporting", "", String.class));
	}
	
	@Override
	protected List<Mapped<DataSetDefinition>> buildDataSets(ReportDescriptor reportDescriptor,
	        ReportDefinition reportDefinition) {
		return Arrays.asList(ReportUtils.map(prepSummaryDatasetDefinition(), "startDate=${startDate},endDate=${endDate}"));
	}
	
	/**
	 * Creates the dataset for section #1 - #8: PrEP summary reporting tool
	 * 
	 * @return the dataset
	 */
	protected DataSetDefinition prepSummaryDatasetDefinition() {
		CohortIndicatorDataSetDefinition cohortDsd = new CohortIndicatorDataSetDefinition();
		cohortDsd.setName("1");
		cohortDsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		cohortDsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		cohortDsd.addDimension("age", ReportUtils.map(commonDimensions.prepAgeGroups(), "onDate=${endDate}"));
		cohortDsd.addDimension("gender", ReportUtils.map(commonDimensions.gender()));
		String indParams = "startDate=${startDate},endDate=${endDate}";
		
		// 1.0 Prep summary report
		EmrReportingUtils.addRow(cohortDsd, "HV01", "General Population",
		    ReportUtils.map(moh731BIndicators.eligibleForPreEPGP(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08"));
		EmrReportingUtils.addRow(cohortDsd, "HV01", "MSM",
		    ReportUtils.map(moh731BIndicators.eligibleForPreEPMSM(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("09", "10", "11", "12", "13", "14", "15", "16"));
		EmrReportingUtils.addRow(cohortDsd, "HV01", "FSW",
		    ReportUtils.map(moh731BIndicators.eligibleForPreEPFSW(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("17", "18", "19", "20", "21", "22", "23", "24"));
		EmrReportingUtils.addRow(cohortDsd, "HV01", "PWID",
		    ReportUtils.map(moh731BIndicators.eligibleForPreEPPWID(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("25", "26", "27", "28", "29", "30", "31", "32"));
		EmrReportingUtils.addRow(cohortDsd, "HV01", "Discordant Couple",
		    ReportUtils.map(moh731BIndicators.eligibleForPreEPDiscordant(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("33", "34", "35", "36", "37", "38", "39", "40"));
		
		//2.0 Number newly initiated on PrEP
		EmrReportingUtils.addRow(cohortDsd, "HV02", "General Population",
		    ReportUtils.map(moh731BIndicators.newOnPrEPGP(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("41", "42", "43", "44", "45", "46", "47", "48"));
		EmrReportingUtils.addRow(cohortDsd, "HV02", "MSM", ReportUtils.map(moh731BIndicators.newOnPrEPMSM(), indParams),
		    standardDisaggregationAgeAndSex, Arrays.asList("49", "50", "51", "52", "53", "54", "55", "56"));
		EmrReportingUtils.addRow(cohortDsd, "HV02", "FSW", ReportUtils.map(moh731BIndicators.newOnPrEPFSW(), indParams),
		    standardDisaggregationAgeAndSex, Arrays.asList("57", "58", "59", "60", "61", "62", "63", "64"));
		EmrReportingUtils.addRow(cohortDsd, "HV02", "PWID", ReportUtils.map(moh731BIndicators.newOnPrEPPWID(), indParams),
		    standardDisaggregationAgeAndSex, Arrays.asList("65", "66", "67", "68", "69", "70", "71", "72"));
		EmrReportingUtils.addRow(cohortDsd, "HV02", "Discordant Couple",
		    ReportUtils.map(moh731BIndicators.newOnPrEPDiscordant(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("73", "74", "75", "76", "77", "88", "79", "80"));
		
		//3.0 Number continuing PrEP - Refill
		EmrReportingUtils.addRow(cohortDsd, "HV03", "General Population",
		    ReportUtils.map(moh731BIndicators.refillingPrEPGP(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("81", "82", "83", "84", "85", "86", "87", "88"));
		EmrReportingUtils.addRow(cohortDsd, "HV03", "MSM", ReportUtils.map(moh731BIndicators.refillingPrEPMSM(), indParams),
		    standardDisaggregationAgeAndSex, Arrays.asList("89", "90", "91", "92", "93", "94", "95", "96"));
		EmrReportingUtils.addRow(cohortDsd, "HV03", "FSW", ReportUtils.map(moh731BIndicators.refillingPrEPFSW(), indParams),
		    standardDisaggregationAgeAndSex, Arrays.asList("97", "98", "99", "100", "101", "102", "103", "104"));
		EmrReportingUtils.addRow(cohortDsd, "HV03", "PWID",
		    ReportUtils.map(moh731BIndicators.refillingPrEPPWID(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("105", "106", "107", "108", "109", "110", "111", "112"));
		EmrReportingUtils.addRow(cohortDsd, "HV03", "Discordant Couple",
		    ReportUtils.map(moh731BIndicators.refillingPrEPDiscordant(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("113", "114", "115", "116", "117", "118", "119", "120"));
		
		//4.0 Number restarting PrEP
		EmrReportingUtils.addRow(cohortDsd, "HV04", "General Population",
		    ReportUtils.map(moh731BIndicators.restartingPrEPGP(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("121", "122", "123", "124", "125", "126", "127", "128"));
		EmrReportingUtils.addRow(cohortDsd, "HV04", "MSM",
		    ReportUtils.map(moh731BIndicators.restartingPrEPMSM(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("129", "130", "131", "132", "133", "134", "135", "136"));
		EmrReportingUtils.addRow(cohortDsd, "HV04", "FSW",
		    ReportUtils.map(moh731BIndicators.restartingPrEPFSW(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("137", "138", "139", "140", "141", "142", "143", "144"));
		EmrReportingUtils.addRow(cohortDsd, "HV04", "PWID",
		    ReportUtils.map(moh731BIndicators.restartingPrEPPWID(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("145", "146", "147", "148", "149", "140", "141", "142"));
		EmrReportingUtils.addRow(cohortDsd, "HV04", "Discordant Couple",
		    ReportUtils.map(moh731BIndicators.restartingPrEPDiscordant(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("143", "144", "145", "146", "147", "148", "149", "150"));
		
		//5.0 Number currently on PrEP (New + Refill + Restart)
		EmrReportingUtils.addRow(cohortDsd, "HV05", "General Population",
		    ReportUtils.map(moh731BIndicators.currentlyOnPrEPGP(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("151", "152", "153", "154", "155", "156", "157", "158"));
		EmrReportingUtils.addRow(cohortDsd, "HV05", "MSM",
		    ReportUtils.map(moh731BIndicators.currentlyOnPrEPMSM(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("159", "160", "171", "172", "173", "174", "175", "176"));
		EmrReportingUtils.addRow(cohortDsd, "HV05", "FSW",
		    ReportUtils.map(moh731BIndicators.currentlyOnPrEPFSW(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("177", "178", "179", "180", "181", "182", "183", "184"));
		EmrReportingUtils.addRow(cohortDsd, "HV05", "PWID",
		    ReportUtils.map(moh731BIndicators.currentlyOnPrEPPWID(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("185", "186", "187", "188", "189", "190", "191", "192"));
		EmrReportingUtils.addRow(cohortDsd, "HV05", "Discordant Couple",
		    ReportUtils.map(moh731BIndicators.currentlyOnPrEPDiscordant(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("193", "194", "195", "196", "197", "198", "199", "190"));
		
		//6.0 Number tested HIV+ while on PrEP
		EmrReportingUtils.addRow(cohortDsd, "HV06", "General Population",
		    ReportUtils.map(moh731BIndicators.seroconvertedOnPrEPGP(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("191", "192", "193", "194", "195", "196", "197", "198"));
		EmrReportingUtils.addRow(cohortDsd, "HV06", "MSM",
		    ReportUtils.map(moh731BIndicators.seroconvertedOnPrEPMSM(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("199", "200", "201", "202", "203", "204", "205", "206"));
		EmrReportingUtils.addRow(cohortDsd, "HV06", "FSW",
		    ReportUtils.map(moh731BIndicators.seroconvertedOnPrEPFSW(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("207", "208", "209", "210", "211", "212", "213", "214"));
		EmrReportingUtils.addRow(cohortDsd, "HV06", "PWID",
		    ReportUtils.map(moh731BIndicators.seroconvertedOnPrEPPWID(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("215", "216", "217", "218", "219", "220", "221", "222"));
		EmrReportingUtils.addRow(cohortDsd, "HV06", "Discordant Couple",
		    ReportUtils.map(moh731BIndicators.seroconvertedOnPrEPDiscordant(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("223", "224", "225", "226", "227", "228", "229", "230"));
		
		//7.0 Number diagnosed with STI
		EmrReportingUtils.addRow(cohortDsd, "HV07", "General Population",
		    ReportUtils.map(moh731BIndicators.diagnosedWithSTIGP(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("231", "232", "233", "234", "235", "236", "237", "238"));
		EmrReportingUtils.addRow(cohortDsd, "HV07", "MSM",
		    ReportUtils.map(moh731BIndicators.diagnosedWithSTIMSM(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("239", "240", "241", "242", "243", "244", "245", "246"));
		EmrReportingUtils.addRow(cohortDsd, "HV07", "FSW",
		    ReportUtils.map(moh731BIndicators.diagnosedWithSTIFSW(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("247", "248", "249", "250", "251", "252", "253", "254"));
		EmrReportingUtils.addRow(cohortDsd, "HV07", "PWID",
		    ReportUtils.map(moh731BIndicators.diagnosedWithSTIPWID(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("255", "256", "257", "258", "259", "260", "261", "262"));
		EmrReportingUtils.addRow(cohortDsd, "HV07", "Discordant Couple",
		    ReportUtils.map(moh731BIndicators.diagnosedWithSTIDiscordant(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("263", "264", "265", "266", "267", "268", "269", "270"));
		
		//8.0 Number Discontinued PrEP
		EmrReportingUtils.addRow(cohortDsd, "HV08", "General Population",
		    ReportUtils.map(moh731BIndicators.discontinuedPrEPGP(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("271", "272", "273", "274", "275", "276", "277", "278"));
		EmrReportingUtils.addRow(cohortDsd, "HV08", "MSM",
		    ReportUtils.map(moh731BIndicators.discontinuedPrEPMSM(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("279", "280", "281", "282", "283", "284", "285", "286"));
		EmrReportingUtils.addRow(cohortDsd, "HV08", "FSW",
		    ReportUtils.map(moh731BIndicators.discontinuedPrEPFSW(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("287", "288", "289", "290", "291", "292", "293", "294"));
		EmrReportingUtils.addRow(cohortDsd, "HV08", "PWID",
		    ReportUtils.map(moh731BIndicators.discontinuedPrEPPWID(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("295", "296", "297", "298", "299", "300", "301", "302"));
		EmrReportingUtils.addRow(cohortDsd, "HV08", "Discordant Couple",
		    ReportUtils.map(moh731BIndicators.discontinuedPrEPDiscordant(), indParams), standardDisaggregationAgeAndSex,
		    Arrays.asList("303", "304", "305", "306", "307", "308", "309", "400"));
		
		return cohortDsd;
		
	}
}
