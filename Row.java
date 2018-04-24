import java.util.*;

public class Row {
	public final Set<Integer> r1;
	public final Set<Integer> r2;
	public final Integer r;
	public Row(Set<Integer> a1, Set<Integer> a2, Integer a3){
		r1 = a1;
		r2 = a2;
		r = a3;		
	}
	public String toString(){
		return(r1.toString() + "    " + r2.toString() + "    " + r + "\n");
	}
}
