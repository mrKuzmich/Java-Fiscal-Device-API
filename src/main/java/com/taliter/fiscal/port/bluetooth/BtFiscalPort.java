package com.taliter.fiscal.port.bluetooth;

import com.taliter.fiscal.port.FiscalPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ִלטענטי on 13.04.2016.
 */
public class BtFiscalPort implements FiscalPort {
  @Override
  public void open() throws Exception {

  }

  @Override
  public void close() throws Exception {

  }

  @Override
  public boolean isOpen() {
    return false;
  }

  @Override
  public void setTimeout(int ms) throws Exception {

  }

  @Override
  public int getTimeout() throws Exception {
    return 0;
  }

  @Override
  public void setBaudRate(int baudRate) throws Exception {

  }

  @Override
  public int getBaudRate() throws Exception {
    return 0;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return null;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return null;
  }

  @Override
  public void flushAndWait() throws IOException {

  }
}
