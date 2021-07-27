package uk.gov.defra.plants.filestorage.enums;

import static uk.gov.defra.plants.common.eventhub.model.EventPriority.EXCEPTION;
import static uk.gov.defra.plants.common.eventhub.model.EventPriority.NORMAL;
import static uk.gov.defra.plants.common.eventhub.model.EventPriority.UNUSUAL;
import static uk.gov.defra.plants.common.eventhub.model.PMCCode.DELETE_APPLICATIONFORM_DOCUMENT;
import static uk.gov.defra.plants.common.eventhub.model.PMCCode.DELETE_BLOCKED;
import static uk.gov.defra.plants.common.eventhub.model.PMCCode.DOWNLOAD_ALLOWED;
import static uk.gov.defra.plants.common.eventhub.model.PMCCode.DOWNLOAD_BLOCKED;
import static uk.gov.defra.plants.common.eventhub.model.PMCCode.DOWNLOAD_STARTED;
import static uk.gov.defra.plants.common.eventhub.model.PMCCode.FILE_SENT_FOR_SCAN;
import static uk.gov.defra.plants.common.eventhub.model.PMCCode.PROCESS_FILE_SUCCESS;
import static uk.gov.defra.plants.common.eventhub.model.PMCCode.UPLOAD_FILE;
import static uk.gov.defra.plants.common.eventhub.model.PMCCode.UPLOAD_FILE_SUCCESS;
import static uk.gov.defra.plants.common.eventhub.model.PMCCode.VIRUS_SCAN_FAILED;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.defra.plants.common.eventhub.model.EventPriority;
import uk.gov.defra.plants.common.eventhub.model.PMCCode;

@AllArgsConstructor
@Getter
public enum FileEvent {

  UPLOADED(UPLOAD_FILE_SUCCESS, "sanitised, scanned and stored successfully", NORMAL),
  SCAN_FAILED(VIRUS_SCAN_FAILED, "failed AV scan", EXCEPTION),
  UPLOAD_STARTED(UPLOAD_FILE, "upload started", NORMAL),
  STARTED_DOWNLOADING(DOWNLOAD_STARTED, "started downloading", NORMAL),
  LOCAL_VALIDATION_FAILED(PMCCode.LOCAL_VALIDATION_FAILED, "failed local validation", NORMAL),
  FILE_PROCESSED(PROCESS_FILE_SUCCESS, "sanitised and scanned successfully", NORMAL),
  SENT_FOR_SCAN(FILE_SENT_FOR_SCAN, "sent for scan", NORMAL),
  DOWNLOAD_PERMITTED(DOWNLOAD_ALLOWED, "Download allowed", NORMAL),
  DOWNLOAD_NOT_PERMITTED(DOWNLOAD_BLOCKED, "Download blocked", UNUSUAL),
  DELETE_DOCUMENT(DELETE_APPLICATIONFORM_DOCUMENT, "file delete started", NORMAL),
  DELETE_NOT_PERMITTED(DELETE_BLOCKED, "rejected attempt to delete a document of an application", UNUSUAL);

  private final PMCCode eventCode;
  private final String name;
  private final EventPriority priority;
}