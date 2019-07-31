/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package service;

import io.swagger.jaxrs.config.BeanConfig;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 *
 * @author ferhat
 */
@ApplicationPath("/api")
public class JaxRsActivator extends Application{
    
    public JaxRsActivator(){
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost("localhost:8080");
        beanConfig.setBasePath("/officeStock/api");
        beanConfig.setResourcePackage(StockService.class.getPackage().getName());
        beanConfig.setTitle("Office Stock RESTful API");
        beanConfig.setDescription("Powered by: RESTEasy, Swagger and Swagger UI");
        beanConfig.setScan(true);
    }
}
