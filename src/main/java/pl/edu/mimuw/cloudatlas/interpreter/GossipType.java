package pl.edu.mimuw.cloudatlas.interpreter;

public enum GossipType {
    RoundRobinSameFrequency, RoundRobinExpFrequency, RandomSameProbability, RandomExpProbability;

    public static GossipType fromString(String type) {
        switch (type) {
            case "RRSF": return RoundRobinSameFrequency;
            case "RREF": return RoundRobinExpFrequency;
            case "RSP": return RandomSameProbability;
            case "REP": return RandomExpProbability;
        }
        return RandomSameProbability;
    }
}