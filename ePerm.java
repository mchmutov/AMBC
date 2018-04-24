import java.util.*;


// Window for an extended affine periodic partial permutation. "data" contains images of 0,...,n-1,n

public class ePerm {
	// Basic data
	Integer n; // Period
	ArrayList<Integer> data; // Values of the permutation at 0,...,n-1,n. First and last entries must differ by n.
	Boolean isPartial = false; // Is the permutation defined on all of Z?
	
	// River theory data
	Set<Integer> swr; // If this is not null, River theory assumed to be set up
	List<Integer> swrNumbering;
	List<Integer> firsts;
	List<Integer> lasts;
	Map<Integer, Point> backs;
	Map<Integer, Integer> next;
	
	// backwards River theory
	List<Integer> backNumbering;
	List<Integer> riverX; // If this is not null, Backward River theory assumed to be set up
	List<Integer> riverY;
	
	
	private class Point{
		Integer x;
		Integer y;
		Point(Integer xArg, Integer yArg){
			x = xArg;
			y = yArg;
		}
	}
	//------------------------------
	// River theory methods 
	//------------------------------
	
	public Row forwardRSKStep()
	{
		if(isEmpty())
			throw(new RuntimeException("Attempting to do a forward step on an empty permutation"));;
			
		setUpRiverTheory();
		//-------------------
		// Figure out the RSK row data
		Set<Integer> r1 = new HashSet<Integer>();
		Set<Integer> r2 = new HashSet<Integer>();
		Integer r;
		for(Integer i : firsts)
			r1.add(modn(get(i)));
		for(Integer i : lasts)
			r2.add(modn(i));
		// Figure out which rows among the first n contain last (COM: i.e. northeast) elements of SW river numbering
		List<Integer> firstStripLasts = new ArrayList<Integer>();
		for(Integer i = 1; i < n+1; i++){
			if(!isDefined(i))
				continue;
			if(isLast(i))
				firstStripLasts.add(i);
		}
		// To figure out which river we are dealing with, take the "back" of the first one of these
		// find what place it occupies in its nxn square, as well as which nxn square it's in
		Integer posInSquare = 0;
		for(Integer i = 1; i < firstStripLasts.size(); i++)
			if(modnStrPos(getBackX(firstStripLasts.get(i))) < modnStrPos(getBackX(firstStripLasts.get(0))))
				posInSquare++;
		r = swr.size()*(getBackX(firstStripLasts.get(0))-modnStrPos(getBackX(firstStripLasts.get(0))))/n+posInSquare;
		//-----------------
		// Update the permutation
		isPartial = true;
		ArrayList<Integer> newData = new ArrayList<Integer>();
		for(Integer i = 0; i < n+1; i++){
			if(!isDefined(i) || isLast(i))
				newData.add(null);
			else
				newData.add(get(getNext(i)));
		}
		data = newData;
		clearRiver();
		clearBackRiver();
		//-----------------
		// Returns the RSK data
		return(new Row(r1, r2, r));
	}
	public void backwardRSKSetup(Row r){
		// Figure out what the river is and the backward step related numbering of the balls
		if(isFull())
			throw(new RuntimeException("Attempting to do a backward step on a full permutation"));	
		setUpRiverTheory();
		
		// Figure out the actual river
		riverY = new ArrayList<Integer>();
		riverX = new ArrayList<Integer>();
		List<Integer> r1Sorted = new ArrayList<Integer>(r.r1);
		for(int i = 0; i < r1Sorted.size();i++)
			if(r1Sorted.get(i) == 0)
				r1Sorted.set(i, n);
		Collections.sort(r1Sorted);
		List<Integer> r2Sorted = new ArrayList<Integer>(r.r2);
		for(int i = 0; i < r2Sorted.size();i++)
			if(r2Sorted.get(i) == 0)
				r2Sorted.set(i, n);
		Collections.sort(r2Sorted);
		for(Integer i = 0; i < r2Sorted.size(); i++){
			// Position of river
			// The index inside an nxn cube is modulo(r.r+i,r2Sorted.size())); the other part is to get it to correct nxn cube
			riverY.add(r2Sorted.get(i));
			riverX.add(r1Sorted.get(modulo(r.r + i,r2Sorted.size())) + n * (r.r + i - modulo(r.r + i,r2Sorted.size()))/r2Sorted.size());
		}		
		// Give the first n balls initial numbering
		backNumbering = new ArrayList<Integer>();
		Integer tmpMax;
		for(Integer i = 0; i < n; i++){
			if(!isDefined(i))
				backNumbering.add(null);
			else{
				tmpMax = Integer.MIN_VALUE; 
				for(Integer j = 0; j < riverX.size(); j++){
					//find the number of the translate of the river element j that lies NW of ball i
					tmpMax = Math.max(tmpMax, j - riverX.size() * numShifts(new Point(get(i),i), new Point(riverX.get(j), riverY.get(j))));
				}
				backNumbering.add(tmpMax);
			}
		}
		// Go through the balls; figure out if they are first with their numbering downhill order; if so, decrease the number
		Boolean isFirst;
		Boolean areMore;
		Integer shift;
		outer:
			while(true){
				for(Integer i = 0; i < n; i++){
					if(!isDefined(i))
						continue;
					isFirst = true;
					areMore = false;
					// Figure out if the current ball is the northwestmost or southeastmost ball with that number
					for(Integer j = 0; j < n; j++){
						if(!isDefined(j) || j == i || !(((backNumbering.get(j) - backNumbering.get(i)) % riverX.size()) == 0))
							continue;
						shift = (backNumbering.get(j) - backNumbering.get(i))/riverX.size();
						if((j - n*shift < i) && (get(j) - n*shift < get(i)))
							isFirst = false;
						else if((j - n*shift > i) && (get(j) - n*shift > get(i)))
							areMore = true;
					}
					//System.out.println("Checked row " + i + "; it is first with this label: " + isFirst + "; Are there more: " + areMore);
					// If so, decrease its numbering
					if(isFirst && areMore){
						backNumbering.set(i,backNumbering.get(i)-1);
						continue outer;
					}
				}
				break;
			}
		//System.out.println(backNumbering);
	}
	public void backwardRSKStep(){
		if(riverX == null)
			throw(new RuntimeException("The backward step is not set up"));	
		ArrayList<Integer> newData = new ArrayList<Integer>();
		List<Integer> ballsLabeledI = new ArrayList<Integer>();
		newData = new ArrayList<Integer>();
		for(Integer i = 0; i < n ; i++)
			newData.add(null);
		for(Integer i = 0; i < riverX.size(); i++){
			// Figure out what are the new balls which arise from river label i
			
			// Find all balls labeled i
			for(Integer j = 0; j < n; j++){
				if(backNumbering.get(j) == null)
					continue;
				if(((backNumbering.get(j) - i) % riverX.size()) == 0){
					ballsLabeledI.add(j + n*(i-backNumbering.get(j))/riverX.size());
				}
			}
			//System.out.println("Balls labeled " + i + " are located in rows " +ballsLabeledI);
			//Sort the balls
			Collections.sort(ballsLabeledI);
			//Add two fake balls representing the backward river element; one at the beginning and one at the end
			// Generate the new balls
			if(!ballsLabeledI.isEmpty()){
				Integer firstBall = ballsLabeledI.get(0); 
				Integer lastBall = ballsLabeledI.get(ballsLabeledI.size()-1);
				//System.out.println("The first ball labeled " + i + " is in row " + firstBall + "; X coordinate of the backing is " + getBackCoordX(firstBall));
				newData.set(modn(lastBall), getBackCoordX(lastBall) - (lastBall - modn(lastBall)));
				newData.set(modn(getBackCoordY(firstBall)), get(firstBall) - (getBackCoordY(firstBall) - modn(getBackCoordY(firstBall))));
			}
			else{
				if (riverY.get(i) != n)
					newData.set(riverY.get(i), riverX.get(i));
				else
					newData.set(0, riverX.get(i) - n);
			}
			for(Integer j = 1; j < ballsLabeledI.size(); j++){
				newData.set(modn(ballsLabeledI.get(j-1)), get(ballsLabeledI.get(j)) - (ballsLabeledI.get(j-1) - modn(ballsLabeledI.get(j-1))));
			}
			ballsLabeledI.clear();
		}
		if(newData.get(0) == null)
			newData.add(null);
		else
			newData.add(newData.get(0) + n);
		data = newData;
		clearRiver();
		clearBackRiver();
		//System.out.println(data);
		// If the permutation is now full, set the isPartial flag to false
		for(Integer i : data){
			if(i == null)
				return;
		}
		isPartial = false;
	}
	//Private method which says by how many (n,n) should b2 be translated to be NW of b1 
	private Integer numShifts(Point b1, Point b2){
		return(Math.max(Math.max( ceiling(b2.x-b1.x,n) , ceiling(b2.y-b1.y,n)) ,0));
	}
	private void setUpRiverTheory(){
		if(swr==null){
			makeSWR();
			makeSWRNumbering();
			makeSWRBallInfo();
		}
	}
	private void makeSWRBallInfo(){
		firsts = new ArrayList<Integer>(); // For each ball number 0<=i<swr.size() firsts will store the row of the first element
		lasts = new ArrayList<Integer>(); // For each ball number 0<=i<swr.size() lasts will store the row of the last element
		next = new HashMap<Integer, Integer>(); // For each row of ball numbered i, except the last one, next will store the row of the next element labeled i
		backs = new HashMap<Integer, Point>();// For each ball number 0<=i<swr.size() backs will store purple river element
		List<Integer> ballsLabeledI;
		for(Integer i = 0; i < swr.size(); i++){
			// For each ball in the first n rows find the one labelled i if possible; in this case add it to the list of balls
			ballsLabeledI = new ArrayList<Integer>();
			for(Integer j = 0; j < n; j++){
				if(!isDefined(j))
					continue;
				if(((i-swrNumbering.get(j)) % swr.size()) == 0){
					ballsLabeledI.add(j + n * (i - swrNumbering.get(j))/swr.size());
				}
			}
			// Arrange the list of balls labeled i in order from southwest to northeast 
			class BallComparator implements Comparator<Integer> {
				public int compare(Integer b1, Integer b2) {
					if(b1 > b2 && get(b1) < get(b2))
						return(-1);
					else if(b1 < b2 && get(b1) > get(b2))
						return(1);
					else
						return (0);
				}
			}
			Collections.sort(ballsLabeledI, new BallComparator());
			// Fill in the first/last/next/back info
			firsts.add(ballsLabeledI.get(0));
			lasts.add(ballsLabeledI.get(ballsLabeledI.size()-1));
			for(Integer k = 0; k < ballsLabeledI.size()-1; k++){
				next.put(ballsLabeledI.get(k), ballsLabeledI.get(k+1));
				backs.put(ballsLabeledI.get(k), new Point(get(ballsLabeledI.get(0)), ballsLabeledI.get(ballsLabeledI.size()-1))) ;
			}
			backs.put(ballsLabeledI.get(ballsLabeledI.size()-1), new Point(get(ballsLabeledI.get(0)), ballsLabeledI.get(ballsLabeledI.size()-1))) ;
		}
	}
	private void makeSWRNumbering(){
		swrNumbering = new ArrayList<Integer>();
		for(Integer i = 0; i< n;i++)
			swrNumbering.add(Integer.MIN_VALUE);
		// Number the river
		ArrayList<Integer> sortedRiver = new ArrayList<Integer>(swr);
		Collections.sort(sortedRiver);
		Integer count = 0;
		for(Integer i : sortedRiver){
			swrNumbering.set(i, count);
			count++;
		}
		// For each ball k of the first n balls, figure out how much the others need to be shifted to be NW of it
		// "shifts" will be a list of maps. The kth map contains j->(multiple of (n,n) by which jth ball needs to be shifted to be NW of kth ball).
		List<Map<Integer,Integer>> shifts = new ArrayList<Map<Integer, Integer>>();
		for(Integer k = 0; k < n; k++){
			shifts.add(new HashMap<Integer, Integer>());
			if(swr.contains(k) || !isDefined(k))
				continue;
			// Make the original map
			for(Integer j = 0; j < n; j++){
				if(!isDefined(j))
					continue;
				shifts.get(k).put(j, Math.max(Math.max( ceiling(j-k,n) , ceiling((data.get(j)-data.get(k)),n)) ,0) );
			}
			shifts.get(k).keySet().remove(k);
			// Clean up the map to avoid unnecessary steps
			jloop:
			for(Integer j = 0; j < n; j++){
				if(!isDefined(j) || j == k)
					continue;
				for(Integer l : shifts.get(k).keySet()){
					if( (data.get(j)-n*shifts.get(k).get(j) < data.get(l)-n*shifts.get(k).get(l)) && 
							(j-n*shifts.get(k).get(j) < l-n*shifts.get(k).get(l))){
						shifts.get(k).keySet().remove(j);
						continue jloop;
					}						
				}
			}

		}
		// Calculate worth for the balls
		Stack<Set<Integer>> visited = new Stack<Set<Integer>>();
		//Stack<Integer> curX = new Stack<Integer>();
		Stack<Integer> curY = new Stack<Integer>();
		Stack<Integer> steps = new Stack<Integer>();
		//Integer cX;
		Integer cY;
		Set<Integer> v;
		Set<Integer> newv;
		Integer worth;
		Integer st;
		Set<Integer> newStuff;
		
		for(Integer k = 0; k < n; k++){
			if(swr.contains(k) || !isDefined(k))
				continue;
			worth = Integer.MIN_VALUE;
			curY.push(k);
			//curX.push(data.get(k));
			visited.push(new HashSet<Integer>(Arrays.asList(k)));
			steps.push(0);
			while(!curY.isEmpty()){
				cY = curY.pop();
				//cX = curX.pop();
				v = visited.pop();
				st = steps.pop();
				if(swr.contains(modn(cY))){
					//Calculate worth and change maximum if necessary
					worth = Math.max(worth, st + swrNumbering.get(modn(cY)) - (modn(cY)-cY)/n*swr.size()) ;
				}
				else{
					//Take all the things that can be reached from cY; subtract all that were visited; 
					//         if something is left, push "it" onto the stacks
					newStuff = new HashSet<Integer>(shifts.get(modn(cY)).keySet());
					newStuff.removeAll(v);
					for(Integer z : newStuff){
						curY.push(z - (modn(cY) - cY) - shifts.get(modn(cY)).get(z)*n);
						//curX.push(data.get(z)- (modn(cY) - cY) - shifts.get(modn(cY)).get(z)*n);
						newv = new HashSet<Integer>(v);
						newv.add(z);
						visited.push(newv);
						steps.push(st+1);
					}
				}
			}
			swrNumbering.set(k, worth);
		}
	}
	private void makeSWR(){
		Set<Integer> ans = new HashSet<Integer>();
		Set<Integer> cand = new HashSet<Integer>();
		Collection<Set<Integer>> antichains = antichains();
		Integer maxSize = 0;
		for(Set<Integer> ac : antichains)
			maxSize = Math.max(maxSize, ac.size());
		for(Set<Integer> ac : antichains){
			if(ac.size() == maxSize){
				ans = ac;
				break;
			}
		}
		for(Set<Integer> ac : antichains){
			if(ac.size() < maxSize)
				continue;
			for(Integer k : ac){
				for(Integer j : ans){
					if(le(k,j) || k == j)
						cand.add(k);
					else if(le(j,k))
						cand.add(j);
				}
			}
			ans = cand;
			cand = new HashSet<Integer>();
		}
		swr = ans;
	}

