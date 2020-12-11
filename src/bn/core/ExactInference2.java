package bn.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import bn.core.BayesianNetwork.Node;
import bn.parser.BIFParser;
import bn.parser.XMLBIFParser;
import bn.util.ArraySet;

public class ExactInference2 {
	public static String Test(RandomVariable query, Assignment assignment, BayesianNetwork network) {
		
		//unknown randomvariables
		List<RandomVariable> unknowns; 
		
		//all variables in order
		List<RandomVariable> all;
		
	
		
		//add all randomvariables associated with each node to unknown
		unknowns = network.getVariableListTopologicallySorted();
		all = network.getVariableListTopologicallySorted();
		
		//remove query and randomvariables in assignment from unknowns
		unknowns.remove(query);
		for (RandomVariable v:assignment.keySet()) {
			unknowns.remove(v);
		}
		
		//adds an arraylist for each randomvariable in unknown
		ArrayList<ArrayList<Integer>> Lists = new ArrayList<>();
		for (RandomVariable v:unknowns) {
			Lists.add(new ArrayList<Integer>());
		}
		
		//populates arraylists for each randomvariable proportionate to size of domains
		for (int i=0; i<unknowns.size(); i++) {
			int z = 0;
			for (Object o:unknowns.get(i).domain) {
				Lists.get(i).add(z);
				z++;
			}
		}
		
		//result is the combination of assignments to unknowns 
		ArrayList<String> result = new ArrayList<>();
		Permutations(Lists, result, 0, "");
		ArrayList<Double> sums = new ArrayList<>();
		double tempsum = 0;
		for (Object o:query.domain) {
			assignment.put(query, o);
			for (String s:result) {
				for (int i=0; i<s.length(); i++) {
					assignment.put(unknowns.get(i), unknowns.get(i).domain.get(Integer.parseInt(s.substring(i, i+1))));
				}
				double prod = 1.0;
				for (RandomVariable v:all) {
					prod*=network.getProb(v, assignment);
				}
				tempsum+=prod;
				for (RandomVariable v:unknowns) {
					assignment.remove(v);
				}
			}
			sums.add(tempsum);
			tempsum=0;
		}
		String answer = "";
		for (Double d:sums) {
			tempsum += d;
		}
		for (int i=0; i<sums.size(); i++) {
			sums.set(i, sums.get(i)/tempsum);
		}
		for (int i=0; i<sums.size(); i++) {
			answer += query.domain.get(i) + "= " + sums.get(i) + " ";
		}
		return answer;
	}
	
	public static void Permutations(ArrayList<ArrayList<Integer>> Lists, ArrayList<String> result, int depth, String current) {
		if (depth == Lists.size()) {
			result.add(current);
			return;
		}
		
		for (int i=0; i<Lists.get(depth).size(); ++i) {
			Permutations(Lists, result, depth+1, current+Lists.get(depth).get(i));
		}
	}
	
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException{
		//need P(B | j,m) which should be 0.284 ish
		
		BayesianNetwork network = new BayesianNetwork();
		String test = args[0];
		if (test.substring(test.length()-3, test.length()).equals("bif")) {
			BIFParser parser2;
			if (args.length == 0) {
			    parser2 = new BIFParser(System.in);
			} else {
			    parser2 = new BIFParser(new FileInputStream(args[0]));
			}
			//System.out.println(parser.parse());
			network = parser2.parseNetwork();
		}
		else {
			XMLBIFParser parser = new XMLBIFParser();
			network = parser.readNetworkFromFile(args[0]);
		}
		RandomVariable query = network.getVariableByName(args[1]);
		Assignment x = new Assignment();
		for (int i=2; i<args.length-1; i+=2) {
			x.put(network.getVariableByName(args[i]), args[i+1]);
		}
		System.out.println(Test(query, x, network));
		
		
		
	
}
}