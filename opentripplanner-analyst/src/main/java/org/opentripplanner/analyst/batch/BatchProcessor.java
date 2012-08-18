package org.opentripplanner.analyst.batch;

import java.io.IOException;
import java.util.TimeZone;

import javax.annotation.Resource;

import lombok.Data;

import org.opentripplanner.analyst.batch.aggregator.Aggregator;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.error.VertexNotFoundException;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.routing.services.SPTService;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

@Data
public class BatchProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(BatchProcessor.class);
    private static final String EXAMPLE_CONTEXT = "batch-context.xml";
    
    @Autowired private GraphService graphService;
    @Autowired private SPTService sptService;
    @Resource private Population origins;
    @Resource private Population destinations;
    @Resource private RoutingRequest prototypeRoutingRequest;
    private Aggregator aggregator;
    private Accumulator accumulator;
    
    private String date = "2011-02-04";
    private String time = "08:00 AM";
    private TimeZone timeZone = TimeZone.getDefault();
    private String outputPath;

    public static void main(String[] args) throws IOException {
        
        org.springframework.core.io.Resource appContextResource;
        if( args.length == 0) {
            LOG.warn("no configuration XML file specified; using example on classpath");
            appContextResource = new ClassPathResource(EXAMPLE_CONTEXT);
        } else {
            String configFile = args[0];
            appContextResource = new FileSystemResource(configFile);
        }
      
        GenericApplicationContext ctx = new GenericApplicationContext();
        XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
        xmlReader.loadBeanDefinitions(appContextResource);
        ctx.refresh();
        ctx.registerShutdownHook();
        
        BatchProcessor processor = ctx.getBean(BatchProcessor.class);
        if (processor == null)
            LOG.error("No BatchProcessor bean was defined.");
        else
            processor.run();
    }

    private void run() {

        origins.setup();
        destinations.setup();

        int nOrigins = origins.getIndividuals().size();
        if (aggregator != null) {
            ResultSet aggregates = new ResultSet(origins);
            int i = 0;
            for (Individual oi : origins) {
                LOG.debug("individual {}: {}", i, oi);
                if (i%100 == 0)
                    LOG.info("individual {}/{}", i, nOrigins);
                RoutingRequest req = buildRequest(oi);
                if (req != null) {
                    ShortestPathTree spt = sptService.getShortestPathTree(req);
                    ResultSet result = ResultSet.forTravelTimes(destinations, spt);
                    aggregates.results[i] = aggregator.computeAggregate(result);
                    req.cleanup();
                }
                i += 1;
            }
            aggregates.writeAppropriateFormat(outputPath);
        } else if (accumulator != null) { 
            ResultSet accumulated = new ResultSet(destinations);
            int i = 0;
            for (Individual oi : origins) {
                LOG.debug("individual {}: {}", i, oi);
                if (i%100 == 0)
                    LOG.info("individual {}/{}", i, nOrigins);
                RoutingRequest req = buildRequest(oi);
                if (req != null) {
                    ShortestPathTree spt = sptService.getShortestPathTree(req);
                    ResultSet times = ResultSet.forTravelTimes(destinations, spt);
                    accumulator.accumulate(oi.input, times, accumulated);
                    req.cleanup();
                }
                i += 1;
            }
            accumulator.finish();
            accumulated.writeAppropriateFormat(outputPath);
        } else { 
            // neither aggregator nor accumlator
            if (nOrigins > 1 && !outputPath.contains("{}")) {
                LOG.error("output filename must contain origin placeholder.");
                return;
            }
            int i = 0;
            for (Individual oi : origins) {
                RoutingRequest req = buildRequest(oi);
                if (req != null) {
                    ShortestPathTree spt = sptService.getShortestPathTree(req);
                    ResultSet result = ResultSet.forTravelTimes(destinations, spt);
                    if (nOrigins == 1) {
                        result.writeAppropriateFormat(outputPath);
                    } else {
                        String subName = outputPath.replace("{}", String.format("%d_%s", i, oi.label));
                        result.writeAppropriateFormat(subName);
                    }
                    req.cleanup();
                    i += 1;
                }
            }
        }
    }
    
    private RoutingRequest buildRequest(Individual i) {
        RoutingRequest req = prototypeRoutingRequest.clone();
        req.setDateTime(date, time, timeZone);
        String latLon = String.format("%f,%f", i.getLat(), i.getLon());
        req.batch = true;
        if (req.arriveBy)
            req.setTo(latLon);
        else
            req.setFrom(latLon);
        try {
            req.setRoutingContext(graphService.getGraph(req.routerId));
            return req;
        } catch (VertexNotFoundException vnfe) {
            LOG.debug("no vertex could be created near the origin point");
            return null;
        }
    }

}