	//------------------------------
	// Shi poset methods 
	//------------------------------

	// Method returning the collection of antichains of the Shi poset
	// The antichains are given by positions of element in "data"
	public Collection<Set<Integer>> antichains(){
		Collection<Set<Integer>> ans = new HashSet<Set<Integer>>();
		ans.add(new HashSet<Integer>()); // Add the empty set; it is always an antichain
		Set<Set<Integer>> additional = new HashSet<Set<Integer>>(); // variable to hold the newly discovered antichains
		Set<Integer> additionalAC; // variable to hold a newly discovered antichain
		// for each element of a window (note: last element of data is part of the next window so it is not used)
		//   check whether it can be added to the previous antichains; if so the add the antichain 
		for(Integer i = data.size() - 2; i>= 0; i--){
			
			if(!isDefined(i))
				continue;
			acs:
			for(Set<Integer> ac : ans){
				for(Integer j : ac){
					if(isComparable(i,j))
						continue acs;
				}
				additionalAC = new HashSet<Integer>(ac);
				additionalAC.add(i);
				additional.add(additionalAC);
			}
			ans.addAll(additional);	
			additional.clear();
		}
		return(ans);			
	}
	// Methods to determine comparability in the Shi poset
	private Boolean le(Integer k, Integer j){
		if(!isDefined(k) || !isDefined(j))
			throw(new RuntimeException("Attempting to compare rows which are not part of the permutation"));;
		return((k > j && data.get(k) < data.get(j)) || (data.get(j) - data.get(k) > n));
	}
	private Boolean isComparable (Integer i, Integer j){
		if(!isDefined(i) || !isDefined(j))
			throw(new RuntimeException("Attempting to compare rows which are not part of the permutation"));;
		return((i < j && data.get(i) > data.get(j)) || (i > j && data.get(i) < data.get(j)) || (Math.abs(data.get(i) - data.get(j)) > n)); 
	}
	
