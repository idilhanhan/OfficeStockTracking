/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 *
 * @author idilhanhan
 */

public class OfficeStock {
    
    @JsonProperty("stockNo")
    private String stockNo;
    @JsonProperty("description") 
    private String description;
    @JsonProperty("capacity")
    private String capacity;
    @JsonProperty("barcodeBase64")
    private String barcodeBase64;

    @ApiModelProperty(value="")
    public String getStockNo() {
        return stockNo;
    }
    
    public void setStockNo(String stockNo) {
        this.stockNo = stockNo;
    }

    @ApiModelProperty(value="")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ApiModelProperty(value="")
    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    @ApiModelProperty(value="")
    public String getBarcodeBase64() {
        return barcodeBase64;
    }

    public void setBarcodeBase64(String barcode) {
        this.barcodeBase64 = barcode;
    }
    
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("class OfficeStock {\n");
        sb.append("    stockNo: ").append(stockNo).append("\n");
        sb.append("    description: ").append(description).append("\n");
        sb.append("    capacity: ").append(capacity).append("\n");
        sb.append("    barcodeBase64: ").append(barcodeBase64).append("\n");
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
      OfficeStock os = (OfficeStock) other;
      return Objects.equals(this.stockNo, os.stockNo) &&
              Objects.equals(this.description, os.description) &&
              Objects.equals(this.capacity, os.capacity) &&
              Objects.equals(this.barcodeBase64, os.barcodeBase64);
      
  }   
    
    
}
