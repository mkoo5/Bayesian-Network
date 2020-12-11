package bn.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import bn.core.BayesianNetwork.Node;
import bn.parser.XMLBIFParser;
import bn.util.ArraySet;

public class ExactInference {
	
	public static String EnumerationAsk(RandomVariable query, Assignment evidence, BayesianNetwork network) {
		//all of the nodes in the network
		Set<Node> nodes = network.nodes;
		
		//distribution is an arraylist to store double values for each possibility of query
		
		ArrayList<Double> distribution = new ArrayList<>();
		for (Object o:query.domain) {
			ArrayList<RandomVariable> all = new ArrayList<>();
			for (Node n:nodes) {
				all.add(n.variable);
			}
			evidence.put(query, o);
			distribution.add(EnumerateAll(all, evidence, network, new ArrayList<RandomVariable>()));
		}
		double sum = 0.0;
		for (double d:distribution) {
			sum+=d;
		}
		for (int i=0; i<distribution.size(); i++) {
			distribution.set(i, distribution.get(i)/sum);
		}
		
		String answer = "";
		for (double d:distribution) {
			answer+= d + " ";
		}
		return answer;
	}
	
	protected static double EnumerateAll(ArrayList<RandomVariable> variables, Assignment evidence, BayesianNetwork network, ArrayList<RandomVariable> changed) {
		System.out.println(evidence);
		if (variables.size()==0) {
			return 1.0;
		}
		RandomVariable temp = variables.get(0);
		ArrayList<RandomVariable> temparray = variables;
		temparray.remove(0);
		if (evidence.containsKey(temp)) {
			return network.getProb(temp, evidence)*EnumerateAll(temparray, evidence, network, changed);
		}
		else {
			changed.add(temp);
			double d = 0.0;
			for (Object o:temp.domain) {
				evidence.put(temp, o);
				d += network.getProb(temp, evidence)*EnumerateAll(temparray, evidence, network, changed);
			}
			return d;
		}
	}
	
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException{
		//need P(B | j,m) which should be 0.284 ish
		
		XMLBIFParser parser = new XMLBIFParser();
		BayesianNetwork network = parser.readNetworkFromFile("aima-alarm.xml");
		//network.print(System.out);
		//System.out.println(network.getVariableList());
		//System.out.println(network.getChildren(network.getVariableByName("B")));
		//System.out.println(network.getNodeForVariable(network.getVariableByName("B")));
		//List<Node> p;
		//Set<Node> c = new ArraySet<Node>();
		//p = network.getNodeForVariable(network.getVariableByName("A")).parents;
		//c = network.getNodeForVariable(network.getVariableByName("A")).children;
		//for (Node n:p) {
		//	System.out.println(n.variable.name);
		//}
		//for (Node n:c) {
		//	System.out.println(n.variable.name);
		//}
		
		//System.out.println(network.getNodeForVariable(network.getVariableByName("J")).cpt.get(x));
		
		//System.out.println(network.getVariableByName("A").domain);
		//System.out.println(x.variableSet());
		//System.out.println(network.getNodeForVariable(network.getVariableByName("A")).cpt.get(x));
		
		
		//Object o = network.getVariableByName("B").getDomain().get(0);
		//Assignment x = new Assignment();
		//x.put(network.getVariableByName("B"), "true");
		//x.put(network.getVariableByName("E"), "true");
		//x.put(network.getVariableByName("A"), "true");
		//System.out.println(network.getProb(network.getVariableByName("B"), x));
		//public String EnumerationAsk(RandomVariable query, LinkedHashMap<RandomVariable,Object> evidence, BayesianNetwork network) {
		
		Assignment x = new Assignment();
		x.put(network.getVariableByName("J"), "true");
		x.put(network.getVariableByName("M"), "true");
		x.put(network.getVariableByName("J"), "false");
		System.out.println(x);
		
		//System.out.println(EnumerationAsk(network.getVariableByName("B"), x, network));
		
		
		
		//need P(B | j,m) which should be 0.284 ish
		
		int samples = Integer.parseInt(args[0]);
		BayesianNetwork network = new BayesianNetwork();
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
}
}