	//------------------------------
	// Methods to change the permutation
	//------------------------------
		
	// Methods for Knuth moves
	// Bonds labelled 0,...,n-1, where bond i is between s_i and s_{i+1 mod n}
	
	// Do the Knuth move on bond i 
	public void doKnuth (int i){
		if(isPartial)
			throw(new RuntimeException("Knuth move not allowed on partial permutaions."));
		if (i < 0 || i > n-1)
			throw(new RuntimeException("Knuth move with index out of range attempted."));
		if (i==n-1){
			if ((data.get(n-1) < data.get(1)+n && data.get(1)+n < data.get(n)) || (data.get(n) < data.get(1)+n && data.get(1)+n < data.get(n-1))){ 
				multiplyBySimple(n-1);
			}
			else if((data.get(n) < data.get(n-1) && data.get(n-1) < data.get(1)+n) || (data.get(1)+n < data.get(n-1) && data.get(n-1) < data.get(n))){
				multiplyBySimple(0);
			}	
			else
				throw(new RuntimeException("This Knuth move cannot be applied."));
			return;
		}
		if ((data.get(i) < data.get(i+2) && data.get(i+2) < data.get(i+1)) || (data.get(i+1) < data.get(i+2) && data.get(i+2) < data.get(i))){ 
			multiplyBySimple(i);
		}
		else if((data.get(i+1) < data.get(i) && data.get(i) < data.get(i+2)) || (data.get(i+2) < data.get(i) && data.get(i) < data.get(i+1))){
			multiplyBySimple(i+1);
		}	
		else
			throw(new RuntimeException("This Knuth move cannot be applied."));
		
	}

