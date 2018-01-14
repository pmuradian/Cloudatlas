package pl.edu.mimuw.cloudatlas.interpreter;

import pl.edu.mimuw.cloudatlas.model.ValueContact;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class GossipLevelGenerator {
    private GossipType type;
    private ArrayList<Long> previousLevels = new ArrayList();
    private Integer previousLevel = 0;
    private Long depth = 0l;

    public GossipLevelGenerator(GossipType type) {

    }

    public GossipLevelGenerator(GossipType type, Long depth) {
        this.type = type;
        this.depth = depth;
    }

    public Long next() {
        Random rand = new Random();
        Integer gossipLevel = rand.nextInt(depth.intValue()) + 1;
        switch (type) {
            case RandomExpProbability: {
                return gossipLevel.longValue();
            }
            case RandomSameProbability: {
                return gossipLevel.longValue();
            }
            case RoundRobinExpFrequency: {
                Integer level = previousLevel == 2 ? 1 : previousLevel + 1;
                previousLevel = level;
                return level.longValue();
            }
            case RoundRobinSameFrequency: {
                Integer level = previousLevel == 2 ? 1 : previousLevel + 1;
                previousLevel = level;
                return level.longValue();
            }
        }
        return 0l;
    }
}
