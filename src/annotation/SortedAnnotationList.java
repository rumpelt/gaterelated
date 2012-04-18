/**
 * 
 */
package annotation;

import gate.Annotation;

import java.util.Vector;

/**
 * @author ashwani
 * Code copied shamelessly from gate example.
 */
public  class SortedAnnotationList extends Vector<Annotation> {
    /**
	 * 
	 */
	private static final long serialVersionUID = 5770696752459181174L;

	public SortedAnnotationList() {
      super();
    } // SortedAnnotationList

    public boolean addSortedExclusive(Annotation annot) {
      Annotation currAnot = null;

      // overlapping check
      for (int i=0; i<size(); ++i) {
        currAnot = (Annotation) get(i);
        if(annot.overlaps(currAnot)) {
          return false;
        } // if
      } // for

      long annotStart = annot.getStartNode().getOffset().longValue();
      long currStart;
      // insert
      for (int i=0; i < size(); ++i) {
        currAnot = (Annotation) get(i);
        currStart = currAnot.getStartNode().getOffset().longValue();
        if(annotStart < currStart) {
          insertElementAt(annot, i);
          /*
           Out.prln("Insert start: "+annotStart+" at position: "+i+" size="+size());
           Out.prln("Current start: "+currStart);
           */
          return true;
        } // if
      } // for

      int size = size();
      insertElementAt(annot, size);
//Out.prln("Insert start: "+annotStart+" at size position: "+size);
      return true;
    } // addSorted
  } // SortedAnnotationList