	// Do a sequence of Knuth moves
	public void doKnuthSequence (List<Integer> moves){
		if(isPartial)
			throw(new RuntimeException("Knuth moves not allowed on partial permutaions."));
		for(Integer i : moves)
			doKnuth(i);
	}
	
	// Method for multiplication by a simple transposition
	// Multiply by s_i
	public void multiplyBySimple(int i){
		if(isPartial)
			throw(new RuntimeException("Multiplication by generators not allowed on partial permutaions."));
		if (i < 0 || i > n-1)
			throw(new RuntimeException("Trying to multiply by simple transposition which does not exist"));
		if (i == 0){
			Collections.swap(data, 0, 1);			
			data.set(n, data.get(0)+n);
		}
		else if(i == n-1){
			Collections.swap(data, n-1, n);			
			data.set(0, data.get(n)-n);
		}
		else{
			Collections.swap(data, i, i+1);
		}
		clearRiver(); // If we changed the permutation then southwest river information becomes invalid
		clearBackRiver();
	}
	
	//------------------------------
	//------------------------------
	public ePerm inverse(){
		ePerm ans = new ePerm();
		Integer rem;
		Integer offset;
		ans.n = n;
		ans.data = new ArrayList<Integer>(n);
		while(ans.data.size() < n+1)
			ans.data.add(0);
		for(int i = 0; i <= n; i++){
		 	rem = modn(data.get(i));
		 	offset = data.get(i) - rem;
		 	ans.data.set(rem, i-offset);
		}

		ans.data.set(n, ans.data.get(0)+n);
		return ans;
	}
	//------------------------------
	// Constructors
	//------------------------------
	
