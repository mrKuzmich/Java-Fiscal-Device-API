package com.taliter.fiscal.port.bluetooth;

import com.taliter.fiscal.port.FiscalPort;
import com.taliter.fiscal.port.FiscalPortSource;

/**
 * Created by ִלטענטי on 13.04.2016.
 */
public class BtFiscalPortSource implements FiscalPortSource, Cloneable {
  @Override
  public FiscalPort getFiscalPort() throws Exception {
    return null;
  }

  public Object clone()
  {
    try { return super.clone(); }
    catch (CloneNotSupportedException e) { throw new Error(e.toString()); }
  }

}
