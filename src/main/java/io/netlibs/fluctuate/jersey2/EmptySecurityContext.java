package io.netlibs.fluctuate.jersey2;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

/**
 * We don't currently support any HTTP authentication mechanisms. So always return an instance of this.
 * 
 * @author Theo Zourzouvillys
 *
 */

public enum EmptySecurityContext implements SecurityContext
{
  SECURE(true), INSECURE(false);

  private final boolean secure;

  private EmptySecurityContext(boolean secure)
  {
    this.secure = secure;
  }

  @Override
  public Principal getUserPrincipal()
  {
    return null;
  }

  @Override
  public boolean isUserInRole(String s)
  {
    return false;
  }

  @Override
  public boolean isSecure()
  {
    return this.secure;
  }

  @Override
  public String getAuthenticationScheme()
  {
    return null;
  }

}