	// Private default constructor produces a cheap permutation that can be altered
	private ePerm(){n = 0;};
	// Constructor with just integer input produces random permutation
	public ePerm(int nArg){
		n = nArg;
		data = new ArrayList<Integer>();
		Random gen = new Random();
		
		perm p = new perm(n);
		for(int i = 0; i < n; i++)
			data.add(p.getData().get(i) + gen.nextInt(10)*n);
		data.add(data.get(0)+n);
	}
	// Constructor with two integer inputs produces random permutation with maximal randomness limited by maxJump*n
	public ePerm(int nArg, int maxJump){
		n = nArg;
		data = new ArrayList<Integer>();
		Random gen = new Random();
		
		perm p = new perm(n);
		for(int i = 0; i < n; i++)
			data.add(p.getData().get(i) + gen.nextInt(maxJump)*n);
		data.add(data.get(0)+n);
	}
	// Constructor with String input produces permutation as if parsing [1,2,3,4,5]
	public ePerm(String s){
		List<String> l = Arrays.asList((s.replace("[", "").replace("]", "")).split("\\s*,\\s*"));
		n = l.size();
		data = new ArrayList<Integer>();
		if(!l.get(l.size()-1).equals("") )
			data.add(Integer.parseInt(l.get(l.size()-1))-n);
		else
			data.add(null);
		
		for(String sI : l){
			if(!sI.equals(""))
				data.add(Integer.parseInt(sI));
			else{
				data.add(null);
				isPartial = true;
			}
		}
		// This was moved to front after decision that string encodes 1-indexed permutation
//		if(data.get(0) != null)
//			data.add(data.get(0)+n);
//		else
//			data.add(null);
	}
	public ePerm(ePerm pArg){
		n = pArg.n;
		data = new ArrayList<Integer>(pArg.data);
		isPartial = pArg.isPartial;
	}
	
	
	//------------------------------
	// Accessors and Controllers 
	//------------------------------

