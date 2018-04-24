import java.util.*;

public class perm {
	final Integer n;
	//Map<Integer, Integer> data;
	private ArrayList<Integer> data;
	
	//------------------------------
	// Constructors
	//------------------------------
	
	// No default constructor since size would be unknown
	// Constructor with just integer input produces random permutation
	public perm(int nArg){
		n = nArg;
		data = new ArrayList<Integer>();
		Random gen = new Random();
		ArrayList<Integer> source = new ArrayList<Integer>(); 
		for(int i = 0; i < nArg; i++)
			source.add(i);
		for(int i = 0; i < nArg; i++){
			data.add(source.remove(gen.nextInt(nArg - i)));
		}
	}

	//------------------------------
	// Equality testing
	//------------------------------
	public boolean equals (Object o){
		if (!(o instanceof perm))
			return false;
		perm p = (perm) o;
		if(p.n != n) 
			return false;
		for (int i = 0; i < p.n; i++)
			if(p.get(i) != data.get(i))
				return false;
		return true;	
	}
	public int hashCode() {
		return data.get(0)*data.get(data.size()-1);
	}
	//---------------------
	// Traversing
	//---------------------
	public void makeIdentity(){
		for(Integer i = 0; i < n; i++)
			data.set(i, i);
	}
	public void next(){
		Integer i = n - 1;
	    while (data.get(i-1) >= data.get(i)) 
	      i = i-1;

	    int j = n;
	    while (data.get(j-1)<= data.get(i-1)) 
	      j = j-1;
	  
	    swap(i-1, j-1);

	    i++; j = n;
	    while (i < j)
	    {
	      swap(i-1, j-1);
	      i++;
	      j--;
	    }
	}
	
	private void swap(Integer i, Integer j){
		Integer iTmp = data.get(i);
		Integer jTmp = data.get(j);
		data.set(i, jTmp); 
		data.set(j,iTmp);
	}
	public Boolean isDone(){
		for(Integer i = 0; i < n; i++)
			if(data.get(i) != n-i-1) return(false);
		return(true);
	}
	
	//---------------------
	// Accessors
	//---------------------

	public ArrayList<Integer> getData(){
		return data;
	}
	public Integer get(Integer i){
		return data.get(i);
	}
	//------------------------------
	// Printing
	//------------------------------

	public String toString(){
		return(data.toString());
	}
}
