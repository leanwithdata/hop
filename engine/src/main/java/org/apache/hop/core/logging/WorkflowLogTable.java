/*! ******************************************************************************
 *
 * Hop : The Hop Orchestration Platform
 *
 * http://www.project-hop.org
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.apache.hop.core.logging;

import org.apache.hop.core.Const;
import org.apache.hop.core.HopClientEnvironment;
import org.apache.hop.core.Result;
import org.apache.hop.core.RowMetaAndData;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.value.ValueMetaBase;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.xml.XMLHandler;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.workflow.Workflow;
import org.apache.hop.metastore.api.IMetaStore;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * This class describes a workflow logging table
 *
 * @author matt
 */
public class WorkflowLogTable extends BaseLogTable implements Cloneable, ILogTable {

  private static Class<?> PKG = WorkflowLogTable.class; // for i18n purposes, needed by Translator!!

  public static final String XML_TAG = "workflow-log-table";

  public enum ID {

    ID_WORKFLOW( "ID_WORKFLOW" ), CHANNEL_ID( "CHANNEL_ID" ), WORKFLOW_NAME( "WORKFLOW_NAME" ), STATUS( "STATUS" ), LINES_READ(
      "LINES_READ" ), LINES_WRITTEN( "LINES_WRITTEN" ), LINES_UPDATED( "LINES_UPDATED" ), LINES_INPUT(
      "LINES_INPUT" ), LINES_OUTPUT( "LINES_OUTPUT" ), LINES_REJECTED( "LINES_REJECTED" ), ERRORS( "ERRORS" ),
    STARTDATE( "STARTDATE" ), ENDDATE( "ENDDATE" ), LOGDATE( "LOGDATE" ), DEPDATE( "DEPDATE" ), REPLAYDATE(
      "REPLAYDATE" ), LOG_FIELD( "LOG_FIELD" ), EXECUTING_SERVER( "EXECUTING_SERVER" ), EXECUTING_USER(
      "EXECUTING_USER" ), START_ACTION( "START_ACTION" ), CLIENT( "CLIENT" );

    private String id;

    private ID( String id ) {
      this.id = id;
    }

    public String toString() {
      return id;
    }
  }

  private String logInterval;

  private String logSizeLimit;

  private WorkflowLogTable( IVariables variables, IMetaStore metaStore ) {
    super( variables, metaStore, null, null, null );
  }

  @Override
  public Object clone() {
    try {
      WorkflowLogTable table = (WorkflowLogTable) super.clone();
      table.fields = new ArrayList<LogTableField>();
      for ( LogTableField field : this.fields ) {
        table.fields.add( (LogTableField) field.clone() );
      }
      return table;
    } catch ( CloneNotSupportedException e ) {
      return null;
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "      " ).append( XMLHandler.openTag( XML_TAG ) ).append( Const.CR );
    retval.append( "        " ).append( XMLHandler.addTagValue( "connection", connectionName ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "schema", schemaName ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "table", tableName ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "size_limit_lines", logSizeLimit ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "interval", logInterval ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "timeout_days", timeoutInDays ) );
    retval.append( super.getFieldsXML() );
    retval.append( "      " ).append( XMLHandler.closeTag( XML_TAG ) ).append( Const.CR );

    return retval.toString();
  }

  public void loadXML( Node workflowNode, List<TransformMeta> transforms ) {
    connectionName = XMLHandler.getTagValue( workflowNode, "connection" );
    schemaName = XMLHandler.getTagValue( workflowNode, "schema" );
    tableName = XMLHandler.getTagValue( workflowNode, "table" );
    logSizeLimit = XMLHandler.getTagValue( workflowNode, "size_limit_lines" );
    logInterval = XMLHandler.getTagValue( workflowNode, "interval" );
    timeoutInDays = XMLHandler.getTagValue( workflowNode, "timeout_days" );

    super.loadFieldsXML( workflowNode );
  }

