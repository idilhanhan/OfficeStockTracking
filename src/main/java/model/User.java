/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 *
 * @author idilhanhan
 */
//@ApiModel(description="Model for Users")
public class User {
    
    @JsonProperty("id")
    private long id;
    @JsonProperty("username")
    private String username;
    @JsonProperty("password") //password will be kept in redis -- key = pass.user:#
    private String password;
    //@JsonProperty("name")
    //private String name;
    //@JsonProperty("department")
    //private String department;
    @JsonProperty("admin")//???
    private boolean admin;

    @ApiModelProperty(value="")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ApiModelProperty(value="")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    } 

    @ApiModelProperty(value="")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @ApiModelProperty(value="")
    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("class User {\n");
        sb.append("    id: ").append(id).append("\n");
        sb.append("    username: ").append(username).append("\n");
        //sb.append("    name: ").append(name).append("\n");
        //sb.append("    department: ").append(department).append("\n");
        sb.append("    admin: ").append(admin).append("\n");
        sb.append("}");
        return sb.toString();
  }
    
  @Override
  public boolean equals(java.lang.Object other){
      if (this == other){
          return true;
      }
      if ( other == null || this.getClass() != other.getClass()){
          return false;
      }
      User user = (User) other;
      return Objects.equals(this.id, user.id) &&
              Objects.equals(this.username, user.username) &&
              //Objects.equals(this.name, user.name) &&
              //Objects.equals(this.department, user.department) &&
              Objects.equals(this.admin, user.admin);
      
  }   
}
