package no.uib.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Reaction {
    
    public String stId;
    
    public Set<String> inputs;
    public Set<String> outputs;
    public Set<String> regulators;
    public Set<String> catalysts;
    
    public Reaction(String line) {
        String[] parts = line.split(",");
        
        this.stId = parts[0];
        catalysts = new HashSet<>();
        inputs = new HashSet<>();
        outputs = new HashSet<>();
        regulators = new HashSet<>();
        
        if (parts.length > 1) {
            catalysts.addAll(Arrays.asList(parts[1].split(";"))); //catalysts
        }
        if (parts.length > 2) {
            inputs.addAll(Arrays.asList(parts[2].split(";"))); //inputs
        }
        if (parts.length > 3) {
            outputs.addAll(Arrays.asList(parts[3].split(";"))); //outputs
        }
        if (parts.length > 4) {
            regulators.addAll(Arrays.asList(parts[4].split(";"))); //regulators
        }
    }
    
    public Reaction() {
        this.stId = "";
        inputs = new HashSet<>();
        outputs = new HashSet<>();
        regulators = new HashSet<>();
        catalysts = new HashSet<>();
    }
    
    public Set<String> getParticipants() {
        Set<String> participants = new HashSet<>();
        participants.addAll(catalysts);
        participants.addAll(inputs);
        participants.addAll(outputs);
        participants.addAll(regulators);
        return participants;
    }
    
    public List<Pair<String, String>> getIOInteractions() {
        List<Pair<String, String>> result = new ArrayList<>();
        
        for (String input : inputs) {
            for (String output : outputs) {
                if (!input.equals(output)) {
                    result.add(new Pair<>(input, output));
                }
            }
        }
        return result;
    }
    
    public List<Pair<String, String>> getCIInteractions() {
        List<Pair<String, String>> result = new ArrayList<>();
        
        for (String catalyst : catalysts) {
            for (String input : inputs) {
                if (!catalyst.equals(input)) {
                    result.add(new Pair<>(catalyst, input));
                }
            }
        }
        return result;
    }
    
    public List<Pair<String, String>> getCOInteractions() {
        List<Pair<String, String>> result = new ArrayList<>();
        
        for (String catalyst : catalysts) {
            for (String output : outputs) {
                if (!catalyst.equals(output)) {
                    result.add(new Pair<>(catalyst, output));
                }
            }
        }
        
        return result;
    }
    
    public List<Pair<String, String>> getRIInteractions() {
        List<Pair<String, String>> result = new ArrayList<>();
        
        for (String regulator : regulators) {
            for (String input : inputs) {
                if (!regulator.equals(input)) {
                    result.add(new Pair<>(regulator, input));
                }
            }
        }
        
        return result;
    }
    
    public List<Pair<String, String>> getROInteractions() {
        List<Pair<String, String>> result = new ArrayList<>();
        
        for (String regulator : regulators) {
            for (String output : outputs) {
                if (!regulator.equals(output)) {
                    result.add(new Pair<>(regulator, output));
                }
            }
        }
        
        return result;
    }
    
    public void addParticipant(String proteinId, String role) {
        switch (role) {
            case "input":
                inputs.add(proteinId);
                break;
            case "output":
                outputs.add(proteinId);
                break;
            case "regulatedBy":
                regulators.add(proteinId);
                break;
            case "catalystActivity":
                catalysts.add(proteinId);
                break;
        }
    }
    
}