  @Override
  public void replaceMeta( ILogTableCore logTableInterface ) {
    if ( !( logTableInterface instanceof WorkflowLogTable ) ) {
      return;
    }

    WorkflowLogTable logTable = (WorkflowLogTable) logTableInterface;
    super.replaceMeta( logTable );
    logInterval = logTable.logInterval;
    logSizeLimit = logTable.logSizeLimit;
  }

  //CHECKSTYLE:LineLength:OFF
  public static WorkflowLogTable getDefault( IVariables variables, IMetaStore metaStore ) {
    WorkflowLogTable table = new WorkflowLogTable( variables, metaStore );

    table.fields.add(
      new LogTableField( ID.ID_WORKFLOW.id, true, false, "ID_WORKFLOW", BaseMessages.getString( PKG, "JobLogTable.FieldName.BatchID" ), BaseMessages.getString( PKG, "JobLogTable.FieldDescription.BatchID" ),
        IValueMeta.TYPE_INTEGER, 8 ) );
    table.fields.add( new LogTableField( ID.CHANNEL_ID.id, true, false, "CHANNEL_ID", BaseMessages.getString( PKG, "JobLogTable.FieldName.ChannelID" ),
      BaseMessages.getString( PKG, "JobLogTable.FieldDescription.ChannelID" ), IValueMeta.TYPE_STRING, 255 ) );
    table.fields.add(
      new LogTableField( ID.WORKFLOW_NAME.id, true, false, "WORKFLOW_NAME", BaseMessages.getString( PKG, "JobLogTable.FieldName.JobName" ), BaseMessages.getString( PKG, "JobLogTable.FieldDescription.JobName" ),
        IValueMeta.TYPE_STRING, 255 ) );
    table.fields.add(
      new LogTableField( ID.STATUS.id, true, false, "STATUS", BaseMessages.getString( PKG, "JobLogTable.FieldName.Status" ), BaseMessages.getString( PKG, "JobLogTable.FieldDescription.Status" ),
        IValueMeta.TYPE_STRING, 15 ) );
    table.fields.add( new LogTableField( ID.LINES_READ.id, true, false, "LINES_READ", BaseMessages.getString( PKG, "JobLogTable.FieldName.LinesRead" ),
      BaseMessages.getString( PKG, "JobLogTable.FieldDescription.LinesRead" ), IValueMeta.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_WRITTEN.id, true, false, "LINES_WRITTEN", BaseMessages.getString( PKG, "JobLogTable.FieldName.LinesWritten" ),
      BaseMessages.getString( PKG, "JobLogTable.FieldDescription.LinesWritten" ), IValueMeta.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_UPDATED.id, true, false, "LINES_UPDATED", BaseMessages.getString( PKG, "JobLogTable.FieldName.LinesUpdated" ),
      BaseMessages.getString( PKG, "JobLogTable.FieldDescription.LinesUpdated" ), IValueMeta.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_INPUT.id, true, false, "LINES_INPUT", BaseMessages.getString( PKG, "JobLogTable.FieldName.LinesInput" ),
      BaseMessages.getString( PKG, "JobLogTable.FieldDescription.LinesInput" ), IValueMeta.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_OUTPUT.id, true, false, "LINES_OUTPUT", BaseMessages.getString( PKG, "JobLogTable.FieldName.LinesOutput" ),
      BaseMessages.getString( PKG, "JobLogTable.FieldDescription.LinesOutput" ), IValueMeta.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_REJECTED.id, true, false, "LINES_REJECTED", BaseMessages.getString( PKG, "JobLogTable.FieldName.LinesRejected" ),
      BaseMessages.getString( PKG, "JobLogTable.FieldDescription.LinesRejected" ), IValueMeta.TYPE_INTEGER, 18 ) );
    table.fields.add(
      new LogTableField( ID.ERRORS.id, true, false, "ERRORS", BaseMessages.getString( PKG, "JobLogTable.FieldName.Errors" ), BaseMessages.getString( PKG, "JobLogTable.FieldDescription.Errors" ),
        IValueMeta.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.STARTDATE.id, true, false, "STARTDATE", BaseMessages.getString( PKG, "JobLogTable.FieldName.StartDateRange" ),
      BaseMessages.getString( PKG, "JobLogTable.FieldDescription.StartDateRange" ), IValueMeta.TYPE_DATE, -1 ) );
    table.fields.add( new LogTableField( ID.ENDDATE.id, true, false, "ENDDATE", BaseMessages.getString( PKG, "JobLogTable.FieldName.EndDateRange" ),
      BaseMessages.getString( PKG, "JobLogTable.FieldDescription.EndDateRange" ), IValueMeta.TYPE_DATE, -1 ) );
    table.fields.add(
      new LogTableField( ID.LOGDATE.id, true, false, "LOGDATE", BaseMessages.getString( PKG, "JobLogTable.FieldName.LogDate" ), BaseMessages.getString( PKG, "JobLogTable.FieldDescription.LogDate" ),
        IValueMeta.TYPE_DATE, -1 ) );
    table.fields.add(
      new LogTableField( ID.DEPDATE.id, true, false, "DEPDATE", BaseMessages.getString( PKG, "JobLogTable.FieldName.DepDate" ), BaseMessages.getString( PKG, "JobLogTable.FieldDescription.DepDate" ),
        IValueMeta.TYPE_DATE, -1 ) );
    table.fields.add( new LogTableField( ID.REPLAYDATE.id, true, false, "REPLAYDATE", BaseMessages.getString( PKG, "JobLogTable.FieldName.ReplayDate" ),
      BaseMessages.getString( PKG, "JobLogTable.FieldDescription.ReplayDate" ), IValueMeta.TYPE_DATE, -1 ) );
    table.fields.add( new LogTableField( ID.LOG_FIELD.id, true, false, "LOG_FIELD", BaseMessages.getString( PKG, "JobLogTable.FieldName.LogField" ),
      BaseMessages.getString( PKG, "JobLogTable.FieldDescription.LogField" ), IValueMeta.TYPE_STRING, DatabaseMeta.CLOB_LENGTH ) );
    table.fields.add( new LogTableField( ID.EXECUTING_SERVER.id, false, false, "EXECUTING_SERVER", BaseMessages.getString( PKG, "JobLogTable.FieldName.ExecutingServer" ),
      BaseMessages.getString( PKG, "JobLogTable.FieldDescription.ExecutingServer" ), IValueMeta.TYPE_STRING, 255 ) );
    table.fields.add( new LogTableField( ID.EXECUTING_USER.id, false, false, "EXECUTING_USER", BaseMessages.getString( PKG, "JobLogTable.FieldName.ExecutingUser" ),
      BaseMessages.getString( PKG, "JobLogTable.FieldDescription.ExecutingUser" ), IValueMeta.TYPE_STRING, 255 ) );
    table.fields.add( new LogTableField( ID.START_ACTION.id, false, false, "START_ACTION", BaseMessages.getString( PKG, "JobLogTable.FieldName.StartingAction" ),
      BaseMessages.getString( PKG, "JobLogTable.FieldDescription.StartingAction" ), IValueMeta.TYPE_STRING, 255 ) );
    table.fields.add(
      new LogTableField( ID.CLIENT.id, false, false, "CLIENT", BaseMessages.getString( PKG, "JobLogTable.FieldName.Client" ), BaseMessages.getString( PKG, "JobLogTable.FieldDescription.Client" ),
        IValueMeta.TYPE_STRING, 255 ) );

    table.findField( ID.ID_WORKFLOW ).setKey( true );
    table.findField( ID.LOGDATE ).setLogDateField( true );
    table.findField( ID.LOG_FIELD ).setLogField( true );
    table.findField( ID.CHANNEL_ID ).setVisible( false );
    table.findField( ID.WORKFLOW_NAME ).setVisible( false );
    table.findField( ID.STATUS ).setStatusField( true );
    table.findField( ID.ERRORS ).setErrorsField( true );
    table.findField( ID.WORKFLOW_NAME ).setNameField( true );

    return table;
  }