	// Backwards river theory
	public Integer getBackNumber(Integer k){
		if(!isDefined(k))
			throw(new RuntimeException("Attempting to find Backward numbering of undefined ball."));

		setUpRiverTheory();
		return(backNumbering.get(modn(k)) + (k - modn(k))/n*riverX.size());
	}
	public Boolean isBackRiver(Integer k){
		return riverY.contains(modnStrPos(k));
	}
	public Integer getBackCoordX(Integer k){
		// Given a row of a ball, this method gives the column of the corresponding Back River element
		return(riverX.get(modulo(getBackNumber(k), riverX.size())) + n*(getBackNumber(k)-modulo(getBackNumber(k), riverX.size()))/riverX.size() );
	}
	public Integer getBackCoordY(Integer k){
		// Given a row of a ball, this method gives the row of the corresponding Back River element
		return(riverY.get(modulo(getBackNumber(k), riverX.size())) + n*(getBackNumber(k)-modulo(getBackNumber(k), riverX.size()))/riverX.size() );
	}

	public Integer getBackRiverX(Integer k){
		// Given a row of the Back River, this method gives the column
		return(riverX.get(riverY.indexOf(modnStrPos(k))) + (k - modnStrPos(k)));
	}
	public Integer getBackRiverIndex(Integer k){
		// Given a row of the Back River, this method gives the number of the river element
		return(riverY.indexOf(modnStrPos(k)) + (k - modnStrPos(k))/n*riverX.size());
	}

