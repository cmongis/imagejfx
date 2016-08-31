package knop.ij2plugins.bfs.useful;

import java.io.IOException;

	public class MyTime {
	
	///////////////////////////////
	public static double elapsedTime(long t0, long t1) {
		return (t1 - t0) / 1000.0;
	}
	
	//////////////////////////////
	public static void waitInput() {
		System.out.println("\n[waiting input]");
		try{System.in.read();}catch(IOException e) {}
		System.out.println("\n");
	}
	
	///////////////////////////////////
	public static void sleep(long t) {
		System.out.println("\n[sleeping] " + t);
		try{Thread.sleep(t);}catch(InterruptedException e) {}
		System.out.println("\n");
	}

}