  public LogTableField findField( ID id ) {
    return super.findField( id.id );
  }

  public Object getSubject( ID id ) {
    return super.getSubject( id.id );
  }

  public String getSubjectString( ID id ) {
    return super.getSubjectString( id.id );
  }

  public void setBatchIdUsed( boolean use ) {
    findField( ID.ID_WORKFLOW ).setEnabled( use );
  }

  public boolean isBatchIdUsed() {
    return findField( ID.ID_WORKFLOW ).isEnabled();
  }

  public void setLogFieldUsed( boolean use ) {
    findField( ID.LOG_FIELD ).setEnabled( use );
  }

  public boolean isLogFieldUsed() {
    return findField( ID.LOG_FIELD ).isEnabled();
  }

  public String getTransformNameRead() {
    return getSubjectString( ID.LINES_READ );
  }

  public String getTransformNameWritten() {
    return getSubjectString( ID.LINES_WRITTEN );
  }

  public String getTransformNameInput() {
    return getSubjectString( ID.LINES_INPUT );
  }

  public String getTransformNameOutput() {
    return getSubjectString( ID.LINES_OUTPUT );
  }

  public String getTransformNameUpdated() {
    return getSubjectString( ID.LINES_UPDATED );
  }

  public String getTransformNameRejected() {
    return getSubjectString( ID.LINES_REJECTED );
  }

