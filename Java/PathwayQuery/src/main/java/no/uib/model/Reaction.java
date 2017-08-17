package no.uib.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import no.uib.pathwayquery.Conf;

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
            if (parts[1].length() > 0) {
                catalysts.addAll(Arrays.asList(parts[1].split(";"))); //catalysts
            }
        }
        if (parts.length > 2) {
            if (parts[2].length() > 0) {
                inputs.addAll(Arrays.asList(parts[2].split(";"))); //inputs    
            }
        }
        if (parts.length > 3) {
            if (parts[3].length() > 0) {
                outputs.addAll(Arrays.asList(parts[3].split(";"))); //outputs
            }
        }
        if (parts.length > 4) {
            if (parts[4].length() > 0) {
                regulators.addAll(Arrays.asList(parts[4].split(";"))); //regulators
            }
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

    private Set<String> getSetByRole(String role) {
        switch (role) {
            case "inputs":
                return inputs;
            case "outputs":
                return outputs;
            case "catalysts":
                return catalysts;
            case "regulators":
                return regulators;
        }
        return new HashSet<String>();
    }

    public List<Pair<String, String>> getInteractions(Conf.ReactionArcs arcType) {
        List<Pair<String, String>> result = new ArrayList<>();
        Set<String> set1 = new HashSet<>(), set2 = new HashSet<>();

        switch (arcType) {
            case InputToOutput:
                set1 = inputs;
                set2 = outputs;
                break;
            case CatalystToInput:
                set1 = catalysts;
                set2 = inputs;
                break;
            case CatalystToOutput:
                set1 = catalysts;
                set2 = outputs;
                break;
            case OutputToCatalyst:
                set1 = outputs;
                set2 = catalysts;
                break;
            case OutputToInput:
                set1 = outputs;
                set2 = inputs;
                break;
            case OutputToRegulator:
                set1 = outputs;
                set2 = regulators;
                break;
            case RegulatorToInput:
                set1 = regulators;
                set2 = inputs;
                break;
            case RegulatorToOutput:
                set1 = regulators;
                set2 = outputs;
                break;
        }

        for (String p1 : set1) {
            for (String p2 : set2) {
                if (!p1.equals(p2)) {
                    result.add(new Pair<>(p1, p2));
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
