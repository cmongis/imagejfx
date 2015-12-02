/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package mercury.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import mercury.core.AngularService;
import mercury.test.AngularTestService;
import mercury.core.AngularBinder;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class JavascriptGenerator {

    AngularBinder manager;

    String moduleTemplate;

    String promiseTemplate;

    String syncMethodTemplate;
    
    
    final static String TEMPLATE_PATH = "/mercury/template/";
    
    final static String MODULE_NAME_TOKEN = "ModuleName";
    final static String SERVICE_NAME_TOKEN = "ServiceName";
    final static String PROMISE_NAME_TOKEN = "PromiseName";
    final static String PROMISE_LIST_TOKEN = "//ServicePromises";
    final static String SYNC_METHOD_NAME_TOKEN = "SyncMethod";
    final static String MODULE_NAME = "MercuryAngular";
    final static String ANGULAR_MODULE_DECLARATION = "var module = angular.module('MercuryAngular',[]);\n";
    final static  String METHOD_NAME_TOKEN = "MethodName";
    
    
    public JavascriptGenerator(AngularBinder manager) {
        this.manager = manager;
        String path = TEMPLATE_PATH;

        moduleTemplate = readTextRessource(this, path + "angular-factory.js");
        promiseTemplate = readTextRessource(this, path + "angular-factory-promise.js");
        syncMethodTemplate = readTextRessource(this, path + "angular-factory-sync-method.js");
    }

    public String generate() {
        StringBuilder builder = new StringBuilder();
        ServiceCodeGenerator serviceCodeGenerator = new ServiceCodeGenerator(moduleTemplate, promiseTemplate);
        builder.append(ANGULAR_MODULE_DECLARATION);
        for (AngularService service : manager.services()) {
            builder.append(serviceCodeGenerator.generateServiceCode(service));
        };
        builder.append(";");
        
        return builder.toString();

    }

    public static String readTextRessource(Object source, String path) {
        BufferedReader bReader;
        try {
            
            File f = new File("./src" + path);

            FileReader reader = new FileReader("./src" + path);

            bReader = new BufferedReader(reader, 1000000);

        } catch (Exception e) {
            InputStreamReader reader = new InputStreamReader(source.getClass()
                    .getResourceAsStream(path));
            bReader = new BufferedReader(reader);
        }
        System.gc();
        int count = 0;

        StringBuffer buffer = new StringBuffer(900000);
        try {

            String line = "";
            while (line != null) {
                line = bReader.readLine();
                if (line != null) {
                    buffer.append(line + "\n");
                }
            }

            bReader.close();
            bReader = null;
            System.gc();

            return buffer.toString();

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        }
    }

    public static void main(String... args) {
        AngularBinder manager = new AngularBinder();
        AngularTestService testService = new AngularTestService();
        manager.registerService("TestService", testService);

        JavascriptGenerator generator = new JavascriptGenerator(manager);

    }

    public class ServiceCodeGenerator {

        String serviceTemplate;
        String promiseTemplate;
        

        public ServiceCodeGenerator(String serviceTemplate, String promiseTemplate) {
            this.serviceTemplate = serviceTemplate;
            this.promiseTemplate = promiseTemplate;
        }

        public String generateServiceCode(AngularService service) {
            StringBuilder serviceInnerCodeBuilder = new StringBuilder();

            for (String promise : service.getAsyncActionList()) {
                String javascriptMethodCode = generatePromise(service.getName(), promise);
                serviceInnerCodeBuilder.append(javascriptMethodCode);
            }
            
            for (String syncMethod : service.getSyncActionList()) {
                String javascriptMethodCode = generateSyncMethod(service.getName(), syncMethod);
                serviceInnerCodeBuilder.append(javascriptMethodCode);
            }
            
            
            String serviceCode = serviceTemplate.replaceAll(PROMISE_LIST_TOKEN, Matcher.quoteReplacement(serviceInnerCodeBuilder.toString()));
            return serviceCode.replaceAll(SERVICE_NAME_TOKEN, service.getName());
        }

        public String generatePromise(String serviceName, String promiseName) {
            return promiseTemplate.replaceAll(PROMISE_NAME_TOKEN, promiseName);
        }
        
        public String generateSyncMethod(String serviceName, String methodName) {
            return syncMethodTemplate.replaceAll(METHOD_NAME_TOKEN,methodName);
        }
        
    }

}
