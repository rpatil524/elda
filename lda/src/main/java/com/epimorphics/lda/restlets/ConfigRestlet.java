/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.restlets;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.epimorphics.lda.specmanager.SpecEntry;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
import com.epimorphics.lda.support.pageComposition.ComposeConfigDisplay;
import com.epimorphics.util.Util;

@Path("/api-config") public class ConfigRestlet {

	@GET @Produces("text/html") public Response generateConfigPage( @PathParam("path") String pathstub ) {
		List<SpecEntry> specs = SpecManagerFactory.get().allSpecs();
		String page = new ComposeConfigDisplay().configPageMentioning( specs, pathstub );
		return RouterRestlet.returnAs( Util.withBody( "API configuration", page ), "text/html" );
	}
	
}