  /**
   * Sets the logging interval in seconds. Disabled if the logging interval is <=0.
   *
   * @param logInterval The log interval value. A value higher than 0 means that the log table is updated every 'logInterval'
   *                    seconds.
   */
  public void setLogInterval( String logInterval ) {
    this.logInterval = logInterval;
  }

  /**
   * Get the logging interval in seconds. Disabled if the logging interval is <=0. A value higher than 0 means that the
   * log table is updated every 'logInterval' seconds.
   *
   * @return The log interval,
   */
  public String getLogInterval() {
    return logInterval;
  }

  /**
   * @return the logSizeLimit
   */
  public String getLogSizeLimit() {
    return logSizeLimit;
  }

  /**
   * @param logSizeLimit the logSizeLimit to set
   */
  public void setLogSizeLimit( String logSizeLimit ) {
    this.logSizeLimit = logSizeLimit;
  }

  /**
   * This method calculates all the values that are required for the logging table
   *
   * @param status  the log status to use
   * @param subject
   * @param parent
   */
  public RowMetaAndData getLogRecord( LogStatus status, Object subject, Object parent ) {
    if ( subject == null || subject instanceof Workflow ) {
      Workflow workflow = (Workflow) subject;
      Result result = null;
      if ( workflow != null ) {
        result = workflow.getResult();
      }

      RowMetaAndData row = new RowMetaAndData();

      for ( LogTableField field : fields ) {
        if ( field.isEnabled() ) {
          Object value = null;
          if ( workflow != null ) {

            switch ( ID.valueOf( field.getId() ) ) {
              case ID_WORKFLOW:
                value = new Long( workflow.getBatchId() );
                break;
              case CHANNEL_ID:
                value = workflow.getLogChannelId();
                break;
              case WORKFLOW_NAME:
                value = workflow.getJobname();
                break;
              case STATUS:
                value = status.getStatus();
                break;
              case LINES_READ:
                value = result == null ? null : new Long( result.getNrLinesRead() );
                break;
              case LINES_WRITTEN:
                value = result == null ? null : new Long( result.getNrLinesWritten() );
                break;
              case LINES_INPUT:
                value = result == null ? null : new Long( result.getNrLinesInput() );
                break;
              case LINES_OUTPUT:
                value = result == null ? null : new Long( result.getNrLinesOutput() );
                break;
              case LINES_UPDATED:
                value = result == null ? null : new Long( result.getNrLinesUpdated() );
                break;
              case LINES_REJECTED:
                value = result == null ? null : new Long( result.getNrLinesRejected() );
                break;
              case ERRORS:
                value = result == null ? null : new Long( result.getNrErrors() );
                break;
              case STARTDATE:
                value = workflow.getStartDate();
                break;
              case LOGDATE:
                value = workflow.getLogDate();
                break;
              case ENDDATE:
                value = workflow.getEndDate();
                break;
              case DEPDATE:
                value = workflow.getDepDate();
                break;
              case REPLAYDATE:
                value = workflow.getCurrentDate();
                break;
              case LOG_FIELD:
                value = getLogBuffer( workflow, workflow.getLogChannelId(), status, logSizeLimit );
                break;
              case EXECUTING_SERVER:
                value = workflow.getExecutingServer();
                break;
              case EXECUTING_USER:
                value = workflow.getExecutingUser();
                break;
              case START_ACTION:
                value = workflow.getStartActionCopy() != null ? workflow.getStartActionCopy().getName() : null;
                break;
              case CLIENT:
                value =
                  HopClientEnvironment.getInstance().getClient() != null ? HopClientEnvironment
                    .getInstance().getClient().toString() : "unknown";
                break;
              default:
                break;
            }
          }

          row.addValue( field.getFieldName(), field.getDataType(), value );
          row.getRowMeta().getValueMeta( row.size() - 1 ).setLength( field.getLength() );
        }
      }

      return row;
    } else {
      return null;
    }
  }

