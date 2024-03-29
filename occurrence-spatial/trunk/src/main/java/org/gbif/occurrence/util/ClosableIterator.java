package org.gbif.occurrence.util;

import java.util.Iterator;

/** An iterator that needs to be explicitly closed when it is not used anymore
 * @author markus
 *
 * @param <T>
 */
public interface ClosableIterator<T> extends Iterator<T>{
	public void close();
}
