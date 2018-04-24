import java.util.*;

public class Tabloid {
	private Integer n; // Size of the tabloid
	private Map<Integer,Integer> data = new HashMap<Integer,Integer>(); // maps each entry from 0 to n-1 to the row in which it lies 

	// Redundant things; seems easier to store than compute each time
	private Integer numRows;
	private List<Integer> sh = new ArrayList<Integer>();
	
	//Dual equivalence Data
	private Set<Integer> degI = new HashSet<Integer>(); // keeps track of i such that swap(i,i+1) is an elementary dual equivalence
	
	//-------------
	// Constructors
	//-------------
	
	/**
	 *  Default constructor is only allowed for private use (used in "alteration" methods, e.g si)
	 */
	private Tabloid(){}
	
	/**
	 *  Constructor from a list of rows representation (does not check for entries to be distinct mod n)
	 * @param inp - List of rows of tabloid
	 */
	public Tabloid(List<Set<Integer>> inp){
		n = 0;
		// Fill in n, data and shape field
		for(int i = 0; i < inp.size(); i++){
			sh.add(inp.get(i).size());
			n += inp.get(i).size();
			for(Integer j : inp.get(i)){
				data.put(j,i);
			}
		}
		computeDEGData();
	}
	
	/**
	 *  Constructor from an integer and shape (makes the superstandard tabloid with the given integer as the tau invariant)
	 * @param t - tau invariant of the superstandard tabloid
	 * @param inpSh - shape
	 */
	public Tabloid(Integer t, List<Integer> inpSh, String tp){
		if(tp.equals("rrow")){
			// Fill in n and create the data
			n = 0;
			for(int r = 0; r < inpSh.size(); r++)
				n += inpSh.get(r);
			Integer ct = t;
			for(int r = 0; r < inpSh.size(); r++){
				for(int c = 0; c < inpSh.get(r); c++){
					data.put(ct,r);
					ct = modn(ct - 1);
				}
			}
			// Fill in redundant fields (shape and DEG data)
			numRows = inpSh.size();
			sh = new ArrayList<Integer>(inpSh);
			computeDEGData();
		}
		else if (tp.equals("col")){
			// Fill in redundant fields (shape)
			numRows = inpSh.size();
			sh = new ArrayList<Integer>(inpSh);
			// Fill in n and create the data
			n = 0;
			for(int r = 0; r < inpSh.size(); r++)
				n += inpSh.get(r);
			inpSh.add(0);
			Integer ct = t;
			for(int r = inpSh.size()-1; r > 0; r--){				
				for(int rw = 0; rw < r*(inpSh.get(r-1) - inpSh.get(r)); rw++){
					data.put(ct, mod(rw, r) );
					ct = modn(ct + 1);
				}
			}
			inpSh.remove(inpSh.size()-1);
			// DEG data
			computeDEGData();			
		}
	}
	//TODO Check that degData is ok? Though it was meant for a different application...

	/**
	 * Why is this even here?? it looks like a silly version of a default constructor
	 * @param nArg
	 */
	public Tabloid(Integer nArg){
		n = nArg;
		numRows = 0;
	}
	/**
	 * Constructor to produce a tabloid of a given shape. The string tp should just be "rand" for now; it is there so one can do 
	 * different processing based on its value, as well as to avoid a stupid "erasure" error.
	 * @param inpSh - shape given as list of integers
	 * @param tp - type of tabloid to produce. For now the only option is "rand"
	 */
	public Tabloid(List<Integer> inpSh, String tp){
		if(tp.equals("rand")) 
		{	
			//Necessary fields
			n = 0;
			for(Integer i=0; i<inpSh.size();i++){
				n += inpSh.get(i);
			}
			// data
			Random gen = new Random();
			ArrayList<Integer> source = new ArrayList<Integer>(); 
			for(int i = 0; i < n; i++)
				source.add(i);
			Integer k = 0;
			for(Integer i=0; i<inpSh.size();i++){
				for(Integer j=0; j < inpSh.get(i);j++){
					data.put(source.remove(gen.nextInt(n - k)), i);
					k++;
				}
			}
			// Redundant fields
			sh = new ArrayList<Integer>(inpSh);
			numRows = sh.size();
			computeDEGData();
		}
		else
			throw(new RuntimeException("Invalid option for tabloid making."));

	}

