package bn.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import bn.core.BayesianNetwork.Node;
import bn.parser.BIFParser;
import bn.parser.XMLBIFParser;

public class LikelihoodWeighting {

	public static Assignment test(RandomVariable query, BayesianNetwork network, Assignment evidence) {
		Set<Node> nodes = network.nodes;
		//makes list of randomvariables in network
		List<RandomVariable> variables = network.getVariableListTopologicallySorted();
		
		RandomVariable temp;
		ArrayList<Double> probs = new ArrayList<>();
		Assignment currassignment = new Assignment();
		int count = 0;
		
		//while there are unassigned randomvariables 
		while (!(variables.isEmpty())) {
			//take out the first randomvariable
			temp = variables.remove(0);
			if (evidence.containsKey(temp)){
				currassignment.put(temp, evidence.get(temp));
			} 
			else {	
				//loop through its domain
				for (int i=0; i<temp.domain.size(); i++) {
					//store assignment of randomvariable from its domain
					currassignment.put(temp, temp.domain.get(i));
					//add the probability of that to probs
					probs.add(network.getProb(temp, currassignment));
				}
				//now probs should contain distribution of randomvariable
				double d = Math.random();
				while (!(d<probs.get(count))&&count<probs.size()) {
					d-=probs.get(count);
					count+=1;
				}
				//count should contain index of assignment in domain of randomvariable so store that
				currassignment.put(temp, temp.domain.get(count));
			}
			//probs should be wiped
			probs.clear();
			//count should be set back to 0
			count = 0;
		}
		
		//currassignment contains assignment to all variables now
		return currassignment;
	}

	public static String test2(int trials, RandomVariable query, BayesianNetwork network, Assignment evidence) {
		double weight = 1.0;
		ArrayList<Double> doubs = new ArrayList<>();
		for (Object o : query.domain) {
			doubs.add(0.0);
		}
		doubs.add(0.0);
		int count = 1;
		for (int i = 0; i < trials; i++) {
			// generate an assignment
			Assignment assignment = test(query, network, evidence);
			doubs.set(0, doubs.get(0) + 1);
			while (!(query.domain.get(count - 1).equals(assignment.get(query)))) {
				count += 1;
			}
			for (RandomVariable v:evidence.keySet()) {
				weight = weight * network.getProb(v, assignment);
			}
			doubs.set(count, doubs.get(count) + weight);
			count = 1;
			weight = 1.0;
		}
		String answer = "";
		double sum = 0.0;
		for (int i=1; i<doubs.size(); i++) {
			sum += doubs.get(i);
		}
		for (int i = 1; i < doubs.size(); i++) {
			answer += query.domain.get(i - 1) + "= " + doubs.get(i) / sum + "";
		}
		return answer;
	}

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		BayesianNetwork network = new BayesianNetwork();
		int trials = Integer.parseInt(args[0]);
		String test = args[1];
		if (test.substring(test.length()-3, test.length()).equals("bif")) {
			BIFParser parser2;
			if (args.length == 0) {
			    parser2 = new BIFParser(System.in);
			} else {
			    parser2 = new BIFParser(new FileInputStream(args[1]));
			}
			//System.out.println(parser.parse());
			network = parser2.parseNetwork();
		}
		else {
			XMLBIFParser parser = new XMLBIFParser();
			network = parser.readNetworkFromFile(args[1]);
		}
		RandomVariable query = network.getVariableByName(args[2]);
		Assignment x = new Assignment();
		for (int i=3; i<args.length-1; i+=2) {
			x.put(network.getVariableByName(args[i]), args[i+1]);
		}
		
		System.out.println(test2(trials, query, network, x));

	}
}
