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
package mongis.utils;

import ijfx.ui.context.animated.AnimationPlus;
import ijfx.ui.context.animated.Animations;
import ijfx.ui.main.ImageJFX;
import java.util.LinkedList;
import java.util.Queue;
import javafx.animation.Animation;
import javafx.application.Platform;
import javafx.scene.Node;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class AnimationChain {

    //Stack<Runnable> runnableStack = new Stack<>();
    //Stack<Timeline> timeLineStack = new Stack<>();
    Queue<Launchable> launchables = new LinkedList<>();
    long millis;

    public AnimationChain play(Animation line) {
        launchables.add(new LaunchableAnimation(line));
        return this;
    }

    public AnimationChain animate(Node node, Animations animation) {
        play(animation.configure(node, ImageJFX.getAnimationDurationAsDouble()));
        return this;
    }
    
    public AnimationChain animate(Node node, AnimationPlus animation) {
        play(animation.configure(node, ImageJFX.getAnimationDuration()));
        return this;
    }

    public AnimationChain then(Runnable runnable) {
        launchables.add(new DefaultLaunchable(runnable));
        return this;
    }

    private void next() {
        if (launchables.size() > 0) {
            Launchable l = launchables
                    .poll()
                    .setOnFinished(this::next);
            System.out.println(l.getClass().getSimpleName());
            l.launch();
            System.out.println(System.currentTimeMillis() - millis);
            millis = System.currentTimeMillis();

        }
    }

    public void execute() {
        next();
    }

    public AnimationChain thenInFXThread(Runnable runnable) {
        launchables.add(new LaunchableFX2(runnable));
        return this;
    }

    private interface Launchable {

        public void launch();

        public Launchable setOnFinished(Runnable runnable);
    }

    private class LaunchableAnimation implements Launchable {

        private final Animation timeline;

        public LaunchableAnimation(Animation timeline) {
            this.timeline = timeline;
        }

        @Override
        public Launchable setOnFinished(Runnable runnable) {

            timeline.setOnFinished(event -> {
                System.out.println("Animaition finished");
                runnable.run();
            });
            return this;
        }

        public void launch() {
            Platform.runLater(timeline::play);
        }

    }

    private class LaunchableRunnable implements Launchable {

        protected CallbackTask callback = new CallbackTask();

        public LaunchableRunnable(Runnable runnable) {
            callback.run(runnable);

        }

        public Launchable setOnFinished(Runnable runnable) {
            callback.setOnSucceeded(event -> runnable.run());
            return this;
        }

        public void launch() {
            callback.start();
        }
    }

    private class LaunchableFx extends LaunchableRunnable {

        public LaunchableFx(Runnable runnable) {
            super(runnable);
        }

        @Override
        public void launch() {
            Platform.runLater(callback);
        }

    }
    
    
    private class LaunchableFX2 implements Launchable {
        Runnable task;
        Runnable onFinished;
        public LaunchableFX2(Runnable runanble) {
            this.task = runanble;
        }

        @Override
        public void launch() {
            Platform.runLater(this::run);
        }
        
        protected void run() {
            this.task.run();
            this.onFinished.run();
        }

        @Override
        public Launchable setOnFinished(Runnable runnable) {
            this.onFinished = runnable;
            return this;
        }
    }
    
    private class DefaultLaunchable extends LaunchableFX2 {
        
        public DefaultLaunchable(Runnable runanble) {
            super(runanble);
        }
        
        @Override
        public void launch() {
            new Thread(this::run).start();
        }
        
    }
    

}