	/**
	 * Constructor from string representation of a Tabloid. 
	 * String description: entries within rows separated by space or commas while rows separated by newlines or semicolons. 
	 * @param s string representing a tabloid
	 */
	public Tabloid(String s){
		List<String> l = Arrays.asList(s.trim().split("\\s*[;\\n]\\s*"));
		//ArrayList<Integer> ans = new ArrayList<Integer>();
		String rowString;
		List<String> row;
		n = 0;
		numRows = 0;
		for(String sI : l){
			rowString = sI.replace("[", "").replace("]", "").trim();
			row = Arrays.asList(rowString.split("\\s*[,]\\s*|\\s+"));
			n += row.size();
			numRows++;
			sh.add(row.size());
			for(String elt : row){
				data.put(Integer.parseInt(elt), numRows -1);
			}
		}
	}
	/**
	 * Copy constructor 
	 * @param T 
	 */
	public Tabloid(Tabloid T){
		n = T.n;
		data = new HashMap<Integer,Integer>(T.data); 
		numRows = T.numRows;
		sh = new ArrayList<Integer>(T.sh);
		degI = new HashSet<Integer>(T.degI);
	}
	
	//----------------
	// Private methods
	//----------------
	
	/**
	 *  Correcting the mod n function
	 * @param k - integer
	 * @return the representative of k (mod n) which is between 0 and n-1
	 */
	private Integer modn(Integer k){
		return(((k%n)+n)%n);
	}
	/**
	 *  Correcting the mod  function
	 * @param k - integer
	 * @param nArg - integer
	 * @return the representative of k (mod nArg) which is between 0 and nArg-1
	 */
	private Integer mod(Integer k, Integer nArg){
		return(((k%nArg)+nArg)%nArg);
	}	
	//-----------------
	// Equality Methods
	//-----------------

	public boolean equals(Object obj){
		if (!(obj instanceof Tabloid))
			return false;
		Tabloid t2 = (Tabloid)obj;
		if (n != t2.n)
			return false;
		for(int i = 0; i < n; i++){
			if(data.get(i) != t2.data.get(i))
				return false;
		}
		return true;
	}
	public int hashCode(){
		int ans = 0;
		for(int i = 0; i < n; i++)
			ans += i^data.get(i);
		return ans;
	}
	
	//-------------
	// Data Methods
	//-------------
	
	public Set<Integer> degInds(){
		return(new HashSet(degI));
	}
	public Set<Integer> tau(){
		Set<Integer> ans = new HashSet<Integer>();
	    for(int i = 0; i<n;i++){
	    	if(data.get(i)<data.get((i+1) % n))
	    		ans.add(i);	    	
	    }	    
	    return(ans);
	}
	public Integer size(){
		return n;
	}
	public Integer length(){
		return numRows;
	}
	/**
	 * Get a copy of row k 
	 * @param k
	 * @return
	 */
	public Set<Integer> getRow(Integer k){
		if(k < 0 || k > numRows - 1)
			throw(new RuntimeException("Trying to access row outside the tabloid."));
		Set<Integer> ans = new HashSet<Integer>();
		Integer i;
		for (Iterator<Integer> it = data.keySet().iterator(); it.hasNext(); ){
			i = it.next();
			if (data.get(i) == k){
				ans.add(i);
			}
		}
		return(ans);
	}
	/**
	 * Get row number of a given element
	 * @param k
	 * @return
	 */
	public Integer getRowNumber(Integer k){
		return(data.get(k));
	}
	//---------------------
	// Alteration methods
	//---------------------

