/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$

    File:        ResultSet.java
    Created by:  Dave Reynolds
    Created on:  31 Jan 2010
*/

package com.epimorphics.lda.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.epimorphics.lda.support.LanguageFilter;

import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.epimorphics.lda.vocabularies.XHV;
import com.epimorphics.lda.vocabularies.ELDA;
import com.epimorphics.lda.vocabularies.SPARQL;
import com.hp.hpl.jena.sparql.vocabulary.DOAP;
import com.epimorphics.lda.vocabularies.OpenSearch;
import com.epimorphics.util.ModelUtils;
import com.epimorphics.util.RDFUtils;

/**
  	Wrapper for the results of an API query before rendering.
 	A ResultSet is an ordered list of results and an associated 
 	RDF graph. It may also carry the query used to get the
 	detailed information from the remote endpoint(s).

	@author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
	@version $Revision: $
*/
public class APIResultSet implements SetsMetadata {

    static final Logger log = LoggerFactory.getLogger(APIEndpointImpl.class);
    
	protected Resource root;
	protected URI contentLocation;

	protected final List<Resource> results;
    protected final boolean isCompleted;
    protected final MergedModels model;
    protected final String detailsQuery;
    protected long hash;
    protected Date timestamp;
    protected String selectQuery = "";
    protected boolean enableETags = false;
    
    final View view;
    
    public static class MergedModels {
    	
    	private final Model merged;
    	private final Model object;
    	private final Model meta;
    	
    	public MergedModels( Model objectModel ) {
    		this.object = objectModel;
    		this.meta = ModelFactory.createDefaultModel();
    		this.merged = ModelFactory.createUnion( this.object,  this.meta );
    	}
    	
    	public Model getObjectModel() {
    		return object;
    	}
    	
    	public Model getMetaModel() {
    		return meta;
    	}
    	
    	public Model getMergedModel() {
    		return merged;
    	}

		public void setNsPrefix(String prefix, String uri) {
			object.setNsPrefix( prefix, uri );
		}

		public void setNsPrefixes(PrefixMapping supplied) {
			object.setNsPrefixes( supplied );
		}
    }
    	
    /** 
        Map holding named metadata options. 
    */
	protected final Map<String, Model> metadata = new HashMap<String, Model>();
    
    public URI getContentLocation() {
        return contentLocation;
    }

    public void setContentLocation(URI contentLocation) {
        this.contentLocation = contentLocation;
    }

    public APIResultSet(Graph graph, List<Resource> results, boolean isCompleted, boolean enableETags, String detailsQuery, View v) {
    	
        model = new MergedModels( ModelFactory.createModelForGraph( graph ) );
        
        PrefixMapping imported = getPrefixes( results );
		setUsedPrefixes( model, imported );
        this.results = results;
        this.isCompleted = isCompleted;
        this.detailsQuery = detailsQuery;
        this.hash = 0;
        this.timestamp = new Date();
        this.enableETags = enableETags;
        this.view = v;
        if (!results.isEmpty()) this.root = results.get(0).inModel(model.merged);
    }
    
    protected APIResultSet(Graph graph, List<Resource> results, boolean isCompleted, boolean enableETags, String detailsQuery, Map<String, Model> meta, View v ) {
    	this( graph, results, isCompleted, enableETags, detailsQuery, v );
    	this.metadata.putAll( meta );
    }

    private PrefixMapping getPrefixes( List<Resource> lr ) {
    	if (lr.isEmpty()) return RDFUtils.noPrefixes;
    	Model m = lr.get(0).getModel();
    	return m == null ? RDFUtils.noPrefixes : m;
	}
    
    public long getHash() {
    	if (hash == 0) hash = ModelUtils.hashModel( model.merged ) ^ ((long) results.hashCode() << 32 );
    	return hash;
    }

	/**
        Set prefixes for the namespaces of terms that Elda uses
        in its generated models. THey may be over-ridden by the
        supplied mapping.
    */
	public static void setUsedPrefixes( MergedModels model, PrefixMapping supplied ) {
		setUsedPrefixes( model.merged, supplied );
	}
	
	public static void setUsedPrefixes( Model model, PrefixMapping supplied ) {
        model.setNsPrefix( "rdf", RDF.getURI() );
        model.setNsPrefix( "rdfs", RDFS.getURI() );
        model.setNsPrefix( "dct", DCTerms.getURI() );
        model.setNsPrefix( "os", OpenSearch.getURI() );
        model.setNsPrefix( "sparql", SPARQL.NS );
        model.setNsPrefix( "doap", DOAP.NS );
        model.setNsPrefix( "xhv", XHV.getURI() );
        model.setNsPrefix( "opmv", ELDA.COMMON.NS );
        model.setNsPrefixes( supplied );
	}

