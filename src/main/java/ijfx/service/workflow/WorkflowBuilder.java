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
package ijfx.service.workflow;

import ijfx.service.batch.BatchService;
import ijfx.service.batch.BatchSingleInput;
import ijfx.service.batch.input.BatchInputBuilder;
import ijfx.service.ui.LoadingScreenService;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.save.SaveOptions;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import mongis.utils.CallbackTask;
import mongis.utils.ProgressHandler;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class WorkflowBuilder {

    @Parameter
    private Context context;

    @Parameter
    private BatchService batchService;

    @Parameter
    private LoadingScreenService loadingScreenService;

    private List<BatchInputBuilder> inputs = new ArrayList<>();

    private List<WorkflowStep> steps = new ArrayList<>();

    private String name;
    
    public WorkflowBuilder(Context context) {
        context.inject(this);
    }

    public WorkflowBuilder addInput(Collection<? extends BatchSingleInput> inputs) {
        inputs.forEach(this::addInput);
        return this;
    }

    public WorkflowBuilder addInputs(Collection<? extends Explorable> explorableList) {
        explorableList.forEach(this::addInput);
        return this;
    }

    public WorkflowBuilder addInput(BatchSingleInput input) {

        inputs.add(new BatchInputBuilder(context).wrap(input));

        return this;
    }

    public WorkflowBuilder addInput(File file) {
        inputs.add(new BatchInputBuilder(context).from(file));
        return this;
    }
    
    public WorkflowBuilder addInputFiles(Collection<File> files) {
        files.forEach(this::addInput);
        return this;
    }
    
    public WorkflowBuilder addInput(Dataset dataset) {
        inputs.add(new BatchInputBuilder(context).from(dataset));
        return this;
    }
    
    public WorkflowBuilder addInput(Dataset dataset, long[] planePosition) {
        inputs.add(new BatchInputBuilder(context).from(dataset,planePosition));
        return this;
    }

    public WorkflowBuilder addInput(Explorable exp) {
        inputs.add(new BatchInputBuilder(context).from(exp));
        return this;
    }

    public WorkflowBuilder addInput(ImageDisplay imageDisplay) {
        inputs.add(new BatchInputBuilder(context).from(imageDisplay));
        return this;
    }

    public WorkflowBuilder addStepIfTrue(boolean predicateResult,Class<?> moduleClass, Object... params) {
        if(predicateResult) {
           return  addStep(moduleClass,params);
        }
        else {
            return this;
        }
    }
    
    public WorkflowBuilder addStep(Class<?> moduleClass, Object... params) {

        DefaultWorkflowStep step = new DefaultWorkflowStep(moduleClass.getName());

        context.inject(step);

        for (int i = 0; i != params.length; i += 2) {
            step.setParameter(params[i].toString(), params[i + 1]);
        }

        steps.add(step);

        return this;

    }

    public WorkflowBuilder then(Consumer<BatchSingleInput> consumer) {

        remapInputs(builder -> builder.onFinished(consumer));
        return this;
    }
    public WorkflowBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public WorkflowBuilder thenUseDataset(Consumer<Dataset> consumer) {
        then(batchInput -> consumer.accept(batchInput.getDataset()));
        return this;
    }

    public <OUTPUT> CallbackTask<BatchSingleInput, OUTPUT> thenMapToTask(Class<? extends OUTPUT> output) {
        CallbackTask<BatchSingleInput, OUTPUT> task = new CallbackTask<>();
        then(task);
        return task;
    }

    protected void remapInputs(Function<? super BatchInputBuilder, ? extends BatchInputBuilder> remapper) {
        inputs = inputs.stream().map(remapper).collect(Collectors.toList());
    }
    public WorkflowBuilder and(Consumer<BatchInputBuilder> consumer) {
        inputs.forEach(consumer);
        return this;
    }
    
    public WorkflowBuilder saveUsingOptions(SaveOptions options) {
        remapInputs(builder->builder.saveUsingOptions(options));
        return this;
    }
    
    public WorkflowBuilder saveTo(File directory) {

        remapInputs(builder -> builder.saveIn(directory));
        return this;

    }

    public WorkflowBuilder execute(Collection<WorkflowStep> stepList) {
        steps.addAll(stepList);
        return this;
    }

    public WorkflowBuilder execute(Workflow workflow) {
        steps.addAll(workflow.getStepList());
        return this;
    }

    public Workflow getWorkflow(String name) {
        DefaultWorkflow workflow = new DefaultWorkflow(steps);
        workflow.setName(name);
        return workflow;
    }

    private List<BatchSingleInput> getInputs() {
        return inputs.stream().map(builder -> builder.getInput()).collect(Collectors.toList());
    }

    public CallbackTask<?,Boolean> start() {

        DefaultWorkflow workflow = new DefaultWorkflow(steps);

        return new CallbackTask<List<BatchSingleInput>, Boolean>()
                .setInput(getInputs())
                .run((progress, input) -> batchService.applyWorkflow(progress, input, workflow))
                .start();
    }

    public CallbackTask<?,Boolean> startAndShow() {
        CallbackTask<?,Boolean> task = start();
        loadingScreenService.frontEndTask(task, true);
        return task;
    }

    public boolean runSync(ProgressHandler handler) {
        handler = ProgressHandler.check(handler);

        return batchService.applyWorkflow(handler, getInputs(), new DefaultWorkflow(steps));

    }
}
