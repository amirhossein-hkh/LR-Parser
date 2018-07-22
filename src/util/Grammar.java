package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class Grammar {

    private ArrayList<Rule> rules;
    private HashSet<String> terminals;
    private HashSet<String> variables;
    private String startVariable;
    private HashMap<String, HashSet<String>> firstSets;
    private HashMap<String, HashSet<String>> fallowSets;

    public Grammar(String s) {
        rules = new ArrayList<>();
        terminals = new HashSet<>();
        variables = new HashSet<>();
        int line = 0;
        for(String st : s.split("\n")){
            String[] sides = st.split("->");
            String leftSide = sides[0].trim();
            variables.add(leftSide);
            String[] rulesRightSide = sides[1].trim().split("\\|");
            for (String rule : rulesRightSide) {
                String[] rightSide = rule.trim().split("\\s+");
                for (String terminal : rightSide) {
                    terminals.add(terminal);
                }

                if (line == 0) {
                    startVariable = leftSide;
                    rules.add(new Rule("S'", new String[]{startVariable}));
                }
                rules.add(new Rule(leftSide, rightSide));
                line++;
            }
        }
        for (String variable : variables) {
            terminals.remove(variable);
        }
        System.out.println("Rules: ");
        for (int i=0 ; i<rules.size() ; i++) {
            System.out.println(i+" : " +rules.get(i));
        }

        
        computeFirstSets();
        computeFollowSet();
    }

    public ArrayList<Rule> getRules() {
        return rules;
    }
   
    public int findRuleIndex(Rule rule){
        for(int i=0 ; i<rules.size();i++){
            if(rules.get(i).equals(rule)){
                return i;
            }
        }
        return -1;
    }
    public HashSet<String> getVariables() {
        return variables;
    }

    public String getStartVariable() {
        return startVariable;
    }

    private void computeFirstSets() {
        firstSets = new HashMap<>();

        for (String s : variables) {

            HashSet<String> temp = new HashSet<>();
            firstSets.put(s, temp);
        }
        while (true) {
            boolean isChanged = false;
            for (String variable : variables) {
                HashSet<String> firstSet = new HashSet<>();
                for (Rule rule : rules) {
                    if (rule.getLeftSide().equals(variable)) {
                        HashSet<String> addAll = computeFirst(rule.getRightSide(), 0);
                        firstSet.addAll(addAll);
                    }
                }
                if (!firstSets.get(variable).containsAll(firstSet)) {
                    isChanged = true;
                    firstSets.get(variable).addAll(firstSet);
                }

            }
            if (!isChanged) {
                break;
            }
        }

        firstSets.put("S'", firstSets.get(startVariable));
    }

    private void computeFollowSet() {
        fallowSets = new HashMap<>();
        for (String s : variables) {
            HashSet<String> temp = new HashSet<>();
            fallowSets.put(s, temp);
        }
        HashSet<String> start = new HashSet<>();
        start.add("$");
        fallowSets.put("S'", start);

        while (true) {
            boolean isChange = false;
            for (String variable : variables) {
                for (Rule rule : rules) {
                    for (int i = 0; i < rule.getRightSide().length; i++) {
                        if (rule.getRightSide()[i].equals(variable)) {
                            if (i == rule.getRightSide().length - 1) {
                                fallowSets.get(variable).addAll(fallowSets.get(rule.leftSide));
                            } else {
                                HashSet<String> first = computeFirst(rule.getRightSide(), i + 1);
                                if (first.contains("epsilon")) {
                                    first.remove("epsilon");
                                    first.addAll(fallowSets.get(rule.leftSide));
                                }
                                if (!fallowSets.get(variable).containsAll(first)) {
                                    isChange = true;
                                    fallowSets.get(variable).addAll(first);
                                }
                            }
                        }
                    }
                }
            }
            if (!isChange) {
                break;
            }
        }
    }

    public HashSet<String> computeFirst(String[] string, int index) {
        HashSet<String> first = new HashSet<>();
        if (index == string.length) {
            return first;
        }
        if (terminals.contains(string[index])) {
            first.add(string[index]);
            return first;
        }

        if (variables.contains(string[index])) {
            for (String str : firstSets.get(string[index])) {
                first.add(str);
            }
        }

        if (first.contains("epsilon")) {
            if (index != string.length - 1) {
                first.addAll(computeFirst(string, index + 1));
                first.remove("epsilon");
            }
        }
        return first;
    }

    public HashSet<Rule> getRuledByLeftVariable(String variable) {
        HashSet<Rule> variableRules = new HashSet<>();
        for (Rule rule : rules) {
            if (rule.getLeftSide().equals(variable)) {
                variableRules.add(rule);
            }
        }
        return variableRules;
    }

    public boolean isVariable(String s) {
        return variables.contains(s);
    }

    public HashMap<String, HashSet<String>> getFirstSets() {
        return firstSets;
    }

    public HashMap<String, HashSet<String>> getFallowSets() {
        return fallowSets;
    }

    public HashSet<String> getTerminals() {
        return terminals;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.rules);
        hash = 37 * hash + Objects.hashCode(this.terminals);
        hash = 37 * hash + Objects.hashCode(this.variables);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Grammar other = (Grammar) obj;
        if (!Objects.equals(this.rules, other.rules)) {
            return false;
        }
        if (!Objects.equals(this.terminals, other.terminals)) {
            return false;
        }
        if (!Objects.equals(this.variables, other.variables)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String str = "";
        for(Rule rule: rules){
            str += rule + "\n";
        }
        return str;
    }
}
