package com.taliter.fiscal.device.epson;

import com.taliter.fiscal.device.AbortedFiscalRequestException;
import com.taliter.fiscal.device.FiscalDeviceEventHandler;
import com.taliter.fiscal.device.FiscalPacket;
import com.taliter.fiscal.device.InvalidFiscalResponseException;
import com.taliter.fiscal.device.hasar.BasicFiscalDevice;
import com.taliter.fiscal.device.hasar.HasarFiscalPacket;
import com.taliter.fiscal.port.FiscalPort;

import java.io.IOException;

/**
 * A FiscalDevice implementation used to communicate with Epson fiscal devices such as fiscal printers.
 */
public class EpsonFiscalDevice extends BasicFiscalDevice {
  private static final int SN_MIN = 0x81;
  private static final int SN_MAX = 0xFF;

  private static final int CMD_STATUS_REQUEST = EpsonConstants.CMD_STATUS_REQUEST;
  private static final int CMD_STATPRN = EpsonConstants.CMD_STATPRN;;

  private boolean handleExtendedProtocol;
  private final FiscalPacket REQ_STATPRN;

  public EpsonFiscalDevice(FiscalPort port, int timeout, boolean extendedProtocol, boolean handleExtendedProtocol, int maxTries, String encoding, int baseRolloverYear) {
    super(port, timeout, extendedProtocol, maxTries, encoding, baseRolloverYear);
    this.handleExtendedProtocol = handleExtendedProtocol;
    (REQ_STATPRN = createFiscalPacket()).setCommandCode(CMD_STATPRN);
  }

  public void open() throws Exception {
    super.open();
    boolean success = false;
    try {
//      synchronize();
      success = true;
    } finally {
      if (!success) close();
    }
  }

  public void synchronize() throws IOException {
    FiscalDeviceEventHandler h = eventHandler;
    eventHandler = null;  // Suppress event generation.
    try {
      FiscalPacket request = createFiscalPacket();
      request.setCommandCode(CMD_STATUS_REQUEST);
      FiscalPacket response = createFiscalPacket();
      basicExecute(request, response, request);  // The first try may hit the last used serial number
      basicExecute(request, response, request);  // and get its corresponding response.
      if (CMD_STATUS_REQUEST != response.getCommandCode())
        throw new InvalidFiscalResponseException(request, response);  // Will not tolerate STATPRN here.
    } finally {
      eventHandler = h;
    }
  }

  /**
   * True to handle STATPRN responses via the FiscalDeviceEventHandler.onExtendedStatus() event.
   * False to return STATPRN responses from the FiscalDevice.execute() methods.
   */
  public void setHandleExtendedProtocol(boolean handleExtendedProtocol) {
    this.handleExtendedProtocol = handleExtendedProtocol;
  }

  /**
   * True to handle STATPRN responses via the FiscalDeviceEventHandler.onExtendedStatus() event.
   * False to return STATPRN responses from the FiscalDevice.execute() methods.
   */
  public boolean getHandleExtendedProtocol() {
    return handleExtendedProtocol;
  }

  public void execute(FiscalPacket request, FiscalPacket response) throws IOException {
    if (request == null) throw new NullPointerException();
    if (response == null) throw new NullPointerException();
    if (request == response) throw new IllegalArgumentException();
    int reqc = request.getCommandCode();
    basicExecute(request, response, request);
    int resc = response.getCommandCode();
    if (reqc != resc && reqc != CMD_STATPRN) {
      if (resc != CMD_STATPRN) throw new InvalidFiscalResponseException(request, response);
      if (handleExtendedProtocol) {
        do {
          if (!onExtendedStatus(request, response)) throw new AbortedFiscalRequestException(request, response);
          basicExecute(REQ_STATPRN, response, request);
          resc = response.getCommandCode();
        }
        while (resc == CMD_STATPRN);
        if (reqc != resc) throw new InvalidFiscalResponseException(request, response);
      }
    }
    onExecute(request, response);
  }

  /**
   * onExtendedStatus() event dispacther.
   */
  protected boolean onExtendedStatus(FiscalPacket request, FiscalPacket status)  // Abort if false.
  {
    // Make sure the event lasts at least for a timeout period if true is returned.
    boolean retry;
    long time;
    if (eventHandler != null) {
      long startTime = System.currentTimeMillis();
      retry = eventHandler.onExtendedStatus(this, request, status);
      if (retry) {
        time = System.currentTimeMillis() - startTime;
        int timeout = getTimeout();
        if (time < timeout) time = timeout - time;
        else time = 0;
      } else time = 0;
    } else {
      retry = true;
      time = getTimeout();
    }
    if (time > 0) {
      try {
        Thread.sleep(time);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    return retry;
  }

  @Override
  protected int getSnMin() {
    return SN_MIN;
  }

  @Override
  protected int getSnMax() {
    return SN_MAX;
  }

  @Override
  public FiscalPacket createFiscalPacket() {
    return new EpsonFiscalPacket(getEncoding(), getBaseRolloverYear());
  }
}
