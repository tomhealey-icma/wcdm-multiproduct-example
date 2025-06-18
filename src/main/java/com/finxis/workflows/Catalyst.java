package com.finxis.workflows;

import cdm.event.common.*;
import cdm.event.position.PortfolioState;
import cdm.event.position.PositionStatusEnum;
import cdm.event.workflow.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Array;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;

import cdm.event.workflow.Workflow;
import cdm.event.workflow.WorkflowStep;
import com.finxis.util.CsvField;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class Catalyst {


  public void processCatalystEvent () throws IOException, CsvValidationException {

    Catalyst c = new Catalyst();
    Trade trade = Trade.builder().build();

    //Output file
    String fileName = "./sample-output/catalyst2Cdm.csv";
    String[] header = {"tradestate", "tradestatus", "action", "actionsubtype", "cdm workflow"};
    BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

    writer.write(String.join("|", header));
    writer.newLine();

    int rowsCount = -1;
    BufferedReader csvReader;

    char delimiter = ',';
    File csvFile  = new File("./src/main/resources/Catalyst-Events.csv");

    csvReader = new BufferedReader(new FileReader(csvFile));

    //** Now using the OpenCSV **//
    CSVParser parser = new CSVParserBuilder()
            .withSeparator(delimiter)
            .build();

    CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile))
            .withCSVParser(parser)
            .build();

    ArrayList<CsvField> csvMap = new ArrayList<CsvField>(40);

    String[] nextLine;
    int line = 0;
    List<String> headers = new ArrayList<String>(5);
    nextLine = reader.readNext();
    for (String col : nextLine) {
      headers.add(col);
    }

    String[] data = new String[5];
    String cdmWorkflow = null;

    while ((nextLine = reader.readNext()) != null) {

          String tradeState=nextLine[0];
          String tradeStatus=nextLine[1];
          String action=nextLine[2];
          String actionSubtype=nextLine[3];

          cdmWorkflow = RosettaObjectMapper.getNewRosettaObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(c.createWorkflowEvent(trade, tradeState, tradeStatus, action, actionSubtype));

          data = new String[]{tradeState, tradeStatus, action, actionSubtype, cdmWorkflow};
          writer.write(String.join("|", data));
          writer.newLine();

          System.out.println(c.createWorkflowEvent(trade, tradeState, tradeStatus, action, actionSubtype));

      }
      line++;
  }

  public WorkflowStep createWorkflowEvent (Trade trade, String tradeStateStr, String tradeStatusStr, String actionStr, String actionSubtypeStr){

    State state = null;
    switch (tradeStatusStr) {
      case "ACTIVE":
        state = State.builder()
                .setPositionState(PositionStatusEnum.EXECUTED)
                .build();
            break;
      case "CNCL":
        state = State.builder()
                .setClosedState(ClosedState.builder()
                        .setState(ClosedStateEnum.CANCELLED))
                .build();
        break;
      default:
        state = State.builder()
                .setClosedState(ClosedState.builder()
                        .setState(ClosedStateEnum.TERMINATED))
                .build();
        break;

    }

    TradeState tradeState  = TradeState.builder()
            .setTrade(trade)
            .setState(state)
            .build();

    ActionEnum actionEnum = null;
    WorkflowStatusEnum workflowStatusEnum = null;

    switch (actionStr) {
      case "AMEND":
        actionEnum = ActionEnum.CORRECT;
        workflowStatusEnum = WorkflowStatusEnum.AMENDED;
        break;
      case "CNCL":
        actionEnum = ActionEnum.CANCEL;
        workflowStatusEnum = WorkflowStatusEnum.CANCELLED;
        break;
      case "NEW":
        actionEnum = ActionEnum.NEW;
        workflowStatusEnum = WorkflowStatusEnum.SUBMITTED;
        break;
      case "OUTCNCL":
        actionEnum = ActionEnum.CANCEL;
        workflowStatusEnum = WorkflowStatusEnum.CANCELLED;
        break;
      case "TRADEUPDATE":
        actionEnum = ActionEnum.CORRECT;
        workflowStatusEnum = WorkflowStatusEnum.AMENDED;
        break;
      default:
        actionEnum = ActionEnum.CANCEL;
        workflowStatusEnum = WorkflowStatusEnum.CANCELLED;
        break;
    }

    WorkflowStep wfs = WorkflowStep.builder()
          .setAction(actionEnum)
            .setBusinessEvent(BusinessEvent.builder()
                    .setAfter(List.of(tradeState)))
          .setWorkflowState(WorkflowState.builder()
                  .setWorkflowStatus(workflowStatusEnum)
                  .setPartyCustomisedWorkflow(List.of(PartyCustomisedWorkflow.builder()
                          .setCustomisedWorkflow(List.of(CustomisedWorkflow.builder()
                                  .setItemName("ACTION_SUBTYPE")
                                  .setItemValue(actionSubtypeStr),
                                CustomisedWorkflow.builder()
                                  .setItemName("TRADESTATE")
                                  .setItemValue(tradeStateStr))))))
          .build();

  return wfs;

  }



}
