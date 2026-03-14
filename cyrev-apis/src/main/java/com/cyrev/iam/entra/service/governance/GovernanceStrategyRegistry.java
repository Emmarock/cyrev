package com.cyrev.iam.entra.service.governance;

import com.cyrev.common.dtos.GovernanceOperationType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class GovernanceStrategyRegistry {

    private final Map<GovernanceOperationType, GovernanceStrategy> strategies;

    public GovernanceStrategyRegistry(List<GovernanceStrategy> strategyList) {

        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        GovernanceStrategy::getOperationType,
                        Function.identity()
                ));
    }

    public GovernanceStrategy get(GovernanceOperationType type) {
        return Optional.ofNullable(strategies.get(type))
                .orElseThrow(() ->
                        new IllegalArgumentException("No strategy for " + type));
    }
}