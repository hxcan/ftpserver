package com.stupidbeauty.ftpserver.lib;

public class User
{
  private String passWord=null; //!< Pass word provided.
  private boolean authenticated=true; //!< Is Login correct?
  private String userName=null; //!< User name provided.
  
  public void setPassWord(String passWord)
  {
    this.passWord=passWord;
  }
  
  public void setUserName(String userName)
  {
    this.userName=userName;
  }
  
  public String getUserName()
  {
    return userName;
  }

  public String getPassWord()
  {
    return passWord;
  } // public String getPassWord()
}
