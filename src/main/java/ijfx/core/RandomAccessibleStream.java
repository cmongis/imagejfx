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
package ijfx.core;

import java.lang.reflect.Array;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.imglib2.Cursor;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import rx.subjects.PublishSubject;

/**
 *
 * @author cyril
 */
public class RandomAccessibleStream {

    public static ExecutorService executorService = Executors.newCachedThreadPool();

    public static <T> Stream<T> generate(Cursor<T> cursor) {

        cursor.reset();
        Stream.Builder<T> builder = Stream.builder();
        while (cursor.hasNext()) {
            cursor.fwd();
            builder.add(cursor.get());

        }

        return builder.build();

    }

    public static <T, R> PublishSubject<Pair<T, R>> synchronous(Cursor<T> reference, Cursor<R> aligned) {

        final PublishSubject<Pair<T, R>> flow = PublishSubject.create();

        executorService.execute(() -> {
            reference.reset();
            aligned.reset();
            try {
                while (reference.hasNext()) {
                    reference.fwd();
                    aligned.fwd();
                    flow.onNext(new ValuePair<>(reference.get(), aligned.get()));
                }

            } catch (Exception e) {
                flow.onError(e);
            }
        });
        return flow;

    }

    public static <T> Stream<T> generate(Cursor<T> interval, long n) {

        Stream<T> stream = Stream.generate(new TypeSupplier<>(interval)).unordered().limit(n);
        return stream;
    }

    private static class TypeSupplier<T> implements Supplier<T> {

        final Cursor<T> cursor;

        public TypeSupplier(Cursor<T> interval) {
            this.cursor = interval;
            cursor.reset();
        }

        @Override
        public T get() {

            cursor.fwd();
            return cursor.get();
        }

        public boolean hasNext() {
            return cursor.hasNext();
        }

    }

}
