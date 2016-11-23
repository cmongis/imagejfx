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
package mongis.utils.panecell;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.scene.Node;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 *
 * @author cyril
 */
public class PaneCellUpdateProcess<T> {

    final PublishSubject<CellUpdate> updateQueue;
    final PublishSubject<CellCreation> createQueue = PublishSubject.create();

    final List<T> items;
    final List<PaneCell<T>> cachedController;
    final List<Node> nodeList;

    Callable<PaneCell<T>> factory;

    int updateCount = 0;

    boolean cancelled = false;

    public PaneCellUpdateProcess(List<T> items, List<PaneCell<T>> cachedController, List<Node> toUpdate, Callable<PaneCell<T>> factory) {
        this.items = items;
        this.cachedController = cachedController;
        this.nodeList = toUpdate;
        this.factory = factory;
        nodeList.clear();
        createQueue
                .limit(items.size())
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .subscribe(this::onCreationRequest, this::onError);

        updateQueue = PublishSubject.create();

        updateQueue
                
                .subscribe(this::onUpdateRequest, this::onError);

        updateQueue
                .limit(items.size())
                .filter(r -> nodeList.contains(r.getCell().getContent()) == false)
                .buffer(20)
                .subscribe(this::toAdd, this::onError);

        if (items.size() >= cachedController.size()) {

            if (cachedController.size() > 0) // creating CellUpdate request
            {
                IntStream
                        .range(0,cachedController.size())
                        .mapToObj(i -> new CellUpdate(cachedController.get(i), items.get(i)))
                        .forEach(updateQueue::onNext);
            }

            // creating CellCreationRequest
            IntStream
                    .range(cachedController.size(), items.size())
                    .mapToObj(items::get)
                    .map(CellCreation::new)
                    .forEach(createQueue::onNext);
        } else {
            Platform.runLater(() -> nodeList.removeAll(nodeList.subList(items.size(), nodeList.size() - 1)));

            // creating CellUpdate request
            IntStream
                    .range(0,items.size())
                    .mapToObj(i -> new CellUpdate(cachedController.get(i), items.get(i)))
                    .forEach(updateQueue::onNext);

        }

    }

    private void onCreationRequest(CellCreation c) {
        if (cancelled) {
            return;
        }
        System.out.println("creation request");
        PaneCell<T> create = c.create();
        updateQueue.onNext(new CellUpdate(create, c.object));
    }

    private void onError(Throwable t) {
        Logger
                .getLogger(this.getClass().getName())
                .log(Level.SEVERE, null, t);
    }

    private void onUpdateRequest(CellUpdate request) {

        System.out.println("update request");
        if (cancelled) {
            return;
        }

        request.update();
        updateCount++;

    }

    private void toAdd(List<CellUpdate> updates) {

        if (cancelled) {
            return;
        }

        List<PaneCell<T>> cells = updates
                .stream()
                .map(u->u.paneCell)
                .filter(p->cachedController.contains(p) == false)
                .collect(Collectors.toList());
        cachedController.addAll(cells);
        
        List<Node> collect = updates
                .stream()
                .map(r -> r.paneCell.getContent())
                .collect(Collectors.toList());

        Platform.runLater(() -> nodeList.addAll(collect));

    }

    public void cancel() {
        cancelled = true;
        System.out.println("Cancelling...");
    }

    private class CellCreation {

        T object;

        public CellCreation(T object) {
            this.object = object;
        }

        public PaneCell<T> create() {
            try {
                return factory.call();
            } catch (Exception e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
            }
            return null;
        }

    }

    private class CellUpdate {

        final PaneCell<T> paneCell;
        final T item;

        public CellUpdate(PaneCell<T> paneCell, T item) {
            this.paneCell = paneCell;
            this.item = item;
        }

        public void update() {
            paneCell.setItem(item);
        }

        public PaneCell<T> getCell() {
            return paneCell;
        }
    }

    private class SubscriberBuilder<R extends Object> extends Subscriber<R> {

        private Runnable completed;
        private Consumer<Throwable> errorHandler
                = e -> Logger.getLogger("subscriber-builder").log(Level.SEVERE, null, e);
        private Consumer<R> onNext;

        public SubscriberBuilder(Consumer<R> onNext) {
            this.onNext = onNext;
        }

        public SubscriberBuilder<R> onNext(Consumer<R> onNext) {
            this.onNext = onNext;
            return this;
        }

        public SubscriberBuilder onCompleted(Runnable runnable) {
            this.completed = runnable;
            return this;
        }

        @Override
        public void onCompleted() {
            if (completed != null) {
                completed.run();
            }
        }

        @Override
        public void onError(Throwable e) {
            errorHandler.accept(e);
        }

        @Override
        public void onNext(R r) {
            onNext.accept(r);
        }

    }

}