	/**
	 * Returns a new tabloid which differs from the original by switching i and i+1
	 * @param i
	 * @return
	 */
	public Tabloid si(Integer i){
		Tabloid ans = new Tabloid();
		// Fields not affected by swap preserved
		ans.n = n;
		ans.sh = new ArrayList<Integer>(sh);
		// Swap in data
		for(int j = 0; j < n; j++){
			ans.data.put(j, data.get(j));
		}
		ans.data.put(i, data.get(modn(i+1)));
		ans.data.put(modn(i+1), data.get(i));
		// Fix the DEG data
		ans.degI = new HashSet(degI);
		for(int j = -2; j <= 2; j++){
			Integer min = Math.min(ans.data.get(modn(i+j)),  ans.data.get(modn(i+1+j)));
			Integer max = Math.max(ans.data.get(modn(i+j)),  ans.data.get(modn(i+1+j)));
			if((ans.data.get(modn(i-1+j)) >= min && ans.data.get(modn(i-1+j)) < max) || (ans.data.get(modn(i+2+j)) > min && ans.data.get(modn(i+2+j)) <= max))
				ans.degI.add(modn(i+j));
			else
				ans.degI.remove(modn(i+j));
		}
		return(ans);
	}
	/** Switches i and i+1
	 * @param i
	 * @return
	 */
	public void siInPlace(Integer i){
		Integer newRowI = data.get(modn(i+1)); 
		Integer newRowIp1 = data.get(modn(i)); 
		data.put(modn(i), newRowI);
		data.put(modn(i+1), newRowIp1);
		// Fix the DEG data
		for(int j = -2; j <= 2; j++){
			Integer min = Math.min(data.get(modn(i+j)),  data.get(modn(i+1+j)));
			Integer max = Math.max(data.get(modn(i+j)),  data.get(modn(i+1+j)));
			if((data.get(modn(i-1+j)) >= min && data.get(modn(i-1+j)) < max) || (data.get(modn(i+2+j)) > min && data.get(modn(i+2+j)) <= max))
				degI.add(modn(i+j));
			else
				degI.remove(modn(i+j));
		}
	}
	public void addRow(Collection<Integer> r){
		if(r.size() == 0)
			return;
		numRows++;
		for(Integer i : r)
			data.put(i, numRows-1);
		sh.add(r.size());
	}

	public Set<Integer> removeLastRow(){
		if(numRows==0)
			throw(new RuntimeException("Trying to remove row of empty tabloid."));
		Set<Integer> ans = new HashSet<Integer>();
		Integer i;
		for (Iterator<Integer> it = data.keySet().iterator(); it.hasNext(); ){
			i = it.next();
			if (data.get(i) == (numRows - 1) ){
				it.remove();		
				ans.add(i);
			}
		}
		numRows--;
		sh.remove(sh.size()-1);
		return(ans);
	}

	//--------------
	// Misc. methods
	//--------------
	/**
	 * Private method to fill in the Dual Equivalence Graph data
	 */	
	private void computeDEGData(){
		// Compute DEG data
		for(int i = 0; i < n; i++){
			Integer min = Math.min(data.get(i),  data.get(modn(i+1)));
			Integer max = Math.max(data.get(i),  data.get(modn(i+1)));
			if((data.get(modn(i-1)) >= min && data.get(modn(i-1)) < max) || (data.get(modn(i+2)) > min && data.get(modn(i+2)) <= max))
				degI.add(i);
		}
	}
	public String toString(){
		if(data.size() == 0)
			return("");
		StringBuilder ans = new StringBuilder();
		// Figure out the column width necessary
		Integer colWidth = String.valueOf(n).length() + 1;
		String format = "%" + colWidth + "d";
		// Make a friendlier representation of the tabloid
		List<List<Integer>> fr = new ArrayList<List<Integer>>(); 
		for(int i = 0; i < sh.size(); i++)
			fr.add(new ArrayList<Integer>());
		for(int i : data.keySet()){
			fr.get(data.get(i)).add(i);
		}
		// Compose the final string
		for(int i = 0; i < sh.size(); i++){
			for(int j = 0; j < fr.get(i).size(); j++){
				ans.append(String.format(format, fr.get(i).get(j)));
			}
			//ans.append("<BR/>");
			ans.append("\n");
		}
		ans.deleteCharAt(ans.length()-1);
		return(ans.toString());
	}
	public String toHTMLString(){
		if(data.size() == 0)
			return("");
		StringBuilder ans = new StringBuilder();
		// Figure out the column width necessary
		Integer colWidth = String.valueOf(n).length() + 1;
		String format = "%" + colWidth + "d";
		// Make a friendlier representation of the tabloid
		List<List<Integer>> fr = new ArrayList<List<Integer>>(); 
		for(int i = 0; i < sh.size(); i++)
			fr.add(new ArrayList<Integer>());
		for(int i : data.keySet()){
			fr.get(data.get(i)).add(i);
		}
		// Compose the final string
		for(int i = 0; i < sh.size(); i++){
			for(int j = 0; j < fr.get(i).size(); j++){
				ans.append(String.format(format, fr.get(i).get(j)));
			}
			ans.append("<BR/>");
		}
		//ans.deleteCharAt(ans.length()-1);
		return(ans.toString());
	}
	public void clear(){
		data.clear();
		numRows = 0;
		sh.clear();
		degI.clear();
	}
}