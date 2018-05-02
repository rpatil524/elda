package com.epimorphics.lda.bindings;

/**
	MapLookup is an interface capturing the idea of getting
	the value associated with a key in some mapping object.
*/
public interface MapLookup {

	/**
		Return the value associated with keyValue from the map
		named mapName.
	*/
	public String getValueString(String mapName, String keyValue);
	
	/**
		A map with no elements, so all calls to getValueString
		return null.
	*/
	public static final MapLookup empty = new MapLookup() {

		@Override public String getValueString(String mapName, String keyValue) {
			return null;
		}
			
	};
}