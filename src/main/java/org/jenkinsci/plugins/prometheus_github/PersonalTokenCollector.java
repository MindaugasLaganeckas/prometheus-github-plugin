package org.jenkinsci.plugins.prometheus_github;

import hudson.Extension;

import org.kohsuke.github.GHRateLimit;
import org.kohsuke.github.GitHub;

import io.prometheus.client.Collector;
import io.prometheus.client.Gauge;
import jenkins.model.Jenkins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Extension
public class PersonalTokenCollector extends Collector {

    private static final Logger logger = LoggerFactory.getLogger(PersonalTokenCollector.class);
    private final Jenkins jenkins;
    private final Gauge personalTokenRateLimitGauge;

    public PersonalTokenCollector() {
        jenkins = Jenkins.get();
        personalTokenRateLimitGauge = Gauge.build()
                .namespace("default")
                .subsystem("jenkins")
                .name("api_rate_limit")
                .labelNames("personal_token_for")
                .help("Personal token rate limit")
                .create();
    }

    @Override
    @Nonnull
    public List<MetricFamilySamples> collect() {
        final List<MetricFamilySamples> samples = new ArrayList<>();
        try {
            final GitHub github = GitHub.connectUsingOAuth("my token");
            final GHRateLimit rateLimit = github.getRateLimit();
            personalTokenRateLimitGauge.labels("my personal token").set(rateLimit.getRemaining());
            samples.addAll(personalTokenRateLimitGauge.collect());
        } catch (IOException e) {
            logger.warn("Cannot get personal token remaining API points due to an unexpected error", e);
        }
        return samples;
    }
}
