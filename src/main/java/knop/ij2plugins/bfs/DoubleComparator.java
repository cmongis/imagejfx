package knop.ij2plugins.bfs;

public class DoubleComparator {

	////////////////////////////////
	public static boolean equals(double d1, double d2) {
		/*
		 * Then returns true if they are equal
		 */
		String[] formatted_string = formatString(d1, d2);
		
		/*
		System.out.println("Compared: ");
		System.out.println("  " + formatted_string[0] + "\t" + d1); 
		System.out.println("  " + formatted_string[1] + "\t" + d2); 
		*/
		if( formatted_string[0].equals(formatted_string[1])) 
			return true;
		
		//System.out.println("    xx different!");
		return false;
	}
	
	
	//////////////////////////////
	private static String[] formatString(double d1, double d2) {
		/*
		 * Set the precision of the double with the larger precision to the one of the
		 * double with the smallest precision.
		 */
		String s1 = new Double(d1).toString();
		String s2 = new Double(d2).toString();
		
		s1 = s1.split("e|E")[0];
		s2 = s2.split("e|E")[0];
		int len1=s1.length(), len2=s2.length(), len=1; // precision
		
		len = (len1>len2) ? len2 : len1; // we take the shortest
		if(len>1) // Avoid bias:
			len -=1; // the shortest may be upper-rounded
		//len = 2;
		String[] result = new String[2];
		result[0] = s1.substring(0, len);
		result[1] = s2.substring(0, len);
		return result;
	}
	
	
	
	//////////////////////////////
	private static Double[] formatDouble(double d1, double d2) {
		/*
		 * Set the precision of the double with the larger precision to the one of the
		 * double with the smallest precision.
		 */
		String s1 = new Double(d1).toString();
		String s2 = new Double(d2).toString();
		
		
		int len1=1, len2=1, pr=1; // precision
		
		Double D[] = new Double[2];
		D[0] = new Double(s1);
		D[1] = new Double(s2);
		System.out.println("D[0]= |" + D[0] + "|  D[1]= |" + D[1] + "|");

		String[] split;
		String sub1="", sub2="";
		
		/*
		 * Get length of first double past the comma
		 */
		split = s1.split("E|e");
		split = split[0].split("\\.");

		if(split.length>1) {
			sub1 = split[1];
			len1 = split[1].length();
			System.out.println("split1: " + split[1]);
		}
		
		/*
		 * Get length of second double past the comma
		 */
		split = s2.split("E|e");
		
		split = split[0].split("\\.");
		if(split.length>1) {
			sub2 = split[1];
			len2 = split[1].length();
			System.out.println("split2: " + split[1]);
		}
		
		/*
		 * get the first non 0 position
		 */
		String subFirst="", subSecond="";
		if(len1 > len2) { // we take the shortest
			subFirst = sub2;
			subSecond = sub1;
		}
		else {
			subFirst = sub1;
			subSecond = sub2;
		}
			
		String realPrecision="%.";
		//System.out.println("subFirst: " + subFirst);
		int precision = getPrecision(subFirst);
		//System.out.println("subFirst prec: " + precision);
		if(precision > 0) {
			realPrecision += Integer.toString( precision +1);
		}
		else {
			precision = getPrecision(subSecond);
			realPrecision += Integer.toString( precision +1);
			//System.out.println("subSecond: " + precision);
			//System.out.println("second precision: " + precision);
		}
		
		realPrecision += "g";
		
		//System.out.println("precision: " + realPrecision);
		D[0] = new Double( String.format(realPrecision, D[0].doubleValue()));
		D[1] = new Double( String.format(realPrecision, D[1].doubleValue()));
		//System.out.println("D1: " + D[0].doubleValue() );
		//System.out.println("D2: " + D[1].doubleValue());
		return D;
	}
	
	private static int getPrecision(String sub) {
		/*
		 * return the index f the first non 0 number
		 */
		int i=0;
		int start_precision=0;

		while( i < sub.length() && sub.charAt(i) == '0' ) {
			start_precision++;
			i++;
		}

		i = sub.length() - start_precision;

		return i;
	}
	
	public static void main(String [] args) {
	}
	
}
