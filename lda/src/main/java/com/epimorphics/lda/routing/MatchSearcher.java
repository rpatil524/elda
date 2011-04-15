/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
    A MatchSearcher<T> maintains a collection of MatchTemplate<T>s.
    The collection can be added to and removed from [TBD]. It can be 
    searched for an entry matching a supplied path; if there is one, 
    bindings are updated and an associated value returned.

    @author eh
*/
public class MatchSearcher<T> {
    
    List<MatchTemplate<T>> templates = new ArrayList<MatchTemplate<T>>();
    boolean needsSorting = false;
    
    static final Logger log = LoggerFactory.getLogger( MatchSearcher.class );
    
    /**
        Add the template <code>path</code> to the collection, associated
        with the supplied result value.
    */
    public void register( String path, T result ) {
    	log.info( "registering " + path + " for " + result.toString() );
        templates.add( MatchTemplate.prepare( path, result ) );
        needsSorting = true;
    }

    /**
        Remove the entry with the given template path from
        the collection.
    */
    public void unregister( String path ) {
        Iterator<MatchTemplate<T>> it = templates.iterator();
        while (it.hasNext())
            if (it.next().template().equals( path )) 
                { it.remove(); return; }
    }
    
    /**
        Search the collection for the most specific entry that
        matches <code>path</code>. If there isn't one, return null.
        If there is, return the associated value, and update the
        bindings with the matches variables.
    */
    public T lookup( Map<String, String> bindings, String path ) {
        if (needsSorting) sortTemplates();    
        for (MatchTemplate<T> t: templates)
            if (t.match(bindings, path)) return t.value();
        return null;
    }

    private void sortTemplates() {
        Collections.sort( templates, MatchTemplate.compare );
        needsSorting = false;
    }
}