	// River Theory
	public Integer getNumber(Integer k){
		if(!isDefined(k))
			throw(new RuntimeException("Attempting to find SWR numbering of undefined ball."));

		setUpRiverTheory();
		return(swrNumbering.get(modn(k)) + (k - modn(k))/n*swr.size());
	}
	public Set<Integer> southwestRiver(){
		setUpRiverTheory();
		return(new HashSet<Integer>(swr));
	}
	public Boolean isOnRiver(Integer i){
		setUpRiverTheory();
		return(swr.contains(modn(i)));
	}
	public ArrayList<Integer> southwestRiverNumbering(){
		setUpRiverTheory();
		return(new ArrayList<Integer>(swrNumbering));
	}
	public Boolean isFirst(Integer k){
		if(!isDefined(k))
			throw(new RuntimeException("Attempting to access SWR numbering of undefined ball."));
		setUpRiverTheory();
		return(firsts.contains(k-n*((getNumber(k) - modulo(getNumber(k),swr.size()))/swr.size())));
	}
	public Boolean isLast(Integer k){
		if(!isDefined(k))
			throw(new RuntimeException("Attempting to access SWR numbering of undefined ball " + k + "."));
		setUpRiverTheory();
		return(lasts.contains(k-n*((getNumber(k) - modulo(getNumber(k),swr.size()))/swr.size())));
	}
	public Integer getFirst(Integer i){
		setUpRiverTheory();
		return(firsts.get(modulo(i, swr.size())) + n*(i-modulo(i, swr.size()))/swr.size() );
	}
	public Integer getNext(Integer k){
		if(!isDefined(k))
			throw(new RuntimeException("Attempting to access SWR numbering of undefined ball."));
		if(isLast(k))
			throw(new RuntimeException("Attempting to access next ball after a last one."));
		setUpRiverTheory();
		return(next.get(k-n*((getNumber(k) - modulo(getNumber(k),swr.size()))/swr.size())) 
				+ n*((getNumber(k) - modulo(getNumber(k),swr.size()))/ swr.size()));
	}
	public Integer getBackX(Integer k){
		if(!isDefined(k))
			throw(new RuntimeException("Attempting to access SWR numbering of undefined ball."));
		setUpRiverTheory();
		return(backs.get(k-n*((getNumber(k) - modulo(getNumber(k),swr.size()))/swr.size())).x 
				+ n*((getNumber(k) - modulo(getNumber(k),swr.size()))/ swr.size()) );
	}
	public Integer getBackY(Integer k){
		if(!isDefined(k))
			throw(new RuntimeException("Attempting to access SWR numbering of undefined ball."));
		setUpRiverTheory();
		return(backs.get(k-n*((getNumber(k) - modulo(getNumber(k),swr.size()))/swr.size())).y 
				+ n*((getNumber(k) - modulo(getNumber(k),swr.size()))/ swr.size()) );
	}
	// Partial permutation structure accessors ####TODO Should these be usin the isPartial field? Should there be one??
	public Boolean isFull(){
		Boolean ans = true;
		for(Integer i = 0; i<n; i++){
			if(data.get(i) == null){
				ans = false;
				break;
			}
		}
		return(ans);
	}
	public Boolean isEmpty(){
		Boolean ans = true;
		for(Integer i = 0; i<n; i++){
			if(!(data.get(i) == null)){
				ans = false;
				break;
			}
		}
		return(ans);
	}
	public Boolean isDefined(Integer i){
		return(!(data.get(modn(i)) == null));
	}
		//Basic
	public Integer get(Integer i){
		if(!isDefined(i))
			throw(new RuntimeException("Attempting to access undefined ball."));
		return(data.get(modn(i)) + (i-modn(i)));
	}
	public Integer getSize(){
		return n;
	}
	public HashSet<Integer> getTau(){
		if(isPartial)
			throw(new RuntimeException("Tau invariants make no sense for partial permutaions."));
		HashSet<Integer> ans = new HashSet<Integer>();
		for(int i=0; i < n; i++){
			if(data.get(i)>data.get(i+1))
				ans.add(i);
		}
		return ans;
	}

