/*****************************************************************************
 * Elda project https://github.com/epimorphics/elda
 * LDA spec: http://code.google.com/p/linked-data-api/
 *
 * Copyright (c) 2014 Epimorphics Ltd. All rights reserved.
 * Licensed under the Apache Software License 2.0.
 * Full license: https://raw.githubusercontent.com/epimorphics/elda/master/LICENCE
 *****************************************************************************/

package com.epimorphics.lda.renderers.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Encapsulates a format that a page is declared as being available in.
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class PageFormat extends RDFNodeWrapper
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( PageFormat.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /** The page object for which this format is one of the available formats */
    private Page page;

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /**
     * Construct a page format whose configuration root is the given resource.
     *
     * @param page The page object this is one of the formats for
     * @param root The configuration root resource
     */
    public PageFormat( Page page, Resource root ) {
        super( page.getModelW(), root );
        this.page = page;
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /**
     * @return The page object that this page format is one of the formats of
     */
    public Page page() {
        return this.page;
    }

    /**
     * Return the label for this format, which is denoted by the <code>rdfs:label</code>
     * on the page format resource
     * @return The format label
     */
    public String label() {
        return getPropertyValue( RDFS.label ).getLexicalForm();
    }

    /**
     * Return the media (mime) type of this format
     * @return  The mime type, or null if not specified
     */
    public MediaType mimeType() {
        MediaType mime = null;

        com.epimorphics.rdfutil.RDFNodeWrapper format = getPropertyValue( DCTerms.format );
        if (format != null) {
            mime = MediaType.decodeType( format.getPropertyValue( RDFS.label ).getLexicalForm() );
        }

        return mime;
    }

    /**
     * @return The resource denoting the page that this page format is a format of
     */
    public com.epimorphics.rdfutil.RDFNodeWrapper isFormatOf() {
        return getPropertyValue( DCTerms.isFormatOf );
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}