  public String getLogTableCode() {
    return "WORKFLOW";
  }

  public String getLogTableType() {
    return BaseMessages.getString( PKG, "JobLogTable.Type.Description" );
  }

  public String getConnectionNameVariable() {
    return Const.HOP_WORKFLOW_LOG_DB;
  }

  public String getSchemaNameVariable() {
    return Const.HOP_WORKFLOW_LOG_SCHEMA;
  }

  public String getTableNameVariable() {
    return Const.HOP_WORKFLOW_LOG_TABLE;
  }

  public List<IRowMeta> getRecommendedIndexes() {
    List<IRowMeta> indexes = new ArrayList<IRowMeta>();

    // First index : ID_JOB if any is used.
    //
    if ( isBatchIdUsed() ) {
      IRowMeta batchIndex = new RowMeta();
      LogTableField keyField = getKeyField();

      IValueMeta keyMeta = new ValueMetaBase( keyField.getFieldName(), keyField.getDataType() );
      keyMeta.setLength( keyField.getLength() );
      batchIndex.addValueMeta( keyMeta );

      indexes.add( batchIndex );
    }

    // The next index includes : ERRORS, STATUS, JOBNAME:

    IRowMeta lookupIndex = new RowMeta();
    LogTableField errorsField = findField( ID.ERRORS );
    if ( errorsField != null ) {
      IValueMeta valueMeta = new ValueMetaBase( errorsField.getFieldName(), errorsField.getDataType() );
      valueMeta.setLength( errorsField.getLength() );
      lookupIndex.addValueMeta( valueMeta );
    }
    LogTableField statusField = findField( ID.STATUS );
    if ( statusField != null ) {
      IValueMeta valueMeta = new ValueMetaBase( statusField.getFieldName(), statusField.getDataType() );
      valueMeta.setLength( statusField.getLength() );
      lookupIndex.addValueMeta( valueMeta );
    }
    LogTableField pipelineNameField = findField( ID.WORKFLOW_NAME );
    if ( pipelineNameField != null ) {
      IValueMeta valueMeta = new ValueMetaBase( pipelineNameField.getFieldName(), pipelineNameField.getDataType() );
      valueMeta.setLength( pipelineNameField.getLength() );
      lookupIndex.addValueMeta( valueMeta );
    }

    indexes.add( lookupIndex );

    return indexes;
  }

  @Override
  public void setAllGlobalParametersToNull() {
    boolean clearGlobalVariables = Boolean.valueOf( System.getProperties().getProperty( Const.HOP_GLOBAL_LOG_VARIABLES_CLEAR_ON_EXPORT, "false" ) );
    if ( clearGlobalVariables ) {
      super.setAllGlobalParametersToNull();

      logInterval = isGlobalParameter( logInterval ) ? null : logInterval;
      logSizeLimit = isGlobalParameter( logSizeLimit ) ? null : logSizeLimit;
    }
  }
}