	//------------------------------
	// Printing
	//------------------------------

	public String toString(){
		return((data.subList(1,n+1)).toString());
	}
	//------------------------------
	// Equality testing
	//------------------------------
	public boolean equals (Object o){
		if (!(o instanceof ePerm))
			return false;
		ePerm p = (ePerm) o;
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
	//------------------------------
	// Miscellaneous stuff
	//------------------------------

	public void clear(){
		data.clear();
		for(int i = 0; i < n;i++){
			data.add(null);
		}
		isPartial = true;
		clearRiver();
		clearBackRiver();
	}
	// Clearing functions
	private void clearRiver(){
		swr = null;
		swrNumbering = null;
		firsts = null;
		lasts = null;
		backs = null;
		next = null;
	}
	
	private void clearBackRiver(){
		backNumbering = null;
		riverX = null;
		riverY = null;
	}
	
	// Correcting the mod functions
	private Integer modn(Integer k){
		return(((k%n)+n)%n);
	}
	private Integer modnStrPos(Integer k){
		if (k%n == 0)
			return n;
		else
			return(((k%n)+n)%n);
	}

	private Integer modulo(Integer k, Integer l){
		return(((k%l)+l)%l);
	}
	// Correcting the ceiling function
	private Integer ceiling(Integer k, Integer l){
		if(k%l == 0)
			return(k/l);
		else
			return((k-((k%l+l)%l)+l)/l);
	}
	
	
	// Old version of river numbering method; kept in case need to traverse permutations later on
	/*public ArrayList<Integer> southwestRiverNumberingv1(){
	ArrayList<Integer> ans = new ArrayList<Integer>();
	if(swr == null)
		makeSWR();
	for(Integer i = 0; i< n;i++)
		ans.add(Integer.MIN_VALUE);
	// Number the river
	ArrayList<Integer> sortedRiver = new ArrayList<Integer>(swr);
	Collections.sort(sortedRiver);
	Integer count = 0;
	for(Integer i : sortedRiver){
		ans.set(i, count);
		count++;
	}
	// Traverse the set of permutations on n elements; for each permutation figure out the corresponding path value
	perm p = new perm(n);
	p.makeIdentity();
	Integer worth;
	Integer steps;
	Integer shift;
	Integer curPosX;
	Integer curPosY;
	Integer k; // The ball whose worth we want to figure out
	Integer l; // the next element in the path
	while(true){
		kloop:
		for(k = 0; k < n; k++){
			if(swr.contains(k)){
				//System.out.println("Ball from row " + k + "already in river");
				continue;
			}
			//System.out.println("Ball in row " + k);
			steps = 1;
			shift = 0;
			curPosX = data.get(k);
			curPosY = k;
			for(l = 0; !swr.contains(p.get(l)); l++){
				if(p.get(l) == k)
					continue kloop;
				steps++;
				while ((p.get(l)-shift*n > curPosY ) || (data.get(p.get(l))-shift*n > curPosX ))
					shift++;
				curPosX = data.get(p.get(l))-shift*n;
				curPosY = p.get(l)-shift*n;
			}
			while ((p.get(l)-shift*n > curPosY ) || (data.get(p.get(l))-shift*n > curPosX ))
				shift++;
			worth = steps - shift*swr.size() + ans.get(p.get(l));
			//System.out.println("    gets number at least " + worth + " corresponding to permutation " + p + " needed " + steps + " steps and " + shift + "shifts");				
			ans.set(k, Math.max(worth, ans.get(k)));
		}
		if(!p.isDone())
			p.next();
		else
			break;
	}		
	return(ans);
}*/
	
}
