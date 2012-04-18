/**
 * 
 */
package process;

import gate.Factory;
import gate.FeatureMap;
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;

/**
 * @author ashwani
 *  A ProcessingResource Implementation to hold feature values and runtime parameters 
 *  without creating the ProcessingResouce.
 */
public class ProcessResourceImpl implements ProcessingResource{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5025755443030647243L;
	/**
	 * the resource representing this process
	 */
	private ProcessingResource resource;
	/**
	 * @param resource the resource to set
	 */
	public void setResource(ProcessingResource resource) {
		this.resource = resource;
	}
	/**
	 * Runtime/Default parameters of process.
	 */
	private FeatureMap parameters;
	/**
	 * features of this process
	 */
	private FeatureMap features;
	/**
	 * the full qualified resource name
	 */
	final private String resourceName;
	public ProcessResourceImpl(String resourceName) {
		this.resourceName = resourceName;
		this.resource = null;
		this.features = null;
		this.parameters = Factory.newFeatureMap();
		this.features = Factory.newFeatureMap();
	}
	
	
	public ProcessResourceImpl(String resourceName, FeatureMap features, FeatureMap parameters) {
		this.resourceName = resourceName;
		this.features = features;
		this.parameters = parameters;
		this.resource = null;
	}
	
	public ProcessingResource getResource() {
		return this.resource;
	}
	
	/* (non-Javadoc)
	 * @see gate.Resource#cleanup()
	 */
	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		if (this.resource != null)
			this.resource.cleanup();
		else
			throw new UnsupportedOperationException();
	}
	/* (non-Javadoc)
	 * @see gate.Resource#getParameterValue(java.lang.String)
	 */
	@Override
	public Object getParameterValue(String arg0)
			throws ResourceInstantiationException {
		// TODO Auto-generated method stub
		if (this.resource != null)
			return this.resource.getParameterValue(arg0);
		else if (this.parameters != null)
			return this.parameters.get(arg0);
		else
			return null;
			
	}
	/* (non-Javadoc)
	 * @see gate.Resource#init()
	 */
	@Override
	public Resource init() throws ResourceInstantiationException {
		// TODO Auto-generated method stub
		if (this.resource != null)
			return this.resource.init();
		try  {
			this.resource = (ProcessingResource)Factory.createResource(this.resourceName, this.parameters, this.features );
		}
		catch (ResourceInstantiationException r) {
			try {
				Class c = Class.forName(this.resourceName);
				try {
					this.resource = (ProcessingResource) c.newInstance();
					this.resource.setFeatures(this.features);
					this.resource.setParameterValues(this.parameters);
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					throw new ResourceInstantiationException();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					throw new ResourceInstantiationException();
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				throw new ResourceInstantiationException();
			}
		}
		return this.resource;
	}
	/* (non-Javadoc)
	 * @see gate.Resource#setParameterValue(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setParameterValue(String arg0, Object arg1)
			throws ResourceInstantiationException {
		// TODO Auto-generated method stub
		if (this.resource != null)
			this.resource.setParameterValue(arg0, arg1);
		else
			this.parameters.put(arg0, arg1);
	}
	
	/* (non-Javadoc)
	 * @see gate.Resource#setParameterValues(gate.FeatureMap)
	 */
	@Override
	public void setParameterValues(FeatureMap arg0)
			throws ResourceInstantiationException {
		// TODO Auto-generated method stub
		if (this.resource != null)
			this.resource.setParameterValues(arg0);
		else
			this.parameters = arg0;
	}
	/* (non-Javadoc)
	 * @see gate.util.FeatureBearer#getFeatures()
	 */
	@Override
	public FeatureMap getFeatures() {
		// TODO Auto-generated method stub
		if (this.resource != null)
			return this.resource.getFeatures();
		return this.features;
	}
	/* (non-Javadoc)
	 * @see gate.util.FeatureBearer#setFeatures(gate.FeatureMap)
	 */
	@Override
	public void setFeatures(FeatureMap arg0) {
		// TODO Auto-generated method stub
		if (this.resource != null)
			this.resource.setFeatures(arg0);
		else
			this.features = arg0;
	}
	/* (non-Javadoc)
	 * @see gate.util.NameBearer#getName()
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		if (this.resource != null)
			return this.resource.getName();
		return this.resourceName;
	}
	/* (non-Javadoc)
	 * @see gate.util.NameBearer#setName(java.lang.String)
	 */
	@Override
	public void setName(String arg0) {
		// TODO Auto-generated method stub
		if (this.resource != null)
			this.resource.setName(arg0);
		
	}
	/* (non-Javadoc)
	 * @see gate.Executable#execute()
	 */
	@Override
	public void execute() throws ExecutionException {
		// TODO Auto-generated method stub
		if (this.resource != null)
		    this.resource.execute();
		else
			throw new ExecutionException();
	}
	/* (non-Javadoc)
	 * @see gate.Executable#interrupt()
	 */
	@Override
	public void interrupt() {
		// TODO Auto-generated method stub
		if (this.resource != null)
		   this.resource.interrupt();
		else
			throw new UnsupportedOperationException();
	}
	/* (non-Javadoc)
	 * @see gate.Executable#isInterrupted()
	 */
	@Override
	public boolean isInterrupted() {
		// TODO Auto-generated method stub
		if (this.resource != null)
			return this.resource.isInterrupted();
		throw new UnsupportedOperationException();
	}
	/* (non-Javadoc)
	 * @see gate.ProcessingResource#reInit()
	 */
	@Override
	public void reInit() throws ResourceInstantiationException {
		// TODO Auto-generated method stub
		if (this.resource != null)
		    this.resource.reInit();
		else
			throw new ResourceInstantiationException();
	}
	

}
