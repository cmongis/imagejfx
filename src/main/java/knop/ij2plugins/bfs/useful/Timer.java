/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knop.ij2plugins.bfs.useful;

/**
 *
 * @author Cyril MONGIS, 2016
 */
 public class Timer {
        
        long start;
        long last;
        
        public void start() {
            start = System.currentTimeMillis();
            last = System.currentTimeMillis();
        }
        
        public long now() {
            return System.currentTimeMillis();
        }
        
        public long elapsed(String text) {
            long now = System.currentTimeMillis();
            long elapsed = (now-last);
            System.out.println(text + " : "+elapsed+"ms");
            last = now;
            return elapsed;
        }
        
        public long total(String text) {
            System.out.println("Total time : "+(now()-start)+"ms");
            return now()-start;
            }
        
    }