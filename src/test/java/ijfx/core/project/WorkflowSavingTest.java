/*
    This file is part of ImageJ FX.

    ImageJ FX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ImageJ FX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
    
     Copyright 2015,2016 Cyril MONGIS, Michael Knop
	
 */
package ijfx.core.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ijfx.plugins.DefaultInterval;
import ijfx.plugins.LongInterval;
import ijfx.plugins.commands.ExtractSlices;
import ijfx.plugins.projection.MedianProjection;
import ijfx.plugins.projection.Projection;
import ijfx.service.batch.BatchService;
import ijfx.service.ui.LoadingScreenService;
import ijfx.service.workflow.Workflow;
import ijfx.service.workflow.WorkflowBuilder;
import ijfx.service.workflow.WorkflowIOService;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import net.imagej.plugins.commands.imglib.GaussianBlur;
import net.imagej.threshold.ThresholdService;
import org.junit.Assert;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class WorkflowSavingTest extends BaseImageJTest {

    @Parameter
    Context context;

    @Parameter
    WorkflowIOService workflowIOService;

    @Override
    protected Class[] getService() {
        return new Class[]{
            ThresholdService.class, WorkflowIOService.class, BatchService.class,LoadingScreenService.class};
    }

    @Test
    public void testloadsave() {

        File testFile = new File("workflow_test.json");
        if(testFile.exists()) testFile.delete();
        Workflow workflow = new WorkflowBuilder(context)
                .addStep(ExtractSlices.class, "interval", new DefaultInterval(20, 70))
                .addStep(GaussianBlur.class, "sigma", 3.0)
                .addStep(Projection.class, "projectMethod", new MedianProjection())
                
                //.addStep(GaussianBlur.class,"sigma",3.0)
                .getWorkflow("Test workflow");

        workflowIOService.saveWorkflow(workflow, testFile);

        Workflow w = workflowIOService.loadWorkflow(testFile);

        Assert.assertNotNull(w);
        
        // Asserting the size
        Assert.assertEquals(workflow.getStepList().size(), w.getStepList().size());

        // Asserting the workflows
        Assert.assertEquals(
                getWorkflowParam(workflow, 1, "sigma").getClass().getName(),
                getWorkflowParam(w, 1, "sigma").getClass().getName()
        );

    }
    
    public static ObjectMapper objectMapper = new ObjectMapper();
    
    
    @Test
    public void saveLoadLongInterval() throws JsonProcessingException, IOException {
        
        LongInterval interval = new DefaultInterval(10,20,5,40);
        //objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        objectMapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, "@class");
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("int",interval);
        String serialized = objectMapper.writeValueAsString(hashMap);
        System.out.println(serialized);
        
        
        HashMap<String,Object> loaded = objectMapper.readValue(serialized,HashMap.class);
        
        Assert.assertTrue(LongInterval.class.isAssignableFrom(loaded.get("int").getClass()));
        
    }

    private Object getWorkflowParam(Workflow w, int step, String paramKey) {
        return w.getStepList().get(step).getParameters().get(paramKey);
    }
    
    

    
}
