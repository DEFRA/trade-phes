package uk.gov.defra.plants.filestorage.antivirus;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class AntiVirusException extends WebApplicationException {

  public AntiVirusException(String message) {
    super(message, Response.status(422).build());
  }

}

