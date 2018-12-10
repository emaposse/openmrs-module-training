/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.ArrayList;

import org.openmrs.module.eptsreports.reporting.library.cohorts.AgeCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.GenderCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TxNewCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.indicators.BreastfeedingIndicators;
import org.openmrs.module.eptsreports.reporting.library.indicators.HivIndicators;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TxNewDataset extends BaseDataSet {
	
	@Autowired
	private AgeCohortQueries ageCohortQueries;
	
	@Autowired
	private GenderCohortQueries genderCohortQueries;
	
	@Autowired
	private TxNewCohortQueries txNewCohortQueries;
	
	@Autowired
	private HivIndicators hivIndicators;
	
	@Autowired
	private BreastfeedingIndicators breastfeedingIndicators;
	
	public DataSetDefinition constructTxNewDatset() {
		
		CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
		dataSetDefinition.setName("TX_NEW Data Set");
		dataSetDefinition.addParameters(getParameters());
		
		// Looks for patients enrolled in ART program (program 2=SERVICO TARV -
		// TRATAMENTO) before or on end date
		CohortDefinition inARTProgramDuringTimePeriod = txNewCohortQueries.getPatientsinARTProgramDuringTimePeriod();
		
		// Looks for patients registered as START DRUGS (answer to question 1255 = ARV
		// PLAN is 1256 = START DRUGS) in the first drug pickup (encounter type
		// 18=S.TARV: FARMACIA) or follow up consultation for adults and children
		// (encounter types 6=S.TARV: ADULTO SEGUIMENTO and 9=S.TARV: PEDIATRIA
		// SEGUIMENTO) before or on end date
		CohortDefinition patientWithSTARTDRUGSObs = txNewCohortQueries.getPatientWithSTARTDRUGSObs();
		
		// Looks for with START DATE (Concept 1190=HISTORICAL DRUG START DATE) filled in
		// drug pickup (encounter type 18=S.TARV: FARMACIA) or follow up consultation
		// for adults and children (encounter types 6=S.TARV: ADULTO SEGUIMENTO and
		// 9=S.TARV: PEDIATRIA SEGUIMENTO) where START DATE is before or equal end date
		CohortDefinition patientWithHistoricalDrugStartDateObs = txNewCohortQueries.getPatientWithHistoricalDrugStartDateObs();
		
		// Looks for patients who had at least one drug pick up (encounter type
		// 18=S.TARV: FARMACIA) before end date
		CohortDefinition patientsWithDrugPickUpEncounters = txNewCohortQueries.getPatientWithFirstDrugPickupEncounter();
		
		// Looks for patients enrolled on ART program (program 2=SERVICO TARV -
		// TRATAMENTO), transferred from other health facility (program workflow state
		// is 29=TRANSFER FROM OTHER FACILITY) between start date and end date
		CohortDefinition transferredFromOtherHealthFacility = txNewCohortQueries.getPatientsTransferredFromOtherHealthFacility();
		
		CohortDefinition males = genderCohortQueries.MaleCohort();
		
		CohortDefinition females = genderCohortQueries.FemaleCohort();
		
		CohortDefinition PatientBelow1Year = ageCohortQueries.patientWithAgeBelow(1);
		CohortDefinition PatientBetween1And9Years = ageCohortQueries.createXtoYAgeCohort("PatientBetween1And9Years", 1, 9);
		CohortDefinition PatientBetween10And14Years = ageCohortQueries.createXtoYAgeCohort("PatientBetween10And14Years", 10, 14);
		CohortDefinition PatientBetween15And19Years = ageCohortQueries.createXtoYAgeCohort("PatientBetween15And19Years", 15, 19);
		CohortDefinition PatientBetween20And24Years = ageCohortQueries.createXtoYAgeCohort("PatientBetween20And24Years", 20, 24);
		CohortDefinition PatientBetween25And29Years = ageCohortQueries.createXtoYAgeCohort("PatientBetween25And29Years", 25, 29);
		CohortDefinition PatientBetween30And34Years = ageCohortQueries.createXtoYAgeCohort("PatientBetween30And34Years", 30, 34);
		CohortDefinition PatientBetween35And39Years = ageCohortQueries.createXtoYAgeCohort("PatientBetween35And39Years", 35, 39);
		CohortDefinition PatientBetween40And49Years = ageCohortQueries.createXtoYAgeCohort("PatientBetween40And49Years", 40, 49);
		CohortDefinition PatientBetween50YearsAndAbove = ageCohortQueries.patientWithAgeAbove(50);
		PatientBetween50YearsAndAbove.setName("PatientBetween50YearsAndAbove");
		
		ArrayList<CohortDefinition> agesRange = new ArrayList<CohortDefinition>();
		agesRange.add(PatientBetween10And14Years);
		agesRange.add(PatientBetween15And19Years);
		agesRange.add(PatientBetween20And24Years);
		agesRange.add(PatientBetween25And29Years);
		agesRange.add(PatientBetween30And34Years);
		agesRange.add(PatientBetween35And39Years);
		agesRange.add(PatientBetween40And49Years);
		agesRange.add(PatientBetween50YearsAndAbove);
		
		// Male and Female <1
		CohortDefinition patientBelow1YearEnrolledInHIVStartedART = txNewCohortQueries.getTxNewCompositionCohort(
		    "patientBelow1YearEnrolledInHIVStartedART", inARTProgramDuringTimePeriod, patientWithSTARTDRUGSObs,
		    patientWithHistoricalDrugStartDateObs, patientsWithDrugPickUpEncounters, transferredFromOtherHealthFacility,
		    PatientBelow1Year, null);
		CohortIndicator patientBelow1YearEnrolledInHIVStartedARTIndicator = hivIndicators
		        .patientBelow1YearEnrolledInHIVStartedARTIndicator(patientBelow1YearEnrolledInHIVStartedART);
		dataSetDefinition.addColumn("1<1", "TX_NEW: New on ART: Patients below 1 year",
		    new Mapped<CohortIndicator>(patientBelow1YearEnrolledInHIVStartedARTIndicator,
		            ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},location=${location}")),
		    "");
		
		// Male and Female between 1 and 9 years
		CohortDefinition patientBetween1And9YearsEnrolledInHIVStartedART = txNewCohortQueries.getTxNewCompositionCohort(
		    "patientBetween1And9YearEnrolledInHIVStartedART", inARTProgramDuringTimePeriod, patientWithSTARTDRUGSObs,
		    patientWithHistoricalDrugStartDateObs, patientsWithDrugPickUpEncounters, transferredFromOtherHealthFacility,
		    PatientBetween1And9Years, null);
		CohortIndicator patientBetween1And9YearsEnrolledInHIVStartedARTIndicator = hivIndicators
		        .patientBetween1And9YearsEnrolledInHIVStartedARTIndicator(patientBetween1And9YearsEnrolledInHIVStartedART);
		dataSetDefinition.addColumn("119", "TX_NEW: New on ART: Patients Between 1 and 9 years",
		    new Mapped<CohortIndicator>(patientBetween1And9YearsEnrolledInHIVStartedARTIndicator,
		            ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},location=${location}")),
		    "");
		
		// Male
		int i = 2;
		for (CohortDefinition ageCohort : agesRange) {
			CohortDefinition patientInYearRangeEnrolledInARTStarted = txNewCohortQueries.getTxNewCompositionCohort(
			    "patientEnrolledInARTStartedMales", inARTProgramDuringTimePeriod, patientWithSTARTDRUGSObs,
			    patientWithHistoricalDrugStartDateObs, patientsWithDrugPickUpEncounters, transferredFromOtherHealthFacility, ageCohort,
			    males);
			CohortIndicator patientInYearRangeEnrolledInHIVStartedARTIndicator = hivIndicators
			        .patientInYearRangeEnrolledInHIVStartedARTIndicator(patientInYearRangeEnrolledInARTStarted);
			dataSetDefinition.addColumn("1M" + i, "Males:TX_NEW: New on ART by age and sex: " + ageCohort.getName(),
			    new Mapped<CohortIndicator>(patientInYearRangeEnrolledInHIVStartedARTIndicator,
			            ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},location=${location}")),
			    "");
			
			i++;
		}
		
		// Female
		int j = 2;
		for (CohortDefinition ageCohort : agesRange) {
			CohortDefinition patientInYearRangeEnrolledInARTStarted = txNewCohortQueries.getTxNewCompositionCohort(
			    "patientEnrolledInARTStartedMales", inARTProgramDuringTimePeriod, patientWithSTARTDRUGSObs,
			    patientWithHistoricalDrugStartDateObs, patientsWithDrugPickUpEncounters, transferredFromOtherHealthFacility, ageCohort,
			    females);
			CohortIndicator patientInYearRangeEnrolledInHIVStartedARTIndicator = hivIndicators
			        .patientInYearRangeEnrolledInHIVStartedARTIndicator(patientInYearRangeEnrolledInARTStarted);
			dataSetDefinition.addColumn("1F" + j, "Females:TX_NEW: New on ART by age and sex: " + ageCohort.getName(),
			    new Mapped<CohortIndicator>(patientInYearRangeEnrolledInHIVStartedARTIndicator,
			            ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},location=${location}")),
			    "");
			j++;
		}
		
		CohortDefinition patientEnrolledInART = txNewCohortQueries.getTxNewCompositionCohort("patientEnrolledInART",
		    inARTProgramDuringTimePeriod, patientWithSTARTDRUGSObs, patientWithHistoricalDrugStartDateObs,
		    patientsWithDrugPickUpEncounters, transferredFromOtherHealthFacility, null, null);
		CohortIndicator patientEnrolledInHIVStartedARTIndicator = hivIndicators
		        .patientEnrolledInHIVStartedARTIndicator(patientEnrolledInART);
		dataSetDefinition.addColumn("1All", "TX_NEW: New on ART",
		    new Mapped<CohortIndicator>(patientEnrolledInHIVStartedARTIndicator,
		            ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},location=${location}")),
		    "");
		
		// Obtain patients notified to be on TB treatment
		CohortIndicator breastfeedingNewlyInitiatingARTIndicator = breastfeedingIndicators.getBreastfeedingWomenStartedART();
		dataSetDefinition.addColumn("ANC", "TX_NEW: Pregnant Started ART",
		    new Mapped<CohortIndicator>(breastfeedingNewlyInitiatingARTIndicator,
		            ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},location=${location}")),
		    "");
		
		return dataSetDefinition;
	}
}