    /**
        Answer the model this result-set wraps.
    */
    public MergedModels getModels() {
    	return model;
    }
    
    public Model getMergedModel() {
    	return model.merged;
    }

    /**
        Answer the size of this result-set's model.
    */
    public long modelSize() {
		return model.merged.size();
	}
    
    /**
        Set the prefixes of this result-set's model.
    */
    public void setNsPrefixes( PrefixMapping pm ) {
    	model.setNsPrefixes( pm );
    }
    
    /**
        Answer the list of item resources of this result-set.
    */
    public List<Resource> getResultList() {
        return results;
    }
    
    /**
        Answer the query string, if available, used to get the details
        of the values of properties of the selected items.
    */
    public String getDetailsQuery() {
    	return detailsQuery;
    }
    
    /**
        Answer the query string used to select the item resources
        of this result-set. 
    */
    public String getSelectQuery() {
    	return selectQuery;
    }

    /**
        Set the resource to be used as root in this result-set
        (returned by getRoot()).
    */
    public void setRoot( Resource root ) {
        this.root = root;
    }
    
    /**
        Answer the root result of this result-set.
    */
    public Resource getRoot() {
        return root;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public Date createdAt() {
    	return timestamp;
    }
    
    /**
     * Generate and return a new copy of the model filtered to only include
     * statements reachable from the results via allowed properties in the given set.
     * Will not include any root resource, need to create page information after filtering.
     * @param v the view to filter the results with
     * @param languages  acceptable language codes for literals
     */
    public APIResultSet getFilteredSet( View v, String languages ) {
    	if (languages != null) LanguageFilter.filterByLanguages( model.object, languages.split(",") );
        // model.setNsPrefixes( model );
        List<Resource> mappedResults = new ArrayList<Resource>();
        for (Resource r : results) mappedResults.add( r.inModel(model.object) );
        return new APIResultSet( model.object.getGraph(), mappedResults, isCompleted, enableETags, detailsQuery, metadata, v ).setSelectQuery( selectQuery );
    }

	/**
     * Return a new result set with this one as its initial content 
     * but where additions to this model do not affect the source
     */
    @Override public APIResultSet clone() {
        // Dynamic cloning should be a lot better but strangely in fails
        // Perhaps there is a delete going on somewhere that I've missed
//        Graph additions = ModelFactory.createDefaultModel().getGraph();
//        Graph cloneGraph = new Union(additions, graph);
        Model temp = ModelFactory.createDefaultModel();
        temp.add( model.merged );
        Graph cloneGraph = temp.getGraph();
        APIResultSet clone = new APIResultSet(cloneGraph, results, isCompleted, enableETags, detailsQuery, metadata, view );
        clone.setRoot(root);
        clone.setContentLocation(contentLocation);
        clone.setSelectQuery( selectQuery );
        clone.timestamp = timestamp;
        return clone;
    }

	/**
        Answer s statement iterator which delivers all statements matching
        (S, P, O) in the merged model.
    */
	public StmtIterator listStatements( Resource S, Property P, RDFNode O ) {
		return model.merged.listStatements( S, P, O );
	}
	
	/**
	    Set the metadata section called <code>option</code> to the model
	    <code>meta</code>.
	*/
	public void setMetadata( String option, Model meta ) {
		metadata.put( option, meta );		
	}
	
	/**
	    Add to the main model the metadata named by the options.
	*/
	public void includeMetadata( String[] options ) {
		for (String option: options) {
			Model meta = metadata.get( option );
			if (meta == null) log.warn( "Unknown metadata section '" + option + "': ignored." );
			else model.meta.add( meta );
		}
	}

	public APIResultSet setSelectQuery( String selectQuery ) {
		if (this.selectQuery.length() > 0) throw new RuntimeException( "was " + this.selectQuery + " wants " + selectQuery );
		this.selectQuery = selectQuery;
		return this;
	}

	/**
	    Answer true if this result set is empty, ie, it has no result values
	    or the results have no properties.
	*/
	public boolean isEmpty() {
		for (Resource item: results)
			if (item.listProperties().hasNext()) return false;
		return true;
	}

	/**
	    Answer true iff this resultset was built with etags enabled
	*/
	public boolean enableETags() {
		return enableETags;
	}

	public View getView() {
		return view;
	}

	public PrefixMapping getModelPrefixes() {
		return model.merged;
	}    
}

