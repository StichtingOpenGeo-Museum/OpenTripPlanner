package org.opentripplanner.analyst.batch;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.csvreader.CsvWriter;

public class BasicPopulation implements Population {

    private static final Logger LOG = LoggerFactory.getLogger(BasicPopulation.class);
    
    @Setter 
    public String sourceFilename;
    
    @Setter @Getter 
    public List<Individual> individuals = new ArrayList<Individual>(); 
    
    @Setter @Getter 
    public List<IndividualFilter> filterChain = null; 

    @Autowired 
    IndividualFactory individualFactory;
    
    private boolean[] skip = null;
    
    public BasicPopulation() {  }

    public BasicPopulation(Individual... individuals) {
        this.individuals = Arrays.asList(individuals);
    }
    
    public BasicPopulation(Collection<Individual> individuals) {
        this.individuals = new ArrayList<Individual>(individuals);
    }

    @Override 
    public void addIndividual(Individual individual) {
        this.individuals.add(individual);
    }

    @Override 
    public Iterator<Individual> iterator() {
        return new PopulationIterator();
    }

    @Override
    public void clearIndividuals(List<Individual> individuals) {
        //
    }

    @Override
    public void createIndividuals() {
        // nothing to do in basic population case
    }

    @Override
    public int size() {
        return this.individuals.size();
    }
        
    protected void writeCsv(String outFileName, ResultSet results) {
        LOG.debug("Writing population to CSV: {}", outFileName);
        try {
            CsvWriter writer = new CsvWriter(outFileName, ',', Charset.forName("UTF8"));
            writer.writeRecord( new String[] {"label", "lat", "lon", "input", "output"} );
            int i = 0;
            // using internal list rather than filtered iterator
            for (Individual indiv : this.individuals) {
                if ( ! this.skip[i]) {
                    String[] entries = new String[] { 
                            indiv.label, Double.toString(indiv.lat), Double.toString(indiv.lon), 
                            Double.toString(indiv.input), Double.toString(results.results[i]) 
                    };
                    writer.writeRecord(entries);
                }
                i++;
            }
            writer.close(); // flush writes and close
        } catch (Exception e) {
            LOG.error("Error while writing to CSV file: {}", e.getMessage());
            return;
        }
        LOG.debug("Done writing population to CSV at {}.", outFileName);
    }

    @Override
    public void writeAppropriateFormat(String outFileName, ResultSet results) {
        // as a default, save to CSV. override this method in subclasses when more is known about data structure.
        this.writeCsv(outFileName, results);
    }

    /** If a filter chain is specified, apply it to the individuals. To be called after loading individuals. */
    private void applyFilterChain() {
        this.skip = new boolean[individuals.size()]; // init to false
        if (filterChain == null)
            return;
        for (IndividualFilter filter : filterChain) {
            LOG.debug("filter {}", filter);
            int accepted = 0, rejected = 0;
            int i = 0;
            for (Individual individual : this.individuals) {
                boolean skipThis = ! filter.filter(individual);
                if (skipThis)
                    rejected += 1;
                else
                    accepted += 1;
                skip[i++] |= skipThis;
            }
            LOG.debug("accepted {}, rejected {}", accepted, rejected);
        }
    }

    public void setup() {
        // call the subclass-specific file loading method
        this.createIndividuals();
        // call the shared filter chain method
        this.applyFilterChain();
    }

    class PopulationIterator implements Iterator<Individual> {

        int i = 0;
        int n = individuals.size();
        Iterator<Individual> iter = individuals.iterator();
        
        public boolean hasNext() {
            while (i < n && skip[i]) {
                //LOG.debug("in iter, i = {}", i);
                if (! iter.hasNext())
                    return false;
                i += 1;
                iter.next();
            }
            //LOG.debug("done skipping at {}", i);
            return iter.hasNext();
        }
        
        public Individual next() {
            if (this.hasNext()) {
                Individual ret = iter.next();
                i += 1;
                return ret;
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException(); 
        }
        
    }

}

