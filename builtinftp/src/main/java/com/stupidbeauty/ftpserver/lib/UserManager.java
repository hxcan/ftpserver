package com.stupidbeauty.ftpserver.lib;

import java.util.ArrayList;

public class UserManager
{
  private ArrayList<User> users=new ArrayList(); //!< User list.
  
  public boolean authenticate(String userName, String passWord)
  {
    boolean result=false;
  
    for(User currentUser: users)
    {
      if ((currentUser.getUserName().equals(userName)) && (currentUser.getPassWord().equals(passWord)))
      {
        result=true;
      
        break;
        
      } // if ((currentUser.getUserName().equals(userName)) && (currentUser.getPassWord().equals(passWord)))
    }
    
    return result;
  } // public boolean authenticate(String userName, String passWord)
}
